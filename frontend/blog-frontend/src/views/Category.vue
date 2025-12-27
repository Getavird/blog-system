<template>
  <div class="category-page">
    <Header />
    
    <div class="category-container">
      <div class="container">
        <!-- 分类头部 -->
        <div class="category-header">
          <div class="breadcrumb">
            <router-link to="/">首页</router-link>
            <el-icon><ArrowRight /></el-icon>
            <span class="current">{{ categoryName }} 分类</span>
          </div>
          
          <div class="category-info">
            <div class="category-title">
              <h1>
                <el-icon><Folder /></el-icon>
                {{ categoryName }}
              </h1>
              <p class="category-description">{{ categoryDescription }}</p>
            </div>
            
            <div class="category-stats">
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
          
          <!-- 排序选项 -->
          <div class="sort-options">
            <div 
              class="sort-item"
              :class="{ active: sortBy === 'createTime' }"
              @click="changeSort('createTime')"
            >
              <el-icon><Clock /></el-icon>
              最新
            </div>
            <div 
              class="sort-item"
              :class="{ active: sortBy === 'viewCount' }"
              @click="changeSort('viewCount')"
            >
              <el-icon><View /></el-icon>
              热门
            </div>
            <div 
              class="sort-item"
              :class="{ active: sortBy === 'likeCount' }"
              @click="changeSort('likeCount')"
            >
              <el-icon><Star /></el-icon>
              点赞
            </div>
          </div>
        </div>

        <!-- 文章列表 -->
        <div class="category-content">
          <ArticleList
            :articles="displayedArticles"  
            :loading="loading"
            :show-cover="false"
            :show-summary="true"
            :show-author="true"
            :show-time="true"
            :show-views="true"
            :show-likes="true"
            :show-pagination="true"
            :total="total"                
            :current-page="currentPage"
            :page-size="pageSize"
            @article-click="viewArticle"
            @page-change="handlePageChange"
            @size-change="handleSizeChange"
            :empty-title="'暂无文章'"
            :empty-message="`${categoryName} 分类下还没有文章，快去写一篇吧！`"
            :show-create-button="true"
            @create-click="toWriteArticle"
        />
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCategoryStore } from '@/stores/category'
import { useArticleStore } from '@/stores/article'
import { useUserStore } from '@/stores/user'  // ✅ 改为使用 userStore
import { ElMessage } from 'element-plus'
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'
import ArticleList from '@/components/article/ArticleList.vue'

// 路由和状态管理
const route = useRoute()
const router = useRouter()
const categoryStore = useCategoryStore()
const articleStore = useArticleStore()
const userStore = useUserStore()  // ✅ 改为 userStore

// 路由参数：分类ID
const categoryId = ref(parseInt(route.params.id) || 0)

// 分页和排序参数
const currentPage = ref(1)
const pageSize = ref(10)
const sortBy = ref('createTime') // 默认按创建时间排序

// 计算属性：从Pinia Store映射数据
const categoryName = computed(() => categoryStore.currentCategory?.name || '未知分类')
const categoryDescription = computed(() => categoryStore.currentCategory?.description || '')
const displayedArticles = computed(() => articleStore.articles || [])
const loading = computed(() => categoryStore.loading || articleStore.loading)

// 统计信息（需要后端接口支持，这里使用默认值）
const total = computed(() => {
  // 如果分类详情中有文章数量字段，使用它；否则使用文章列表的总数
  return categoryStore.currentCategory?.articleCount || 0
})

const viewCount = computed(() => {
  // 这里需要后端提供分类下的总浏览量，暂时使用文章列表的浏览量之和
  return displayedArticles.value.reduce((sum, article) => sum + (article.viewCount || 0), 0)
})

const likeCount = computed(() => {
  // 这里需要后端提供分类下的总点赞数，暂时使用文章列表的点赞数之和
  return displayedArticles.value.reduce((sum, article) => sum + (article.likeCount || 0), 0)
})

// 数字格式化函数
const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + '千'
  }
  return num
}

// 加载分类数据
const loadCategoryData = async () => {
  try {
    // 1. 加载分类详情
    if (categoryId.value) {
      await categoryStore.fetchCategoryDetail(categoryId.value)
    }
    
    // 2. 加载该分类下的文章
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      sort: sortBy.value
    }
    
    await categoryStore.fetchCategoryArticles(categoryId.value, params)
    
    // 将分类文章同步到文章Store以便其他组件使用
    articleStore.setArticles(categoryStore.currentCategory?.articles || [])
  } catch (error) {
    ElMessage.error('加载分类数据失败')
    console.error('加载分类数据失败:', error)
  }
}

// 监听路由参数变化（当用户直接改变URL时）
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      categoryId.value = parseInt(newId)
      currentPage.value = 1 // 重置到第一页
      loadCategoryData()
    }
  }
)

// 监听分页和排序变化
watch(
  [currentPage, pageSize, sortBy],
  () => {
    if (categoryId.value) {
      loadCategoryData()
    }
  }
)

// 组件挂载时加载数据
onMounted(() => {
  // ✅ 初始化用户状态
  userStore.initFromStorage()
  
  if (categoryId.value) {
    loadCategoryData()
  }
})

// 排序切换
const changeSort = (sortField) => {
  sortBy.value = sortField
}

// 查看文章详情
const viewArticle = (article) => {
  router.push(`/article/${article.id}`)
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

// 跳转到写文章页面
const toWriteArticle = () => {
  // 检查是否登录 - 使用 userStore.isLoggedIn()
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录后再发布文章')
    router.push('/')
    return
  }
  
  router.push('/article/create')
}
</script>

<style scoped>
.category-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.category-container {
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

/* 分类头部 */
.category-info {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.category-title {
  margin-bottom: 25px;
}

.category-title h1 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 28px;
  color: #333;
  margin-bottom: 12px;
}

.category-title h1 .el-icon {
  color: #409eff;
  font-size: 30px;
}

.category-description {
  color: #666;
  font-size: 16px;
  line-height: 1.6;
}

/* 分类统计 */
.category-stats {
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

/* 排序选项 */
.sort-options {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.sort-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #f8f9fa;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
  user-select: none;
}

.sort-item:hover {
  background: #e9ecef;
  color: #333;
}

.sort-item.active {
  background: #409eff;
  color: white;
}

.sort-item.active .el-icon {
  color: white;
}

/* 分类内容 */
.category-content {
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .category-info {
    padding: 20px;
  }
  
  .category-title h1 {
    font-size: 24px;
  }
  
  .category-stats {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .stat-item {
    min-width: 100px;
    flex: 1;
  }
  
  .sort-options {
    overflow-x: auto;
    padding: 10px;
  }
  
  .sort-item {
    white-space: nowrap;
  }
}
</style>