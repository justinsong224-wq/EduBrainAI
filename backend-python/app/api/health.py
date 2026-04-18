# app/api/health.py
from fastapi import APIRouter

router = APIRouter()

@router.get("/health")
async def health_check():
    """健康检查接口，供 Spring Boot 和运维监控调用"""
    return {"status": "ok", "service": "EduRAG AI Service"}