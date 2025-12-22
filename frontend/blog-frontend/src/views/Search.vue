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

<script>
export default {
  name: 'SearchPage',
  data() {
    return {
      keyword: '',
      loading: false,
      results: []
    }
  },
  mounted() {
    // 从URL获取搜索关键词
    const query = this.$route.query.q || ''
    this.keyword = query
    if (query) {
      this.doSearch()
    }
  },
  methods: {
    doSearch() {
      if (!this.keyword.trim()) {
        this.$message.warning('请输入搜索关键词')
        return
      }
      
      this.loading = true
      
      // 更新URL
      this.$router.push({
        path: '/search',
        query: { q: this.keyword }
      })
      
      // 模拟搜索
      setTimeout(() => {
        this.results = [
          {
            id: 1,
            title: `关于 ${this.keyword} 的文章`,
            content: `这是一篇关于 ${this.keyword} 的示例文章内容...`,
            author: '张三',
            time: '2024-01-15'
          },
          {
            id: 2,
            title: `${this.keyword} 入门教程`,
            content: `学习 ${this.keyword} 的基础知识...`,
            author: '李四',
            time: '2024-01-14'
          }
        ]
        this.loading = false
      }, 800)
    }
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