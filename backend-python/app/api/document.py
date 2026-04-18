# app/api/document.py
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app.services.qdrant_service import qdrant_service

router = APIRouter()

class DeleteRequest(BaseModel):
    doc_id: int
    kb_id: int

@router.delete("/vectors")
async def delete_document_vectors(request: DeleteRequest):
    """
    删除文档的向量数据
    当 Spring Boot 删除文档时调用此接口，同步删除 Qdrant 中的向量
    """
    try:
        qdrant_service.delete_by_doc_id(
            kb_id=request.kb_id,
            doc_id=request.doc_id
        )
        return {"message": f"文档 {request.doc_id} 的向量已删除"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/collection/{kb_id}")
async def get_collection_info(kb_id: int):
    """查看某知识库的向量集合信息（用于调试）"""
    try:
        collection_name = qdrant_service.get_collection_name(kb_id)
        info = qdrant_service.client.get_collection(collection_name)
        return {
            "collection": collection_name,
            "vectors_count": info.vectors_count,
            "status": info.status
        }
    except Exception as e:
        raise HTTPException(status_code=404, detail=f"Collection 不存在: {str(e)}")