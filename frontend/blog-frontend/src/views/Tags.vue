<template>
  <div class="tags-page">
    <Header />
    
    <div class="tags-container">
      <div class="container">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1>标签云</h1>
          <p class="page-subtitle">探索文章的不同主题和分类</p>
        </div>

        <!-- 标签统计 -->
        <div class="tags-stats">
          <div class="stat-card">
            <div class="stat-number">{{ tags.length }}</div>
            <div class="stat-label">标签总数</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ totalArticleCount }}</div>
            <div class="stat-label">文章数量</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ mostUsedTag.name }}</div>
            <div class="stat-label">最热标签</div>
          </div>
        </div>

        <!-- 标签搜索 -->
        <div class="tags-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索标签..."
            clearable
            @input="handleSearch"
            @clear="handleSearch"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-button 
            type="text" 
            @click="toggleViewMode"
            class="view-toggle"
          >
            {{ viewMode === 'cloud' ? '切换到列表视图' : '切换到云图视图' }}
          </el-button>
        </div>

        <!-- 标签云/列表 -->
        <div class="tags-content">
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-state">
            <el-skeleton :rows="5" animated />
          </div>

          <!-- 空状态 -->
          <div v-else-if="filteredTags.length === 0" class="empty-state">
            <div class="empty-content">
              <el-icon :size="60" color="#c0c4cc">
                <PriceTag />
              </el-icon>
              <h3>没有找到相关标签</h3>
              <p>尝试其他搜索关键词</p>
            </div>
          </div>

          <!-- 标签云模式 -->
          <div v-else-if="viewMode === 'cloud'" class="tag-cloud-mode">
            <div class="tag-cloud-wrapper">
              <div 
                v-for="tag in filteredTags" 
                :key="tag.id"
                :class="[
                  'tag-cloud-item',
                  `tag-level-${Math.min(Math.floor(tag.count / 10) + 1, 5)}`
                ]"
                :style="{
                  fontSize: `${12 + Math.min(tag.count / 5, 10)}px`,
                  opacity: 0.5 + (tag.count / 100)
                }"
                @click="viewTagArticles(tag.id, tag.name)"
              >
                {{ tag.name }}
                <span class="tag-count">{{ tag.count }}</span>
              </div>
            </div>
          </div>

          <!-- 列表模式 -->
          <div v-else class="tag-list-mode">
            <div class="tags-table">
              <div class="table-header">
                <div class="header-cell">标签名称</div>
                <div class="header-cell">文章数量</div>
                <div class="header-cell">最近更新</div>
                <div class="header-cell">操作</div>
              </div>
              
              <div class="table-body">
                <div 
                  v-for="tag in filteredTags" 
                  :key="tag.id"
                  class="table-row"
                >
                  <div class="table-cell">
                    <el-tag :type="getTagType(tag.count)" size="medium">
                      {{ tag.name }}
                    </el-tag>
                  </div>
                  <div class="table-cell">
                    <span class="article-count">{{ tag.count }} 篇</span>
                  </div>
                  <div class="table-cell">
                    {{ formatTime(tag.lastUpdate) }}
                  </div>
                  <div class="table-cell">
                    <el-button 
                      type="primary" 
                      size="small" 
                      @click="viewTagArticles(tag.id, tag.name)"
                    >
                      查看文章
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 热门标签 -->
        <div v-if="filteredTags.length > 0" class="hot-tags">
          <h3>热门标签</h3>
          <div class="hot-tags-list">
            <el-tag
              v-for="tag in topTags"
              :key="tag.id"
              :type="getTagType(tag.count)"
              size="large"
              class="hot-tag-item"
              @click="viewTagArticles(tag.id, tag.name)"
            >
              {{ tag.name }} ({{ tag.count }})
            </el-tag>
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
import {
  Search,
  PriceTag
} from '@element-plus/icons-vue'
import Header from '../components/layout/Header.vue'
import Footer from '../components/layout/Footer.vue'

const router = useRouter()

// 状态
const loading = ref(true)
const tags = ref([])
const searchKeyword = ref('')
const viewMode = ref('cloud') // 'cloud' 或 'list'

// 模拟标签数据
const mockTags = [
  { id: 1, name: 'Vue', count: 156, lastUpdate: '2024-01-15' },
  { id: 2, name: 'React', count: 128, lastUpdate: '2024-01-14' },
  { id: 3, name: 'JavaScript', count: 210, lastUpdate: '2024-01-16' },
  { id: 4, name: 'TypeScript', count: 98, lastUpdate: '2024-01-13' },
  { id: 5, name: 'CSS', count: 89, lastUpdate: '2024-01-12' },
  { id: 6, name: 'HTML', count: 76, lastUpdate: '2024-01-11' },
  { id: 7, name: 'Node.js', count: 142, lastUpdate: '2024-01-10' },
  { id: 8, name: 'Python', count: 187, lastUpdate: '2024-01-09' },
  { id: 9, name: 'Java', count: 165, lastUpdate: '2024-01-08' },
  { id: 10, name: 'Spring Boot', count: 134, lastUpdate: '2024-01-07' },
  { id: 11, name: '数据库', count: 120, lastUpdate: '2024-01-06' },
  { id: 12, name: 'MySQL', count: 115, lastUpdate: '2024-01-05' },
  { id: 13, name: 'Redis', count: 92, lastUpdate: '2024-01-04' },
  { id: 14, name: 'MongoDB', count: 78, lastUpdate: '2024-01-03' },
  { id: 15, name: 'Docker', count: 115, lastUpdate: '2024-01-02' },
  { id: 16, name: 'Kubernetes', count: 95, lastUpdate: '2024-01-01' },
  { id: 17, name: '云计算', count: 82, lastUpdate: '2023-12-31' },
  { id: 18, name: '人工智能', count: 105, lastUpdate: '2023-12-30' },
  { id: 19, name: '机器学习', count: 98, lastUpdate: '2023-12-29' },
  { id: 20, name: '深度学习', count: 76, lastUpdate: '2023-12-28' },
  { id: 21, name: '算法', count: 110, lastUpdate: '2023-12-27' },
  { id: 22, name: '数据结构', count: 85, lastUpdate: '2023-12-26' },
  { id: 23, name: '设计模式', count: 85, lastUpdate: '2023-12-25' },
  { id: 24, name: '前端工程化', count: 92, lastUpdate: '2023-12-24' },
  { id: 25, name: '微服务', count: 78, lastUpdate: '2023-12-23' },
  { id: 26, name: '架构设计', count: 88, lastUpdate: '2023-12-22' },
  { id: 27, name: '性能优化', count: 94, lastUpdate: '2023-12-21' },
  { id: 28, name: '网络安全', count: 75, lastUpdate: '2023-12-20' },
  { id: 29, name: '测试', count: 68, lastUpdate: '2023-12-19' },
  { id: 30, name: '运维', count: 72, lastUpdate: '2023-12-18' }
]

// 计算属性
const filteredTags = computed(() => {
  if (!searchKeyword.value.trim()) {
    return tags.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return tags.value.filter(tag => 
    tag.name.toLowerCase().includes(keyword)
  )
})

const totalArticleCount = computed(() => {
  return tags.value.reduce((sum, tag) => sum + tag.count, 0)
})

const mostUsedTag = computed(() => {
  if (tags.value.length === 0) return { name: '无', count: 0 }
  return tags.value.reduce((prev, current) => 
    prev.count > current.count ? prev : current
  )
})

const topTags = computed(() => {
  return [...tags.value]
    .sort((a, b) => b.count - a.count)
    .slice(0, 10)
})

// 生命周期
onMounted(() => {
  loadTags()
})

// 方法
const loadTags = () => {
  loading.value = true
  
  // 模拟API调用
  setTimeout(() => {
    tags.value = mockTags
    loading.value = false
  }, 800)
}

const handleSearch = () => {
  // 搜索逻辑已经在 computed 中处理
}

const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'cloud' ? 'list' : 'cloud'
}

const getTagType = (count) => {
  if (count > 150) return 'danger'
  if (count > 100) return 'warning'
  if (count > 50) return 'success'
  if (count > 20) return 'info'
  return ''
}

const formatTime = (timeString) => {
  const date = new Date(timeString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN')
}

const viewTagArticles = (tagId, tagName) => {
  // 跳转到标签文章列表页面
  router.push(`/tag/${tagName}`)
}
</script>

<style scoped>
.tags-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.tags-container {
  flex: 1;
  padding: 20px 0 40px;
}

.container {
  width: 100%;
  max-width: 1200px;
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

/* 标签统计 */
.tags-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.tags-stats .stat-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.tags-stats .stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.tags-stats .stat-number {
  font-size: 36px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 8px;
}

.tags-stats .stat-label {
  color: #666;
  font-size: 14px;
}

/* 标签搜索 */
.tags-search {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 30px;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.search-input {
  flex: 1;
}

.view-toggle {
  white-space: nowrap;
}

/* 标签内容 */
.tags-content {
  margin-bottom: 40px;
}

/* 加载状态 */
.loading-state {
  background: white;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
}

/* 空状态 */
.empty-state {
  background: white;
  border-radius: 12px;
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
}

/* 标签云模式 */
.tag-cloud-mode {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  min-height: 500px;
}

.tag-cloud-wrapper {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;
  text-align: center;
}

.tag-cloud-item {
  padding: 12px 24px;
  margin: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 30px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #333;
  background: #f5f7fa;
  border: 2px solid transparent;
}

.tag-cloud-item:hover {
  transform: translateY(-5px) scale(1.05);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

/* 标签级别样式 */
.tag-level-1 {
  border-color: #dcdfe6;
  background: #f5f7fa;
}
.tag-level-1:hover {
  border-color: #c0c4cc;
  background: #e4e7ed;
}

.tag-level-2 {
  border-color: #b3e19d;
  background: #f0f9eb;
  color: #67c23a;
}
.tag-level-2:hover {
  border-color: #95d475;
  background: #e1f3d8;
}

.tag-level-3 {
  border-color: #a0cfff;
  background: #ecf5ff;
  color: #409eff;
}
.tag-level-3:hover {
  border-color: #79bbff;
  background: #d9ecff;
}

.tag-level-4 {
  border-color: #f3d19e;
  background: #fdf6ec;
  color: #e6a23c;
}
.tag-level-4:hover {
  border-color: #eebe77;
  background: #faecd8;
}

.tag-level-5 {
  border-color: #fab6b6;
  background: #fef0f0;
  color: #f56c6c;
}
.tag-level-5:hover {
  border-color: #f89898;
  background: #fde2e2;
}

.tag-count {
  font-size: 0.8em;
  opacity: 0.8;
  font-weight: 500;
}

/* 列表模式 */
.tag-list-mode {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.tags-table {
  width: 100%;
}

.table-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  background: #f8f9fa;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
}

.header-cell {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.table-body {
  padding: 0;
}

.table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  align-items: center;
  transition: all 0.3s;
}

.table-row:hover {
  background: #f8f9fa;
}

.table-cell {
  color: #333;
}

.article-count {
  color: #666;
  font-size: 14px;
}

/* 热门标签 */
.hot-tags {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.hot-tags h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.hot-tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.hot-tag-item {
  cursor: pointer;
  transition: all 0.3s;
}

.hot-tag-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header h1 {
    font-size: 28px;
  }
  
  .tags-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .tags-search {
    flex-direction: column;
    align-items: stretch;
  }
  
  .tag-cloud-mode {
    padding: 20px;
  }
  
  .table-header {
    display: none;
  }
  
  .table-row {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 20px;
    border-bottom: 2px solid #eee;
  }
  
  .table-cell {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .table-cell::before {
    content: attr(data-label);
    font-weight: 600;
    color: #666;
    font-size: 14px;
  }
  
  .table-cell[data-label="标签名称"]::before {
    content: "标签名称";
  }
  
  .table-cell[data-label="文章数量"]::before {
    content: "文章数量";
  }
  
  .table-cell[data-label="最近更新"]::before {
    content: "最近更新";
  }
  
  .table-cell[data-label="操作"]::before {
    content: "操作";
  }
}

@media (max-width: 480px) {
  .tags-stats {
    grid-template-columns: 1fr;
  }
  
  .tags-stats .stat-card {
    padding: 20px;
  }
  
  .tags-stats .stat-number {
    font-size: 28px;
  }
  
  .tag-cloud-item {
    padding: 10px 20px;
  }
}
</style>