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
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useTagStore } from '@/stores/tag'
import { useArticleStore } from '@/stores/article'
import { ElMessage } from 'element-plus'
import { Search, PriceTag } from '@element-plus/icons-vue'

// 组件导入 - 路径需要根据你的实际结构调整
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'

const router = useRouter()
const tagStore = useTagStore()
const articleStore = useArticleStore()

// 状态
const tags = ref([])  // 添加 tags 变量定义
const loading = ref(false)
const searchKeyword = ref('')
const viewMode = ref('cloud') // 'cloud' 或 'list'

// 计算属性
// 过滤后的标签
const filteredTags = computed(() => {
  if (!searchKeyword.value.trim()) {
    return tags.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return tags.value.filter(tag => 
    tag.name.toLowerCase().includes(keyword)
  )
})

// 总文章数
const totalArticleCount = computed(() => {
  return tags.value.reduce((sum, tag) => sum + (tag.articleCount || 0), 0)
})

// 最热标签（文章数最多的标签）
const mostUsedTag = computed(() => {
  if (tags.value.length === 0) return { name: '无' }
  const tag = tags.value.reduce((prev, current) => 
    (prev.articleCount || 0) > (current.articleCount || 0) ? prev : current
  )
  return tag
})

// 热门标签（前10个）
const topTags = computed(() => {
  return [...tags.value]
    .sort((a, b) => (b.articleCount || 0) - (a.articleCount || 0))
    .slice(0, 10)
})

// 生命周期
onMounted(async () => {
  await loadTags()
})

// 方法
// 加载标签
const loadTags = async () => {
  try {
    loading.value = true
    await tagStore.fetchTags()
    tags.value = tagStore.tags || []
  } catch (error) {
    console.error('加载标签失败:', error)
    ElMessage.error('加载标签失败')
  } finally {
    loading.value = false
  }
}

// 搜索标签
const handleSearch = () => {
  // 搜索逻辑由filteredTags计算属性处理
}

// 切换视图模式
const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'cloud' ? 'list' : 'cloud'
}

// 查看标签文章
const viewTagArticles = (tagId, tagName) => {
  router.push(`/tag/${encodeURIComponent(tagName)}`)
}

// 格式化时间（示例，根据实际数据结构调整）
const formatTime = (time) => {
  if (!time) return '未知'
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN')
}

// 根据文章数量获取标签类型（用于样式）
const getTagType = (count) => {
  if (count >= 100) return 'danger'
  if (count >= 50) return 'warning'
  if (count >= 20) return 'success'
  if (count >= 10) return 'primary'
  return 'info'
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