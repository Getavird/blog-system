<template>
  <header class="blog-header">
    <div class="container">
      <div class="header-content">
        <!-- 左侧：Logo -->
        <div class="logo">
          <router-link to="/" class="logo-link">
            <!-- Logo 图片，失败时显示文字 -->
            <img 
              src="http://localhost:8080/uploads/logo/logo.jpg" 
              alt="博客系统"
              class="logo-img"
              @error="handleLogoError"
              v-if="!logoError"
            >
            <!-- 后备文字，默认隐藏，图片加载失败时显示 -->
            <span class="logo-text" :style="{ display: logoError ? 'inline' : 'none' }">博客系统</span>
          </router-link>
        </div>
        
        <!-- 中间：导航菜单 -->
        <nav class="nav-menu">
          <router-link to="/" class="nav-item">首页</router-link>
          <router-link to="/categories" class="nav-item">分类</router-link>
          <router-link to="/tags" class="nav-item">标签</router-link>
          <router-link to="/archives" class="nav-item">归档</router-link>
          <router-link to="/about" class="nav-item">展示404页面</router-link>
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
            
            <!-- 用户头像和下拉菜单 -->
            <el-dropdown>
              <div class="user-info-dropdown">
                <!-- 点击头像跳转到用户公开主页 -->
                <div class="user-avatar" @click="goToUserPublicPage">
                  <img 
                    v-if="avatarUrl" 
                    :src="avatarUrl" 
                    alt="用户头像"
                    @error="handleAvatarError"
                  />
                  <div v-else class="avatar-placeholder">
                    {{ userInitial }}
                  </div>
                </div>
                
                <span class="user-name">{{ currentUser?.username || '' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              
              <template #dropdown>
                <el-dropdown-menu>
                  <!-- 点击跳转到用户公开主页 -->
                  <el-dropdown-item @click="goToUserPublicPage">
                    <el-icon><User /></el-icon>
                    我的主页
                  </el-dropdown-item>
                  
                  <!-- 点击跳转到个人中心（编辑个人资料） -->
                  <el-dropdown-item @click="toProfile">
                    <el-icon><Setting /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  
                  <el-dropdown-item @click="toMyArticles">
                    <el-icon><Document /></el-icon>
                    我的文章
                  </el-dropdown-item>
                  
                  <el-dropdown-item divided @click="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          
          <template v-else>
            <el-button type="primary" size="small" @click="showLoginDialog">
              登录
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
import { useUserStore } from '@/stores/user'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUIStore } from '@/stores/ui'

import {
  Search,
  ArrowDown,
  User,
  Document,
  SwitchButton,
  Close,
  Setting
} from '@element-plus/icons-vue'

//const emit = defineEmits(['showLogin'])
const router = useRouter()
const userStore = useUserStore()
const authStore = useAuthStore()
const uiStore = useUIStore()

// 搜索相关
const searchKeyword = ref('')
const searching = ref(false)
const showSearchInput = ref(false)
const searchInputRef = ref(null)
const isMobile = ref(false)

// Logo 相关
const logoError = ref(false)

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn())
const currentUser = computed(() => userStore.user)

// 计算头像URL
const avatarUrl = computed(() => {
  if (!currentUser.value || !currentUser.value.avatar) {
    return 'http://localhost:8080/static/images/default-avatars/default_avatar.png'
  }
  
  let url = currentUser.value.avatar
  // 添加时间戳避免缓存
  const separator = url.includes('?') ? '&' : '?'
  return url + separator + 't=' + Date.now()
})

// 用户名字首字母
const userInitial = computed(() => {
  return currentUser.value?.username?.charAt(0)?.toUpperCase() || 'U'
})

// 生命周期
onMounted(() => {
  userStore.initFromStorage()
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})

// 方法
const checkScreenSize = () => {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) {
    showSearchInput.value = true
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

// Logo 加载失败处理
const handleLogoError = (event) => {
  console.log('Logo 图片加载失败，显示文字')
  logoError.value = true
  // 防止循环错误
  if (event.target) {
    event.target.onerror = null
  }
}

// 用户操作
const showLoginDialog = () => {
  uiStore.openLoginDialog('login')
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

// 跳转到用户公开主页
const goToUserPublicPage = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    showLoginDialog()
    return
  }
  
  const username = currentUser.value?.username
  if (username) {
    router.push(`/user/${encodeURIComponent(username)}`)
  } else {
    ElMessage.error('无法获取用户信息')
  }
}

const logout = async () => {
  try {
    // 使用 confirm 确认框
    const confirmResult = await window.confirm('确定要退出登录吗？')
    if (!confirmResult) return
    
    await authStore.logout()
    userStore.clearUser()
    ElMessage.success('已退出登录')
    router.push('/')
    
  } catch (error) {
    console.error('退出登录失败:', error)
    ElMessage.error('退出登录失败')
  }
}

// 头像加载失败处理
const handleAvatarError = (event) => {
  console.log('头像加载失败，使用默认头像')
  event.target.src = 'http://localhost:8080/static/images/default-avatars/default_avatar.png'
  event.target.onerror = null // 防止循环错误
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
  display: flex;
  align-items: center;
  height: 100%;
  min-height: 40px;
}

/* Logo 图片样式 */
.logo-img {
  height: 60px;
  vertical-align: middle;
  transition: opacity 0.3s;
  object-fit: contain;
}

.logo-img:hover {
  opacity: 0.8;
}

/* Logo 文字样式 */
.logo-text {
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

/* 用户头像和下拉菜单 */
.user-info-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.3s;
}

.user-info-dropdown:hover {
  background: #f5f7fa;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid #e6f7ff;
  transition: all 0.3s;
}

.user-avatar:hover {
  border-color: #409eff;
  transform: scale(1.05);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.user-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
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
  
  /* 移动端 Logo 调整 */
  .logo-img {
    height: 32px;
  }
  
  /* 移动端隐藏用户名，只显示头像 */
  .user-name {
    display: none;
  }
  
  .user-info-dropdown {
    padding: 4px 8px;
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
  
  .logo-img {
    height: 28px;
  }
  
  .logo-text {
    font-size: 18px;
  }
  
  .write-btn {
    padding: 6px 12px;
    font-size: 12px;
  }
  
  .user-avatar {
    width: 28px;
    height: 28px;
  }
}
</style>