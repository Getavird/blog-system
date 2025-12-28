<template>
  <div class="search-page">
    <!-- 添加Header -->
    <Header />
    
    <div class="search-container">
      <div class="container">
        <!-- 搜索头部 -->
        <div class="search-header">
          <h1 class="search-title">搜索</h1>
          <p class="search-subtitle">发现你感兴趣的内容</p>
          
          <!-- 搜索框 -->
          <div class="search-box-wrapper">
            <div class="search-box">
              <el-input
                v-model="keyword"
                placeholder="输入关键词搜索文章、用户、标签..."
                @keyup.enter="doSearch"
                @input="handleInput"
                @clear="handleClear"
                clearable
                size="large"
                class="search-input"
              >
                <template #prepend>
                  <el-select 
                    v-model="searchType" 
                    placeholder="类型" 
                    style="width: 120px"
                    @change="handleTypeChange"
                  >
                    <el-option label="全部" value="full" />
                    <el-option label="文章" value="articles" />
                    <el-option label="用户" value="users" />
                    <el-option label="标签" value="tags" />
                  </el-select>
                </template>
                <template #append>
                  <el-button 
                    @click="doSearch" 
                    type="primary" 
                    :loading="searchStore.searchLoading"
                    :icon="Search"
                  >
                    搜索
                  </el-button>
                </template>
              </el-input>
              
              <!-- 搜索建议 -->
              <div v-if="showSuggestions" class="search-suggestions">
                <div 
                  v-for="suggestion in searchStore.searchSuggestions" 
                  :key="suggestion"
                  class="suggestion-item"
                  @click="selectSuggestion(suggestion)"
                >
                  <el-icon><Search /></el-icon>
                  {{ suggestion }}
                </div>
              </div>
            </div>
            
            <!-- 搜索统计 -->
            <div v-if="keyword" class="search-stats">
              <span class="stats-item">
                共找到 {{ searchStore.pagination.total }} 个结果
              </span>
              <span class="stats-item">
                搜索类型: {{ searchTypeLabel }}
              </span>
            </div>
          </div>
          
          <!-- 搜索历史提示 -->
          <div v-if="searchHistory.length > 0 && !keyword" class="search-history-tip">
            <p class="tip-text">
              您有 {{ searchHistory.length }} 条搜索历史记录
            </p>
          </div>
        </div>
        
        <!-- 搜索结果 -->
        <div class="search-results">
          <!-- 加载状态 -->
          <div v-if="searchStore.searchLoading" class="loading-state">
            <div class="loading-content">
              <el-icon class="loading-icon" :size="40" color="#409eff">
                <Loading />
              </el-icon>
              <p>正在搜索...</p>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-else-if="!keyword" class="empty-state">
            <div class="empty-content">
              <el-icon :size="80" color="#c0c4cc">
                <Search />
              </el-icon>
              <h3>输入关键词开始搜索</h3>
              <p>搜索文章、用户、标签等内容</p>
              
              <!-- 搜索历史 -->
              <div v-if="searchHistory.length > 0" class="search-history">
                <h4>搜索历史</h4>
                <div class="history-list">
                  <el-tag
                    v-for="item in searchHistory"
                    :key="item"
                    class="history-tag"
                    @click="selectHistory(item)"
                    closable
                    @close="removeHistory(item)"
                    size="medium"
                  >
                    {{ item }}
                  </el-tag>
                </div>
                <el-button 
                  v-if="searchHistory.length > 0" 
                  type="text" 
                  size="small" 
                  @click="clearHistory"
                >
                  清空历史
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 搜索结果为空 -->
          <div v-else-if="!searchStore.hasResults" class="no-results">
            <div class="no-results-content">
              <el-icon :size="80" color="#c0c4cc">
                <Search />
              </el-icon>
              <h3>没有找到相关结果</h3>
              <p>换个关键词试试看</p>
              <div class="suggestions">
                <p>建议：</p>
                <ul>
                  <li>检查输入的关键词是否正确</li>
                  <li>尝试使用不同的关键词</li>
                  <li>尝试使用更通用的关键词</li>
                  <li>减少筛选条件</li>
                </ul>
              </div>
            </div>
          </div>
          
          <!-- 搜索结果 -->
          <div v-else class="results-container">
            <!-- 搜索结果头部 -->
            <div class="results-header">
              <h2>搜索结果</h2>
              <div class="results-meta">
                共 {{ searchStore.pagination.total }} 个结果，
                第 {{ searchStore.pagination.page }}/{{ searchStore.pagination.totalPages }} 页
              </div>
            </div>
            
            <!-- 搜索结果列表 -->
            <div class="results-list">
              <!-- 文章结果 -->
              <div v-if="searchStore.searchResults.articles.length > 0" class="result-section">
                <h3 class="section-title">
                  <el-icon><Document /></el-icon>
                  文章
                </h3>
                <div class="article-results">
                  <div 
                    v-for="article in searchStore.searchResults.articles" 
                    :key="article.id"
                    class="article-item"
                    @click="viewArticle(article.id)"
                  >
                    <div class="article-content">
                      <h4 class="article-title">{{ article.title }}</h4>
                      <div class="article-meta">
                        <span class="meta-item">
                          <el-icon><User /></el-icon>
                          {{ article.authorName || '匿名' }}
                        </span>
                        <span class="meta-item">
                          <el-icon><Calendar /></el-icon>
                          {{ formatTime(article.createTime) }}
                        </span>
                        <span class="meta-item">
                          <el-icon><View /></el-icon>
                          {{ article.viewCount || 0 }} 阅读
                        </span>
                        <span class="meta-item">
                          <el-icon><Star /></el-icon>
                          {{ article.likeCount || 0 }} 点赞
                        </span>
                      </div>
                      <div v-if="article.summary" class="article-summary">
                        {{ article.summary }}
                      </div>
                      <div class="article-tags">
                        <el-tag 
                          v-for="tag in article.tags || []" 
                          :key="tag"
                          size="small"
                          class="tag-item"
                          @click.stop="searchTag(tag)"
                        >
                          {{ tag }}
                        </el-tag>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 用户结果 -->
              <div v-if="searchStore.searchResults.users.length > 0" class="result-section">
                <h3 class="section-title">
                  <el-icon><User /></el-icon>
                  用户
                </h3>
                <div class="user-results">
                  <div 
                    v-for="user in searchStore.searchResults.users" 
                    :key="user.id"
                    class="user-item"
                    @click="viewUser(user.username)"
                  >
                    <div class="user-avatar">
                      <img v-if="user.avatar" :src="user.avatar" alt="用户头像" />
                      <div v-else class="avatar-placeholder">
                        {{ user.username?.charAt(0)?.toUpperCase() || 'U' }}
                      </div>
                    </div>
                    <div class="user-info">
                      <h4 class="user-name">{{ user.username }}</h4>
                      <p v-if="user.bio" class="user-bio">{{ user.bio }}</p>
                      <div class="user-stats">
                        <span class="stat-item">
                          <el-icon><Document /></el-icon>
                          {{ user.articleCount || 0 }} 文章
                        </span>
                        <span class="stat-item">
                          <el-icon><Star /></el-icon>
                          {{ user.followerCount || 0 }} 粉丝
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 标签结果 -->
              <div v-if="searchStore.searchResults.tags.length > 0" class="result-section">
                <h3 class="section-title">
                  <el-icon><CollectionTag /></el-icon>
                  标签
                </h3>
                <div class="tag-results">
                  <el-tag
                    v-for="tag in searchStore.searchResults.tags"
                    :key="tag.id || tag.name"
                    class="tag-item-large"
                    @click="viewTag(tag.name || tag)"
                    size="large"
                    :type="getTagType(tag)"
                    effect="dark"
                  >
                    {{ tag.name || tag }}
                    <span class="tag-count" v-if="tag.count">
                      ({{ tag.count }})
                    </span>
                  </el-tag>
                </div>
              </div>
            </div>
            
            <!-- 分页 -->
            <div v-if="searchStore.pagination.total > 0" class="pagination-wrapper">
              <el-pagination
                :current-page="searchStore.pagination.page"
                :page-size="searchStore.pagination.size"
                :total="searchStore.pagination.total"
                :page-sizes="[10, 20, 30, 50]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handlePageChange"
                background
              />
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 添加Footer -->
    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useSearchStore } from '@/stores/search'
import { ElMessage } from 'element-plus'
import {
  Search,
  Loading,
  Document,
  User,
  Calendar,
  View,
  Star,
  CollectionTag
} from '@element-plus/icons-vue'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'

const route = useRoute()
const router = useRouter()

// Pinia Store
const searchStore = useSearchStore()

// 搜索状态
const keyword = ref('')
const searchType = ref('full')
const showSuggestions = ref(false)

// 计算属性
const searchTypeLabel = computed(() => {
  const labels = {
    full: '全部',
    articles: '文章',
    users: '用户',
    tags: '标签'
  }
  return labels[searchType.value] || '全部'
})

// 从本地存储加载搜索历史
const searchHistory = computed(() => searchStore.searchHistory)

// 生命周期
onMounted(() => {
  // 从路由参数获取搜索关键词
  const queryKeyword = route.query.q || ''
  const queryType = route.query.type || 'full'
  
  if (queryKeyword) {
    keyword.value = queryKeyword
    searchType.value = queryType
    doSearch()
  }
  
  // 加载搜索历史
  searchStore.loadSearchHistoryFromStorage()
})

// 监听路由参数变化
watch(
  () => route.query,
  (newQuery) => {
    const newKeyword = newQuery.q || ''
    const newType = newQuery.type || 'full'
    
    if (newKeyword && newKeyword !== keyword.value) {
      keyword.value = newKeyword
      searchType.value = newType
      doSearch()
    }
  }
)

// 监听输入变化
const handleInput = () => {
  if (keyword.value.trim()) {
    showSuggestions.value = true
    searchStore.fetchSearchSuggestions(keyword.value)
  } else {
    showSuggestions.value = false
  }
}

// 清空输入
const handleClear = () => {
  showSuggestions.value = false
  searchStore.clearSearchResults()
}

// 搜索类型改变
const handleTypeChange = () => {
  if (keyword.value.trim()) {
    doSearch()
  }
}

// 执行搜索
const doSearch = async () => {
  const searchKeyword = keyword.value.trim()
  
  if (!searchKeyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  try {
    // 更新URL参数
    router.replace({
      path: '/search',
      query: { 
        q: searchKeyword,
        type: searchType.value 
      }
    })
    
    // 根据搜索类型调用不同的搜索方法
    let searchResult
    switch (searchType.value) {
      case 'articles':
        searchResult = await searchStore.searchArticles(searchKeyword, 
          searchStore.pagination.page, 
          searchStore.pagination.size)
        break
      case 'users':
        searchResult = await searchStore.searchUsers(searchKeyword, 
          searchStore.pagination.page, 
          searchStore.pagination.size)
        break
      case 'tags':
        searchResult = await searchStore.searchTags(searchKeyword, 
          searchStore.pagination.page, 
          searchStore.pagination.size)
        break
      default:
        searchResult = await searchStore.fullSearch(searchKeyword, 
          searchStore.pagination.page, 
          searchStore.pagination.size)
    }
    
    // 添加到搜索历史
    if (searchKeyword) {
      searchStore.addToSearchHistory(searchKeyword)
    }
    
    return searchResult
    
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error(error.message || '搜索失败，请稍后重试')
  }
}

// 选择搜索建议
const selectSuggestion = (suggestion) => {
  keyword.value = suggestion
  showSuggestions.value = false
  doSearch()
}

// 选择搜索历史
const selectHistory = (historyItem) => {
  keyword.value = historyItem
  doSearch()
}

// 移除搜索历史
const removeHistory = (historyItem) => {
  searchStore.searchHistory = searchStore.searchHistory.filter(item => item !== historyItem)
  localStorage.setItem('search_history', JSON.stringify(searchStore.searchHistory))
}

// 清空搜索历史
const clearHistory = async () => {
  try {
    await searchStore.clearSearchHistory()
    ElMessage.success('搜索历史已清空')
  } catch (error) {
    console.error('清空历史失败:', error)
    ElMessage.error('清空历史失败')
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  try {
    const date = new Date(time)
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    
    if (days === 0) {
      return '今天'
    } else if (days === 1) {
      return '昨天'
    } else if (days < 7) {
      return `${days}天前`
    } else if (days < 30) {
      const weeks = Math.floor(days / 7)
      return `${weeks}周前`
    } else {
      return date.toLocaleDateString('zh-CN')
    }
  } catch (error) {
    return time
  }
}

// 获取标签类型
const getTagType = (tag) => {
  const count = tag.count || 0
  if (count > 50) return 'danger'
  if (count > 20) return 'warning'
  if (count > 10) return 'success'
  if (count > 5) return 'primary'
  return 'info'
}

// 查看文章
const viewArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

// 查看用户
const viewUser = (username) => {
  router.push(`/user/${username}`)
}

// 查看标签
const viewTag = (tagName) => {
  router.push(`/tag/${encodeURIComponent(tagName)}`)
}

// 搜索标签
const searchTag = (tagName) => {
  keyword.value = tagName
  searchType.value = 'tags'
  doSearch()
}

// 分页处理
const handlePageChange = async (page) => {
  searchStore.pagination.page = page
  if (keyword.value.trim()) {
    await doSearch()
  }
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 每页数量改变
const handleSizeChange = async (size) => {
  searchStore.pagination.size = size
  searchStore.pagination.page = 1
  if (keyword.value.trim()) {
    await doSearch()
  }
}
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.search-container {
  flex: 1;
  padding: 20px 0 40px;
}

.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 搜索头部 */
.search-header {
  margin-bottom: 40px;
  text-align: center;
}

.search-title {
  font-size: 36px;
  color: #333;
  margin-bottom: 10px;
  font-weight: 600;
}

.search-subtitle {
  font-size: 16px;
  color: #666;
  margin-bottom: 30px;
}

/* 搜索框 */
.search-box-wrapper {
  max-width: 800px;
  margin: 0 auto 30px;
  position: relative;
}

.search-box {
  position: relative;
  margin-bottom: 15px;
}

.search-input {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border-radius: 50px;
  overflow: hidden;
}

.search-input :deep(.el-input-group__prepend) {
  background: #f5f7fa;
  border: none;
}

.search-input :deep(.el-input-group__append) {
  background: #409eff;
  border: none;
}

.search-input :deep(.el-input-group__append .el-button) {
  color: white;
  padding: 0 30px;
}

/* 搜索建议 */
.search-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  margin-top: 5px;
  z-index: 1000;
  max-height: 300px;
  overflow-y: auto;
}

.suggestion-item {
  padding: 12px 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.suggestion-item:last-child {
  border-bottom: none;
}

.suggestion-item:hover {
  background: #f5f7fa;
  color: #409eff;
}

/* 搜索统计 */
.search-stats {
  display: flex;
  justify-content: center;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.stats-item {
  padding: 4px 12px;
  background: #f0f2f5;
  border-radius: 4px;
}

/* 搜索历史提示 */
.search-history-tip {
  margin-top: 20px;
  padding: 10px 20px;
  background: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #d9ecff;
  display: inline-block;
}

.tip-text {
  color: #409eff;
  font-size: 14px;
  margin: 0;
}

/* 搜索结果区域 */
.search-results {
  min-height: 300px;
}

/* 加载状态 */
.loading-state {
  padding: 60px 20px;
  text-align: center;
}

.loading-content {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.loading-icon {
  animation: rotate 1.5s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.empty-content h3 {
  font-size: 20px;
  color: #333;
  margin: 15px 0 8px;
}

.empty-content p {
  color: #666;
  margin-bottom: 30px;
}

/* 搜索历史 */
.search-history {
  max-width: 600px;
  margin: 40px auto 0;
  text-align: left;
}

.search-history h4 {
  font-size: 16px;
  color: #333;
  margin-bottom: 15px;
  text-align: center;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  margin-bottom: 15px;
}

.history-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.history-tag:hover {
  background: #409eff;
  color: white;
}

/* 无结果状态 */
.no-results {
  padding: 60px 20px;
  text-align: center;
}

.no-results-content h3 {
  font-size: 20px;
  color: #333;
  margin: 15px 0 8px;
}

.no-results-content p {
  color: #666;
  margin-bottom: 30px;
}

.suggestions {
  max-width: 500px;
  margin: 30px auto 0;
  text-align: left;
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
}

.suggestions p {
  font-weight: 500;
  margin-bottom: 10px;
}

.suggestions ul {
  margin: 0;
  padding-left: 20px;
  color: #666;
}

.suggestions li {
  margin-bottom: 5px;
}

/* 搜索结果容器 */
.results-container {
  animation: fadeIn 0.5s ease-out;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.results-header h2 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.results-meta {
  color: #666;
  font-size: 14px;
}

/* 结果部分 */
.result-section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 文章结果 */
.article-results {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.article-item {
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
}

.article-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.article-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 12px;
  font-weight: 500;
}

.article-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 13px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-summary {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 15px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-item {
  cursor: pointer;
  transition: all 0.3s;
}

.tag-item:hover {
  transform: translateY(-2px);
}

/* 用户结果 */
.user-results {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
}

.user-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.user-avatar {
  width: 60px;
  height: 60px;
  flex-shrink: 0;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  color: white;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}

.user-bio {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.user-stats {
  display: flex;
  gap: 15px;
  color: #909399;
  font-size: 13px;
}

.user-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 标签结果 */
.tag-results {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item-large {
  cursor: pointer;
  padding: 10px 20px;
  font-size: 16px;
  transition: all 0.3s;
}

.tag-item-large:hover {
  transform: scale(1.05);
}

.tag-count {
  margin-left: 4px;
  font-size: 14px;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

/* 动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-title {
    font-size: 28px;
  }
  
  .search-input :deep(.el-input-group__prepend) {
    display: none;
  }
  
  .search-stats {
    flex-direction: column;
    align-items: center;
    gap: 10px;
  }
  
  .results-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .user-results {
    grid-template-columns: 1fr;
  }
  
  .article-meta {
    gap: 10px;
  }
}

@media (max-width: 480px) {
  .search-title {
    font-size: 24px;
  }
  
  .search-input :deep(.el-input-group__append .el-button) {
    padding: 0 15px;
  }
  
  .history-list {
    justify-content: flex-start;
  }
}
</style>