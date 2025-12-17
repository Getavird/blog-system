<template>
  <div class="not-found-page">
    <div class="error-container">
      <h1>404</h1>
      <p>页面未找到</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.not-found-page .error-container {
  text-align: center;
}

.not-found-page .error-container h1 {
  font-size: 100px;
  margin-bottom: 20px;
  color: #333;
}

.not-found-page .error-container p {
  font-size: 20px;
  margin-bottom: 30px;
  color: #666;
}
</style>