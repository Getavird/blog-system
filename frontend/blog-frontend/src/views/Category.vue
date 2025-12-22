<template>
  <div class="category-page">
    <Header />
    
    <div class="category-container">
      <div class="container">
        <!-- 分类头部 -->
        <div class="category-header">
          <div class="breadcrumb">
            <router-link to="/">首页</router-link>
            <el-icon><ArrowRight /></el-icon>
            <span class="current">{{ categoryName }} 分类</span>
          </div>
          
          <div class="category-info">
            <div class="category-title">
              <h1>
                <el-icon><Folder /></el-icon>
                {{ categoryName }}
              </h1>
              <p class="category-description">{{ categoryDescription }}</p>
            </div>
            
            <div class="category-stats">
              <div class="stat-item">
                <div class="stat-number">{{ total }}</div>
                <div class="stat-label">篇文章</div>
              </div>
              <div class="stat-item">
                <div class="stat-number">{{ formatNumber(viewCount) }}</div>
                <div class="stat-label">次阅读</div>
              </div>
              <div class="stat-item">
                <div class="stat-number">{{ formatNumber(likeCount) }}</div>
                <div class="stat-label">次点赞</div>
              </div>
            </div>
          </div>
          
          <!-- 排序选项 -->
          <div class="sort-options">
            <div 
              class="sort-item"
              :class="{ active: sortBy === 'createTime' }"
              @click="changeSort('createTime')"
            >
              <el-icon><Clock /></el-icon>
              最新
            </div>
            <div 
              class="sort-item"
              :class="{ active: sortBy === 'viewCount' }"
              @click="changeSort('viewCount')"
            >
              <el-icon><View /></el-icon>
              热门
            </div>
            <div 
              class="sort-item"
              :class="{ active: sortBy === 'likeCount' }"
              @click="changeSort('likeCount')"
            >
              <el-icon><Star /></el-icon>
              点赞
            </div>
          </div>
        </div>

        <!-- 文章列表 -->
        <div class="category-content">
          <ArticleList
            :articles="articles"
            :loading="loading"
            :show-cover="false"
            :show-summary="true"
            :show-author="true"
            :show-time="true"
            :show-views="true"
            :show-likes="true"
            :show-pagination="true"
            :total="total"
            :current-page="currentPage"
            :page-size="pageSize"
            @article-click="viewArticle"
            @page-change="handlePageChange"
            @size-change="handleSizeChange"
            :empty-title="'暂无文章'"
            :empty-message="`${categoryName} 分类下还没有文章，快去写一篇吧！`"
            :show-create-button="true"
            @create-click="toWriteArticle"
          />
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowRight,
  Folder,
  Clock,
  View,
  Star
} from '@element-plus/icons-vue'
import Header from '../components/layout/Header.vue'
import Footer from '../components/layout/Footer.vue'
import ArticleList from '../components/article/ArticleList.vue'

const route = useRoute()
const router = useRouter()

// 分类信息
const categoryId = ref(route.params.id)
const categoryName = ref('')
const categoryDescription = ref('')
const total = ref(0)
const viewCount = ref(0)
const likeCount = ref(0)

// 文章列表相关
const loading = ref(true)
const articles = ref([])
const sortBy = ref('createTime')
const currentPage = ref(1)
const pageSize = ref(10)

// 模拟数据
const categoriesData = {
  1: { name: '技术分享', description: '编程、框架、工具等技术相关文章', viewCount: 12500, likeCount: 3200 },
  2: { name: '生活随笔', description: '记录生活点滴、感悟与思考', viewCount: 8500, likeCount: 2100 },
  3: { name: '学习笔记', description: '学习过程中的心得体会和总结', viewCount: 9200, likeCount: 1800 },
  4: { name: '项目实战', description: '实际项目开发经验分享', viewCount: 15600, likeCount: 4200 }
}

// 模拟文章数据
const mockArticles = (categoryId, sortBy) => {
  const categoryNames = ['技术分享', '生活随笔', '学习笔记', '项目实战']
  const baseArticles = [
    {
      id: 1,
      title: 'Vue 3 新特性详解与实战指南',
      summary: '深入解析 Vue 3 的新特性和使用技巧，带你快速上手 Vue 3 开发...',
      authorName: '张三',
      viewCount: 320,
      likeCount: 42,
      commentCount: 12,
      categoryName: '技术分享',
      createTime: '2024-01-14T14:20:00',
      updateTime: '2024-01-15T09:30:00'
    },
    {
      id: 2,
      title: 'Spring Boot入门教程',
      summary: '详细介绍Spring Boot的基本使用和配置，快速上手后端开发...',
      authorName: '李四',
      viewCount: 156,
      likeCount: 25,
      commentCount: 8,
      categoryName: '技术分享',
      createTime: '2024-01-15T10:30:00',
      updateTime: '2024-01-15T10:30:00'
    },
    {
      id: 3,
      title: '我的学习笔记：如何高效学习编程',
      summary: '分享我多年来学习编程的经验和方法，帮助初学者快速入门...',
      authorName: '王五',
      viewCount: 89,
      likeCount: 15,
      commentCount: 5,
      categoryName: '学习笔记',
      createTime: '2024-01-13T09:15:00',
      updateTime: '2024-01-16T14:45:00'
    },
    {
      id: 4,
      title: '生活随笔：记录美好的一天',
      summary: '今天天气很好，去公园散步，看到很多有趣的事情...',
      authorName: '赵六',
      viewCount: 45,
      likeCount: 8,
      commentCount: 3,
      categoryName: '生活随笔',
      createTime: '2024-01-16T16:20:00',
      updateTime: '2024-01-16T16:20:00'
    },
    {
      id: 5,
      title: 'React Hooks 使用指南',
      summary: '详细介绍React Hooks的各种用法和最佳实践...',
      authorName: '张三',
      viewCount: 210,
      likeCount: 36,
      commentCount: 10,
      categoryName: '技术分享',
      createTime: '2024-01-12T11:45:00',
      updateTime: '2024-01-14T16:30:00'
    }
  ]
  
  // 按分类筛选
  let filteredArticles = baseArticles.filter(article => {
    const categoryName = categoriesData[categoryId]?.name
    return article.categoryName === categoryName
  })
  
  // 排序
  filteredArticles.sort((a, b) => {
    if (sortBy === 'createTime') {
      return new Date(b.createTime) - new Date(a.createTime)
    } else if (sortBy === 'viewCount') {
      return b.viewCount - a.viewCount
    } else if (sortBy === 'likeCount') {
      return b.likeCount - a.likeCount
    }
    return 0
  })
  
  return filteredArticles
}

// 生命周期
onMounted(() => {
  loadCategoryData()
})

// 监听路由变化
watch(() => route.params.id, (newId) => {
  categoryId.value = newId
  currentPage.value = 1
  loadCategoryData()
})

// 方法
const loadCategoryData = () => {
  loading.value = true
  
  // 模拟加载延迟
  setTimeout(() => {
    const categoryData = categoriesData[categoryId.value]
    if (categoryData) {
      categoryName.value = categoryData.name
      categoryDescription.value = categoryData.description
      viewCount.value = categoryData.viewCount || 0
      likeCount.value = categoryData.likeCount || 0
    } else {
      categoryName.value = '未找到分类'
      categoryDescription.value = '该分类不存在或已被删除'
    }
    
    // 加载文章数据
    articles.value = mockArticles(categoryId.value, sortBy.value)
    total.value = articles.value.length
    
    loading.value = false
  }, 600)
}

const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + '千'
  }
  return num
}

const changeSort = (type) => {
  sortBy.value = type
  articles.value = mockArticles(categoryId.value, sortBy.value)
  
  let sortText = ''
  switch(type) {
    case 'createTime': sortText = '按最新排序'; break
    case 'viewCount': sortText = '按热门排序'; break
    case 'likeCount': sortText = '按点赞排序'; break
  }
  ElMessage.success(sortText)
}

const viewArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

const toWriteArticle = () => {
  // 检查是否登录
  const token = localStorage.getItem('blog_token')
  if (!token) {
    ElMessage.warning('请先登录')
    // 可以触发Header的登录事件
    const event = new CustomEvent('showLogin')
    window.dispatchEvent(event)
    return
  }
  router.push('/article/create')
}

const handlePageChange = (page) => {
  currentPage.value = page
  // 这里实际应该调用API获取对应页的数据
  // 由于是模拟数据，我们只做滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}
</script>

<style scoped>
.category-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.category-container {
  flex: 1;
  padding: 20px 0 40px;
  background: #f8f9fa;
}

.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 面包屑导航 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
  margin-bottom: 30px;
}

.breadcrumb a {
  color: #666;
  text-decoration: none;
}

.breadcrumb a:hover {
  color: #409eff;
}

.breadcrumb .current {
  color: #333;
  font-weight: 500;
}

/* 分类头部 */
.category-info {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.category-title {
  margin-bottom: 25px;
}

.category-title h1 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 28px;
  color: #333;
  margin-bottom: 12px;
}

.category-title h1 .el-icon {
  color: #409eff;
  font-size: 30px;
}

.category-description {
  color: #666;
  font-size: 16px;
  line-height: 1.6;
}

/* 分类统计 */
.category-stats {
  display: flex;
  gap: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  min-width: 120px;
  transition: all 0.3s;
}

.stat-item:hover {
  background: #e9ecef;
  transform: translateY(-2px);
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

/* 排序选项 */
.sort-options {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.sort-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #f8f9fa;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
  user-select: none;
}

.sort-item:hover {
  background: #e9ecef;
  color: #333;
}

.sort-item.active {
  background: #409eff;
  color: white;
}

.sort-item.active .el-icon {
  color: white;
}

/* 分类内容 */
.category-content {
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .category-info {
    padding: 20px;
  }
  
  .category-title h1 {
    font-size: 24px;
  }
  
  .category-stats {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .stat-item {
    min-width: 100px;
    flex: 1;
  }
  
  .sort-options {
    overflow-x: auto;
    padding: 10px;
  }
  
  .sort-item {
    white-space: nowrap;
  }
}
</style>