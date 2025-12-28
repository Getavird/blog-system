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
              <el-button v-if="!isLoggedIn" type="primary" size="large"
                @click="showLoginDialog = true; activeTab = 'login'">
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
            <h2><el-icon>
                <Document />
              </el-icon> 最新文章</h2>

            <!-- 文章列表加载状态 -->
            <div v-if="articleStore.articlesLoading" class="loading-container">
              <el-skeleton :rows="5" animated />
            </div>

            <!-- 文章列表为空 -->
            <div v-else-if="articles.length === 0" class="empty-state">
              <el-empty description="暂无文章" />
            </div>

            <!-- 文章列表 -->
            <ArticleList v-else :articles="articles" :loading="articleStore.articlesLoading" 
              :show-time="true" :show-views="true" :show-author="true" 
              :show-summary="true" :show-pagination="true" :total="total"
              :current-page="currentPage" :page-size="pageSize" 
              @article-click="viewArticle" @create-click="toWriteArticle"
              @size-change="handleSizeChange" @page-change="handlePageChange" />
          </div>

          <!-- 右侧：侧边栏 -->
          <aside class="sidebar">
            <!-- 热门文章 -->
            <div class="hot-articles">
              <h3><el-icon>
                  <Star />
                </el-icon> 热门文章</h3>
              
              <div v-if="articleStore.hotLoading" class="loading-mini">
                <el-skeleton :rows="3" animated />
              </div>
              
              <ul v-else class="hot-list">
                <li v-for="article in hotArticles" :key="article.id">
                  <a href="javascript:;" class="hot-item" @click="viewArticle(article.id)">
                    <span class="hot-title">{{ article.title }}</span>
                    <span class="hot-views">👁 {{ article.viewCount || 0 }}</span>
                  </a>
                </li>
                <li v-if="hotArticles.length === 0" class="empty-item">
                  暂无热门文章
                </li>
              </ul>
            </div>

            <!-- 分类统计 -->
            <div class="category-card">
              <h3><el-icon>
                  <Folder />
                </el-icon> 文章分类</h3>
              
              <div v-if="categoryStore.loading" class="loading-mini">
                <el-skeleton :rows="3" animated />
              </div>
              
              <ul v-else class="category-list">
                <li v-for="category in categoriesWithStats" :key="category.id">
                  <a href="javascript:;" class="category-item" @click="viewCategory(category.id)">
                    <span class="category-name">{{ category.name }}</span>
                    <span class="category-count">({{ category.articleCount || 0 }})</span>
                  </a>
                </li>
                <li v-if="categoriesWithStats.length === 0" class="empty-item">
                  暂无分类
                </li>
              </ul>
            </div>

            <!-- 标签云 -->
            <div class="tags-card">
              <h3><el-icon>
                  <PriceTag />
                </el-icon> 热门标签</h3>
              
              <div v-if="tagStore.loading" class="loading-mini">
                <el-skeleton :rows="2" animated />
              </div>
              
              <div v-else class="tags-cloud">
                <el-tag v-for="tag in tagsWithStats" :key="tag.id" 
                  :type="tagTypes[tag.id % tagTypes.length]" size="medium"
                  class="tag-cloud-item" @click="viewTag(tag)">
                  {{ tag.name }} ({{ tag.articleCount || 0 }})
                </el-tag>
                <div v-if="tagsWithStats.length === 0" class="empty-tags">
                  暂无标签
                </div>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </main>

    <Footer />

    <!-- 登录/注册弹窗 -->
    <el-dialog v-model="showLoginDialog" :title="activeTab === 'login' ? '用户登录' : '用户注册'" width="400px"
      :close-on-click-modal="false" :close-on-press-escape="false" @closed="resetForm">
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
            <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" size="large"
              show-password />
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useArticleStore } from '@/stores/article'
import { useCategoryStore } from '@/stores/category'
import { useTagStore } from '@/stores/tag'
import { ElMessage } from 'element-plus'
import { Document, Star, Folder, PriceTag } from '@element-plus/icons-vue'
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'
import ArticleList from '@/components/article/ArticleList.vue'

const router = useRouter()

// Pinia Stores
const authStore = useAuthStore()
const articleStore = useArticleStore()
const categoryStore = useCategoryStore()
const tagStore = useTagStore()

// 状态
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

// 计算属性
const isLoggedIn = computed(() => authStore.isLoggedIn)
const articles = computed(() => articleStore.articles || [])
const hotArticles = computed(() => articleStore.hotArticles || [])
const categories = computed(() => categoryStore.categories || [])
const tags = computed(() => tagStore.tags || [])
const total = computed(() => articleStore.total || 0)

// 处理带统计的分类和标签数据
const categoriesWithStats = computed(() => {
  return categories.value.map(category => ({
    ...category,
    articleCount: category.articleCount || category.count || 0
  }))
})

const tagsWithStats = computed(() => {
  return tags.value.map(tag => ({
    ...tag,
    articleCount: tag.articleCount || 0
  }))
})

// 标签类型数组
const tagTypes = ['', 'success', 'info', 'warning', 'danger']

// 登录/注册弹窗相关
const showLoginDialog = ref(false)
const activeTab = ref('login')
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

// 监听登录状态变化
watch(isLoggedIn, (newValue) => {
  if (newValue) {
    // 登录后刷新数据
    loadData()
  }
})

// 生命周期
onMounted(async () => {
  console.log('首页 mounted，开始加载数据...')
  
  // 如果已登录，自动获取用户信息
  if (authStore.isLoggedIn) {
    try {
      await authStore.fetchCurrentUser()
    } catch (error) {
      console.warn('自动获取用户信息失败:', error)
    }
  }
  
  // 加载首页数据
  await loadData()
})

// 方法：加载首页数据
const loadData = async () => {
  try {
    loading.value = true
    console.log('开始加载首页数据...')

    // 并行加载所有数据
    await Promise.all([
      // 1. 加载文章列表
      articleStore.fetchArticles({
        page: currentPage.value,
        size: pageSize.value
      }),

      // 2. 加载热门文章
      articleStore.fetchHotArticles(5),

      // 3. 加载分类列表
      categoryStore.fetchCategories(),

      // 4. 加载标签列表
      tagStore.fetchTags()
    ])
    
    console.log('首页数据加载完成')
    console.log('文章数量:', articles.value.length)
    console.log('热门文章数量:', hotArticles.value.length)
    console.log('分类数量:', categories.value.length)
    console.log('标签数量:', tags.value.length)

  } catch (error) {
    console.error('加载首页数据失败:', error)
    ElMessage.error('数据加载失败，请刷新重试')
  } finally {
    loading.value = false
  }
}

// 查看文章详情
const viewArticle = (article) => {
  const articleId = typeof article === 'object' ? article.id : article
  router.push(`/article/${articleId}`)
}

// 查看分类
const viewCategory = (categoryId) => {
  router.push(`/category/${categoryId}`)
}

// 查看标签
const viewTag = (tag) => {
  if (typeof tag === 'object') {
    router.push(`/tag/${encodeURIComponent(tag.name)}`)
  } else {
    const tagObj = tags.value.find(t => t.id === tag)
    if (tagObj) {
      router.push(`/tag/${encodeURIComponent(tagObj.name)}`)
    }
  }
}

// 写文章
const toWriteArticle = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    showLoginDialog.value = true
    activeTab.value = 'login'
    return
  }
  router.push('/article/create')
}

// 查看文章列表
const toArticlesList = () => {
  // 如果已经在第一页，刷新数据
  if (currentPage.value === 1) {
    loadData()
  } else {
    currentPage.value = 1
    handlePageChange(1)
  }
}

// 分页处理
const handlePageChange = async (page) => {
  currentPage.value = page
  try {
    await articleStore.fetchArticles({
      page: currentPage.value,
      size: pageSize.value
    })
    // 滚动到顶部
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } catch (error) {
    console.error('分页加载失败:', error)
    ElMessage.error('加载文章失败')
  }
}

// 每页数量改变
const handleSizeChange = async (size) => {
  pageSize.value = size
  currentPage.value = 1
  try {
    await articleStore.fetchArticles({
      page: currentPage.value,
      size: pageSize.value
    })
  } catch (error) {
    console.error('分页大小改变失败:', error)
    ElMessage.error('加载文章失败')
  }
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
    showLoginDialog.value = false
    resetForm()
    
    // 登录成功后刷新数据
    await loadData()
    
  } catch (error) {
    // 错误信息已经在 authStore 中处理了
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
    // 错误信息已经在 authStore 中处理了
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
.loading-container {
  padding: 20px;
  background: white;
  border-radius: 8px;
}

.empty-state {
  padding: 40px;
  background: white;
  border-radius: 8px;
  text-align: center;
}

.loading-mini {
  padding: 10px;
}

.hot-list .empty-item,
.category-list .empty-item,
.empty-tags {
  padding: 10px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.tag-cloud-item {
  margin: 4px;
  cursor: pointer;
  transition: transform 0.2s;
}

.tag-cloud-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}


.hero-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 80px 0;
  text-align: center;
  margin-bottom: 40px;
}

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
.hot-articles,
.category-card,
.tags-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.hot-articles h3,
.category-card h3,
.tags-card h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 18px;
  color: #333;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.hot-list,
.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.hot-list li,
.category-list li {
  margin-bottom: 10px;
}

.hot-list li:last-child,
.category-list li:last-child {
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