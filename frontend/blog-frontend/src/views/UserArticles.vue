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
                            <el-option label="全部" :value="null" />
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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
    Search,
    Plus,
    Delete,
    Document
} from '@element-plus/icons-vue'
import Header from '../components/layout/Header.vue'
import Footer from '../components/layout/Footer.vue'

const router = useRouter()

// 状态
const loading = ref(true)
const articles = ref([])
const searchKeyword = ref('')
const statusFilter = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedArticles = ref([])

// 模拟用户ID（实际应该从登录状态获取）
const currentUserId = 1

// 模拟文章数据
const mockArticles = [
    {
        id: 1,
        title: 'Vue 3 新特性详解',
        summary: '深入解析 Vue 3 的新特性和使用技巧，带你快速上手 Vue 3 开发...',
        categoryName: '技术',
        status: 1, // 已发布
        viewCount: 320,
        likeCount: 42,
        commentCount: 12,
        createTime: '2024-01-14 14:20:00',
        updateTime: '2024-01-15 09:30:00',
        authorId: 1
    },
    {
        id: 2,
        title: 'Spring Boot入门教程',
        summary: '详细介绍Spring Boot的基本使用和配置，快速上手后端开发...',
        categoryName: '技术',
        status: 1,
        viewCount: 156,
        likeCount: 25,
        commentCount: 8,
        createTime: '2024-01-15 10:30:00',
        updateTime: '2024-01-15 10:30:00',
        authorId: 1
    },
    {
        id: 3,
        title: '数据库设计规范',
        summary: '分享数据库设计的最佳实践和规范，让你的数据架构更合理...',
        categoryName: '技术',
        status: 0, // 草稿
        viewCount: 0,
        likeCount: 0,
        commentCount: 0,
        createTime: '2024-01-13 09:15:00',
        updateTime: '2024-01-16 14:45:00',
        authorId: 1
    },
    {
        id: 4,
        title: '我的学习笔记',
        summary: '记录最近学习的一些心得体会和技术要点...',
        categoryName: '学习',
        status: 0,
        viewCount: 0,
        likeCount: 0,
        commentCount: 0,
        createTime: '2024-01-16 16:20:00',
        updateTime: '2024-01-16 16:20:00',
        authorId: 1
    }
]

// 计算属性
const filteredArticles = computed(() => {
    let result = [...articles.value]

    // 状态筛选
    if (statusFilter.value !== null) {
        result = result.filter(article => article.status === statusFilter.value)
    }

    // 搜索筛选
    if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        result = result.filter(article =>
            article.title.toLowerCase().includes(keyword) ||
            (article.summary && article.summary.toLowerCase().includes(keyword))
        )
    }

    // 分页
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value

    return result.slice(start, end)
})

// 生命周期
onMounted(() => {
    loadArticles()
})

// 方法
const loadArticles = () => {
    loading.value = true

    // 模拟API调用
    setTimeout(() => {
        // 过滤当前用户的文章
        articles.value = mockArticles.filter(article => article.authorId === currentUserId)
        total.value = articles.value.length
        loading.value = false
    }, 800)
}

const formatTime = (time) => {
    if (!time) return ''
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
    } else {
        return date.toLocaleDateString('zh-CN')
    }
}

const handleSearch = () => {
    currentPage.value = 1
    // 这里可以调用API搜索
    console.log('搜索关键词:', searchKeyword.value)
}

const handleFilter = () => {
    currentPage.value = 1
    // 这里可以调用API筛选
    console.log('筛选状态:', statusFilter.value)
}

const handleSelectionChange = (selection) => {
    selectedArticles.value = selection
}

const viewArticle = (articleId) => {
    router.push(`/article/${articleId}`)
}

const editArticle = (articleId) => {
    router.push(`/article/edit/${articleId}`)
}

const deleteArticle = async (article) => {
    try {
        const confirm = await ElMessageBox.confirm(
            `确定要删除文章 "${article.title}" 吗？此操作不可恢复。`,
            '删除确认',
            {
                confirmButtonText: '确定删除',
                cancelButtonText: '取消',
                type: 'warning'
            }
        ).catch(() => false)

        if (!confirm) return

        // 模拟删除
        const index = articles.value.findIndex(a => a.id === article.id)
        if (index !== -1) {
            articles.value.splice(index, 1)
            total.value = articles.value.length
            ElMessage.success('文章删除成功')
        }
    } catch (error) {
        ElMessage.error('删除失败')
    }
}

const handleBatchDelete = async () => {
    if (selectedArticles.value.length === 0) return

    try {
        const confirm = await ElMessageBox.confirm(
            `确定要删除选中的 ${selectedArticles.value.length} 篇文章吗？此操作不可恢复。`,
            '批量删除确认',
            {
                confirmButtonText: '确定删除',
                cancelButtonText: '取消',
                type: 'warning'
            }
        ).catch(() => false)

        if (!confirm) return

        // 模拟批量删除
        const selectedIds = selectedArticles.value.map(article => article.id)
        articles.value = articles.value.filter(article => !selectedIds.includes(article.id))
        total.value = articles.value.length
        selectedArticles.value = []

        ElMessage.success(`成功删除 ${selectedIds.length} 篇文章`)
    } catch (error) {
        ElMessage.error('删除失败')
    }
}

const handlePageChange = (page) => {
    currentPage.value = page
    // 这里可以调用API获取对应页的数据
}

const handleSizeChange = (size) => {
    pageSize.value = size
    currentPage.value = 1
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