<template>
  <div class="admin-page">
    <h1>后台管理页面</h1>
    <p>后台管理页面正在开发中...</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped lang="scss">
.admin-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  
  h1 {
    font-size: 36px;
    margin-bottom: 20px;
    color: #333;
  }
  
  p {
    font-size: 18px;
    color: #666;
    margin-bottom: 30px;
  }
}
</style>