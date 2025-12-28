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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTagStore } from '@/stores/tag'
import { useArticleStore } from '@/stores/article'
import { ElMessage } from 'element-plus'
import { ArrowRight, PriceTag, Clock, View, Star } from '@element-plus/icons-vue'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'
import ArticleList from '@/components/article/ArticleList.vue'

const route = useRoute()
const router = useRouter()

// Pinia Stores
const tagStore = useTagStore()
const articleStore = useArticleStore()

// 路由参数
const tagName = ref('')
const originalTagName = ref('') // 保存原始标签名用于API调用

// 分页和排序
const currentPage = ref(1)
const pageSize = ref(10)
const sortBy = ref('createTime')

// 状态
const loading = ref(false)
const tagData = ref(null)

// 安全解码URL参数
const safeDecodeURI = (str) => {
  try {
    return decodeURIComponent(str)
  } catch (error) {
    console.warn('URL解码失败:', str, error)
    return str
  }
}

// 标签详情
const tagDescription = computed(() => tagData.value?.description || '')

// 文章列表
const articles = computed(() => articleStore.articles || [])
const total = computed(() => articleStore.total || 0)

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
  if (!num && num !== 0) return 0
  
  const number = parseInt(num)
  if (isNaN(number)) return 0
  
  if (number >= 1000000) {
    return (number / 1000000).toFixed(1) + '百万'
  }
  if (number >= 10000) {
    return (number / 10000).toFixed(1) + '万'
  }
  if (number >= 1000) {
    return (number / 1000).toFixed(1) + '千'
  }
  return number.toString()
}

// 组件挂载
onMounted(async () => {
  await initializeTagPage()
})

// 初始化标签页面
const initializeTagPage = async () => {
  const routeName = route.params.name
  if (!routeName) {
    ElMessage.warning('标签名称不能为空')
    router.push('/tags')
    return
  }
  
  try {
    // 安全解码标签名
    const decodedName = safeDecodeURI(routeName)
    tagName.value = decodedName
    originalTagName.value = decodedName
    
    await loadTagData()
  } catch (error) {
    console.error('初始化标签页面失败:', error)
    ElMessage.error('加载标签页面失败')
    router.push('/tags')
  }
}

// 监听路由参数变化
watch(
  () => route.params.name,
  async (newName) => {
    if (newName) {
      const decodedName = safeDecodeURI(newName)
      tagName.value = decodedName
      originalTagName.value = decodedName
      currentPage.value = 1 // 重置分页
      await loadTagData()
    }
  }
)

// 加载标签数据
const loadTagData = async () => {
  try {
    loading.value = true
    
    // 1. 获取标签详情（通过标签名称）
    const detailResult = await tagStore.fetchTagDetailByName(tagName.value)
    
    if (!detailResult || detailResult.error) {
      throw new Error('标签不存在或获取失败')
    }
    
    tagData.value = detailResult
    
    // 2. 获取标签下的文章
    await loadTagArticles()
    
  } catch (error) {
    console.error('加载标签数据失败:', error)
    ElMessage.error(error.message || '加载标签数据失败')
    
    // 如果获取详情失败，尝试通过标签名获取文章
    if (tagName.value) {
      await loadTagArticles()
    }
  } finally {
    loading.value = false
  }
}

// 加载标签文章
const loadTagArticles = async () => {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      sort: sortBy.value,
      tagName: originalTagName.value
    }
    
    // 使用标签名获取文章
    const result = await tagStore.fetchTagArticles(originalTagName.value, params)
    
    // 更新文章Store
    if (result && (result.articles || result.list)) {
      articleStore.setArticles(result.articles || result.list || [])
      articleStore.total = result.total || result.count || 0
    }
  } catch (error) {
    console.error('加载标签文章失败:', error)
    ElMessage.error('加载文章列表失败')
    throw error
  }
}

// 监听分页和排序变化
watch(
  [currentPage, sortBy],
  () => {
    if (tagName.value) {
      loadTagArticles()
    }
  }
)

// 监听每页数量变化
watch(
  pageSize,
  () => {
    if (tagName.value) {
      currentPage.value = 1
      loadTagArticles()
    }
  }
)

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