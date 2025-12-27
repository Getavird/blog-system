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
import { ArrowRight, PriceTag } from '@element-plus/icons-vue'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'
import ArticleList from '@/components/article/ArticleList.vue'

const route = useRoute()
const router = useRouter()

// Pinia Store
const tagStore = useTagStore()
const articleStore = useArticleStore()

// 路由参数
const tagName = ref(decodeURIComponent(route.params.name) || '')

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)

// 状态
const loading = ref(false)
const tagId = ref(0)

// 标签详情
const tagDetail = computed(() => tagStore.currentTag)
const tagDescription = computed(() => tagDetail.value?.description || '')

// 文章列表
const articles = computed(() => articleStore.articles || [])

// 统计信息
const total = computed(() => tagDetail.value?.articleCount || 0)

const viewCount = computed(() => {
  return articles.value.reduce((sum, article) => sum + (article.viewCount || 0), 0)
})

const likeCount = computed(() => {
  return articles.value.reduce((sum, article) => sum + (article.likeCount || 0), 0)
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

// 组件挂载
onMounted(async () => {
  if (tagName.value) {
    await loadTagData()
  }
})

// 监听路由参数变化
watch(
  () => route.params.name,
  async (newName) => {
    if (newName) {
      tagName.value = decodeURIComponent(newName)
      currentPage.value = 1 // 重置分页
      await loadTagData()
    }
  }
)

// 加载标签数据
const loadTagData = async () => {
  try {
    loading.value = true
    
    // 1. 先获取所有标签，找到匹配的标签ID
    await tagStore.fetchTags()
    
    const foundTag = tagStore.tags.find(tag => 
      tag.name.toLowerCase() === tagName.value.toLowerCase()
    )
    
    if (!foundTag) {
      ElMessage.warning('标签不存在')
      router.push('/tags')
      return
    }
    
    tagId.value = foundTag.id
    
    // 2. 获取标签详情
    await tagStore.fetchTagDetail(tagId.value)
    
    // 3. 获取标签下的文章
    await loadTagArticles()
    
  } catch (error) {
    console.error('加载标签数据失败:', error)
    ElMessage.error('加载失败')
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
      tagId: tagId.value
    }
    
    await articleStore.fetchArticles(params)
  } catch (error) {
    console.error('加载标签文章失败:', error)
    throw error
  }
}

// 监听分页变化
watch(
  [currentPage, pageSize],
  () => {
    if (tagId.value) {
      loadTagArticles()
    }
  }
)

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
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .stat-item {
    min-width: 100px;
    flex: 1;
  }
}
</style>