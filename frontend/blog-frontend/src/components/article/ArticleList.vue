<template>
    <div class="article-list">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
            <div v-for="n in 3" :key="n" class="skeleton-item">
                <el-skeleton :rows="3" animated />
            </div>
        </div>

        <!-- 文章列表 -->
        <div v-else-if="articles.length > 0" class="articles-container">
            <ArticleCard v-for="article in articles" :key="article.id" :article="article" :show-cover="showCover"
                :show-summary="showSummary" :show-author="showAuthor" :show-category="showCategory"
                :show-time="showTime" :show-views="showViews" :show-likes="showLikes" :show-comments="showComments"
                :show-tags="showTags" :show-status="showStatus" @click="handleArticleClick" class="article-item" />
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
            <div class="empty-content">
                <el-icon :size="60" color="#c0c4cc">
                    <Document />
                </el-icon>
                <h3>{{ emptyTitle }}</h3>
                <p>{{ emptyMessage }}</p>
                <slot name="empty-action">
                    <el-button v-if="showCreateButton" type="primary" @click="handleCreate">
                        创建文章
                    </el-button>
                </slot>
            </div>
        </div>

        <!-- 分页 -->
        <div v-if="showPagination && articles.length > 0 && !loading" class="pagination-wrapper">
            <el-pagination :current-page="currentPage" :page-size="pageSize" :total="total" :page-sizes="pageSizes"
                :layout="paginationLayout" @size-change="handleSizeChange" @current-change="handlePageChange"
                @update:current-page="val => emit('page-change', val)"
                @update:page-size="val => emit('size-change', val)" />
        </div>
    </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, watch } from 'vue'
import ArticleCard from './ArticleCard.vue'
import { Document } from '@element-plus/icons-vue'

const props = defineProps({
    articles: {
        type: Array,
        default: () => []
    },
    loading: {
        type: Boolean,
        default: false
    },
    // 显示选项
    showCover: {
        type: Boolean,
        default: false
    },
    showSummary: {
        type: Boolean,
        default: true
    },
    showAuthor: {
        type: Boolean,
        default: true
    },
    showCategory: {
        type: Boolean,
        default: false
    },
    showTime: {
        type: Boolean,
        default: true
    },
    showViews: {
        type: Boolean,
        default: true
    },
    showLikes: {
        type: Boolean,
        default: false
    },
    showComments: {
        type: Boolean,
        default: false
    },
    showTags: {
        type: Boolean,
        default: false
    },
    showStatus: {
        type: Boolean,
        default: false
    },
    // 分页配置
    showPagination: {
        type: Boolean,
        default: false
    },
    total: {
        type: Number,
        default: 0
    },
    currentPage: {
        type: Number,
        default: 1
    },
    pageSize: {
        type: Number,
        default: 10
    },
    pageSizes: {
        type: Array,
        default: () => [5, 10, 20, 50]
    },
    paginationLayout: {
        type: String,
        default: 'total, sizes, prev, pager, next'
    },
    // 空状态配置
    emptyTitle: {
        type: String,
        default: '暂无文章'
    },
    emptyMessage: {
        type: String,
        default: '这里还没有任何文章'
    },
    showCreateButton: {
        type: Boolean,
        default: false
    }
})

const emit = defineEmits([
    'article-click',
    'create-click',
    'size-change',
    'page-change'
])

// 处理文章点击
const handleArticleClick = (articleId) => {
    emit('article-click', articleId)
}

// 处理创建文章
const handleCreate = () => {
    emit('create-click')
}

// 处理分页大小变化
const handleSizeChange = (size) => {
    emit('size-change', size)
}

// 处理页码变化
const handlePageChange = (page) => {
    emit('page-change', page)
}
</script>

<style scoped>
.article-list {
    width: 100%;
}

.loading-state {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.skeleton-item {
    background: white;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.articles-container {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.article-item {
    margin-bottom: 0;
}

.empty-state {
    padding: 60px 20px;
    text-align: center;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.empty-content h3 {
    font-size: 18px;
    color: #333;
    margin: 15px 0 8px;
}

.empty-content p {
    color: #666;
    margin-bottom: 25px;
    font-size: 14px;
}

.pagination-wrapper {
    margin-top: 30px;
    display: flex;
    justify-content: center;
}

@media (max-width: 768px) {
    .pagination-wrapper {
        overflow-x: auto;
    }

    :deep(.el-pagination) {
        flex-wrap: wrap;
        justify-content: center;
    }
}
</style>