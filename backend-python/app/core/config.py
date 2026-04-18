# app/core/config.py
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    # FastAPI 服务配置
    APP_NAME: str = "EduRAG AI Service"
    APP_PORT: int = 8001

    # Ollama 配置（本地LLM）
    OLLAMA_BASE_URL: str = "http://localhost:11434"
    OLLAMA_MODEL: str = "qwen2.5:7b"

    # Embedding 模型配置
    EMBEDDING_MODEL: str = "BAAI/bge-large-zh-v1.5"
    # 若为 True，则仅从本地缓存/本地路径加载模型（离线/内网环境用）
    EMBEDDING_LOCAL_FILES_ONLY: bool = False

    # Qdrant 向量数据库配置
    QDRANT_HOST: str = "localhost"
    QDRANT_PORT: int = 6333

    # MySQL 配置（和 Spring Boot 共用同一个数据库）
    DB_HOST: str = "localhost"
    DB_PORT: int = 3307
    DB_NAME: str = "edu_rag"
    DB_USER: str = "era_user"
    DB_PASSWORD: str = "era_pass_2024"

    # RabbitMQ 配置
    RABBITMQ_HOST: str = "localhost"
    RABBITMQ_PORT: int = 5672
    RABBITMQ_USER: str = "era_user"
    RABBITMQ_PASSWORD: str = "era_mq_2024"

    # 文件存储路径（和 Spring Boot 共用）
    UPLOAD_DIR: str = "../backend/uploads"

    # Spring Boot 回调配置（用于更新文档处理状态）
    SPRING_BASE_URL: str = "http://localhost:8080"
    # 若 Spring Boot 接口受 Spring Security 保护，可在这里配置 Bearer Token
    SPRING_CALLBACK_BEARER_TOKEN: str = ""

    class Config:
        env_file = ".env"

settings = Settings()