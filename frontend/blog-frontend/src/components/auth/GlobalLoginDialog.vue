<template>
  <el-dialog 
    v-model="showDialog" 
    :title="activeTab === 'login' ? '用户登录' : '用户注册'" 
    width="400px"
    :close-on-click-modal="false" 
    @closed="resetForm"
    class="global-login-dialog"
  >
    <!-- 标签切换 -->
    <div class="dialog-tabs">
      <div class="tab-item" :class="{ active: activeTab === 'login' }" @click="activeTab = 'login'">
        登录
      </div>
      <div class="tab-item" :class="{ active: activeTab === 'register' }" @click="activeTab = 'register'">
        注册
      </div>
    </div>

    <!-- 登录表单 -->
    <div v-if="activeTab === 'login'" class="login-form">
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名" size="large" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>

        <div class="form-options">
          <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
          <a href="javascript:;" class="forgot-link">忘记密码？</a>
        </div>

        <el-button type="primary" size="large" :loading="authStore.loading" @click="handleLogin" class="submit-btn">
          登录
        </el-button>
      </el-form>
    </div>

    <!-- 注册表单 -->
    <div v-else class="register-form">
      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" @submit.prevent="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="用户名" size="large" />
        </el-form-item>

        <el-form-item prop="email">
          <el-input v-model="registerForm.email" placeholder="邮箱" size="large" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" size="large" show-password />
        </el-form-item>

        <el-form-item prop="agree">
          <el-checkbox v-model="registerForm.agree">
            我已阅读并同意
            <a href="javascript:;" class="link">服务条款</a>
          </el-checkbox>
        </el-form-item>

        <el-button type="primary" size="large" :loading="authStore.loading" @click="handleRegister" class="submit-btn">
          注册
        </el-button>
      </el-form>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const uiStore = useUIStore()
const authStore = useAuthStore()
const userStore = useUserStore()

// 计算属性
const showDialog = computed({
  get: () => uiStore.showLoginDialog,
  set: (value) => {
    if (!value) uiStore.closeLoginDialog()
  }
})

const activeTab = computed({
  get: () => uiStore.loginDialogTab,
  set: (value) => {
    uiStore.loginDialogTab = value
  }
})

// 表单相关
const loginFormRef = ref(null)
const registerFormRef = ref(null)

// 登录表单
const loginForm = ref({
  username: '',
  password: '',
  remember: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 注册表单
const registerForm = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agree: false
})

// 验证密码
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能小于6位'))
  } else {
    callback()
  }
}

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.value.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 验证同意条款
const validateAgree = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请同意服务条款'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在3到20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agree: [
    { validator: validateAgree, trigger: 'change' }
  ]
}

// 登录方法
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    // 表单验证
    await loginFormRef.value.validate()

    // 使用 authStore 的 login 方法
    await authStore.login(loginForm.value.username, loginForm.value.password)

    ElMessage.success('登录成功')
    uiStore.closeLoginDialog()
    resetForm()
    
    // 登录成功后刷新页面数据（由各页面自行处理）
    router.go(0) // 简单刷新当前页面
    
  } catch (error) {
    console.error('登录失败:', error)
  }
}

// 注册方法
const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    // 表单验证
    await registerFormRef.value.validate()

    // 使用 authStore 的 register 方法
    await authStore.register({
      username: registerForm.value.username,
      email: registerForm.value.email,
      password: registerForm.value.password
    })

    ElMessage.success('注册成功')
    activeTab.value = 'login' // 注册成功后切换到登录标签
    resetForm()
    
  } catch (error) {
    console.error('注册失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  if (loginFormRef.value) {
    loginFormRef.value.resetFields()
  }
  if (registerFormRef.value) {
    registerFormRef.value.resetFields()
  }
  loginForm.value = {
    username: '',
    password: '',
    remember: false
  }
  registerForm.value = {
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    agree: false
  }
}
</script>

<style scoped>
.global-login-dialog {
  border-radius: 8px;
}

.dialog-tabs {
  display: flex;
  margin-bottom: 30px;
  border-bottom: 1px solid #eee;
}

.dialog-tabs .tab-item {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  cursor: pointer;
  font-size: 16px;
  color: #666;
  transition: all 0.3s;
}

.dialog-tabs .tab-item:hover {
  color: #409eff;
}

.dialog-tabs .tab-item.active {
  color: #409eff;
  border-bottom: 2px solid #409eff;
  font-weight: bold;
}

.login-form .el-form-item,
.register-form .el-form-item {
  margin-bottom: 20px;
}

.login-form .form-options,
.register-form .form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.login-form .form-options .forgot-link,
.register-form .form-options .forgot-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.login-form .form-options .forgot-link:hover,
.register-form .form-options .forgot-link:hover {
  text-decoration: underline;
}

.login-form .submit-btn,
.register-form .submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
}

.login-form .link,
.register-form .link {
  color: #409eff;
  text-decoration: none;
}

.login-form .link:hover,
.register-form .link:hover {
  text-decoration: underline;
}

/* 响应式 */
@media (max-width: 768px) {
  .el-dialog {
    width: 90% !important;
    max-width: 400px !important;
  }
}
</style>