# app/api/chat.py
from fastapi import APIRouter
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from app.services.rag_service import rag_service

router = APIRouter()

class ChatRequest(BaseModel):
    query: str          # 用户问题
    kb_id: int          # 知识库ID
    style: str = "simple"   # 回答风格：academic/simple/socratic

@router.post("/stream")
async def chat_stream(request: ChatRequest):
    """
    流式问答接口
    返回 SSE（Server-Sent Events）格式的流式响应
    前端用 EventSource 或 fetch + ReadableStream 接收
    """
    return StreamingResponse(
        rag_service.chat_stream(
            query=request.query,
            kb_id=request.kb_id,
            style=request.style
        ),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "X-Accel-Buffering": "no"   # 禁用 Nginx 缓冲，确保实时推送
        }
    )

@router.post("/simple")
async def chat_simple(request: ChatRequest):
    """非流式问答接口，一次性返回完整答案（用于测试）"""
    result = await rag_service.chat_simple(
        query=request.query,
        kb_id=request.kb_id,
        style=request.style
    )
    return result