<template>
  <div class="archives-page">
    <Header />
    
    <div class="archives-container">
      <div class="container">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1>文章归档</h1>
          <p class="page-subtitle">按时间线浏览所有文章，记录每一刻的思考</p>
        </div>

        <!-- 归档统计 -->
        <div class="archive-stats">
          <div class="stat-card">
            <div class="stat-number">{{ totalArticles }}</div>
            <div class="stat-label">文章总数</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ yearsCount }}</div>
            <div class="stat-label">归档年份</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ totalMonths }}</div>
            <div class="stat-label">有文章的月份</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ mostActiveYear }}</div>
            <div class="stat-label">最活跃年份</div>
          </div>
        </div>

        <!-- 年份筛选 -->
        <div class="year-filter">
          <h3>按年份筛选</h3>
          <div class="year-buttons">
            <el-button 
              :type="selectedYear === 'all' ? 'primary' : 'default'"
              size="small"
              @click="selectYear('all')"
            >
              全部
            </el-button>
            <el-button 
              v-for="year in availableYears" 
              :key="year"
              :type="selectedYear === year ? 'primary' : 'default'"
              size="small"
              @click="selectYear(year)"
            >
              {{ year }}年
            </el-button>
          </div>
        </div>

        <!-- 时间线内容 -->
        <div class="timeline">
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-state">
            <div class="loading-year" v-for="n in 3" :key="n">
              <el-skeleton :rows="3" animated />
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else-if="filteredArchives.length === 0" class="empty-state">
            <div class="empty-content">
              <el-icon :size="60" color="#c0c4cc">
                <Calendar />
              </el-icon>
              <h3>暂无归档数据</h3>
              <p>还没有发表过文章，开始你的第一篇创作吧</p>
              <el-button type="primary" @click="toWriteArticle">
                写第一篇文章
              </el-button>
            </div>
          </div>

          <!-- 时间线 -->
          <div v-else class="timeline-content">
            <div 
              v-for="yearData in filteredArchives" 
              :key="yearData.year"
              :id="`year-${yearData.year}`"
              class="year-section"
            >
              <!-- 年份标题 -->
              <div class="year-header" @click="toggleYear(yearData.year)">
                <div class="year-title">
                  <h2>
                    <el-icon class="year-icon">
                      <Calendar />
                    </el-icon>
                    {{ yearData.year }} 年
                  </h2>
                  <div class="year-meta">
                    <span class="year-count">{{ yearData.total }} 篇文章</span>
                    <span class="year-views">👁 {{ formatNumber(yearData.viewCount) }} 阅读</span>
                    <span class="year-likes">❤ {{ formatNumber(yearData.likeCount) }} 点赞</span>
                  </div>
                </div>
                <div class="year-toggle">
                  <el-icon :class="{ 'rotate': yearData.expanded }">
                    <ArrowDown />
                  </el-icon>
                </div>
              </div>

              <!-- 月份列表 -->
              <el-collapse-transition>
                <div v-show="yearData.expanded" class="months-container">
                  <div 
                    v-for="monthData in yearData.months" 
                    :key="`${yearData.year}-${monthData.month}`"
                    class="month-section"
                  >
                    <!-- 月份标题 -->
                    <div class="month-header">
                      <div class="month-title">
                        <h3>{{ monthData.month }}月</h3>
                        <span class="month-count">{{ monthData.count }} 篇文章</span>
                      </div>
                      <div class="month-date">
                        {{ yearData.year }}年{{ monthData.month }}月
                      </div>
                    </div>

                    <!-- 文章列表 -->
                    <div class="articles-list">
                      <div 
                        v-for="article in monthData.articles" 
                        :key="article.id"
                        class="article-item"
                        @click="viewArticle(article.id)"
                      >
                        <div class="article-date">
                          {{ formatDay(article.createTime) }}
                        </div>
                        <div class="article-content">
                          <div class="article-title">
                            {{ article.title }}
                            <el-tag v-if="article.status === 0" type="info" size="mini">
                              草稿
                            </el-tag>
                          </div>
                          <div class="article-meta">
                            <span class="meta-item">
                              <el-icon><View /></el-icon>
                              {{ article.viewCount || 0 }}
                            </span>
                            <span class="meta-item">
                              <el-icon><Star /></el-icon>
                              {{ article.likeCount || 0 }}
                            </span>
                            <span class="meta-item">
                              <el-icon><ChatDotRound /></el-icon>
                              {{ article.commentCount || 0 }}
                            </span>
                          </div>
                        </div>
                        <div class="article-arrow">
                          <el-icon><ArrowRight /></el-icon>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </el-collapse-transition>
            </div>
          </div>
        </div>

        <!-- 快速导航 -->
        <div v-if="!loading && filteredArchives.length > 0" class="quick-nav">
          <h3>快速导航</h3>
          <div class="nav-buttons">
            <el-button 
              v-for="year in availableYears.slice(0, 5)" 
              :key="year"
              type="text"
              @click="scrollToYear(year)"
            >
              {{ year }}年
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Calendar,
  ArrowDown,
  ArrowRight,
  View,
  Star,
  ChatDotRound
} from '@element-plus/icons-vue'
import Header from '../components/layout/Header.vue'
import Footer from '../components/layout/Footer.vue'

const router = useRouter()

// 状态
const loading = ref(true)
const archives = ref([])
const selectedYear = ref('all')
const expandedYears = ref(new Set([2024])) // 默认展开当前年份

// 模拟归档数据
const mockArchivesData = () => {
  const years = [2024, 2023, 2022, 2021, 2020]
  const monthNames = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月']
  const articleTitles = [
    'Vue 3 新特性详解与实战指南',
    'Spring Boot入门教程',
    '数据库设计规范与实践',
    '前端工程化建设指南',
    '微服务架构设计原则',
    'TypeScript 类型系统深入理解',
    'React Hooks 最佳实践',
    'Docker 容器化部署实战',
    'Redis 高级应用场景',
    'MySQL 性能优化技巧',
    'Kubernetes 入门到实战',
    'Node.js 高并发处理',
    'Webpack 5 配置指南',
    'Git 高级使用技巧',
    'Linux 服务器运维指南',
    'Python 数据分析实战',
    '机器学习入门教程',
    '网络安全基础',
    '性能监控与优化',
    'DevOps 实践指南'
  ]

  const archivesData = []

  years.forEach(year => {
    const yearData = {
      year,
      total: 0,
      viewCount: 0,
      likeCount: 0,
      expanded: expandedYears.value.has(year),
      months: []
    }

    // 随机生成月份数据
    const monthCount = Math.floor(Math.random() * 8) + 4 // 4-11个月有文章
    const monthIndexes = Array.from({ length: 12 }, (_, i) => i + 1)
      .sort(() => Math.random() - 0.5)
      .slice(0, monthCount)

    monthIndexes.forEach(month => {
      const articleCount = Math.floor(Math.random() * 8) + 2 // 2-9篇文章
      const articles = []

      for (let i = 0; i < articleCount; i++) {
        const day = Math.floor(Math.random() * 28) + 1
        const viewCount = Math.floor(Math.random() * 500) + 100
        const likeCount = Math.floor(viewCount * 0.15)
        const commentCount = Math.floor(likeCount * 0.5)
        
        const article = {
          id: parseInt(`${year}${month}${day}${i}`),
          title: articleTitles[Math.floor(Math.random() * articleTitles.length)],
          createTime: `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`,
          viewCount,
          likeCount,
          commentCount,
          status: Math.random() > 0.1 ? 1 : 0 // 90%已发布，10%草稿
        }

        articles.push(article)
        yearData.total++
        yearData.viewCount += viewCount
        yearData.likeCount += likeCount
      }

      yearData.months.push({
        month,
        monthName: monthNames[month - 1],
        count: articleCount,
        articles: articles.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      })
    })

    // 按月份倒序排列
    yearData.months.sort((a, b) => b.month - a.month)

    archivesData.push(yearData)
  })

  // 按年份倒序排列
  return archivesData.sort((a, b) => b.year - a.year)
}

// 计算属性
const filteredArchives = computed(() => {
  if (selectedYear.value === 'all') {
    return archives.value
  }
  return archives.value.filter(yearData => yearData.year === parseInt(selectedYear.value))
})

const totalArticles = computed(() => {
  return archives.value.reduce((sum, year) => sum + year.total, 0)
})

const yearsCount = computed(() => {
  return archives.value.length
})

const totalMonths = computed(() => {
  return archives.value.reduce((sum, year) => sum + year.months.length, 0)
})

const availableYears = computed(() => {
  return archives.value.map(year => year.year)
})

const mostActiveYear = computed(() => {
  if (archives.value.length === 0) return 0
  const mostActive = archives.value.reduce((prev, current) => 
    prev.total > current.total ? prev : current
  )
  return mostActive.year
})

// 生命周期
onMounted(() => {
  loadArchives()
})

// 方法
const loadArchives = () => {
  loading.value = true
  
  // 模拟API调用
  setTimeout(() => {
    archives.value = mockArchivesData()
    loading.value = false
  }, 1000)
}

const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + '千'
  }
  return num
}

const formatDay = (dateString) => {
  const date = new Date(dateString)
  return date.getDate()
}

const toggleYear = (year) => {
  const yearData = archives.value.find(y => y.year === year)
  if (yearData) {
    yearData.expanded = !yearData.expanded
    if (yearData.expanded) {
      expandedYears.value.add(year)
    } else {
      expandedYears.value.delete(year)
    }
  }
}

const selectYear = (year) => {
  selectedYear.value = year
  if (year !== 'all') {
    const yearData = archives.value.find(y => y.year === parseInt(year))
    if (yearData && !yearData.expanded) {
      yearData.expanded = true
      expandedYears.value.add(parseInt(year))
    }
    
    // 滚动到对应的年份
    setTimeout(() => {
      const element = document.getElementById(`year-${year}`)
      if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }
    }, 100)
  }
}

const scrollToYear = (year) => {
  selectYear(year.toString())
}

const viewArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

const toWriteArticle = () => {
  // 检查是否登录
  const token = localStorage.getItem('blog_token')
  if (!token) {
    ElMessage.warning('请先登录')
    const event = new CustomEvent('showLogin')
    window.dispatchEvent(event)
    return
  }
  router.push('/article/create')
}
</script>

<style scoped>
.archives-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.archives-container {
  flex: 1;
  padding: 20px 0 40px;
}

.container {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 32px;
  color: #333;
  margin-bottom: 12px;
}

.page-subtitle {
  color: #666;
  font-size: 16px;
}

/* 归档统计 */
.archive-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

/* 年份筛选 */
.year-filter {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.year-filter h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 15px;
}

.year-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

/* 时间线 */
.timeline {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  position: relative;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 40px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(to bottom, #409eff, #67c23a);
  opacity: 0.3;
}

/* 加载状态 */
.loading-state {
  padding: 20px 0;
}

.loading-year {
  margin-bottom: 30px;
}

/* 空状态 */
.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.empty-content h3 {
  font-size: 18px;
  color: #333;
  margin: 15px 0 8px;
}

.empty-content p {
  color: #666;
  margin-bottom: 25px;
}

/* 年份部分 */
.year-section {
  margin-bottom: 40px;
  position: relative;
  padding-left: 80px;
}

.year-section:last-child {
  margin-bottom: 0;
}

.year-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.year-header:hover {
  background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
  transform: translateX(5px);
}

.year-title {
  flex: 1;
}

.year-title h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.year-icon {
  color: #409eff;
}

.year-meta {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.year-toggle {
  color: #999;
  transition: transform 0.3s;
}

.rotate {
  transform: rotate(180deg);
}

/* 月份部分 */
.months-container {
  margin-left: 20px;
}

.month-section {
  margin-bottom: 30px;
  position: relative;
  padding-left: 40px;
}

.month-section::before {
  content: '';
  position: absolute;
  left: 0;
  top: 25px;
  width: 10px;
  height: 10px;
  background: #67c23a;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 0 0 3px rgba(103, 194, 58, 0.2);
}

.month-section:last-child {
  margin-bottom: 0;
}

.month-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.month-title {
  display: flex;
  align-items: center;
  gap: 15px;
}

.month-title h3 {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.month-count {
  background: #409eff;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.month-date {
  color: #999;
  font-size: 14px;
}

/* 文章列表 */
.articles-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.article-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.article-item:hover {
  background: #e9ecef;
  transform: translateX(5px);
}

.article-item:hover .article-arrow {
  color: #409eff;
  transform: translateX(5px);
}

.article-date {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.article-content {
  flex: 1;
  min-width: 0;
}

.article-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.article-title .el-tag {
  flex-shrink: 0;
}

.article-meta {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-arrow {
  color: #c0c4cc;
  transition: all 0.3s;
}

/* 快速导航 */
.quick-nav {
  margin-top: 40px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.quick-nav h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 15px;
}

.nav-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header h1 {
    font-size: 28px;
  }
  
  .archive-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .timeline::before {
    left: 20px;
  }
  
  .year-section {
    padding-left: 60px;
  }
  
  .year-header {
    padding: 15px;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .year-title h2 {
    font-size: 20px;
  }
  
  .year-meta {
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .month-section {
    padding-left: 30px;
  }
  
  .month-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .article-item {
    padding: 12px;
  }
  
  .article-title {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .archive-stats {
    grid-template-columns: 1fr;
  }
  
  .year-buttons {
    justify-content: center;
  }
  
  .stat-card {
    padding: 20px;
  }
  
  .stat-number {
    font-size: 28px;
  }
}
</style>