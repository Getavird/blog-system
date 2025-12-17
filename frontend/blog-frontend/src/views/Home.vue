<template>
  <div class="home-page">
    <Header @show-login="showLoginDialog = true" />
    
    <main class="main-content">
      <!-- 欢迎横幅 -->
      <section class="hero-banner">
        <div class="container">
          <div class="hero-content">
            <h1 class="hero-title">欢迎来到博客系统</h1>
            <p class="hero-subtitle">分享技术，记录生活，共同成长</p>
            <div class="hero-actions">
              <el-button v-if="!isLoggedIn" type="primary" size="large" @click="showLoginDialog = true; activeTab = 'login'">
                开始使用
              </el-button>
              <el-button v-else type="primary" size="large" @click="toWriteArticle">
                写文章
              </el-button>
              <el-button type="default" size="large" @click="toArticlesList">
                浏览文章
              </el-button>
            </div>
          </div>
        </div>
      </section>
      
      <!-- 主要内容 -->
      <div class="container">
        <div class="home-content">
          <!-- 左侧：文章列表 -->
          <div class="articles-section">
            <h2><el-icon><Document /></el-icon> 最新文章</h2>
            
            <!-- 加载状态 -->
            <div v-if="loading" class="loading-state">
              <p>加载中...</p>
            </div>
            
            <!-- 文章列表 -->
            <div v-else-if="articles.length > 0" class="article-list">
              <div v-for="article in articles" :key="article.id" class="article-item" @click="viewArticle(article.id)">
                <div class="article-content">
                  <h3>{{ article.title }}</h3>
                  <p class="article-summary">{{ article.summary }}</p>
                  <div class="article-meta">
                    <span class="author">{{ article.authorName }}</span>
                    <span class="time">{{ formatTime(article.createTime) }}</span>
                    <span class="views">👁 {{ article.viewCount }}</span>
                    <span class="likes">❤ {{ article.likeCount }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 空状态 -->
            <div v-else class="empty-state">
              <p>暂无文章</p>
            </div>
            
            <!-- 分页 -->
            <div v-if="articles.length > 0" class="pagination-wrapper">
              <el-pagination
                :current-page="currentPage"
                :page-size="pageSize"
                :total="total"
                :page-sizes="[5, 10, 20]"
                layout="total, sizes, prev, pager, next"
                @size-change="handleSizeChange"
                @current-change="handlePageChange"
              />
            </div>
          </div>
          
          <!-- 右侧：侧边栏 -->
          <aside class="sidebar">
            <!-- 热门文章 -->
            <div class="hot-articles">
              <h3><el-icon><Star /></el-icon> 热门文章</h3>
              <ul class="hot-list">
                <li v-for="article in hotArticles" :key="article.id">
                  <a href="javascript:;" class="hot-item" @click="viewArticle(article.id)">
                    <span class="hot-title">{{ article.title }}</span>
                    <span class="hot-views">👁 {{ article.viewCount }}</span>
                  </a>
                </li>
              </ul>
            </div>
            
            <!-- 分类统计 -->
            <div class="category-card">
              <h3><el-icon><Folder /></el-icon> 文章分类</h3>
              <ul class="category-list">
                <li v-for="category in categories" :key="category.id">
                  <a href="javascript:;" class="category-item">
                    <span class="category-name">{{ category.name }}</span>
                    <span class="category-count">({{ category.count }})</span>
                  </a>
                </li>
              </ul>
            </div>
          </aside>
        </div>
      </div>
    </main>
    
    <Footer />
    
    <!-- 登录/注册弹窗 -->
    <el-dialog
      v-model="showLoginDialog"
      :title="activeTab === 'login' ? '用户登录' : '用户注册'"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @closed="resetForm"
    >
      <!-- 标签切换 -->
      <div class="dialog-tabs">
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'login' }"
          @click="activeTab = 'login'"
        >
          登录
        </div>
        <div 
          class="tab-item"
          :class="{ active: activeTab === 'register' }"
          @click="activeTab = 'register'"
        >
          注册
        </div>
      </div>
      
      <!-- 登录表单 -->
      <div v-if="activeTab === 'login'" class="login-form">
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="用户名"
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              size="large"
              show-password
            />
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
            <a href="javascript:;" class="forgot-link">忘记密码？</a>
          </div>
          
          <el-button
            type="primary"
            size="large"
            :loading="loginLoading"
            @click="handleLogin"
            class="submit-btn"
          >
            登录
          </el-button>
        </el-form>
      </div>
      
      <!-- 注册表单 -->
      <div v-else class="register-form">
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          @submit.prevent="handleRegister"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="用户名"
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="邮箱"
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码"
              size="large"
              show-password
            />
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              size="large"
              show-password
            />
          </el-form-item>
          
          <el-form-item prop="agree">
            <el-checkbox v-model="registerForm.agree">
              我已阅读并同意
              <a href="javascript:;" class="link">服务条款</a>
            </el-checkbox>
          </el-form-item>
          
          <el-button
            type="primary"
            size="large"
            :loading="registerLoading"
            @click="handleRegister"
            class="submit-btn"
          >
            注册
          </el-button>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, defineAsyncComponent } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Star, Folder } from '@element-plus/icons-vue'

// 使用 defineAsyncComponent 导入组件
const Header = defineAsyncComponent(() => import('../components/layout/Header.vue'))
const Footer = defineAsyncComponent(() => import('../components/layout/Footer.vue'))

const router = useRouter()

// 状态
const loading = ref(true)
const isLoggedIn = ref(false)
const articles = ref([])
const hotArticles = ref([])
const categories = ref([])
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)

// 登录/注册弹窗相关
const showLoginDialog = ref(false)
const activeTab = ref('login') // 'login' 或 'register'

// 登录表单
const loginFormRef = ref(null)
const loginLoading = ref(false)
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
const registerFormRef = ref(null)
const registerLoading = ref(false)
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

// 模拟数据
const mockArticles = [
  {
    id: 1,
    title: 'Spring Boot入门教程',
    summary: '详细介绍Spring Boot的基本使用和配置，快速上手后端开发...',
    authorName: '张三',
    viewCount: 156,
    likeCount: 25,
    createTime: '2024-01-15 10:30:00'
  },
  {
    id: 2,
    title: 'Vue 3新特性详解',
    summary: '深入解析Vue 3的新特性和使用技巧，带你快速上手Vue 3开发...',
    authorName: '李四',
    viewCount: 203,
    likeCount: 42,
    createTime: '2024-01-14 14:20:00'
  },
  {
    id: 3,
    title: '数据库设计规范',
    summary: '分享数据库设计的最佳实践和规范，让你的数据架构更合理...',
    authorName: '王五',
    viewCount: 89,
    likeCount: 18,
    createTime: '2024-01-13 09:15:00'
  }
]

const mockHotArticles = [
  { id: 1, title: 'Vue 3新特性详解', viewCount: 320 },
  { id: 2, title: 'MyBatis使用技巧', viewCount: 280 },
  { id: 3, title: '数据库设计规范', viewCount: 250 }
]

const mockCategories = [
  { id: 1, name: '技术', count: 12 },
  { id: 2, name: '生活', count: 8 },
  { id: 3, name: '学习', count: 5 }
]

// 生命周期
onMounted(() => {
  // 检查登录状态
  const token = localStorage.getItem('blog_token')
  isLoggedIn.value = !!token
  
  loadData()
})

// 方法
const loadData = () => {
  loading.value = true
  
  // 模拟API调用
  setTimeout(() => {
    articles.value = mockArticles
    hotArticles.value = mockHotArticles
    categories.value = mockCategories
    total.value = 25
    loading.value = false
  }, 800)
}

const viewArticle = (id) => {
  router.push(`/article/${id}`)
}

const toWriteArticle = () => {
  router.push('/article/create')
}

const toArticlesList = () => {
  currentPage.value = 1
  loadData()
}

const formatTime = (time) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    return '今天'
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

// 登录方法
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loginLoading.value = true
    
    // 模拟登录
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟用户数据
    const userData = {
      id: 1,
      username: loginForm.value.username,
      email: 'test@example.com',
      role: 1,
      articleCount: 5,
      likeCount: 23,
      viewCount: 156
    }
    
    // 保存到localStorage
    localStorage.setItem('blog_user', JSON.stringify(userData))
    localStorage.setItem('blog_token', 'mock_token_123456')
    
    ElMessage.success('登录成功')
    showLoginDialog.value = false
    isLoggedIn.value = true
    
    // 刷新页面以更新状态
    window.location.reload()
  } catch (error) {
    ElMessage.error('登录失败')
  } finally {
    loginLoading.value = false
  }
}

// 注册方法
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    registerLoading.value = true
    
    // 模拟注册
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('注册成功')
    // 注册成功后切换到登录标签
    activeTab.value = 'login'
    
    // 清空注册表单
    registerForm.value = {
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      agree: false
    }
  } catch (error) {
    ElMessage.error('注册失败')
  } finally {
    registerLoading.value = false
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
  loginLoading.value = false
  registerLoading.value = false
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}

.hero-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 80px 0;
  text-align: center;
  margin-bottom: 40px;
}

.hero-banner .container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.hero-banner .hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-banner .hero-title {
  font-size: 48px;
  margin-bottom: 20px;
  font-weight: bold;
}

.hero-banner .hero-subtitle {
  font-size: 20px;
  margin-bottom: 40px;
  opacity: 0.9;
}

.hero-banner .hero-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.hero-banner .hero-actions .el-button {
  padding: 12px 32px;
  font-weight: bold;
}

.home-content {
  display: flex;
  gap: 30px;
  margin-bottom: 50px;
}

.home-content .container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  gap: 30px;
}

.home-content .articles-section {
  flex: 1;
}

.home-content .articles-section h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}

.home-content .sidebar {
  width: 300px;
}

.loading-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

.article-list .article-item {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.article-list .article-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.article-list .article-item .article-content h3 {
  font-size: 18px;
  margin-bottom: 10px;
  color: #333;
}

.article-list .article-item .article-content .article-summary {
  color: #666;
  line-height: 1.6;
  margin-bottom: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
  display: -moz-box;
  -moz-box-orient: vertical;
  display: box;
  box-orient: vertical;
}

.article-list .article-item .article-content .article-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #999;
}

.article-list .article-item .article-content .article-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
  background: white;
  border-radius: 8px;
}

.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.hot-articles, .category-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.hot-articles h3, .category-card h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 18px;
  color: #333;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.hot-list, .category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.hot-list li, .category-list li {
  margin-bottom: 10px;
}

.hot-list li:last-child, .category-list li:last-child {
  margin-bottom: 0;
}

.hot-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  border-radius: 4px;
  text-decoration: none;
  color: #333;
  transition: background-color 0.3s;
}

.hot-item:hover {
  background-color: #f5f7fa;
}

.hot-item:hover .hot-title {
  color: #409eff;
}

.hot-item .hot-title {
  flex: 1;
  font-size: 14px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  margin-right: 10px;
}

.hot-item .hot-views {
  color: #999;
  font-size: 12px;
  white-space: nowrap;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  border-radius: 4px;
  text-decoration: none;
  color: #333;
  transition: background-color 0.3s;
}

.category-item:hover {
  background-color: #f5f7fa;
}

.category-item:hover .category-name {
  color: #409eff;
}

.category-item .category-name {
  flex: 1;
}

.category-item .category-count {
  color: #999;
  font-size: 12px;
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

.login-form .el-form-item, .register-form .el-form-item {
  margin-bottom: 20px;
}

.login-form .form-options, .register-form .form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.login-form .form-options .forgot-link, .register-form .form-options .forgot-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.login-form .form-options .forgot-link:hover, .register-form .form-options .forgot-link:hover {
  text-decoration: underline;
}

.login-form .submit-btn, .register-form .submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
}

.login-form .link, .register-form .link {
  color: #409eff;
  text-decoration: none;
}

.login-form .link:hover, .register-form .link:hover {
  text-decoration: underline;
}

@media (max-width: 992px) {
  .home-content .container {
    flex-direction: column;
  }
  
  .home-content .sidebar {
    width: 100%;
  }
  
  .hero-title {
    font-size: 36px !important;
  }
  
  .hero-subtitle {
    font-size: 16px !important;
  }
}

@media (max-width: 768px) {
  .hero-banner {
    padding: 60px 0;
  }
  
  .hero-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .hero-actions .el-button {
    width: 200px;
  }
  
  .el-dialog {
    width: 90% !important;
    max-width: 400px !important;
  }
}
</style>