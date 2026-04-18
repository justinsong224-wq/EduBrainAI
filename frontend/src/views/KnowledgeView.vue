<template>
  <div class="knowledge">
    <div class="page-header">
      <h2 class="page-title">知识库管理</h2>
      <el-button type="primary" :icon="Plus" @click="showCreate = true">
        新建知识库
      </el-button>
    </div>

    <!-- 知识库列表 -->
    <el-row :gutter="20">
      <el-col :span="8" v-for="kb in kbList" :key="kb.id">
        <div class="kb-card">
          <div class="kb-header">
            <div class="kb-icon">📖</div>
            <div class="kb-actions">
              <el-button
                type="primary" size="small" :icon="ChatDotRound"
                @click="goChat(kb.id)">
                开始问答
              </el-button>
              <el-button
                type="danger" size="small" :icon="Delete"
                @click="handleDelete(kb.id)">
              </el-button>
            </div>
          </div>

          <div class="kb-name">{{ kb.name }}</div>
          <div class="kb-desc">{{ kb.description || '暂无描述' }}</div>

          <div class="kb-meta">
            <el-tag size="small" :type="kb.isPublic ? 'success' : 'info'">
              {{ kb.isPublic ? '公开' : '私有' }}
            </el-tag>
            <span class="kb-date">{{ formatDate(kb.createdAt) }}</span>
          </div>

          <!-- 文档列表 -->
          <el-divider />
          <div class="doc-section">
            <div class="doc-header">
              <span class="doc-title">文档列表</span>
              <el-upload
                :action="`http://localhost:8080/api/document/upload`"
                :headers="uploadHeaders"
                :data="{ kbId: kb.id }"
                :on-success="() => loadDocs(kb.id)"
                :on-error="handleUploadError"
                :show-file-list="false"
                accept=".pdf,.txt,.md,.docx"
              >
                <el-button size="small" :icon="Upload">上传文档</el-button>
              </el-upload>
            </div>

            <div v-if="docMap[kb.id]?.length" class="doc-list">
              <div v-for="doc in docMap[kb.id]" :key="doc.id" class="doc-item">
                <el-icon><Document /></el-icon>
                <span class="doc-name">{{ doc.originalName }}</span>
                <el-tag size="small" :type="getStatusType(doc.status)">
                  {{ getStatusText(doc.status) }}
                </el-tag>
                <el-button
                  type="danger" size="small" :icon="Delete" circle
                  @click="handleDeleteDoc(doc.id,kb.id)"
                />
              </div>
            </div>
            <el-empty v-else description="暂无文档" :image-size="50" />
          </div>
        </div>
      </el-col>

      <el-col :span="8" v-if="!kbList.length">
        <el-empty description="还没有知识库，快来创建一个吧" />
      </el-col>
    </el-row>

    <!-- 新建知识库弹窗 -->
    <el-dialog v-model="showCreate" title="新建知识库" width="480px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="createForm.name" placeholder="请输入知识库名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="createForm.description"
            type="textarea" :rows="3"
            placeholder="请输入知识库描述（选填）"
          />
        </el-form-item>
        <el-form-item label="是否公开">
          <el-switch v-model="createForm.isPublic" />
          <span class="switch-hint">公开后其他用户也可以访问</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreate">
          创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted,onUnmounted,computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Upload, ChatDotRound } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const kbList = ref([])
const docMap = ref({})
const showCreate = ref(false)
const createLoading = ref(false)
const createForm = ref({ name: '', description: '', isPublic: false })

// 上传时自动带 token
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

let pollTimer = null

onMounted(() => {
  loadKbList()
  // 每5秒自动刷新文档状态
  pollTimer = setInterval(() => {
    kbList.value.forEach(kb => loadDocs(kb.id))
  }, 1000)
})

// 组件卸载时清除定时器
onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})

const loadKbList = async () => {
  try {
    const res = await axios.get('/api/kb')
    kbList.value = res.data.data || []
    // 加载每个知识库的文档
    kbList.value.forEach(kb => loadDocs(kb.id))
  } catch (e) {
    ElMessage.error('加载知识库失败')
  }
}

const loadDocs = async (kbId) => {
  try {
    const res = await axios.get(`/api/document/list?kbId=${kbId}`)
    docMap.value[kbId] = res.data.data || []
  } catch (e) {}
}

const handleCreate = async () => {
  if (!createForm.value.name) {
    ElMessage.warning('请输入知识库名称')
    return
  }
  createLoading.value = true
  try {
    const res = await axios.post('/api/kb', createForm.value)
    if (res.data.code === 200) {
      ElMessage.success('创建成功！')
      showCreate.value = false
      createForm.value = { name: '', description: '', isPublic: false }
      loadKbList()
    }
  } catch (e) {
    ElMessage.error('创建失败')
  } finally {
    createLoading.value = false
  }
}

const handleDeleteDoc = async (docId, kbId) => {
  await ElMessageBox.confirm('确定删除这个文档吗？删除后向量数据也会同步清除。', '警告', { type: 'warning' })
  try {
    await axios.delete(`/api/document/${docId}`)
    ElMessage.success('文档删除成功')
    loadDocs(kbId)
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handleUploadError = () => ElMessage.error('上传失败，请重试')

const goChat = (kbId) => router.push(`/chat/${kbId}`)

const getStatusType = (status) => {
  const map = { PENDING: 'info', PROCESSING: 'warning', DONE: 'success', FAILED: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { PENDING: '待处理', PROCESSING: '处理中', DONE: '已完成', FAILED: '失败' }
  return map[status] || status
}

const formatDate = (date) => date ? new Date(date).toLocaleDateString('zh-CN') : ''
</script>

<style scoped>
.knowledge {}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.kb-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: transform 0.2s;
}

.kb-card:hover {
  transform: translateY(-2px);
}

.kb-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.kb-icon { font-size: 32px; }

.kb-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 6px;
}

.kb-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
  line-height: 1.5;
}

.kb-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.kb-date { font-size: 12px; color: #c0c4cc; }

.doc-section {}

.doc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.doc-title { font-size: 13px; color: #606266; font-weight: 500; }

.doc-list { max-height: 150px; overflow-y: auto; }

.doc-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  font-size: 13px;
  color: #606266;
}

.doc-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.switch-hint { font-size: 12px; color: #909399; margin-left: 8px; }
</style>