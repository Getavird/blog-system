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

      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useArchiveStore } from '@/stores/archive'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  Calendar,
  ArrowDown,
  ArrowRight,
  View,
  Star,
  ChatDotRound
} from '@element-plus/icons-vue'
import { ElCollapseTransition } from 'element-plus'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'

const router = useRouter()

// Pinia Stores
const archiveStore = useArchiveStore()
const userStore = useUserStore()

// 状态
const loading = ref(false)
const selectedYear = ref('all')
const expandedYears = ref({}) // 存储年份的展开状态

// 归档数据
const archives = computed(() => {
  const archiveData = archiveStore.archives || []
  return archiveData.map(year => ({
    ...year,
    expanded: expandedYears.value[year.year] !== false // 默认展开
  }))
})

const availableYears = computed(() => {
  return archiveStore.archiveYears || []
})

// 计算属性
const totalArticles = computed(() => {
  return archives.value.reduce((total, year) => total + (year.total || 0), 0)
})

const yearsCount = computed(() => availableYears.value.length || 0)

const totalMonths = computed(() => {
  return archives.value.reduce((total, year) => total + (year.months?.length || 0), 0)
})

const mostActiveYear = computed(() => {
  if (archives.value.length === 0) return '暂无'
  
  const sortedYears = [...archives.value].sort((a, b) => (b.total || 0) - (a.total || 0))
  return sortedYears[0]?.year || '暂无'
})

// 筛选后的归档数据
const filteredArchives = computed(() => {
  let result = archives.value
  
  // 按年份筛选
  if (selectedYear.value !== 'all') {
    const yearNum = parseInt(selectedYear.value)
    result = archives.value.filter(year => year.year === yearNum)
  }
  
  return result
})

// 组件挂载
onMounted(async () => {
  await loadArchives()
})

// 加载归档数据
const loadArchives = async () => {
  try {
    loading.value = true
    
    // 并行加载数据
    await Promise.all([
      archiveStore.fetchAllArchives(),
      archiveStore.fetchArchiveYears()
    ])
    
    // 初始化所有年份为展开状态
    if (archives.value.length > 0) {
      const initialExpanded = {}
      archives.value.forEach(year => {
        initialExpanded[year.year] = true
      })
      expandedYears.value = initialExpanded
    }
    
  } catch (error) {
    console.error('加载归档数据失败:', error)
    ElMessage.error('加载归档数据失败')
  } finally {
    loading.value = false
  }
}

// 选择年份
const selectYear = (year) => {
  selectedYear.value = year
  
  // 如果选择单个年份，确保它展开
  if (year !== 'all') {
    expandedYears.value[parseInt(year)] = true
  }
}

// 切换年份展开/收起
const toggleYear = (year) => {
  expandedYears.value[year] = !expandedYears.value[year]
}

// 格式化数字
const formatNumber = (num) => {
  if (!num && num !== 0) return 0
  
  const number = parseInt(num)
  if (isNaN(number)) return 0
  
  if (number >= 1000000) {
    return (number / 1000000).toFixed(1) + '百万'
  }
  if (number >= 10000) {
    return (number / 10000).toFixed(1) + '万'
  }
  if (number >= 1000) {
    return (number / 1000).toFixed(1) + '千'
  }
  return number.toString()
}

// 格式化日期
const formatDay = (dateString) => {
  if (!dateString) return ''
  
  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return ''
    return date.getDate().toString().padStart(2, '0')
  } catch (error) {
    console.warn('日期格式化失败:', dateString, error)
    return ''
  }
}

// 查看文章
const viewArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

// 跳转到写文章页面
const toWriteArticle = () => {
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录后再发布文章')
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
  cursor: pointer;
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

.year-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
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
  
  .article-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .article-date {
    align-self: flex-start;
  }
  
  .article-arrow {
    position: absolute;
    right: 15px;
    bottom: 15px;
  }
}
</style>