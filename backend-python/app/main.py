# app/main.py
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api import document, chat, health
import os
os.environ['NO_PROXY']='localhost,127.0.0.1.0.0.0.0'
os.environ['no_proxy']='localhost,127.0.0.1.0.0.0.0'

app = FastAPI(
    title="EduRAG AI Service",
    description="教育知识库RAG AI服务，基于本地Qwen2.5模型",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080", "http://localhost:5173"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health.router, prefix="/api", tags=["健康检查"])
app.include_router(document.router, prefix="/api/document", tags=["文档处理"])
app.include_router(chat.router, prefix="/api/chat", tags=["智能问答"])

@app.on_event("startup")
async def startup():
    """
    服务启动时初始化：
    1. 启动 RabbitMQ 消费者（后台线程监听文档处理队列）
    """
    from app.services.mq_consumer import mq_consumer
    mq_consumer.start()
    print("EduRAG AI Service 启动成功！")
    print("Swagger UI: http://localhost:8001/docs")