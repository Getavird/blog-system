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
            <router-link to="/categories">分类</router-link>
            <el-icon><ArrowRight /></el-icon>
            <span class="current">{{ categoryName }}</span>
          </div>
          
          <div class="category-info">
            <div class="category-title">
              <h1>
                <el-icon :color="categoryColor"><component :is="categoryIcon" /></el-icon>
                {{ categoryName }}
              </h1>
              <p class="category-description">{{ categoryDescription || '暂无描述' }}</p>
            </div>
            
            <div class="category-stats">
              <div class="stat-item" @click="currentPage = 1; loadCategoryData()">
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
              <div class="stat-item">
                <div class="stat-number">{{ formatNumber(commentCount) }}</div>
                <div class="stat-label">条评论</div>
              </div>
            </div>
          </div>
          
          <!-- 排序选项 -->
          <div class="sort-options">
            <el-radio-group v-model="sortBy" @change="handleSortChange">
              <el-radio-button label="createTime">
                <el-icon><Clock /></el-icon>
                最新
              </el-radio-button>
              <el-radio-button label="viewCount">
                <el-icon><View /></el-icon>
                热门
              </el-radio-button>
              <el-radio-button label="likeCount">
                <el-icon><Star /></el-icon>
                点赞
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- 文章列表 -->
        <div class="category-content">
          <ArticleList
            :articles="articles"
            :loading="loading"
            :show-cover="false"
            :show-summary="true"
            :show-author="true"
            :show-time="true"
            :show-views="true"
            :show-likes="true"
            :show-comments="true"
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
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'
import ArticleList from '@/components/article/ArticleList.vue'

// 图标导入
import { 
  ArrowRight, Clock, View, Star, 
  Folder, Document, Collection, Flag,
  Setting, Share, Help, ChatDotRound 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const categoryStore = useCategoryStore()
const articleStore = useArticleStore()
const userStore = useUserStore()

// 路由参数
const categoryId = ref(parseInt(route.params.id) || 0)

// 分页和排序
const currentPage = ref(1)
const pageSize = ref(10)
const sortBy = ref('createTime')

// 计算属性
const categoryInfo = ref({})
const categoryName = computed(() => {
  // 尝试从不同地方获取分类名称
  if (categoryInfo.value.name) return categoryInfo.value.name
  if (categoryInfo.value.categoryName) return categoryInfo.value.categoryName
  if (categoryInfo.value.category && categoryInfo.value.category.name) 
    return categoryInfo.value.category.name
  return '未知分类'
})

const categoryDescription = computed(() => {
  if (categoryInfo.value.description) return categoryInfo.value.description
  if (categoryInfo.value.category && categoryInfo.value.category.description)
    return categoryInfo.value.category.description
  return '暂无描述'
})
const loading = ref(false)
const articles = ref([])
const total = ref(0)

// 分类图标和颜色
const categoryIcon = computed(() => {
  const icons = [
    Document, Collection, Flag, Star, 
    Setting, Share, Help, ChatDotRound,
    Folder
  ]
  return icons[categoryId.value % icons.length]
})

const categoryColor = computed(() => {
  const colors = [
    '#409eff', '#67c23a', '#e6a23c', '#f56c6c',
    '#909399', '#ff69b4', '#9b30ff', '#00bfff',
    '#32cd32'
  ]
  return colors[categoryId.value % colors.length]
})

// 统计信息（从文章列表中计算）
const viewCount = computed(() => {
  return articles.value.reduce((sum, article) => sum + (article.viewCount || 0), 0)
})

const likeCount = computed(() => {
  return articles.value.reduce((sum, article) => sum + (article.likeCount || 0), 0)
})

const commentCount = computed(() => {
  return articles.value.reduce((sum, article) => sum + (article.commentCount || 0), 0)
})

// 数字格式化
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
    loading.value = true  
    
    // 1. 加载分类详情
    if (categoryId.value) {
      const detail = await categoryStore.fetchCategoryDetail(categoryId.value)
      console.log('分类详情:', detail)
      // 设置分类信息到 ref，不是 computed
      categoryInfo.value = detail.data || detail || {}
    }
    
    // 2. 构建请求参数
    const sortMapping = {
      createTime: 'latest',
      viewCount: 'hot',
      likeCount: 'likes'
    }
    
    const backendSort = sortMapping[sortBy.value] || 'latest'
    
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      sort: backendSort
    }
    
    console.log('请求参数:', params)
    
    // 3. 加载分类文章
    const result = await categoryStore.fetchCategoryArticles(categoryId.value, params)
    
    console.log('API返回结果:', result)
    
    // 4. 直接使用 result.articles 和 result.total
    const articlesData = result?.articles || []
    const totalCount = result?.total || 0
    
    console.log('文章数据:', articlesData)
    console.log('文章总数:', totalCount)
    
    // 设置到本地状态 - 使用 ref，不是 computed
    articles.value = articlesData
    total.value = totalCount
    
    // 如果需要，也设置到store
    if (articleStore && articleStore.setArticles) {
      articleStore.setArticles(articlesData)
      articleStore.total = totalCount
    }
    
  } catch (error) {
    console.error('加载分类数据失败:', error)
    ElMessage.error('加载失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 监听路由参数变化
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      categoryId.value = parseInt(newId)
      currentPage.value = 1
      articleStore.setArticles([]) // 清空旧文章
      loadCategoryData()
    }
  }
)

// 监听分页和排序变化
watch(
  [currentPage, sortBy],
  () => {
    if (categoryId.value) {
      loadCategoryData()
    }
  }
)

// 监听每页数量变化
watch(
  pageSize,
  () => {
    if (categoryId.value) {
      currentPage.value = 1
      loadCategoryData()
    }
  }
)

// 组件挂载
onMounted(() => {
  if (categoryId.value) {
    loadCategoryData()
  }
})

// 排序改变
const handleSortChange = () => {
  currentPage.value = 1
}

// 查看文章详情
const viewArticle = (article) => {
  const articleId = typeof article === 'object' ? article.id : article
  router.push(`/article/${articleId}`)
}

// 分页改变
const handlePageChange = (page) => {
  currentPage.value = page
}

// 每页数量改变
const handleSizeChange = (size) => {
  pageSize.value = size
}

// 跳转到写文章页面
const toWriteArticle = () => {
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
  gap: 12px;
  font-size: 28px;
  color: #333;
  margin-bottom: 15px;
}

.category-title h1 .el-icon {
  font-size: 32px;
}

.category-description {
  color: #666;
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
}

/* 分类统计 */
.category-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.sort-options {
  margin-bottom: 20px;
}

.sort-options .el-radio-group {
  width: 100%;
}

.sort-options .el-radio-button {
  flex: 1;
}

.sort-options .el-radio-button .el-icon {
  margin-right: 6px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-item:hover {
  background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
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