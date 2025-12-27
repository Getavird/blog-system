<template>
  <div class="search-page">
    <div class="search-header">
      <div class="container">
        <h1>搜索页面</h1>
        <div class="search-box">
          <el-input
            v-model="keyword"
            placeholder="输入搜索关键词..."
            @keyup.enter="doSearch"
            clearable
            size="large"
          >
            <template #append>
              <el-button @click="doSearch" type="primary">搜索</el-button>
            </template>
          </el-input>
        </div>
        <p class="search-tip">搜索关键词: "{{ keyword || '无' }}"</p>
      </div>
    </div>
    
    <div class="search-results container">
      <h2 v-if="keyword">搜索结果</h2>
      <div v-else class="empty-search">
        <p>请输入搜索关键词</p>
      </div>
      
      <div v-if="loading" class="loading">
        <p>搜索中...</p>
      </div>
      
      <div v-else-if="results.length > 0" class="results-list">
        <div v-for="result in results" :key="result.id" class="result-item">
          <h3>{{ result.title }}</h3>
          <p>{{ result.content }}</p>
          <div class="result-meta">
            <span>作者: {{ result.author }}</span>
            <span>时间: {{ result.time }}</span>
          </div>
        </div>
      </div>
      
      <div v-else-if="keyword && !loading" class="no-results">
        <p>没有找到相关结果</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { ElMessage } from 'element-plus'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'

const route = useRoute()
const router = useRouter()

// Pinia Store
const articleStore = useArticleStore()

// 搜索状态
const keyword = ref('')
const loading = ref(false)
const results = ref([]) // 修复：统一使用 results 变量名

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 从路由参数获取搜索关键词
onMounted(() => {
  const queryKeyword = route.query.q || ''
  if (queryKeyword) {
    keyword.value = queryKeyword
    doSearch()
  }
})

// 监听路由参数变化
watch(
  () => route.query.q,
  (newKeyword) => {
    if (newKeyword && newKeyword !== keyword.value) {
      keyword.value = newKeyword
      doSearch()
    }
  }
)

// 执行搜索
const doSearch = async () => {
  const searchKeyword = keyword.value.trim()
  
  if (!searchKeyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  try {
    loading.value = true
    
    // 更新URL参数，不刷新页面
    router.replace({
      path: '/search',
      query: { q: searchKeyword }
    })
    
    // 调用搜索API
    const result = await articleStore.searchArticles(searchKeyword, {
      page: currentPage.value,
      size: pageSize.value
    })
    
    if (result && result.list) {
      results.value = result.list.map(article => ({
        id: article.id,
        title: article.title,
        content: article.summary || '暂无摘要',
        author: article.authorName || '匿名',
        time: formatTime(article.createTime)
      }))
      total.value = result.total || 0
    } else {
      results.value = []
      total.value = 0
    }
    
    if (results.value.length === 0) {
      ElMessage.info('没有找到相关结果')
    }
    
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
    results.value = []
  } finally {
    loading.value = false
  }
}

// 格式化时间
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

// 分页改变
const handlePageChange = (page) => {
  currentPage.value = page
  if (keyword.value.trim()) {
    doSearch()
  }
}

// 每页数量改变
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  if (keyword.value.trim()) {
    doSearch()
  }
}
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.search-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40px 0;
  margin-bottom: 40px;
}

.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.search-header h1 {
  text-align: center;
  margin-bottom: 30px;
  font-size: 36px;
}

.search-box {
  max-width: 600px;
  margin: 0 auto 20px;
}

.search-tip {
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
}

.search-results {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.search-results h2 {
  margin-bottom: 20px;
  color: #333;
}

.empty-search {
  text-align: center;
  padding: 40px;
  color: #666;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #409eff;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.result-item {
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s;
}

.result-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
}

.result-item h3 {
  color: #333;
  margin-bottom: 10px;
}

.result-item p {
  color: #666;
  margin-bottom: 10px;
  line-height: 1.6;
}

.result-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
}

.no-results {
  text-align: center;
  padding: 40px;
  color: #666;
}

.no-results p {
  font-size: 18px;
}
</style>