<template>
  <div class="tag-page">
    <Header />
    
    <div class="tag-container">
      <div class="container">
        <!-- 标签头部 -->
        <div class="tag-header">
          <div class="breadcrumb">
            <router-link to="/">首页</router-link>
            <el-icon><ArrowRight /></el-icon>
            <router-link to="/tags">标签</router-link>
            <el-icon><ArrowRight /></el-icon>
            <span class="current">#{{ tagName }}</span>
          </div>
          
          <div class="tag-info">
            <div class="tag-title">
              <h1>
                <el-icon><PriceTag /></el-icon>
                #{{ tagName }}
              </h1>
              <p class="tag-description">{{ tagDescription }}</p>
            </div>
            
            <div class="tag-stats">
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
        </div>

        <!-- 相关标签 -->
        <div v-if="relatedTags.length > 0" class="related-tags">
          <h3>相关标签</h3>
          <div class="related-tags-list">
            <el-tag
              v-for="tag in relatedTags"
              :key="tag.id"
              :type="getTagType(tag.count)"
              size="medium"
              class="related-tag-item"
              @click="viewRelatedTag(tag.name)"
            >
              {{ tag.name }} ({{ tag.count }})
            </el-tag>
          </div>
        </div>

        <!-- 文章列表 -->
        <div class="tag-content">
          <ArticleList
            :articles="articles"
            :loading="loading"
            :show-cover="false"
            :show-summary="true"
            :show-author="true"
            :show-time="true"
            :show-views="true"
            :show-likes="true"
            :show-tags="false"
            :show-pagination="true"
            :total="total"
            :current-page="currentPage"
            :page-size="pageSize"
            @article-click="viewArticle"
            @page-change="handlePageChange"
            @size-change="handleSizeChange"
            :empty-title="'暂无文章'"
            :empty-message="`标签 #${tagName} 下还没有文章`"
            :show-create-button="false"
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
  PriceTag
} from '@element-plus/icons-vue'
import Header from '../components/layout/Header.vue'
import Footer from '../components/layout/Footer.vue'
import ArticleList from '../components/article/ArticleList.vue'

const route = useRoute()
const router = useRouter()

// 标签信息
const tagName = ref(route.params.name)
const tagDescription = ref('')
const total = ref(0)
const viewCount = ref(0)
const likeCount = ref(0)

// 相关标签
const relatedTags = ref([])

// 文章列表相关
const loading = ref(true)
const articles = ref([])
const currentPage = ref(1)
const pageSize = ref(10)

// 标签描述映射
const tagDescriptions = {
  'Vue': 'Vue.js 是一套用于构建用户界面的渐进式框架',
  'React': 'React 是一个用于构建用户界面的 JavaScript 库',
  'JavaScript': 'JavaScript 是一种高级的、解释型的编程语言',
  'TypeScript': 'TypeScript 是 JavaScript 的一个超集',
  'CSS': '层叠样式表，用于描述网页的样式',
  'HTML': '超文本标记语言，用于创建网页',
  'Node.js': 'Node.js 是一个基于 Chrome V8 引擎的 JavaScript 运行环境',
  'Python': 'Python 是一种广泛使用的高级编程语言',
  'Java': 'Java 是一种面向对象的编程语言',
  'Spring Boot': 'Spring Boot 简化了基于 Spring 的应用开发',
  '数据库': '数据库是数据的集合，用于存储和管理数据',
  'MySQL': 'MySQL 是最流行的关系型数据库管理系统之一',
  'Redis': 'Redis 是一个开源的内存数据结构存储系统',
  'MongoDB': 'MongoDB 是一个基于分布式文件存储的数据库',
  'Docker': 'Docker 是一个开源的应用容器引擎',
  'Kubernetes': 'Kubernetes 是一个开源的容器编排平台',
  '算法': '算法是解决问题的一系列明确指令',
  '数据结构': '数据结构是计算机中存储、组织数据的方式',
  '设计模式': '设计模式是软件设计中常见问题的解决方案',
  '前端工程化': '前端工程化是前端开发的标准化和自动化',
  '微服务': '微服务是一种软件架构风格',
  '架构设计': '架构设计是软件系统的高级结构',
  '性能优化': '性能优化是提高软件系统性能的过程',
  '网络安全': '网络安全是保护网络系统和数据免受攻击的过程'
}

// 标签数据（用于获取相关标签）
const allTags = [
  { id: 1, name: 'Vue', count: 156 },
  { id: 2, name: 'React', count: 128 },
  { id: 3, name: 'JavaScript', count: 210 },
  { id: 4, name: 'TypeScript', count: 98 },
  { id: 5, name: 'CSS', count: 89 },
  { id: 6, name: 'HTML', count: 76 },
  { id: 7, name: 'Node.js', count: 142 },
  { id: 8, name: 'Python', count: 187 },
  { id: 9, name: 'Java', count: 165 },
  { id: 10, name: 'Spring Boot', count: 134 },
  { id: 11, name: '数据库', count: 120 },
  { id: 12, name: 'MySQL', count: 115 },
  { id: 13, name: 'Redis', count: 92 },
  { id: 14, name: 'MongoDB', count: 78 },
  { id: 15, name: 'Docker', count: 115 },
  { id: 16, name: 'Kubernetes', count: 95 }
]

// 模拟文章数据
const mockArticles = (tagName) => {
  const baseArticles = [
    {
      id: 1,
      title: `深入理解 ${tagName} 核心概念`,
      summary: `本文将深入探讨 ${tagName} 的核心概念，帮助你更好地理解和应用...`,
      authorName: '张三',
      viewCount: 320,
      likeCount: 42,
      commentCount: 12,
      createTime: '2024-01-15T10:30:00',
      updateTime: '2024-01-15T10:30:00'
    },
    {
      id: 2,
      title: `${tagName} 最佳实践指南`,
      summary: `分享使用 ${tagName} 的最佳实践，避免常见错误...`,
      authorName: '李四',
      viewCount: 156,
      likeCount: 25,
      commentCount: 8,
      createTime: '2024-01-14T14:20:00',
      updateTime: '2024-01-14T14:20:00'
    },
    {
      id: 3,
      title: `${tagName} 项目实战经验分享`,
      summary: `在实际项目中使用 ${tagName} 的经验总结和技巧分享...`,
      authorName: '王五',
      viewCount: 210,
      likeCount: 36,
      commentCount: 10,
      createTime: '2024-01-13T09:15:00',
      updateTime: '2024-01-13T09:15:00'
    },
    {
      id: 4,
      title: `${tagName} 常见问题解决方案`,
      summary: `整理了使用 ${tagName} 过程中遇到的常见问题及其解决方案...`,
      authorName: '赵六',
      viewCount: 145,
      likeCount: 18,
      commentCount: 5,
      createTime: '2024-01-12T11:45:00',
      updateTime: '2024-01-12T11:45:00'
    },
    {
      id: 5,
      title: `${tagName} 性能优化技巧`,
      summary: `介绍 ${tagName} 的性能优化方法和实用技巧...`,
      authorName: '张三',
      viewCount: 189,
      likeCount: 29,
      commentCount: 7,
      createTime: '2024-01-11T16:30:00',
      updateTime: '2024-01-11T16:30:00'
    }
  ]
  
  return baseArticles.slice(0, Math.floor(Math.random() * 3) + 3) // 随机3-5篇文章
}

// 获取相关标签
const getRelatedTags = (tagName) => {
  const currentTag = allTags.find(tag => tag.name === tagName)
  if (!currentTag) return []
  
  // 移除当前标签，取其他标签
  return allTags
    .filter(tag => tag.name !== tagName)
    .sort((a, b) => b.count - a.count)
    .slice(0, 6)
}

// 生命周期
onMounted(() => {
  loadTagData()
})

// 监听路由变化
watch(() => route.params.name, (newName) => {
  tagName.value = newName
  currentPage.value = 1
  loadTagData()
})

// 方法
const loadTagData = () => {
  loading.value = true
  
  // 模拟加载延迟
  setTimeout(() => {
    // 设置标签信息
    tagDescription.value = tagDescriptions[tagName.value] || `关于 ${tagName.value} 的相关文章`
    
    // 生成随机统计数据
    total.value = Math.floor(Math.random() * 50) + 10
    viewCount.value = Math.floor(Math.random() * 5000) + 1000
    likeCount.value = Math.floor(viewCount.value * 0.15)
    
    // 获取相关标签
    relatedTags.value = getRelatedTags(tagName.value)
    
    // 加载文章数据
    articles.value = mockArticles(tagName.value)
    
    loading.value = false
  }, 800)
}

const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + '千'
  }
  return num
}

const getTagType = (count) => {
  if (count > 100) return 'danger'
  if (count > 50) return 'warning'
  if (count > 20) return 'success'
  if (count > 10) return 'info'
  return ''
}

const viewArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

const viewRelatedTag = (tagName) => {
  router.push(`/tag/${tagName}`)
}

const handlePageChange = (page) => {
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}
</script>

<style scoped>
.tag-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.tag-container {
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

/* 标签头部 */
.tag-info {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.tag-title {
  margin-bottom: 25px;
}

.tag-title h1 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 28px;
  color: #333;
  margin-bottom: 12px;
}

.tag-title h1 .el-icon {
  color: #67c23a;
  font-size: 30px;
}

.tag-description {
  color: #666;
  font-size: 16px;
  line-height: 1.6;
}

/* 标签统计 */
.tag-stats {
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

/* 相关标签 */
.related-tags {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.related-tags h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 15px;
}

.related-tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.related-tag-item {
  cursor: pointer;
  transition: all 0.3s;
}

.related-tag-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 标签内容 */
.tag-content {
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tag-info {
    padding: 20px;
  }
  
  .tag-title h1 {
    font-size: 24px;
  }
  
  .tag-stats {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .stat-item {
    min-width: 100px;
    flex: 1;
  }
}
</style>