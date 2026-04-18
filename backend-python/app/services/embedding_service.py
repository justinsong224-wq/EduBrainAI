# app/services/embedding_service.py
from __future__ import annotations

from app.core.config import settings

from typing import Optional, List
import os
os.environ['NO_PROXY'] = 'localhost,127.0.0.1,0.0.0.0'
os.environ['no_proxy'] = 'localhost,127.0.0.1,0.0.0.0'
os.environ['HF_HUB_OFFLINE'] = '1'        # 强制离线，不联网检查
os.environ['TRANSFORMERS_OFFLINE'] = '1'  # transformers 也强制离线

class EmbeddingService:
    """
    文本向量化服务
    使用 BGE-large-zh 模型把文本转成向量
    BGE 是目前中文效果最好的开源 Embedding 模型，由北京智源研究院发布
    """

    def __init__(self):
        self._model = None

    def _get_model(self):
        if self._model is not None:
            return self._model

        # 延迟导入，避免服务启动时因网络/下载失败直接崩溃
        from sentence_transformers import SentenceTransformer

        print(f"正在加载 Embedding 模型: {settings.EMBEDDING_MODEL}")
        try:
            self._model = SentenceTransformer(
                settings.EMBEDDING_MODEL,
                local_files_only=settings.EMBEDDING_LOCAL_FILES_ONLY,
            )
        except Exception as e:
            # 保留原始异常信息，调用方会看到清晰错误
            raise RuntimeError(
                "Embedding 模型加载失败。"
                "如果你在离线/内网环境运行，请先把模型下载到本地缓存，"
                "或设置 EMBEDDING_LOCAL_FILES_ONLY=true。"
            ) from e

        print("Embedding 模型加载完成！")
        return self._model

    def embed_text(self, text: str) -> List[float]:
        """
        把单条文本转成向量
        BGE 模型建议在查询文本前加 "为这个句子生成表示以用于检索相关文章："
        这样检索效果更好（官方推荐做法）
        """
        model = self._get_model()
        embedding = model.encode(text, normalize_embeddings=True)
        
        return embedding.tolist()

    def embed_query(self, query: str) -> List[float]:
        """
        查询文本向量化（加 BGE 推荐前缀，提升检索精度）
        """
        prefixed_query = f"为这个句子生成表示以用于检索相关文章：{query}"
        return self.embed_text(prefixed_query)

    def embed_batch(self, texts: List[str]) -> List[List[float]]:
        """
        批量向量化，处理大量文本时比逐条处理快很多
        """
        model = self._get_model()
        embeddings = model.encode(texts, normalize_embeddings=True, batch_size=32)
        return embeddings.tolist()

    @property
    def dimension(self) -> int:
        """返回向量维度，Qdrant 建库时需要用到"""
        model = self._get_model()
        return model.get_sentence_embedding_dimension()


# 单例模式：全局只加载一次模型，避免重复占用显存
embedding_service = EmbeddingService()