<template>
  <div class="article-detail-page">
    <Header />
    
    <div class="container">
      <div class="article-content">
        <h1>文章详情页</h1>
        <p>文章详情页正在开发中...</p>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </div>
    </div>
    
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { defineAsyncComponent } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const Header = defineAsyncComponent(() => import('../components/layout/Header.vue'))
const Footer = defineAsyncComponent(() => import('../components/layout/Footer.vue'))
</script>

<style scoped>
.article-detail-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.article-detail-page .container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  flex: 1;
}

.article-detail-page .container .article-content {
  text-align: center;
  padding: 100px 0;
}

.article-detail-page .container .article-content h1 {
  font-size: 36px;
  margin-bottom: 20px;
  color: #333;
}

.article-detail-page .container .article-content p {
  font-size: 18px;
  color: #666;
  margin-bottom: 30px;
}
</style>