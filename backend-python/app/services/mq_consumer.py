# app/services/mq_consumer.py
import pika
import json
import threading
import os
import httpx
from app.core.config import settings
from app.services.document_parser import document_parser
from app.services.qdrant_service import qdrant_service

class MQConsumer:
    """
    RabbitMQ 消费者
    监听 Spring Boot 发来的文档处理消息
    收到消息后：解析文档 → 切片 → 向量化 → 存入 Qdrant → 回调更新状态
    
    在独立线程里运行，不阻塞 FastAPI 主进程
    """

    def __init__(self):
        self.connection = None
        self.channel = None

    def connect(self):
        """建立 RabbitMQ 连接"""
        credentials = pika.PlainCredentials(
            settings.RABBITMQ_USER,
            settings.RABBITMQ_PASSWORD
        )
        parameters = pika.ConnectionParameters(
            host=settings.RABBITMQ_HOST,
            port=settings.RABBITMQ_PORT,
            credentials=credentials
        )
        self.connection = pika.BlockingConnection(parameters)
        self.channel = self.connection.channel()

        # 声明队列（和 Spring Boot 保持一致）
        self.channel.queue_declare(
            queue="document.process.queue",
            durable=True    # 持久化，MQ 重启消息不丢失
        )
        print("RabbitMQ 消费者连接成功！")

    def process_document(self, ch, method, properties, body):
        """
        处理文档消息的回调函数
        每收到一条消息就执行一次
        """
        doc_id = None
        try:
            # 解析消息体
            message = json.loads(body)
            doc_id = message["documentId"]
            kb_id = message["kbId"]
            filename = message["filename"]

            print(f"收到文档处理任务: doc_id={doc_id}, kb_id={kb_id}, file={filename}")

            # 构建文件路径
            file_path = os.path.join(
                settings.UPLOAD_DIR,
                str(kb_id),
                filename
            )

            # 解析文档并切片
            chunks = document_parser.parse(file_path)
            print(f"文档切片完成，共 {len(chunks)} 个片段")
            print(f"第一个片段预览: {chunks[0][:100] if chunks else '空！！！'}")

            # 向量化并存入 Qdrant
            try:
                count = qdrant_service.add_chunks(kb_id=kb_id, doc_id=doc_id, chunks=chunks)
                print(f"向量化完成，写入 {count} 个向量")
            except Exception as e:
                print(f"Qdrant 写入失败: {repr(e)}")
                raise

            # 回调 Spring Boot 更新状态（回调失败不影响向量写入本身）
            try:
                headers = {}
                if settings.SPRING_CALLBACK_BEARER_TOKEN:
                    headers["Authorization"] = f"Bearer {settings.SPRING_CALLBACK_BEARER_TOKEN}"
                with httpx.Client() as client:
                    r = client.put(
                        f"{settings.SPRING_BASE_URL}/api/document/{doc_id}/status",
                        json={"status": "DONE", "chunkCount": count},
                        headers=headers,
                        timeout=10.0
                    )
                if r.status_code >= 400:
                    print(f"回调状态 DONE: doc_id={doc_id}, http={r.status_code}, body={r.text[:500]}")
                else:
                    print(f"回调状态 DONE: doc_id={doc_id}, http={r.status_code}")
            except Exception as e:
                print(f"回调状态 DONE 失败: {repr(e)}")

            # 通知 RabbitMQ 消息处理成功（ack）
            ch.basic_ack(delivery_tag=method.delivery_tag)
            print(f"文档 {doc_id} 处理完成！")
            # NOTE: DB_URL was referenced here but not defined; remove to avoid NameError.

        except Exception as e:
            print(f"文档处理失败: {repr(e)}")
            try:
                headers = {}
                if settings.SPRING_CALLBACK_BEARER_TOKEN:
                    headers["Authorization"] = f"Bearer {settings.SPRING_CALLBACK_BEARER_TOKEN}"
                with httpx.Client() as client:
                    r = client.put(
                       f"{settings.SPRING_BASE_URL}/api/document/{doc_id}/status",
                       json={"status": "FAILED", "chunkCount": 0},
                       headers=headers,
                       timeout=10.0
                    )
                if r.status_code >= 400:
                    print(f"回调状态 FAILED: doc_id={doc_id}, http={r.status_code}, body={r.text[:500]}")
                else:
                    print(f"回调状态 FAILED: doc_id={doc_id}, http={r.status_code}")
            except:
                pass
            # 处理失败，消息重新入队
            ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)

    def start(self):
        """在独立线程中启动消费者，不阻塞主进程"""
        def run():
            try:
                self.connect()
                # 每次只取一条消息处理，处理完再取下一条
                self.channel.basic_qos(prefetch_count=1)
                self.channel.basic_consume(
                    queue="document.process.queue",
                    on_message_callback=self.process_document
                )
                print("开始监听文档处理队列...")
                self.channel.start_consuming()
            except Exception as e:
                print(f"RabbitMQ 消费者异常: {e}")

        thread = threading.Thread(target=run, daemon=True)
        thread.start()


# 单例
mq_consumer = MQConsumer()