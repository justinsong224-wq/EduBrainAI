# app/services/rag_service.py
import httpx
import json
from app.core.config import settings
from app.services.qdrant_service import qdrant_service

# 教学风格 Prompt 模板
# 这就是之前说的"风格切换"功能，面试时可以演示
STYLE_PROMPTS = {
    "academic": """你是一位严谨的学术助手。请用专业、准确的学术语言回答问题。
要求：引用知识库中的具体内容，逻辑清晰，结构完整。""",

    "simple": """你是一位善于化繁为简的教师。请用通俗易懂的语言回答问题。
要求：避免专业术语，多用类比和例子，让初学者也能理解。""",

    "socratic": """你是一位苏格拉底式的教育者。不要直接给出答案，而是通过提问引导学生思考。
要求：先提出2-3个引导性问题，再给出提示，最后补充相关知识点。"""
}

class RAGService:
    """
    RAG 核心服务
    RAG = Retrieval Augmented Generation（检索增强生成）
    核心思路：不靠模型的"记忆"，而是先从知识库检索相关内容，
    再让模型基于检索到的内容回答，保证答案准确且可溯源
    """

    def __init__(self):
        # httpx 异步客户端，用于调用 Ollama API
        self.client = httpx.AsyncClient(timeout=120.0)

    def build_prompt(self, query: str, contexts: list[dict], style: str = "simple") -> str:
        """
        构建 RAG Prompt
        把检索到的文档片段和用户问题拼接成完整的 Prompt
        """
        # 获取风格对应的系统提示
        style_prompt = STYLE_PROMPTS.get(style, STYLE_PROMPTS["simple"])

        # 把检索到的片段拼接成上下文
        if contexts:
            context_text = "\n\n".join([
                f"【参考资料{i+1}】\n{ctx['text']}"
                for i, ctx in enumerate(contexts)
            ])
            context_section = f"""
以下是从知识库中检索到的相关资料，请基于这些资料回答问题：

{context_text}

---
"""
        else:
            context_section = "（知识库中未找到相关资料，请根据你的知识回答）\n\n"

        prompt = f"""{style_prompt}

{context_section}用户问题：{query}

请基于以上资料回答："""

        return prompt

    async def chat_stream(self, query: str, kb_id: int, style: str = "simple"):
        """
        流式 RAG 问答（核心方法）
        使用 Python 异步生成器，逐字返回模型输出
        前端通过 SSE（Server-Sent Events）接收流式响应，实现打字机效果
        """
        # 第一步：从 Qdrant 检索相关文档片段
        contexts = qdrant_service.search(kb_id=kb_id, query=query, top_k=5)

        # 第二步：构建 Prompt
        prompt = self.build_prompt(query, contexts, style)

        # 第三步：调用 Ollama API（流式模式）
        payload = {
            "model": settings.OLLAMA_MODEL,
            "prompt": prompt,
            "stream": True,     # 开启流式输出
            "options": {
                "temperature": 0.7,     # 控制输出随机性，0=确定性，1=创意性
                "top_p": 0.9,
                "num_ctx": 4096         # 上下文窗口大小
            }
        }

        # 先把检索到的来源信息发给前端
        sources = [
            {"doc_id": ctx["doc_id"], "score": round(ctx["score"], 3), "text": ctx["text"][:100]}
            for ctx in contexts
        ]
        yield f"data: {json.dumps({'type': 'sources', 'sources': sources}, ensure_ascii=False)}\n\n"

        # 流式接收 Ollama 的输出，逐块转发给前端
        async with self.client.stream("POST", f"{settings.OLLAMA_BASE_URL}/api/generate", json=payload) as response:
            async for line in response.aiter_lines():
                if line:
                    try:
                        data = json.loads(line)
                        token = data.get("response", "")
                        if token:
                            # 把每个 token 包装成 SSE 格式发送
                            yield f"data: {json.dumps({'type': 'token', 'content': token}, ensure_ascii=False)}\n\n"
                        if data.get("done"):
                            yield f"data: {json.dumps({'type': 'done'}, ensure_ascii=False)}\n\n"
                    except json.JSONDecodeError:
                        continue

    async def chat_simple(self, query: str, kb_id: int, style: str = "simple") -> dict:
        """
        非流式问答（一次性返回完整答案）
        用于简单场景或测试
        """
        contexts = qdrant_service.search(kb_id=kb_id, query=query, top_k=5)
        prompt = self.build_prompt(query, contexts, style)

        payload = {
            "model": settings.OLLAMA_MODEL,
            "prompt": prompt,
            "stream": False
        }

        response = await self.client.post(
            f"{settings.OLLAMA_BASE_URL}/api/generate",
            json=payload
        )
        result = response.json()

        return {
            "answer": result.get("response", ""),
            "sources": [
                {"doc_id": ctx["doc_id"], "score": round(ctx["score"], 3)}
                for ctx in contexts
            ]
        }


# 单例
rag_service = RAGService()