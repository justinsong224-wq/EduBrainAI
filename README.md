# EduBrainAI — 教育知识库 RAG 智能助手

> 基于 RAG（检索增强生成）技术的教育知识库问答系统，支持多格式文档上传、向量化存储与流式智能问答。

---

## 项目亮点

- 🔍 **RAG 检索问答**：文档向量化后精准检索，结合 LLM 生成高质量回答
- 🌊 **流式输出**：SSE 实现打字机效果，问答体验流畅
- 🎓 **三种教学风格**：学术严谨 / 通俗易懂 / 苏格拉底式，一键切换
- 📄 **多格式支持**：PDF（含扫描版 OCR）、DOCX、TXT、Markdown
- ⚡ **异步处理**：RabbitMQ 解耦文档处理流程，上传不阻塞用户操作
- 🔐 **JWT 认证**：完整的用户注册/登录权限体系

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Element Plus + Vite |
| Java 后端 | Spring Boot 3 + Spring Security + JPA + RabbitMQ |
| Python AI 服务 | FastAPI + Uvicorn |
| LLM | Ollama + Qwen2.5:7B（本地 GPU） |
| Embedding | BGE-large-zh-v1.5（本地，1024维） |
| 向量数据库 | Qdrant |
| 关系数据库 | MySQL 8 |
| 缓存 | Redis 7 |
| 消息队列 | RabbitMQ 3.12 |
| 部署 | Docker Compose |

---

## 系统架构

```
用户浏览器（Vue 3）
     │
     ▼
Spring Boot 后端（8080）
  ├── JWT 认证
  ├── 知识库 / 文档 CRUD
  ├── 文件存储（磁盘）
  └── RabbitMQ 生产者
         │
         ▼
    RabbitMQ 消息队列
         │
         ▼
FastAPI AI 服务（8001）
  ├── RabbitMQ 消费者
  ├── 文档解析（PDF/DOCX/TXT/MD）
  ├── BGE Embedding 向量化
  ├── Qdrant 向量存储
  └── RAG 检索 + Qwen2.5 流式问答（SSE）
```

---

## 功能模块

### 知识库管理
- 创建 / 删除知识库
- 上传文档（支持 PDF、DOCX、TXT、MD）
- 文档处理状态实时刷新（PENDING → PROCESSING → DONE）
- 删除文档（同步清除磁盘文件 + MySQL 记录 + Qdrant 向量）

### 智能问答
- 基于知识库内容进行 RAG 检索问答
- 流式打字机输出效果
- 参考来源展示（相似度分数）
- 教学风格切换：学术严谨 / 通俗易懂 / 苏格拉底式

### 数据看板
- 系统各服务运行状态监控
- 知识库与文档统计

---

## 快速启动

### 前置条件

- Docker Desktop
- JDK 17+
- Python 3.10+
- Node.js 18+
- Ollama（已拉取 `qwen2.5:7b` 模型）

### 启动步骤

```bash
# 1. 启动基础服务（MySQL / Redis / RabbitMQ / Qdrant）
docker compose up -d

# 2. 启动 Spring Boot 后端
cd backend
./mvnw spring-boot:run

# 3. 启动 FastAPI AI 服务
cd backend-python
source venv/Scripts/activate   # Windows
# source venv/bin/activate     # Mac/Linux
uvicorn app.main:app --reload --port 8001

# 4. 启动前端
cd frontend
npm install
npm run dev
```

浏览器访问：[http://localhost:5173](http://localhost:5173)

---

## 项目结构

```
EduBrainAI/
├── backend/                # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/edurag/backend/
│   │       ├── controller/ # API 控制器
│   │       ├── service/    # 业务逻辑
│   │       ├── entity/     # 数据库实体
│   │       ├── repository/ # JPA Repository
│   │       ├── mq/         # RabbitMQ 生产者
│   │       └── security/   # JWT 认证
│   └── uploads/            # 文件存储目录
│
├── backend-python/         # FastAPI AI 服务
│   └── app/
│       ├── api/            # API 路由
│       ├── services/
│       │   ├── document_parser.py   # 文档解析
│       │   ├── embedding_service.py # 向量化
│       │   ├── qdrant_service.py    # 向量存储
│       │   ├── rag_service.py       # RAG 问答
│       │   └── mq_consumer.py       # MQ 消费者
│       └── core/
│           └── config.py   # 配置管理
│
├── frontend/               # Vue 3 前端
│   └── src/
│       ├── views/          # 页面组件
│       ├── router/         # 路由配置
│       └── main.js
│
└── docker-compose.yml      # 基础服务编排
```

---

## 接口说明

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录（返回 JWT） |
| GET | `/api/kb` | 获取知识库列表 |
| POST | `/api/kb` | 创建知识库 |
| DELETE | `/api/kb/{id}` | 删除知识库 |
| POST | `/api/document/upload` | 上传文档 |
| GET | `/api/document/list` | 获取文档列表 |
| DELETE | `/api/document/{id}` | 删除文档 |
| GET | `/api/chat/stream` | RAG 流式问答（SSE） |

---

## 环境变量配置

**Spring Boot（`application.yml`）**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/edurag
  rabbitmq:
    host: localhost
file:
  upload-dir: ./uploads
fastapi:
  base-url: http://localhost:8001
```

**FastAPI（`.env`）**

```env
QDRANT_HOST=localhost
QDRANT_PORT=6333
RABBITMQ_HOST=localhost
SPRING_BASE_URL=http://localhost:8080
UPLOAD_DIR=../backend/uploads
```

---

## 开发者

Justin — 全栈独立开发