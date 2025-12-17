<template>
  <header class="blog-header">
    <div class="container">
      <div class="header-content">
        <div class="logo">
          <router-link to="/" class="logo-link">
            <span class="logo-text">博客系统</span>
          </router-link>
        </div>
        
        <nav class="nav-menu">
          <router-link to="/" class="nav-item">首页</router-link>
          <router-link to="/categories" class="nav-item">分类</router-link>
          <router-link to="/archives" class="nav-item">归档</router-link>
          <router-link to="/about" class="nav-item">关于</router-link>
        </nav>
        
        <div class="user-actions">
          <template v-if="isLoggedIn">
            <el-button type="primary" size="small" @click="toWrite">
              写文章
            </el-button>
            <el-dropdown>
              <span class="user-dropdown">
                {{ username }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="toProfile">个人中心</el-dropdown-item>
                  <el-dropdown-item @click="toMyArticles">我的文章</el-dropdown-item>
                  <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button link @click="$emit('showLogin')">登录</el-button>
            <el-button type="primary" size="small" @click="$emit('showLogin')">
              注册
            </el-button>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'

defineOptions({
  name: 'BlogHeader'
})

const router = useRouter()

// 定义组件发出的事件
const emit = defineEmits(['showLogin'])

// 用户状态
const isLoggedIn = ref(false)
const username = ref('')

onMounted(() => {
  // 检查是否登录
  const token = localStorage.getItem('blog_token')
  const userStr = localStorage.getItem('blog_user')
  
  if (token && userStr) {
    isLoggedIn.value = true
    const user = JSON.parse(userStr)
    username.value = user.username || '用户'
  }
})

// 导航方法
const toWrite = () => {
  router.push('/article/create')
}

const toProfile = () => {
  router.push('/profile')
}

const toMyArticles = () => {
  router.push('/user/articles')
}

const logout = () => {
  localStorage.removeItem('blog_token')
  localStorage.removeItem('blog_user')
  isLoggedIn.value = false
  username.value = ''
  ElMessage.success('已退出登录')
  // 刷新页面以更新状态
  window.location.reload()
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.blog-header .header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}

.blog-header .logo .logo-link {
  text-decoration: none;
  color: #409eff;
  font-weight: bold;
  font-size: 20px;
}

.blog-header .nav-menu {
  display: flex;
  gap: 30px;
}

.blog-header .nav-menu .nav-item {
  text-decoration: none;
  color: #666;
  padding: 5px 0;
}

.blog-header .nav-menu .nav-item:hover {
  color: #409eff;
}

.blog-header .nav-menu .nav-item.router-link-active {
  color: #409eff;
  font-weight: bold;
}

.blog-header .user-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.blog-header .user-actions .user-dropdown {
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
}

.blog-header .user-actions .user-dropdown:hover {
  background: #f5f7fa;
}

@media (max-width: 768px) {
  .blog-header .header-content {
    flex-wrap: wrap;
    height: auto;
    padding: 10px 0;
  }
  
  .blog-header .nav-menu {
    order: 3;
    width: 100%;
    justify-content: center;
    margin-top: 10px;
    gap: 15px;
  }
}
</style>