package com.edurag.backend.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 文档处理消息生产者
 * 负责把"需要处理的文档"信息发送到 RabbitMQ 队列
 * FastAPI 消费者监听同一个队列，收到消息后执行解析和向量化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    // 队列名称常量，和 FastAPI 消费者保持一致
    public static final String DOCUMENT_PROCESS_QUEUE = "document.process.queue";
    public static final String DOCUMENT_PROCESS_EXCHANGE = "document.process.exchange";
    public static final String DOCUMENT_PROCESS_ROUTING_KEY = "document.process";

    /**
     * 发送文档处理消息
     * @param documentId 文档ID（FastAPI处理完后用这个ID回调更新状态）
     * @param kbId       知识库ID（决定向量存到 Qdrant 的哪个 collection）
     * @param filename   文件名（FastAPI 用这个找到磁盘上的文件）
     */
    public void sendDocumentProcessMessage(Long documentId, Long kbId, String filename) {
        // 用 Map 组装消息体，序列化成 JSON 发送
        Map<String, Object> message = new HashMap<>();
        message.put("documentId", documentId);
        message.put("kbId", kbId);
        message.put("filename", filename);

        rabbitTemplate.convertAndSend(
                DOCUMENT_PROCESS_EXCHANGE,
                DOCUMENT_PROCESS_ROUTING_KEY,
                message
        );
        log.info("文档处理消息已发送: documentId={}", documentId);
    }
}