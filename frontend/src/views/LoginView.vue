<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>📚 EduBrain AI</h1>
        <p>教育知识库智能助手</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-link">
        没有账号？
        <el-button type="primary" link @click="showRegister = true">
          立即注册
        </el-button>
      </div>
    </div>

    <!-- 注册弹窗 -->
    <el-dialog v-model="showRegister" title="注册账号" width="400px">
      <el-form :model="registerForm" ref="registerFormRef" size="large">
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="registerForm.department" placeholder="请输入部门（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegister = false">取消</el-button>
        <el-button type="primary" :loading="registerLoading" @click="handleRegister">
          注册
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const showRegister = ref(false)
const registerLoading = ref(false)

const form = ref({ username: '', password: '' })
const registerForm = ref({ username: '', password: '', email: '', department: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await axios.post('/api/auth/login', form.value)
      if (res.data.code === 200) {
        // 存储 token 和用户信息
        localStorage.setItem('token', res.data.data.token)
        localStorage.setItem('username', res.data.data.username)
        localStorage.setItem('role', res.data.data.role)
        ElMessage.success('登录成功！')
        router.push('/dashboard')
      } else {
        ElMessage.error(res.data.message)
      }
    } catch (e) {
      ElMessage.error('登录失败，请检查用户名密码')
    } finally {
      loading.value = false
    }
  })
}

const handleRegister = async () => {
  registerLoading.value = true
  try {
    const res = await axios.post('/api/auth/register', registerForm.value)
    if (res.data.code === 200) {
      ElMessage.success('注册成功，请登录')
      showRegister.value = false
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('注册失败')
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  background: white;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 8px;
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.login-btn {
  width: 100%;
}

.register-link {
  text-align: center;
  margin-top: 16px;
  color: #909399;
  font-size: 14px;
}
</style>