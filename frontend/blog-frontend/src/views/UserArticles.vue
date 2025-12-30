<template>
    <div class="user-articles-page">
        <Header />

        <div class="articles-container">
            <div class="container">
                <!-- 页面标题 -->
                <div class="page-header">
                    <h1>我的文章</h1>
                    <p class="page-subtitle">管理您的文章，包括草稿和已发布的文章</p>

                    <!-- 快速统计 -->
                    <div class="quick-stats">
                        <div class="stat-item">
                            <div class="stat-number">{{ displayTotal }}</div>
                            <div class="stat-label">文章总数</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-number">{{ publishedCount }}</div>
                            <div class="stat-label">已发布</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-number">{{ draftCount }}</div>
                            <div class="stat-label">草稿</div>
                        </div>
                    </div>

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

                        <el-select v-model="categoryFilter" placeholder="分类" @change="handleFilter"
                            class="category-select" clearable>
                            <el-option v-for="category in userCategories" :key="category.id" :label="category.name"
                                :value="category.id" />
                        </el-select>
                    </div>

                    <div class="toolbar-right">
                        <el-button type="primary" @click="createArticle" class="create-btn">
                            <el-icon>
                                <Plus />
                            </el-icon>
                            写新文章
                        </el-button>

                        <el-dropdown v-if="selectedArticles.length > 0" @command="handleBatchCommand">
                            <el-button class="batch-btn">
                                <el-icon>
                                    <More />
                                </el-icon>
                                批量操作
                            </el-button>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item command="publish" :disabled="!canBatchPublish">
                                        <el-icon>
                                            <CircleCheck />
                                        </el-icon>批量发布
                                    </el-dropdown-item>
                                    <el-dropdown-item command="delete">
                                        <el-icon>
                                            <Delete />
                                        </el-icon>批量删除
                                    </el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
                </div>

                <!-- 文章列表 -->
                <div class="articles-list">
                    <!-- 加载状态 -->
                    <div v-if="loading" class="loading-state">
                        <div class="loading-content">
                            <el-icon class="loading-icon">
                                <Loading />
                            </el-icon>
                            <p>加载中...</p>
                        </div>
                    </div>

                    <!-- 文章表格 -->
                    <div v-else-if="displayArticles.length > 0" class="articles-table">
                        <el-table :data="displayArticles" style="width: 100%" @selection-change="handleSelectionChange"
                            :row-key="row => row.id" v-loading="loading">
                            <el-table-column type="selection" width="55" />

                            <el-table-column label="文章标题" min-width="300">
                                <template #default="{ row }">
                                    <div class="article-title-cell">
                                        <div class="title-content" @click="viewArticle(row.id)">
                                            <span class="title-text">{{ row.title }}</span>

                                            <el-tag v-if="row.status === 0" type="info" size="small" class="draft-tag">
                                                草稿
                                            </el-tag>
                                            <el-tag v-else-if="row.status === 1" type="success" size="small">
                                                已发布
                                            </el-tag>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>

                            <el-table-column label="分类" width="120">
                                <template #default="{ row }">
                                    <el-tag v-if="row.categoryName" size="small">
                                        {{ row.categoryName }}
                                    </el-tag>
                                    <span v-else class="no-category">未分类</span>
                                </template>
                            </el-table-column>

                            <el-table-column label="统计" width="180">
                                <template #default="{ row }">
                                    <div class="stats">
                                        <el-tooltip content="阅读数" placement="top">
                                            <span class="stat-item">
                                                <el-icon>
                                                    <View />
                                                </el-icon>
                                                {{ row.viewCount || 0 }}
                                            </span>
                                        </el-tooltip>
                                        <el-tooltip content="点赞数" placement="top">
                                            <span class="stat-item">
                                                <el-icon>
                                                    <Star />
                                                </el-icon>
                                                {{ row.likeCount || 0 }}
                                            </span>
                                        </el-tooltip>
                                        <el-tooltip content="评论数" placement="top">
                                            <span class="stat-item">
                                                <el-icon>
                                                    <ChatDotRound />
                                                </el-icon>
                                                {{ row.commentCount || 0 }}
                                            </span>
                                        </el-tooltip>
                                    </div>
                                </template>
                            </el-table-column>

                            <el-table-column label="更新时间" width="160">
                                <template #default="{ row }">
                                    {{ formatTime(row.updateTime || row.createTime) }}
                                </template>
                            </el-table-column>

                            <el-table-column label="操作" width="240" fixed="right">
                                <template #default="{ row }">
                                    <div class="action-buttons">
                                        <el-button link type="primary" @click="editArticle(row.id)" class="action-btn">
                                            编辑
                                        </el-button>
                                        <el-button link type="primary" @click="viewArticle(row.id)" class="action-btn">
                                            查看
                                        </el-button>
                                        <el-button v-if="row.status === 0" link type="success"
                                            @click="publishArticle(row)" class="action-btn">
                                            发布
                                        </el-button>
                                        <el-dropdown @command="(command) => handleMoreAction(row, command)">
                                            <el-button link type="primary" class="more-btn">
                                                更多
                                            </el-button>
                                            <template #dropdown>
                                                <el-dropdown-menu>
                                                    <el-dropdown-item command="copy">复制链接</el-dropdown-item>
                                                    <el-dropdown-item command="stats">查看统计</el-dropdown-item>
                                                    <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                                                </el-dropdown-menu>
                                            </template>
                                        </el-dropdown>
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
                            <p v-if="searchKeyword || statusFilter !== -1 || categoryFilter">
                                没有找到符合条件的文章
                            </p>
                            <p v-else>您还没有写过任何文章，开始创作吧！</p>
                            <el-button type="primary" @click="createArticle" class="create-btn-empty">
                                <el-icon>
                                    <Plus />
                                </el-icon>
                                写第一篇文章
                            </el-button>
                        </div>
                    </div>
                </div>

                <!-- 分页 -->
                <div v-if="displayTotal > 0 && !loading" class="pagination-wrapper">
                    <el-pagination :current-page="currentPage" :page-size="pageSize" :total="displayTotal"
                        :page-sizes="[10, 20, 30, 50]" layout="total, sizes, prev, pager, next, jumper"
                        @size-change="handleSizeChange" @current-change="handlePageChange" />
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
import { useCategoryStore } from '@/stores/category'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
    Search,
    Plus,
    Delete,
    Document,
    More,
    View,
    Star,
    ChatDotRound,
    Loading,
    CircleCheck
} from '@element-plus/icons-vue'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'

const router = useRouter()

// Pinia Store
const articleStore = useArticleStore()
const userStore = useUserStore()
const categoryStore = useCategoryStore()

// 搜索和筛选
const searchKeyword = ref('')
const statusFilter = ref(-1)
const categoryFilter = ref('')

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)


const displayArticles = ref([])
const displayTotal = ref(0)

// 状态
const loading = ref(false)

// 选中文章
const selectedArticles = ref([])

// 计算属性
const publishedCount = computed(() => {
    return articleStore.myArticles.total || 0
})

const draftCount = computed(() => {
    return articleStore.myDrafts.total || 0
})

const userCategories = computed(() => {
    return categoryStore.categories || []
})

// 是否可以批量发布（选中的都是草稿）
const canBatchPublish = computed(() => {
    if (selectedArticles.value.length === 0) return false
    return selectedArticles.value.every(article => article.status === 0)
})

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

    // 加载分类数据
    await categoryStore.fetchCategories()

    // 加载用户文章
    await loadUserArticles()
})

// 监听分页和筛选变化
watch(
    [currentPage, pageSize, statusFilter, categoryFilter],
    () => {
        // 重置到第一页
        if (!searchKeyword.value) {
            loadUserArticles()
        }
    }
)

// 监听搜索关键词变化（带防抖）
let searchTimer = null
watch(
    searchKeyword,
    () => {
        if (searchTimer) clearTimeout(searchTimer)
        searchTimer = setTimeout(() => {
            currentPage.value = 1
            loadUserArticles()
        }, 500)
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

        // 添加分类筛选
        if (categoryFilter.value) {
            params.categoryId = categoryFilter.value
        }

        console.log('📊 加载文章参数:', params)
        console.log('🔍 当前筛选状态:', statusFilter.value)

        let result = null

        // 根据状态筛选调用不同的 API
        if (statusFilter.value === 0) {
            // 草稿：调用草稿接口
            console.log('🔄 加载草稿...')
            result = await articleStore.fetchMyDrafts(params)

            // ✅ 正确赋值：使用响应式引用
            displayArticles.value = articleStore.myDrafts.list || []
            displayTotal.value = articleStore.myDrafts.total || 0

            console.log('📝 草稿数据:', {
                storeData: articleStore.myDrafts,
                displayArticles: displayArticles.value.length,
                displayTotal: displayTotal.value
            })

        } else if (statusFilter.value === 1) {
            // 已发布：调用发布文章接口
            console.log('🔄 加载已发布文章...')
            result = await articleStore.fetchMyArticles(params)

            // ✅ 正确赋值：使用响应式引用
            displayArticles.value = articleStore.myArticles.list || []
            displayTotal.value = articleStore.myArticles.total || 0

        } else {
            // 全部：分别获取然后合并
            console.log('🔄 加载全部文章...')

            try {
                // 分别获取草稿和已发布文章
                const draftsPromise = articleStore.fetchMyDrafts({ page: 1, size: 1000 })
                const publishedPromise = articleStore.fetchMyArticles({ page: 1, size: 1000 })

                const [draftsResult, publishedResult] = await Promise.all([draftsPromise, publishedPromise])

                console.log('📊 合并前的数据:', {
                    草稿: articleStore.myDrafts.list,
                    草稿数量: articleStore.myDrafts.list?.length,
                    已发布: articleStore.myArticles.list,
                    已发布数量: articleStore.myArticles.list?.length
                })

                // 合并数据
                const allArticles = [
                    ...(articleStore.myDrafts.list || []),
                    ...(articleStore.myArticles.list || [])
                ]

                console.log('📊 合并后的所有文章:', allArticles)

                // 前端过滤
                let filteredArticles = allArticles

                if (searchKeyword.value.trim()) {
                    const keyword = searchKeyword.value.trim().toLowerCase()
                    filteredArticles = filteredArticles.filter(article =>
                        article.title && article.title.toLowerCase().includes(keyword)
                    )
                }

                if (categoryFilter.value) {
                    filteredArticles = filteredArticles.filter(article =>
                        article.categoryId == categoryFilter.value
                    )
                }

                console.log('🎯 过滤后的文章:', filteredArticles)

                // 前端分页
                const startIndex = (currentPage.value - 1) * pageSize.value
                const endIndex = startIndex + pageSize.value

                displayArticles.value = filteredArticles.slice(startIndex, endIndex)
                displayTotal.value = filteredArticles.length

                console.log('✅ 最终显示的文章:', {
                    总数: displayTotal.value,
                    当前页数据: displayArticles.value.length,
                    数据: displayArticles.value
                })

            } catch (error) {
                console.error('合并文章数据失败:', error)
                displayArticles.value = []
                displayTotal.value = 0
            }

            return
        }
        // 如果当前页没有数据且不是第一页，回到上一页
        if (displayArticles.value.length === 0 && currentPage.value > 1) {
            currentPage.value = Math.max(1, currentPage.value - 1)
            await loadUserArticles()
        }

    } catch (error) {
        console.error('加载用户文章失败:', error)
        ElMessage.error('加载文章失败: ' + (error.message || '未知错误'))
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

    try {
        const date = new Date(time)
        const now = new Date()
        const diff = now.getTime() - date.getTime()
        const days = Math.floor(diff / (1000 * 60 * 60 * 24))

        if (isNaN(date.getTime())) {
            return '无效日期'
        }

        if (days === 0) {
            // 今天，显示时间
            return date.toLocaleTimeString('zh-CN', {
                hour: '2-digit',
                minute: '2-digit'
            })
        } else if (days === 1) {
            return '昨天 ' + date.toLocaleTimeString('zh-CN', {
                hour: '2-digit',
                minute: '2-digit'
            })
        } else if (days < 7) {
            return `${days}天前`
        } else if (days < 30) {
            return `${Math.floor(days / 7)}周前`
        } else {
            return date.toLocaleDateString('zh-CN')
        }
    } catch (error) {
        console.error('格式化时间失败:', time, error)
        return ''
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

// 创建文章
const createArticle = () => {
    router.push('/article/create')
}

// 发布草稿
const publishArticle = async (article) => {
    try {
        await ElMessageBox.confirm(
            `确定要发布文章 "${article.title}" 吗？`,
            '发布确认',
            {
                type: 'warning',
                confirmButtonText: '确定发布',
                cancelButtonText: '取消'
            }
        )

        // 调用articleStore的发布方法
        await articleStore.publishDraft(article.id)
        ElMessage.success('文章发布成功')

        // 重新加载文章列表
        await loadUserArticles()

    } catch (error) {
        if (error !== 'cancel') {
            console.error('发布文章失败:', error)
            ElMessage.error(error.message || '发布失败')
        }
    }
}

// 删除文章
const deleteArticle = async (article) => {
    try {
        await ElMessageBox.confirm(
            `确定要${article.status === 0 ? '删除草稿' : '软删除文章'} "${article.title}" 吗？`,
            '提示',
            {
                type: 'warning',
                confirmButtonText: '确定',
                cancelButtonText: '取消'
            }
        )

        console.log('🗑️ 执行软删除:', article)

        // ✅ 使用软删除（更新状态为2）
        // 我们需要调用updateArticle来将状态改为2
        const updateData = {
            ...article,
            status: 2, // 软删除状态
            // 确保所有必需字段都有值
            title: article.title || '无标题',
            content: article.content || '',
            summary: article.summary || '',
            coverImage: article.coverImage || null,
            categoryId: article.categoryId || 0,
            isTop: article.isTop || 0,
            allowComment: article.allowComment || 1,
            tags: article.tags || '',
            likeCount: article.likeCount || 0
        }

        console.log('📤 软删除更新数据:', updateData)

        await articleStore.updateArticle(article.id, updateData)

        ElMessage.success(article.status === 0 ? '草稿已删除' : '文章已删除')

        // 重新加载文章列表
        await loadUserArticles()

    } catch (error) {
        if (error !== 'cancel') {
            console.error('删除文章失败:', error)
            ElMessage.error('删除失败: ' + (error.message || '未知错误'))
        }
    }
}

// 批量操作
const handleBatchCommand = async (command) => {
    if (selectedArticles.value.length === 0) return

    try {
        if (command === 'delete') {
            const articleIds = selectedArticles.value.map(article => article.id)
            const articleTitles = selectedArticles.value.map(article => article.title).join('、')

            await ElMessageBox.confirm(
                `确定要软删除选中的 ${selectedArticles.value.length} 篇文章吗？`,
                '批量软删除确认',
                {
                    type: 'warning',
                    confirmButtonText: '确定删除',
                    cancelButtonText: '取消',
                    dangerouslyUseHTMLString: true
                }
            )

            // 批量软删除
            let successCount = 0
            for (const article of selectedArticles.value) {
                try {
                    // 使用软删除
                    const updateData = {
                        ...article,
                        status: 2, // 软删除状态
                        title: article.title || '无标题',
                        content: article.content || '',
                        summary: article.summary || '',
                        categoryId: article.categoryId || 0
                    }

                    await articleStore.updateArticle(article.id, updateData)
                    successCount++
                } catch (error) {
                    console.error(`软删除文章 ${article.id} 失败:`, error)
                }
            }

            ElMessage.success(`成功软删除 ${successCount} 篇文章`)

            // 清空选中
            selectedArticles.value = []

            // 重新加载文章列表
            await loadUserArticles()
        }
        // ... 其他批量操作
    } catch (error) {
        if (error !== 'cancel') {
            console.error('批量操作失败:', error)
            ElMessage.error('操作失败')
        }
    }
}

// 更多操作
const handleMoreAction = async (article, command) => {
    switch (command) {
        case 'copy':
            // 复制文章链接
            const url = `${window.location.origin}/article/${article.id}`
            try {
                await navigator.clipboard.writeText(url)
                ElMessage.success('链接已复制到剪贴板')
            } catch (error) {
                console.error('复制失败:', error)
                ElMessage.error('复制失败')
            }
            break

        case 'stats':
            // 查看统计（可以跳转到统计页面）
            router.push(`/article/${article.id}/stats`)
            break

        case 'delete':
            await deleteArticle(article)
            break
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
    margin-bottom: 20px;
}

/* 快速统计 */
.quick-stats {
    display: flex;
    gap: 20px;
    margin-bottom: 30px;
}

.stat-item {
    background: white;
    border-radius: 8px;
    padding: 15px 20px;
    min-width: 100px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s;
    cursor: pointer;
}

.stat-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.stat-number {
    font-size: 24px;
    font-weight: 700;
    color: #409eff;
    margin-bottom: 5px;
}

.stat-label {
    color: #666;
    font-size: 13px;
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

.status-select,
.category-select {
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

.batch-btn {
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
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #666;
    cursor: default;
}

.action-buttons {
    display: flex;
    gap: 8px;
    align-items: center;
}

.action-btn {
    padding: 4px 0;
    font-size: 13px;
}

.more-btn {
    padding: 4px 0;
    font-size: 13px;
}

/* 加载状态 */
.loading-state {
    padding: 80px 20px;
    text-align: center;
}

.loading-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 15px;
}

.loading-icon {
    font-size: 40px;
    color: #409eff;
    animation: rotate 1s linear infinite;
}

@keyframes rotate {
    from {
        transform: rotate(0deg);
    }

    to {
        transform: rotate(360deg);
    }
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

    .toolbar-left {
        flex-wrap: wrap;
    }

    .search-input {
        width: 100%;
    }

    .quick-stats {
        flex-wrap: wrap;
    }

    .stat-item {
        flex: 1;
        min-width: calc(50% - 10px);
    }

    .action-buttons {
        flex-wrap: wrap;
        gap: 6px;
    }

    .action-btn,
    .more-btn {
        padding: 2px 0;
        font-size: 12px;
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

    .quick-stats {
        gap: 10px;
    }

    .stat-item {
        min-width: calc(100% - 10px);
        padding: 12px 15px;
    }
}

/* 表格悬停效果 */
:deep(.el-table__row:hover) {
    background-color: #f5f7fa !important;
}

:deep(.el-table__row:hover .title-text) {
    color: #409eff;
}
</style>