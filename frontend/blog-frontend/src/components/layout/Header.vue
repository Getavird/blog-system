<template>
  <header class="blog-header">
    <div class="container">
      <div class="header-content">
        <!-- 左侧：Logo -->
        <div class="logo">
          <router-link to="/" class="logo-link">
            <span class="logo-text">博客系统</span>
          </router-link>
        </div>
        
        <!-- 中间：导航菜单 -->
        <nav class="nav-menu">
          <router-link to="/" class="nav-item">首页</router-link>
          <router-link to="/categories" class="nav-item">分类</router-link>
          <router-link to="/tags" class="nav-item">标签</router-link>
          <router-link to="/archives" class="nav-item">归档</router-link>
          <router-link to="/about" class="nav-item">关于</router-link>
        </nav>
        
        <!-- 搜索区域 -->
        <div class="search-area">
          <!-- 搜索框（桌面端） -->
          <div class="desktop-search" v-show="!isMobile || showSearchInput">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索文章、标签、作者..."
              size="small"
              clearable
              @keyup.enter="handleSearch"
              @clear="clearSearch"
              class="search-input"
              ref="searchInputRef"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
              <template #append>
                <el-button @click="handleSearch" :loading="searching" class="search-btn">
                  搜索
                </el-button>
              </template>
            </el-input>
            
          </div>
          
          <!-- 搜索图标（移动端） -->
          <div v-if="isMobile && !showSearchInput" class="mobile-search-icon" @click="toggleSearchInput">
            <el-icon><Search /></el-icon>
          </div>
          
          <!-- 关闭搜索图标（移动端） -->
          <div v-if="isMobile && showSearchInput" class="close-search-icon" @click="toggleSearchInput">
            <el-icon><Close /></el-icon>
          </div>
        </div>
        
        <!-- 右侧：用户操作 -->
        <div class="user-actions">
          <template v-if="isLoggedIn">
            <el-button type="primary" size="small" @click="toWrite" class="write-btn">
              写文章
            </el-button>
            <el-dropdown>
              <span class="user-dropdown">
                {{ username }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="toProfile">
                    <el-icon><User /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item @click="toMyArticles">
                    <el-icon><Document /></el-icon>我的文章
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="logout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button link @click="showLoginDialog">登录</el-button>
            <el-button type="primary" size="small" @click="showLoginDialog">
              注册
            </el-button>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'  
import { ElMessage } from 'element-plus'
import {
  Search,
  ArrowDown,
  User,
  Document,
  SwitchButton,
  Close
} from '@element-plus/icons-vue'

const emit = defineEmits(['showLogin'])
const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

// 搜索相关
const searchKeyword = ref('')
const searching = ref(false)
const showSearchInput = ref(false)
const searchInputRef = ref(null)
const isMobile = ref(false)

// 从Pinia获取用户状态
const isLoggedIn = computed(() => {
  return userStore.isLoggedIn()
})
const username = computed(() => userStore.user?.username || '')

// 生命周期
onMounted(() => {
  // 初始化用户状态
  userStore.initFromStorage()

  // 监听全局登录事件
  window.addEventListener('showLogin', () => {
    showLoginDialog()
  })
  
  // 检查登录状态
  checkLoginStatus()
  
  // 检测屏幕尺寸
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})

// 检查登录状态
const checkLoginStatus = async () => {
  try {
    // 如果有用户信息但没有从API验证过，可以调用验证
    if (userStore.user && !userStore.verified) {
      const currentUser = await authStore.fetchCurrentUser()
      console.log('验证登录状态:', currentUser)
    }
  } catch (error) {
    console.log('登录验证失败，可能已过期:', error)
  }
}

// 方法
const checkScreenSize = () => {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) {
    showSearchInput.value = true // 桌面端始终显示搜索框
  }
}

// 搜索方法
const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  searching.value = true
  
  // 如果是移动端，收起搜索框
  if (isMobile.value) {
    showSearchInput.value = false
  }
  
  try {
    // 跳转到搜索页面
    router.push({
      path: '/search',
      query: { q: keyword }
    })
  } finally {
    searching.value = false
    searchKeyword.value = ''
  }
}

const clearSearch = () => {
  searchKeyword.value = ''
}

const toggleSearchInput = () => {
  showSearchInput.value = !showSearchInput.value
  if (showSearchInput.value && searchInputRef.value) {
    // 显示搜索框后自动聚焦
    setTimeout(() => {
      searchInputRef.value?.focus()
    }, 100)
  }
}

// 用户操作
const showLoginDialog = () => {
  emit('showLogin')
}

const toWrite = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    showLoginDialog()
    return
  }
  router.push('/article/create')
}

const toProfile = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    showLoginDialog()
    return
  }
  router.push('/user/profile')
}

const toMyArticles = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    showLoginDialog()
    return
  }
  router.push('/user/articles')
}

const logout = async () => {
  try {
    await authStore.logout()
    ElMessage.success('已退出登录')
    // 跳转到首页
    router.push('/')
  } catch (error) {
    console.error('退出登录失败:', error)
    ElMessage.error('退出登录失败')
  }
}
</script>

<style scoped>
.blog-header {
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.blog-header .container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.blog-header .header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  gap: 20px;
}

/* Logo 样式 */
.blog-header .logo .logo-link {
  text-decoration: none;
  color: #409eff;
  font-weight: bold;
  font-size: 20px;
  white-space: nowrap;
}

/* 导航菜单 */
.blog-header .nav-menu {
  display: flex;
  gap: 30px;
  flex: 1;
  justify-content: center;
}

.blog-header .nav-menu .nav-item {
  text-decoration: none;
  color: #666;
  padding: 5px 0;
  font-size: 15px;
  white-space: nowrap;
  position: relative;
}

.blog-header .nav-menu .nav-item:hover {
  color: #409eff;
}

.blog-header .nav-menu .nav-item.router-link-active {
  color: #409eff;
  font-weight: 500;
}

.blog-header .nav-menu .nav-item.router-link-active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: #409eff;
  border-radius: 1px;
}

/* 搜索区域 */
.search-area {
  position: relative;
  flex: 1;
  max-width: 400px;
  min-width: 150px;
}

.desktop-search {
  position: relative;
  width: 100%;
}

.search-input {
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.3s;
}

.search-input:focus-within {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.search-input :deep(.el-input-group__append) {
  background-color: #409eff;
  border-color: #409eff;
  border-radius: 0 20px 20px 0;
}

.search-input :deep(.el-input-group__append .el-button) {
  color: white;
  font-weight: 500;
  padding: 0 15px;
}

.search-btn {
  border: none;
}

/* 搜索建议 */
.search-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  margin-top: 5px;
  max-height: 300px;
  overflow-y: auto;
  z-index: 1001;
}

.suggestion-item {
  padding: 12px 16px;
  cursor: pointer;
  color: #333;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.suggestion-item:hover {
  background: #f5f7fa;
  color: #409eff;
}

.suggestion-item:last-child {
  border-bottom: none;
}

/* 移动端搜索图标 */
.mobile-search-icon,
.close-search-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  cursor: pointer;
  color: #666;
  border-radius: 50%;
  transition: all 0.3s;
}

.mobile-search-icon:hover,
.close-search-icon:hover {
  background: #f5f7fa;
  color: #409eff;
}

/* 用户操作区 */
.user-actions {
  display: flex;
  align-items: center;
  gap: 15px;
  white-space: nowrap;
}

.write-btn {
  border-radius: 20px;
  padding: 8px 16px;
}

.user-dropdown {
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  color: #333;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s;
}

.user-dropdown:hover {
  background: #f5f7fa;
}

/* 响应式设计 */
@media (max-width: 1100px) {
  .blog-header .nav-menu {
    gap: 20px;
  }
  
  .search-area {
    max-width: 300px;
  }
}

@media (max-width: 900px) {
  .blog-header .nav-menu {
    gap: 15px;
  }
  
  .search-area {
    max-width: 250px;
  }
  
  .blog-header .nav-menu .nav-item {
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .blog-header .header-content {
    height: auto;
    padding: 10px 0;
    flex-wrap: wrap;
  }
  
  .blog-header .logo {
    order: 1;
  }
  
  .blog-header .nav-menu {
    order: 3;
    width: 100%;
    justify-content: center;
    margin-top: 10px;
    gap: 12px;
    overflow-x: auto;
    padding: 5px 0;
  }
  
  .search-area {
    order: 2;
    max-width: none;
    flex: none;
    display: flex;
    align-items: center;
    gap: 10px;
  }
  
  .desktop-search {
    position: fixed;
    top: 10px;
    left: 50%;
    transform: translateX(-50%);
    width: calc(100% - 40px);
    z-index: 1002;
    background: white;
    padding: 10px;
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  }
  
  .user-actions {
    order: 4;
    width: 100%;
    justify-content: center;
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px solid #eee;
  }
  
  .mobile-search-icon,
  .close-search-icon {
    display: flex;
  }
}

@media (max-width: 480px) {
  .blog-header .nav-menu {
    justify-content: flex-start;
    padding-left: 10px;
    padding-right: 10px;
  }
  
  .blog-header .nav-menu .nav-item {
    font-size: 13px;
    padding: 5px 8px;
  }
  
  .logo-text {
    font-size: 18px;
  }
  
  .write-btn {
    padding: 6px 12px;
    font-size: 12px;
  }
}
</style>