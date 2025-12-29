<template>
  <div class="tag-page">
    <Header />
    
    <div class="tag-container">
      <div class="container">
        <!-- 标签头部 -->
        <div class="tag-header">
          <div class="breadcrumb">
            <router-link to="/">首页</router-link>
            <el-icon><ArrowRight /></el-icon>
            <router-link to="/tags">标签</router-link>
            <el-icon><ArrowRight /></el-icon>
            <span class="current">#{{ tagName }}</span>
          </div>
          
          <div class="tag-info">
            <div class="tag-title">
              <h1>
                <el-icon><PriceTag /></el-icon>
                #{{ tagName }}
              </h1>
              <p class="tag-description">{{ tagDescription }}</p>
            </div>
            
            <div class="tag-stats">
              <div class="stat-item" @click="currentPage = 1; loadTagArticles()">
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
        <div class="tag-content">
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
            :show-tags="false"
            :show-pagination="true"
            :total="total"
            :current-page="currentPage"
            :page-size="pageSize"
            @article-click="viewArticle"
            @page-change="handlePageChange"
            @size-change="handleSizeChange"
            :empty-title="'暂无文章'"
            :empty-message="`标签 #${tagName} 下还没有文章`"
            :show-create-button="false"
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
import { useTagStore } from '@/stores/tag'
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
const tagStore = useTagStore()
const articleStore = useArticleStore()
const userStore = useUserStore()

// 路由参数（支持ID或名称）
const tagId = ref(parseInt(route.params.id) || 0)
const tagName = ref(route.params.name || '')

// 本地状态管理 - 使用 ref
const tagInfo = ref({})
const articles = ref([])
const total = ref(0)
const loading = ref(false)

// 分页和排序 - 使用 ref
const currentPage = ref(1)
const pageSize = ref(10)
const sortBy = ref('createTime')

// 计算属性 - 从 ref 派生
const displayTagName = computed(() => {
  if (tagInfo.value.name) return tagInfo.value.name
  if (tagInfo.value.tagName) return tagInfo.value.tagName
  return tagName.value || '未知标签'
})

const tagDescription = computed(() => {
  if (tagInfo.value.description) return tagInfo.value.description
  return '暂无描述'
})

// 标签图标和颜色
const tagIcon = computed(() => {
  const icons = [
    Collection, Flag, Star, Document,
    Setting, Share, Help, ChatDotRound,
    Folder
  ]
  return icons[tagId.value % icons.length]
})

const tagColor = computed(() => {
  const colors = [
    '#409eff', '#67c23a', '#e6a23c', '#f56c6c',
    '#909399', '#ff69b4', '#9b30ff', '#00bfff',
    '#32cd32', '#ff4500'
  ]
  return colors[tagId.value % colors.length]
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

// 加载标签数据
const loadTagData = async () => {
  try {
    loading.value = true
    
    // 1. 加载标签详情
    if (tagName.value) {
      // 优先使用标签名称获取详情
      const detail = await tagStore.fetchTagDetailByName(tagName.value)
      console.log('标签详情:', detail)
      tagInfo.value = detail.data || detail || {}
    } else if (tagId.value) {
      // 如果传的是ID，使用ID获取详情
      const detail = await tagStore.fetchTagDetail(tagId.value)
      console.log('标签详情:', detail)
      tagInfo.value = detail.data || detail || {}
    }
    
    // 2. 加载标签文章
    await loadTagArticles()
    
  } catch (error) {
    console.error('加载标签数据失败:', error)
    ElMessage.error('加载标签数据失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 加载标签文章
const loadTagArticles = async () => {
  try {
    // 映射排序参数
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
    
    console.log('加载标签文章，参数:', params)
    
    let result
    if (tagName.value) {
      // 使用标签名称获取文章
      result = await tagStore.fetchTagArticlesByName(tagName.value, params)
    } else if (tagId.value) {
      // 使用标签ID获取文章
      result = await tagStore.fetchTagArticles(tagId.value, params)
    }
    
    console.log('标签文章返回:', result)
    
    // 处理返回数据
    if (result) {
      let articlesData = []
      let totalCount = 0
      
      // 尝试多种可能的返回结构
      if (result.articles !== undefined) {
        articlesData = result.articles || []
        totalCount = result.total || 0
      } else if (result.data && result.data.articles !== undefined) {
        articlesData = result.data.articles || []
        totalCount = result.data.total || 0
      } else if (result.data && result.data.data && result.data.data.articles !== undefined) {
        articlesData = result.data.data.articles || []
        totalCount = result.data.data.total || 0
      } else if (Array.isArray(result)) {
        articlesData = result
        totalCount = result.length
      } else if (Array.isArray(result.data)) {
        articlesData = result.data
        totalCount = result.data.length
      }
      
      console.log('提取的文章数据:', articlesData)
      console.log('文章总数:', totalCount)
      
      // 设置到本地状态
      articles.value = articlesData
      total.value = totalCount
      
      // 设置到store
      articleStore.setArticles(articlesData)
      articleStore.total = totalCount
    }
  } catch (error) {
    console.error('加载标签文章失败:', error)
    throw error
  }
}

// 监听路由参数变化
watch(
  () => route.params,
  (newParams) => {
    if (newParams.name) {
      tagName.value = newParams.name
      tagId.value = 0 // 重置ID
      currentPage.value = 1
      articles.value = [] // 清空旧文章
      tagInfo.value = {} // 清空旧标签信息
      loadTagData()
    } else if (newParams.id) {
      tagId.value = parseInt(newParams.id)
      tagName.value = '' // 重置名称
      currentPage.value = 1
      articles.value = []
      tagInfo.value = {}
      loadTagData()
    }
  }
)

// 监听分页和排序变化
watch(
  [currentPage, sortBy],
  () => {
    if (tagName.value || tagId.value) {
      loadTagArticles()
    }
  }
)

// 监听每页数量变化
watch(
  pageSize,
  () => {
    if (tagName.value || tagId.value) {
      currentPage.value = 1
      loadTagArticles()
    }
  }
)

// 组件挂载
onMounted(() => {
  if (tagName.value || tagId.value) {
    loadTagData()
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
.tag-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.tag-container {
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

/* 标签头部 */
.tag-info {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.tag-title {
  margin-bottom: 25px;
}

.tag-title h1 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 28px;
  color: #333;
  margin-bottom: 12px;
}

.tag-title h1 .el-icon {
  color: #67c23a;
  font-size: 30px;
}

.tag-description {
  color: #666;
  font-size: 16px;
  line-height: 1.6;
}

/* 标签统计 */
.tag-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  padding-top: 20px;
  border-top: 1px solid #eee;
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

.stat-item:hover .stat-number {
  color: #67c23a;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 5px;
  transition: color 0.3s;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

/* 排序选项 */
.sort-options {
  margin-bottom: 20px;
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
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

/* 标签内容 */
.tag-content {
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tag-info {
    padding: 20px;
  }
  
  .tag-title h1 {
    font-size: 24px;
  }
  
  .tag-stats {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
  
  .stat-item {
    padding: 12px;
  }
  
  .stat-number {
    font-size: 22px;
  }
  
  .sort-options .el-radio-button {
    flex: none;
    width: 100%;
    margin-bottom: 5px;
  }
  
  .sort-options .el-radio-group {
    display: flex;
    flex-direction: column;
  }
}
</style>