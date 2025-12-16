<template>
  <footer class="blog-footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-info">
          <h3>博客系统</h3>
          <p>基于 Vue 3 + Spring Boot 开发</p>
        </div>
        <div class="copyright">
          <p>© 2024 博客系统 - 实训项目</p>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
// 定义组件名称
defineOptions({
  name: 'BlogFooter'
})
</script>

<style scoped>
.blog-footer {
  background: #2c3e50;
  color: white;
  padding: 30px 0;
  margin-top: auto;
}

.blog-footer .container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.blog-footer .footer-content {
  text-align: center;
}

.blog-footer .footer-info {
  margin-bottom: 20px;
}

.blog-footer .footer-info h3 {
  font-size: 24px;
  margin-bottom: 10px;
}

.blog-footer .footer-info p {
  color: #bdc3c7;
}

.blog-footer .copyright {
  color: #7f8c8d;
  font-size: 14px;
}
</style>