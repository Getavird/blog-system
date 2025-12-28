<template>
  <div class="not-found-page">
    <!-- 添加Header组件 -->
    <Header />
    
    <div class="error-container">
      <div class="error-content">
        <!-- 404动画/图标 -->
        <div class="error-icon">
          <svg width="200" height="200" viewBox="0 0 24 24" fill="none">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" 
                  fill="#409EFF"/>
          </svg>
        </div>
        
        <!-- 错误信息 -->
        <div class="error-text">
          <h1>404</h1>
          <h2>页面不存在</h2>
          <p class="error-description">
            抱歉，您访问的页面可能已被删除、重命名或暂时不可用
          </p>
          
          <!-- 返回按钮组 -->
          <div class="action-buttons">
            <el-button type="primary" @click="goHome" :icon="HomeFilled" size="large">
              返回首页
            </el-button>
            <el-button @click="goBack" :icon="Back" size="large">
              返回上一页
            </el-button>
            <el-button @click="goToSearch" :icon="Search" size="large">
              搜索内容
            </el-button>
          </div>
          
          <!-- 快速导航 -->
          <div class="quick-links">
            <h3>快速导航</h3>
            <div class="link-items">
              <a @click="goToArticles">📝 浏览文章</a>
              <a @click="goToCategories">📂 查看分类</a>
              <a @click="goToTags">🏷️ 标签云</a>
              <a @click="goToArchives">📅 文章归档</a>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 装饰元素 -->
      <div class="decoration">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
      </div>
    </div>
    
    <!-- 添加Footer组件 -->
    <Footer />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { HomeFilled, Back, Search } from '@element-plus/icons-vue'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/')
  }
}

const goToSearch = () => {
  router.push('/search')
}

const goToArticles = () => {
  router.push('/')
}

const goToCategories = () => {
  router.push('/categories')
}

const goToTags = () => {
  router.push('/tags')
}

const goToArchives = () => {
  router.push('/archives')
}
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
}

.error-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
}

.error-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  max-width: 800px;
  padding: 40px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 2;
  animation: fadeIn 0.8s ease-out;
}

.error-icon {
  margin-bottom: 30px;
  animation: float 3s ease-in-out infinite;
}

.error-text h1 {
  font-size: 120px;
  font-weight: 800;
  margin: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  line-height: 1;
}

.error-text h2 {
  font-size: 32px;
  color: #333;
  margin: 10px 0 15px;
  font-weight: 600;
}

.error-description {
  font-size: 18px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 40px;
  max-width: 600px;
}

.action-buttons {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
  flex-wrap: wrap;
  justify-content: center;
}

.action-buttons .el-button {
  min-width: 160px;
  padding: 12px 30px;
  border-radius: 50px;
  font-size: 16px;
  transition: all 0.3s ease;
}

.action-buttons .el-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 20px rgba(64, 158, 255, 0.3);
}

.action-buttons .el-button:active {
  transform: translateY(0);
}

.quick-links {
  width: 100%;
  max-width: 600px;
  margin-top: 30px;
  padding-top: 30px;
  border-top: 1px solid #eee;
}

.quick-links h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  font-weight: 600;
}

.link-items {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;
}

.link-items a {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #f8f9fa;
  border-radius: 50px;
  color: #409eff;
  text-decoration: none;
  font-size: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid transparent;
}

.link-items a:hover {
  background: #409eff;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

/* 装饰元素 */
.decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: 5%;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: 15%;
  right: 10%;
  animation-delay: 2s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: 80%;
  animation-delay: 4s;
}

/* 动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .error-content {
    padding: 30px 20px;
    margin: 20px;
  }
  
  .error-text h1 {
    font-size: 80px;
  }
  
  .error-text h2 {
    font-size: 24px;
  }
  
  .error-description {
    font-size: 16px;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .action-buttons .el-button {
    width: 100%;
    max-width: 280px;
  }
  
  .link-items {
    flex-direction: column;
    align-items: center;
  }
  
  .link-items a {
    width: 100%;
    max-width: 280px;
    justify-content: center;
  }
  
  .circle-1,
  .circle-2,
  .circle-3 {
    display: none;
  }
}

@media (max-width: 480px) {
  .error-text h1 {
    font-size: 60px;
  }
  
  .error-text h2 {
    font-size: 20px;
  }
  
  .action-buttons .el-button {
    padding: 10px 20px;
    font-size: 14px;
  }
}
</style>