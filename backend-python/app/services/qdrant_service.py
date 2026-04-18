from qdrant_client import QdrantClient
from qdrant_client.models import (
    Distance, VectorParams, PointStruct, Filter,
    FieldCondition, MatchValue
)
from app.core.config import settings
from app.services.embedding_service import embedding_service
import uuid


class QdrantService:

    def __init__(self):
        self.client = QdrantClient(
            host=settings.QDRANT_HOST,
            port=settings.QDRANT_PORT,
            timeout=30,
            check_compatibility=False,
        )
        print(f"Qdrant client ready: http://{settings.QDRANT_HOST}:{settings.QDRANT_PORT}")

    def get_collection_name(self, kb_id: int) -> str:
        return f"kb_{kb_id}"

    # ✅ 修复：放回类里面
    def ensure_collection(self, kb_id: int):
        collection_name = self.get_collection_name(kb_id)
        print(f"[ensure_collection] 确保 collection 存在: {collection_name}")
        try:
            self.client.create_collection(
                collection_name=collection_name,
                vectors_config=VectorParams(
                    size=embedding_service.dimension,
                    distance=Distance.COSINE
                )
            )
            print(f"[ensure_collection] Collection 创建成功: {collection_name}")
        except Exception as e:
            err_str = str(e)
            if "already exists" in err_str or "409" in err_str:
                print(f"[ensure_collection] Collection 已存在，跳过")
            else:
                print(f"[ensure_collection] 创建失败: {repr(e)}")
                raise
            

    def _reconnect(self):
    # 重建 Qdrant 连接
        try:
            self.client = QdrantClient(
                host=settings.QDRANT_HOST,
                port=settings.QDRANT_PORT,
                timeout=30,
                check_compatibility=False,
            )
            print("[Qdrant] 重连成功")
        except Exception as e:
            print(f"[Qdrant] 重连失败: {repr(e)}")
            raise
    

    # ✅ 修复：确保在 class 内、与其他方法同级

    def add_chunks(self, kb_id: int, doc_id: int, chunks: list[str]):
        print(f"[add_chunks] 开始处理 kb_id={kb_id}, doc_id={doc_id}, chunks={len(chunks)}")
        self.ensure_collection(kb_id)
        collection_name = self.get_collection_name(kb_id)
        
        vectors = embedding_service.embed_batch(chunks)
        print("👉 vectors类型:", type(vectors))
        print("👉 第一个vector类型:", type(vectors[0]))
        print("👉 向量维度:", len(vectors[0]))

        

        points = [
            PointStruct(
                id=uuid.uuid4(),
                vector=vector,
                payload={
                    "doc_id": doc_id,
                    "kb_id": kb_id,
                    "text": chunk,
                    "chunk_index": i
                }
            )
            for i, (chunk, vector) in enumerate(zip(chunks, vectors))
        ]

        try:
            result = self.client.upsert(collection_name=collection_name, points=points)
            print(f"upsert result: {result}")
        except Exception as e:
            print(f"upsert 详细错误: {repr(e)}")
            print(f"upsert 错误类型: {type(e)}")
            if hasattr(e, 'status_code'):
                print(f"status_code: {e.status_code}")
            if hasattr(e, 'reason_phrase'):
                print(f"reason_phrase: {e.reason_phrase}")
            if hasattr(e, 'content'):
                print(f"content: {e.content}")
            raise
        print(f"成功写入 {len(points)} 个向量到 {collection_name}")
        return len(points)
    def search(self, kb_id: int, query: str, top_k: int = 5) -> list[dict]:
        collection_name = self.get_collection_name(kb_id)

        query_vector = embedding_service.embed_query(query)

        results = self.client.query_points(
            collection_name=collection_name,
            query=query_vector,
            limit=top_k,
            with_payload=True
        ).points

        return [
            {
                "text": r.payload["text"],
                "doc_id": r.payload["doc_id"],
                "score": r.score,
                "chunk_index": r.payload["chunk_index"]
            }
            for r in results
        ]

    def delete_by_doc_id(self, kb_id: int, doc_id: int):
        collection_name = self.get_collection_name(kb_id)
        self.client.delete(
            collection_name=collection_name,
            points_selector=Filter(
                must=[FieldCondition(
                    key="doc_id",
                    match=MatchValue(value=doc_id)
                )]
            )
        )


# 单例
qdrant_service = QdrantService()
print("当前 qdrant_service 对象：", qdrant_service, qdrant_service.__class__)