<template>
  <div class="dashboard">
    <h2 class="page-title">数据看板</h2>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #667eea20; color: #667eea">
            <el-icon size="28"><Collection /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.kbCount }}</div>
            <div class="stat-label">知识库数量</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #67c23a20; color: #67c23a">
            <el-icon size="28"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.docCount }}</div>
            <div class="stat-label">文档总数</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #e6a23c20; color: #e6a23c">
            <el-icon size="28"><ChatDotRound /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.queryCount }}</div>
            <div class="stat-label">问答次数</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #f56c6c20; color: #f56c6c">
            <el-icon size="28"><Cpu /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">Qwen2.5</div>
            <div class="stat-label">本地模型</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card class="welcome-card">
      <template #header>
        <span>🚀 系统状态</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="status-item">
            <el-badge is-dot :type="status.spring ? 'success' : 'danger'">
              <span>Spring Boot 后端</span>
            </el-badge>
            <span class="status-text">{{ status.spring ? '运行中' : '未连接' }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="status-item">
            <el-badge is-dot :type="status.fastapi ? 'success' : 'danger'">
              <span>FastAPI AI 服务</span>
            </el-badge>
            <span class="status-text">{{ status.fastapi ? '运行中' : '未连接' }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="status-item">
            <el-badge is-dot :type="status.ollama ? 'success' : 'danger'">
              <span>Ollama 本地模型</span>
            </el-badge>
            <span class="status-text">{{ status.ollama ? '运行中' : '未连接' }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const stats = ref({ kbCount: 0, docCount: 0, queryCount: 0 })
const status = ref({ spring: false, fastapi: false, ollama: false })

onMounted(async () => {
  // 检查 Spring Boot 状态
 // 检查 Spring Boot 状态
try {
  const res = await axios.get('http://localhost:8080/actuator/health', { timeout: 2000 })
  status.value.spring = res.data.status === 'UP'
} catch (e) {
  // 能收到响应就说明服务在运行（即使是401也表示服务正常）
  status.value.spring = e.response?.status !== undefined
}

  // 检查 FastAPI 状态
  try {
    const res = await axios.get('http://localhost:8001/api/health', { timeout: 2000 })
    status.value.fastapi = res.data.status === 'ok'
  } catch (e) {
    status.value.fastapi = false
  }

  // 检查 Ollama 状态
  try {
    const res = await axios.get('http://localhost:11434', { timeout: 2000 })
    status.value.ollama = true
  } catch (e) {
    status.value.ollama = e.response?.status !== undefined
  }

  // 获取知识库数量
  try {
    const res = await axios.get('/api/kb')
    stats.value.kbCount = res.data.data?.length || 0
  } catch (e) {}
})
</script>

<style scoped>
.dashboard { }

.page-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 24px;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.welcome-card {
  border-radius: 12px;
}

.status-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
}

.status-text {
  color: #909399;
  font-size: 13px;
}
</style>