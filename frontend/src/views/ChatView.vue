<template>
  <div class="chat-container">
    <div class="chat-header">
      <el-button :icon="ArrowLeft" @click="$router.back()">返回</el-button>
      <h3>智能问答</h3>
      <el-select v-model="style" size="small" style="width: 140px">
        <el-option label="🎓 学术严谨" value="academic" />
        <el-option label="💡 通俗易懂" value="simple" />
        <el-option label="🤔 苏格拉底式" value="socratic" />
      </el-select>
    </div>

    <!-- 消息列表 -->
    <div class="messages" ref="messagesRef">
      <div v-if="!messages.length" class="welcome">
        <div class="welcome-icon">🤖</div>
        <p>你好！我是基于本地 Qwen2.5 模型的知识库助手</p>
        <p>请输入你的问题，我将从知识库中检索相关内容来回答</p>
      </div>

      <div v-for="(msg, index) in messages" :key="index"
           :class="['message', msg.role]">
        <div class="avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
        <div class="bubble">
          <div class="content" v-html="formatContent(msg.content)"></div>
          <!-- 显示参考来源 -->
          <div v-if="msg.sources?.length" class="sources">
            <el-collapse>
              <el-collapse-item title="📎 参考来源" name="1">
                <div v-for="(src, i) in msg.sources" :key="i" class="source-item">
                  <el-tag size="small" type="info">相似度 {{ src.score }}</el-tag>
                  <span>{{ src.text }}</span>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </div>
      </div>

      <!-- 流式输出时的加载状态 -->
      <div v-if="loading" class="message assistant">
        <div class="avatar">🤖</div>
        <div class="bubble">
          <div class="content">{{ streamContent }}<span class="cursor">▋</span></div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="3"
        placeholder="输入你的问题... (Ctrl+Enter 发送)"
        @keydown.ctrl.enter="handleSend"
        resize="none"
      />
      <el-button
        type="primary" size="large"
        :loading="loading"
        :disabled="!inputText.trim()"
        @click="handleSend"
      >
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const kbId = parseInt(route.params.kbId)

const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const streamContent = ref('')
const style = ref('simple')
const messagesRef = ref(null)

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const formatContent = (text) => {
  // 简单的换行处理
  return text?.replace(/\n/g, '<br>') || ''
}

const handleSend = async () => {
  if (!inputText.value.trim() || loading.value) return

  const query = inputText.value.trim()
  inputText.value = ''

  // 添加用户消息
  messages.value.push({ role: 'user', content: query })
  scrollToBottom()

  loading.value = true
  streamContent.value = ''
  let sources = []

  try {
    // 调用 FastAPI 流式接口
    const response = await fetch('http://localhost:8001/api/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ query, kb_id: kbId, style: style.value })
    })

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const text = decoder.decode(value)
      const lines = text.split('\n').filter(l => l.startsWith('data: '))

      for (const line of lines) {
        try {
          const data = JSON.parse(line.slice(6))
          if (data.type === 'sources') {
            sources = data.sources
          } else if (data.type === 'token') {
            streamContent.value += data.content
            scrollToBottom()
          } else if (data.type === 'done') {
            // 流式结束，把内容加入消息列表
            messages.value.push({
              role: 'assistant',
              content: streamContent.value,
              sources
            })
            streamContent.value = ''
            loading.value = false
            scrollToBottom()
          }
        } catch (e) {}
      }
    }
  } catch (e) {
    ElMessage.error('请求失败，请检查 AI 服务是否正常运行')
    loading.value = false
  }
}
</script>

<style scoped>
.chat-container {
  height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;
  background: white;
}

.chat-header h3 { flex: 1; margin: 0; font-size: 16px; }

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome {
  text-align: center;
  color: #909399;
  margin: auto;
}

.welcome-icon { font-size: 48px; margin-bottom: 16px; }

.message {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.message.user {
  flex-direction: row-reverse;
  align-self: flex-end;
}

.avatar { font-size: 24px; flex-shrink: 0; }

.bubble {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 12px 16px;
  line-height: 1.6;
}

.message.user .bubble {
  background: #667eea;
  color: white;
}

.content { font-size: 14px; }

.cursor {
  animation: blink 1s infinite;
  color: #667eea;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.sources { margin-top: 8px; }

.source-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 6px 0;
  font-size: 12px;
  color: #606266;
  border-bottom: 1px solid #f0f0f0;
}

.input-area {
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 12px;
  align-items: flex-end;
  background: white;
}

.input-area .el-textarea { flex: 1; }
</style>