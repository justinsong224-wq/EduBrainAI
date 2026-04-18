<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span>📚 EduBrain AI</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        background-color="#1a1a2e"
        text-color="#ffffffaa"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据看板</span>
        </el-menu-item>

        <el-menu-item index="/knowledge">
          <el-icon><Collection /></el-icon>
          <span>知识库管理</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <el-dropdown @command="handleCommand">
          <div class="user-info">
            <el-avatar size="small" :style="{ background: '#667eea' }">
              {{ username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <span>{{ username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const username = computed(() => localStorage.getItem('username'))
const activeMenu = computed(() => '/' + route.path.split('/')[1])

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.clear()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background: #1a1a2e;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  color: white;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #ffffff15;
}

.el-menu {
  border-right: none;
  flex: 1;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #ffffff15;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ffffffaa;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #ffffff15;
}

.main-content {
  background: #f5f7fa;
  padding: 24px;
}
</style>