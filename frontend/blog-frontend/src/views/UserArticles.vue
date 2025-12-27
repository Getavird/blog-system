<template>
    <div class="user-articles-page">
        <Header />

        <div class="articles-container">
            <div class="container">
                <!-- 页面标题 -->
                <div class="page-header">
                    <h1>我的文章</h1>
                    <p class="page-subtitle">管理您的文章，包括草稿和已发布的文章</p>
                </div>

                <!-- 工具栏 -->
                <div class="toolbar">
                    <div class="toolbar-left">
                        <el-input v-model="searchKeyword" placeholder="搜索文章标题..." clearable @clear="handleSearch"
                            @keyup.enter="handleSearch" class="search-input" :prefix-icon="Search" />

                        <el-select v-model="statusFilter" placeholder="文章状态" @change="handleFilter"
                            class="status-select">
                            <el-option label="全部" :value="-1" />
                            <el-option label="已发布" :value="1" />
                            <el-option label="草稿" :value="0" />
                        </el-select>
                    </div>

                    <div class="toolbar-right">
                        <el-button type="primary" @click="$router.push('/article/create')" class="create-btn">
                            <el-icon>
                                <Plus />
                            </el-icon>
                            写新文章
                        </el-button>

                        <el-button :disabled="selectedArticles.length === 0" @click="handleBatchDelete"
                            class="batch-delete-btn">
                            <el-icon>
                                <Delete />
                            </el-icon>
                            批量删除
                        </el-button>
                    </div>
                </div>

                <!-- 文章列表 -->
                <div class="articles-list">
                    <!-- 加载状态 -->
                    <div v-if="loading" class="loading-state">
                        <el-skeleton :rows="5" animated />
                    </div>

                    <!-- 文章表格 -->
                    <div v-else-if="articles.length > 0" class="articles-table">
                        <el-table :data="articles" style="width: 100%" @selection-change="handleSelectionChange"
                            :row-key="row => row.id">
                            <el-table-column type="selection" width="55" />

                            <el-table-column label="文章标题" min-width="300">
                                <template #default="{ row }">
                                    <div class="article-title-cell">
                                        <div class="title-content" @click="viewArticle(row.id)">
                                            <span class="title-text">{{ row.title }}</span>
                                            <el-tag v-if="row.status === 0" type="info" size="small" class="draft-tag">
                                                草稿
                                            </el-tag>
                                        </div>
                                        <div v-if="row.summary" class="article-summary">
                                            {{ row.summary }}
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>

                            <el-table-column prop="categoryName" label="分类" width="120">
                                <template #default="{ row }">
                                    <el-tag v-if="row.categoryName" size="small">
                                        {{ row.categoryName }}
                                    </el-tag>
                                    <span v-else class="no-category">未分类</span>
                                </template>
                            </el-table-column>

                            <el-table-column label="阅读/点赞/评论" width="180">
                                <template #default="{ row }">
                                    <div class="stats">
                                        <span class="stat-item">👁 {{ row.viewCount || 0 }}</span>
                                        <span class="stat-item">❤ {{ row.likeCount || 0 }}</span>
                                        <span class="stat-item">💬 {{ row.commentCount || 0 }}</span>
                                    </div>
                                </template>
                            </el-table-column>

                            <el-table-column prop="updateTime" label="更新时间" width="160">
                                <template #default="{ row }">
                                    {{ formatTime(row.updateTime || row.createTime) }}
                                </template>
                            </el-table-column>

                            <el-table-column label="操作" width="200" fixed="right">
                                <template #default="{ row }">
                                    <div class="action-buttons">
                                        <el-button link type="primary" @click="editArticle(row.id)" class="action-btn">
                                            编辑
                                        </el-button>
                                        <el-button link type="primary" @click="viewArticle(row.id)" class="action-btn">
                                            查看
                                        </el-button>
                                        <el-button link type="danger" @click="deleteArticle(row)" class="action-btn">
                                            删除
                                        </el-button>
                                    </div>
                                </template>
                            </el-table-column>
                        </el-table>
                    </div>

                    <!-- 空状态 -->
                    <div v-else class="empty-state">
                        <div class="empty-content">
                            <el-icon :size="60" color="#c0c4cc">
                                <Document />
                            </el-icon>
                            <h3>暂无文章</h3>
                            <p>您还没有写过任何文章，开始创作吧！</p>
                            <el-button type="primary" @click="$router.push('/article/create')" class="create-btn-empty">
                                <el-icon>
                                    <Plus />
                                </el-icon>
                                写第一篇文章
                            </el-button>
                        </div>
                    </div>
                </div>

                <!-- 分页 -->
                <div v-if="articles.length > 0 && !loading" class="pagination-wrapper">
                    <el-pagination :current-page="currentPage" :page-size="pageSize" :total="total"
                        :page-sizes="[10, 20, 30, 50]" layout="total, sizes, prev, pager, next, jumper"
                        @size-change="handleSizeChange" @current-change="handlePageChange"
                        @update:current-page="val => currentPage = val" @update:page-size="val => pageSize = val" />
                </div>
            </div>
        </div>

        <Footer />
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Plus,
  Delete,
  Document
} from '@element-plus/icons-vue'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'

const router = useRouter()

// Pinia Store
const articleStore = useArticleStore()
const userStore = useUserStore()

// 搜索和筛选
const searchKeyword = ref('')
const statusFilter = ref(-1)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 状态
const loading = ref(false)

// 文章列表
const articles = computed(() => articleStore.articles || [])

// 选中文章
const selectedArticles = ref([])

// 组件挂载
onMounted(async () => {
  // 初始化用户状态
  userStore.initFromStorage()
  
  // 检查登录状态
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/')
    return
  }
  
  // 加载用户文章
  await loadUserArticles()
})

// 监听分页和筛选变化
watch(
  [currentPage, pageSize, statusFilter],
  () => {
    loadUserArticles()
  }
)

// 加载用户文章
const loadUserArticles = async () => {
  try {
    loading.value = true
    
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    // 添加搜索关键词
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }
    
    // 添加状态筛选
    if (statusFilter.value !== null) {
      params.status = statusFilter.value
    }
    
    const result = await articleStore.fetchMyArticles(params)
    
    if (result) {
      total.value = result.total || 0
    }
    
  } catch (error) {
    console.error('加载用户文章失败:', error)
    ElMessage.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

// 搜索文章
const handleSearch = () => {
  currentPage.value = 1
  loadUserArticles()
}

// 筛选文章
const handleFilter = () => {
  currentPage.value = 1
  loadUserArticles()
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    // 今天，显示时间
    return date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 查看文章详情
const viewArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

// 编辑文章
const editArticle = (articleId) => {
  router.push(`/article/edit/${articleId}`)
}

// 删除文章
const deleteArticle = async (article) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文章 "${article.title}" 吗？删除后不可恢复。`,
      '提示',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    await articleStore.deleteArticle(article.id)
    ElMessage.success('文章删除成功')
    
    // 重新加载文章列表
    await loadUserArticles()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文章失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除文章
const handleBatchDelete = async () => {
  if (selectedArticles.value.length === 0) return
  
  try {
    const articleIds = selectedArticles.value.map(article => article.id)
    const articleTitles = selectedArticles.value.map(article => article.title).join('、')
    
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedArticles.value.length} 篇文章吗？删除后不可恢复。\n\n${articleTitles}`,
      '批量删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        dangerouslyUseHTMLString: true
      }
    )
    
    // 批量删除（需要后端支持批量删除接口）
    // 这里暂时使用循环单个删除
    for (const articleId of articleIds) {
      try {
        await articleStore.deleteArticle(articleId)
      } catch (error) {
        console.error(`删除文章 ${articleId} 失败:`, error)
      }
    }
    
    ElMessage.success(`成功删除 ${selectedArticles.value.length} 篇文章`)
    
    // 清空选中
    selectedArticles.value = []
    
    // 重新加载文章列表
    await loadUserArticles()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedArticles.value = selection
}

// 分页改变
const handlePageChange = (page) => {
  currentPage.value = page
}

// 每页数量改变
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 重置到第一页
}
</script>

<style scoped>
.user-articles-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background: #f8f9fa;
}

.articles-container {
    flex: 1;
    padding: 20px 0 40px;
}

.container {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
}

/* 页面头部 */
.page-header {
    margin-bottom: 30px;
}

.page-header h1 {
    font-size: 28px;
    color: #333;
    margin-bottom: 8px;
}

.page-subtitle {
    color: #666;
    font-size: 14px;
}

/* 工具栏 */
.toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.toolbar-left {
    display: flex;
    align-items: center;
    gap: 15px;
}

.search-input {
    width: 280px;
}

.status-select {
    width: 120px;
}

.toolbar-right {
    display: flex;
    gap: 10px;
}

.create-btn {
    display: flex;
    align-items: center;
    gap: 6px;
}

/* 文章列表 */
.articles-list {
    background: white;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    min-height: 400px;
}

/* 文章表格 */
.article-title-cell {
    cursor: pointer;
}

.title-content {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 6px;
}

.title-text {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.title-text:hover {
    color: #409eff;
}

.draft-tag {
    flex-shrink: 0;
}

.article-summary {
    font-size: 13px;
    color: #666;
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-clamp: 2;
}

.no-category {
    color: #999;
    font-size: 13px;
}

.stats {
    display: flex;
    gap: 15px;
}

.stat-item {
    font-size: 13px;
    color: #666;
}

.action-buttons {
    display: flex;
    gap: 12px;
}

.action-btn {
    padding: 4px 0;
    font-size: 13px;
}

/* 加载状态 */
.loading-state {
    padding: 40px;
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

.create-btn-empty {
    display: flex;
    align-items: center;
    gap: 6px;
    margin: 0 auto;
}

/* 分页 */
.pagination-wrapper {
    margin-top: 30px;
    display: flex;
    justify-content: center;
}

/* 响应式设计 */
@media (max-width: 992px) {
    .toolbar {
        flex-direction: column;
        align-items: stretch;
        gap: 15px;
    }

    .toolbar-left,
    .toolbar-right {
        width: 100%;
    }

    .search-input {
        width: 100%;
    }

    .action-buttons {
        flex-direction: column;
        gap: 8px;
    }

    .action-btn {
        padding: 2px 0;
    }
}

@media (max-width: 768px) {
    .stats {
        flex-direction: column;
        gap: 4px;
    }

    .page-header h1 {
        font-size: 24px;
    }
}
</style>