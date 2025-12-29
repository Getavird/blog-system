<template>
  <div class="not-found-page">
    <!-- 添加Header组件 -->
    <Header />
    
    <div class="error-container">
      <div class="error-content">
        <!-- 404图标 -->
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
            <el-button type="primary" @click="goHome" size="large">
              <el-icon><HomeFilled /></el-icon>
              返回首页
            </el-button>
            
            <el-button @click="goToSearch" size="large">
              <el-icon><Search /></el-icon>
              搜索内容
            </el-button>
          </div>
          
          <!-- 快速导航 -->
          <div class="quick-links">
            <h3>快速导航</h3>
            <div class="link-items">
              <el-button type="text" @click="goToArticles">
                <el-icon><Document /></el-icon>
                浏览文章
              </el-button>
              <el-button type="text" @click="goToCategories">
                <el-icon><Folder /></el-icon>
                查看分类
              </el-button>
              <el-button type="text" @click="goToTags">
                <el-icon><CollectionTag /></el-icon>
                标签云
              </el-button>
              <el-button type="text" @click="goToArchives">
                <el-icon><Calendar /></el-icon>
                文章归档
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 添加Footer组件 -->
    <Footer />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { 
  HomeFilled, 
  Search, 
  Document, 
  Folder, 
  CollectionTag, 
  Calendar 
} from '@element-plus/icons-vue'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
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
  background: #f8f9fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
}

.error-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.error-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  max-width: 800px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  animation: fadeIn 0.8s ease-out;
}

.error-icon {
  margin-bottom: 30px;
}

.error-text h1 {
  font-size: 100px;
  font-weight: 800;
  margin: 0;
  color: #333;
  line-height: 1;
}

.error-text h2 {
  font-size: 28px;
  color: #333;
  margin: 10px 0 15px;
  font-weight: 600;
}

.error-description {
  font-size: 16px;
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
  min-width: 140px;
  padding: 12px 30px;
  border-radius: 8px;
  font-size: 16px;
}

.quick-links {
  width: 100%;
  max-width: 600px;
  margin-top: 30px;
  padding-top: 30px;
  border-top: 1px solid #eee;
}

.quick-links h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 20px;
  font-weight: 500;
}

.link-items {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 15px;
}

.link-items .el-button {
  padding: 10px 20px;
  color: #409eff;
}

.link-items .el-button:hover {
  background-color: #f0f7ff;
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
    font-size: 14px;
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
  
  .link-items .el-button {
    width: 100%;
    max-width: 280px;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .error-text h1 {
    font-size: 60px;
  }
  
  .error-text h2 {
    font-size: 20px;
  }
}
</style>