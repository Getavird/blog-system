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
            
            <!-- 使用 ArticleList 组件 -->
            <ArticleList
              :articles="articles"
              :loading="loading"
              :show-time="true"
              :show-views="true"
              :show-author="true"
              :show-summary="true"
              :show-pagination="true"
              :total="total"
              :current-page="currentPage"
              :page-size="pageSize"
              @article-click="viewArticle"
              @create-click="toWriteArticle"
              @size-change="handleSizeChange"
              @page-change="handlePageChange"
            />
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
                  <a href="javascript:;" class="category-item" @click="viewCategory(category.id)">
                    <span class="category-name">{{ category.name }}</span>
                    <span class="category-count">({{ category.count }})</span>
                  </a>
                </li>
              </ul>
            </div>
            
            <!-- 标签云 -->
            <div class="tags-card">
              <h3><el-icon><PriceTag /></el-icon> 热门标签</h3>
              <div class="tags-cloud">
                <el-tag
                  v-for="tag in tags"
                  :key="tag.id"
                  :type="tagTypes[tag.id % tagTypes.length]"
                  size="medium"
                  class="tag-cloud-item"
                  @click="viewTag(tag.id)"
                >
                  {{ tag.name }} ({{ tag.count }})
                </el-tag>
              </div>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Star, Folder, PriceTag } from '@element-plus/icons-vue'

// 导入组件
import Header from '../components/layout/Header.vue'
import Footer from '../components/layout/Footer.vue'
import ArticleList from '../components/article/ArticleList.vue'

const router = useRouter()

// 状态
const loading = ref(true)
const isLoggedIn = ref(false)
const articles = ref([])
const hotArticles = ref([])
const categories = ref([])
const tags = ref([])
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)

// 标签类型数组
const tagTypes = ['', 'success', 'info', 'warning', 'danger']

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
    authorId: 1,
    viewCount: 156,
    likeCount: 25,
    commentCount: 8,
    createTime: '2024-01-15 10:30:00',
    updateTime: '2024-01-15 10:30:00',
    categoryName: '技术',
    tags: ['Spring Boot', 'Java', '后端']
  },
  {
    id: 2,
    title: 'Vue 3新特性详解',
    summary: '深入解析Vue 3的新特性和使用技巧，带你快速上手Vue 3开发...',
    authorName: '李四',
    authorId: 2,
    viewCount: 203,
    likeCount: 42,
    commentCount: 12,
    createTime: '2024-01-14 14:20:00',
    updateTime: '2024-01-15 09:30:00',
    categoryName: '前端',
    tags: ['Vue 3', '前端', 'JavaScript']
  },
  {
    id: 3,
    title: '数据库设计规范',
    summary: '分享数据库设计的最佳实践和规范，让你的数据架构更合理...',
    authorName: '王五',
    authorId: 3,
    viewCount: 89,
    likeCount: 18,
    commentCount: 5,
    createTime: '2024-01-13 09:15:00',
    updateTime: '2024-01-13 09:15:00',
    categoryName: '数据库',
    tags: ['数据库', 'MySQL', '设计']
  },
  {
    id: 4,
    title: '如何写好技术文档',
    summary: '技术文档写作的实用技巧和经验分享，提升文档质量和可读性...',
    authorName: '赵六',
    authorId: 4,
    viewCount: 124,
    likeCount: 32,
    commentCount: 7,
    createTime: '2024-01-12 16:45:00',
    updateTime: '2024-01-12 16:45:00',
    categoryName: '学习',
    tags: ['文档', '写作', '技巧']
  },
  {
    id: 5,
    title: 'Git 高级使用技巧',
    summary: '掌握Git的高级功能和工作流，提升团队协作效率...',
    authorName: '钱七',
    authorId: 5,
    viewCount: 78,
    likeCount: 15,
    commentCount: 3,
    createTime: '2024-01-11 11:20:00',
    updateTime: '2024-01-11 11:20:00',
    categoryName: '工具',
    tags: ['Git', '版本控制', '工具']
  }
]

const mockHotArticles = [
  { id: 2, title: 'Vue 3新特性详解', viewCount: 320 },
  { id: 1, title: 'Spring Boot入门教程', viewCount: 280 },
  { id: 4, title: '如何写好技术文档', viewCount: 250 },
  { id: 3, title: '数据库设计规范', viewCount: 210 },
  { id: 5, title: 'Git 高级使用技巧', viewCount: 180 }
]

const mockCategories = [
  { id: 1, name: '技术', count: 12 },
  { id: 2, name: '生活', count: 8 },
  { id: 3, name: '学习', count: 5 },
  { id: 4, name: '工具', count: 3 },
  { id: 5, name: '随笔', count: 7 }
]

const mockTags = [
  { id: 1, name: 'Vue', count: 15 },
  { id: 2, name: 'React', count: 12 },
  { id: 3, name: 'JavaScript', count: 28 },
  { id: 4, name: 'Spring Boot', count: 10 },
  { id: 5, name: 'Java', count: 22 },
  { id: 6, name: 'Python', count: 18 },
  { id: 7, name: '数据库', count: 9 },
  { id: 8, name: '前端', count: 25 },
  { id: 9, name: '后端', count: 20 },
  { id: 10, name: '算法', count: 14 }
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
    tags.value = mockTags
    total.value = 25
    loading.value = false
  }, 800)
}

const viewArticle = (id) => {
  router.push(`/article/${id}`)
}

const viewCategory = (categoryId) => {
  // 这里可以跳转到分类页面或筛选该分类的文章
  console.log('查看分类:', categoryId)
  // 简单实现：在当前页面筛选该分类的文章
  // 实际项目中可以跳转到分类页面
  ElMessage.info(`查看分类 ${categories.value.find(c => c.id === categoryId)?.name || categoryId} 的文章`)
}

const viewTag = (tagId) => {
  // 这里可以跳转到标签页面或筛选该标签的文章
  console.log('查看标签:', tagId)
  const tagName = tags.value.find(t => t.id === tagId)?.name || tagId
  ElMessage.info(`查看标签 #${tagName} 的文章`)
}

const toWriteArticle = () => {
  // 检查是否登录
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    showLoginDialog.value = true
    return
  }
  router.push('/article/create')
}

const toArticlesList = () => {
  currentPage.value = 1
  loadData()
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
  width: 320px;
  flex-shrink: 0;
}

/* 侧边栏卡片样式 */
.hot-articles, .category-card, .tags-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.hot-articles h3, .category-card h3, .tags-card h3 {
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

/* 标签云样式 */
.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-cloud-item {
  cursor: pointer;
  transition: transform 0.2s;
}

.tag-cloud-item:hover {
  transform: translateY(-2px);
}

/* 登录/注册弹窗样式 */
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

/* 响应式设计 */
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