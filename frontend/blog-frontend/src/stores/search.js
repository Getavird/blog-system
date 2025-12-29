// stores/search.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as searchApi from '@/api/search'
import * as userApi from '@/api/user'

export const useSearchStore = defineStore('search', () => {
  // 搜索状态
  const searchLoading = ref(false)
  const searchError = ref(null)
  
  // 搜索结果
  const searchResults = ref({
    articles: [],
    users: [],
    tags: [],
    total: 0
  })
  
  // 搜索历史
  const searchHistory = ref([])
  
  // 热门关键词
  const hotKeywords = ref([])
  const hotKeywordsLoading = ref(false)
  
  // 搜索建议
  const searchSuggestions = ref([])
  const suggestionsLoading = ref(false)
  
  // 当前搜索关键词
  const currentKeyword = ref('')
  
  // 搜索类型
  const searchType = ref('full') // full, articles, users, tags
  
  // 分页信息
  const pagination = ref({
    page: 1,
    size: 20,
    total: 0,
    totalPages: 0
  })
  
  // 清空搜索结果
  const clearSearchResults = () => {
    searchResults.value = {
      articles: [],
      users: [],
      tags: [],
      total: 0
    }
    pagination.value = {
      page: 1,
      size: 20,
      total: 0,
      totalPages: 0
    }
    searchError.value = null
  }
  
// 全文搜索
const fullSearch = async (keyword, page = 1, size = 20) => {
  try {
    searchLoading.value = true
    searchError.value = null
    currentKeyword.value = keyword
    searchType.value = 'full'
    
    const response = await searchApi.fullSearch(keyword, page, size)
     // 调试：查看完整响应结构
    debugResponseStructure(response)

    console.log('✅ fullSearch 响应:', response)
    console.log('🔍 新的数据结构:', {
      keyword: response.keyword,
      total: response.total,
      articlesCount: response.articles?.length,
      usersCount: response.users?.length,
      tagsCount: response.tags?.length
    })
    
    // 处理返回数据 - 新的 FullSearchResponse 结构
    let articles = []
    let users = []
    let tags = []
    let total = 0
    let currentPage = page
    let currentSize = size
    
    if (response) {
      // 新数据结构：直接使用 articles, users, tags 字段
      articles = response.articles || []
      users = response.users || []
      tags = response.tags || []
      total = response.total || 0
      currentPage = response.page || page
      currentSize = response.size || size
      
      // 如果后端返回的是嵌套在 data 字段中（有时API会这样）
      if (response.data) {
        articles = response.data.articles || response.data.data?.articles || []
        users = response.data.users || response.data.data?.users || []
        tags = response.data.tags || response.data.data?.tags || []
        total = response.data.total || 0
        currentPage = response.data.page || page
        currentSize = response.data.size || size
      }
    }
    
    console.log('📊 提取的结果:', {
      articles: articles.length,
      users: users.length,
      tags: tags.length,
      total: total
    })
    
    // 处理用户数据，确保有必要的字段
    const processedUsers = users.map(user => {
      console.log('👤 处理用户数据:', user)
      return {
        id: user.id,
        username: user.username || '',
        avatar: user.avatar || '',
        bio: user.bio || '',
        articleCount: user.articleCount || 0,
        followerCount: user.followerCount || 0,
        likeCount: user.likeCount || 0,
        viewCount: user.viewCount || 0,
        // 保持原始数据用于调试
        _raw: user
      }
    })
    
    searchResults.value = {
      articles: articles,
      users: processedUsers,
      tags: tags,
      total: total
    }
    
    pagination.value = {
      page: currentPage,
      size: currentSize,
      total: total,
      totalPages: Math.ceil(total / currentSize)
    }
    
    console.log('✅ 最终搜索结果:', searchResults.value)
    
    // 保存到搜索历史
    if (keyword.trim()) {
      addToSearchHistory(keyword)
    }
    
    return response
  } catch (error) {
    searchError.value = error.response?.data?.message || error.message || '搜索失败'
    console.error('搜索失败:', error)
    throw error
  } finally {
    searchLoading.value = false
  }
}
  
  // 搜索文章
  const searchArticles = async (keyword, page = 1, size = 10) => {
    try {
      searchLoading.value = true
      searchError.value = null
      currentKeyword.value = keyword
      searchType.value = 'articles'
      
      const data = await searchApi.searchArticles(keyword, page, size)
      
      if (data) {
        searchResults.value = {
          articles: data.list || data.articles || data.data || [],
          users: [],
          tags: [],
          total: data.total || data.count || 0
        }
        
        pagination.value = {
          page: data.page || page,
          size: data.size || size,
          total: data.total || data.count || 0,
          totalPages: Math.ceil((data.total || data.count || 0) / (data.size || size))
        }
      }
      
      if (keyword.trim()) {
        addToSearchHistory(keyword)
      }
      
      return data
    } catch (error) {
      searchError.value = error.response?.data?.message || error.message || '搜索文章失败'
      console.error('搜索文章失败:', error)
      throw error
    } finally {
      searchLoading.value = false
    }
  }
  

// 搜索用户
const searchUsers = async (keyword, page = 1, size = 10) => {
  try {
    searchLoading.value = true
    searchError.value = null
    currentKeyword.value = keyword
    searchType.value = 'users'
    
    const response = await searchApi.searchUsers(keyword, page, size)
    
    console.log('✅ searchUsers 响应:', response)
    
    let users = []
    let total = 0
    let currentPage = page
    let currentSize = size
    
    if (response) {
      // 尝试不同的数据结构
      if (response.users) {
        // 新结构：直接有 users 字段
        users = response.users || []
        total = response.total || 0
        currentPage = response.page || page
        currentSize = response.size || size
      } else if (response.data && response.data.users) {
        // 嵌套结构
        users = response.data.users || []
        total = response.data.total || 0
        currentPage = response.data.page || page
        currentSize = response.data.size || size
      } else if (response.data && response.data.items) {
        // 旧结构
        users = response.data.items || []
        total = response.data.total || 0
        currentPage = response.data.page || page
        currentSize = response.data.size || size
      } else if (response.items) {
        // 另一种旧结构
        users = response.items || []
        total = response.total || 0
        currentPage = response.page || page
        currentSize = response.size || size
      }
    }
    
    console.log('👤 原始用户数据:', users)
    
    // 处理用户数据
    const processedUsers = users.map(user => ({
      id: user.id,
      username: user.username || '',
      avatar: user.avatar || '',
      bio: user.bio || '',
      articleCount: user.articleCount || 0,
      followerCount: user.followerCount || 0,
      likeCount: user.likeCount || 0,
      viewCount: user.viewCount || 0
    }))
    
    console.log('👤 处理后的用户数据:', processedUsers)
    
    searchResults.value = {
      articles: [],
      users: processedUsers,
      tags: [],
      total: total
    }
    
    pagination.value = {
      page: currentPage,
      size: currentSize,
      total: total,
      totalPages: Math.ceil(total / currentSize)
    }
    
    console.log('✅ 最终用户搜索结果:', searchResults.value)
    
    return response
  } catch (error) {
    searchError.value = error.response?.data?.message || error.message || '搜索用户失败'
    console.error('搜索用户失败:', error)
    throw error
  } finally {
    searchLoading.value = false
  }
}

// 调试方法：查看完整的响应结构
const debugResponseStructure = (response) => {
  console.log('🔍 完整响应结构分析:')
  console.log('1. 响应对象:', response)
  console.log('2. 响应类型:', typeof response)
  console.log('3. 响应原型:', Object.getPrototypeOf(response))
  console.log('4. 所有属性:', Object.keys(response))
  
  if (response && typeof response === 'object') {
    console.log('5. 详细属性:')
    Object.keys(response).forEach(key => {
      const value = response[key]
      console.log(`   ${key}:`, value, `(类型: ${typeof value}, 是数组: ${Array.isArray(value)})`)
    })
  }
}
  
  // 搜索标签
  const searchTags = async (keyword, page = 1, size = 10) => {
    try {
      searchLoading.value = true
      searchError.value = null
      currentKeyword.value = keyword
      searchType.value = 'tags'
      
      const data = await searchApi.searchTags(keyword, page, size)
      
      if (data) {
        searchResults.value = {
          articles: [],
          users: [],
          tags: data.list || data.tags || data.data || [],
          total: data.total || data.count || 0
        }
        
        pagination.value = {
          page: data.page || page,
          size: data.size || size,
          total: data.total || data.count || 0,
          totalPages: Math.ceil((data.total || data.count || 0) / (data.size || size))
        }
      }
      
      return data
    } catch (error) {
      searchError.value = error.response?.data?.message || error.message || '搜索标签失败'
      console.error('搜索标签失败:', error)
      throw error
    } finally {
      searchLoading.value = false
    }
  }
  
  // 高级搜索
  const advancedSearch = async (searchParams = {}) => {
    try {
      searchLoading.value = true
      searchError.value = null
      
      const data = await searchApi.advancedSearchArticles(searchParams)
      
      if (data) {
        searchResults.value = {
          articles: data.list || data.articles || data.data || [],
          users: [],
          tags: [],
          total: data.total || data.count || 0
        }
        
        pagination.value = {
          page: data.page || 1,
          size: data.size || 10,
          total: data.total || data.count || 0,
          totalPages: Math.ceil((data.total || data.count || 0) / (data.size || 10))
        }
      }
      
      return data
    } catch (error) {
      searchError.value = error.response?.data?.message || error.message || '高级搜索失败'
      console.error('高级搜索失败:', error)
      throw error
    } finally {
      searchLoading.value = false
    }
  }
  
  // 获取热门关键词
  const fetchHotKeywords = async (limit = 10) => {
    try {
      hotKeywordsLoading.value = true
      const data = await searchApi.getHotKeywords(limit)
      hotKeywords.value = data.list || data.keywords || data.data || []
      return data
    } catch (error) {
      console.error('获取热门关键词失败:', error)
      return []
    } finally {
      hotKeywordsLoading.value = false
    }
  }
  
  // 获取搜索建议
  const fetchSearchSuggestions = async (prefix, limit = 5) => {
    try {
      if (!prefix.trim()) {
        searchSuggestions.value = []
        return []
      }
      
      suggestionsLoading.value = true
      const data = await searchApi.getSearchSuggestions(prefix, limit)
      
      // 处理返回数据
      if (Array.isArray(data)) {
        searchSuggestions.value = data
      } else if (data?.list) {
        searchSuggestions.value = data.list
      } else if (data?.suggestions) {
        searchSuggestions.value = data.suggestions
      } else if (data?.data) {
        searchSuggestions.value = data.data
      } else {
        searchSuggestions.value = []
      }
      
      return searchSuggestions.value
    } catch (error) {
      console.error('获取搜索建议失败:', error)
      searchSuggestions.value = []
      return []
    } finally {
      suggestionsLoading.value = false
    }
  }
  
  // 获取搜索历史
  const fetchSearchHistory = async (limit = 20) => {
    try {
      const data = await searchApi.getSearchHistory({ limit })
      
      if (Array.isArray(data)) {
        searchHistory.value = data
      } else if (data?.list) {
        searchHistory.value = data.list
      } else if (data?.history) {
        searchHistory.value = data.history
      } else if (data?.data) {
        searchHistory.value = data.data
      }
      
      return searchHistory.value
    } catch (error) {
      console.error('获取搜索历史失败:', error)
      return []
    }
  }
  
  // 添加到搜索历史（本地）
  const addToSearchHistory = (keyword) => {
    if (!keyword.trim()) return
    
    // 移除重复项
    searchHistory.value = searchHistory.value.filter(item => item !== keyword)
    
    // 添加到开头
    searchHistory.value.unshift(keyword)
    
    // 限制历史记录数量
    if (searchHistory.value.length > 20) {
      searchHistory.value = searchHistory.value.slice(0, 20)
    }
    
    // 可以保存到本地存储
    try {
      localStorage.setItem('search_history', JSON.stringify(searchHistory.value))
    } catch (e) {
      console.error('保存搜索历史到本地存储失败:', e)
    }
  }
  
  // 清空搜索历史
  const clearSearchHistory = async () => {
    try {
      await searchApi.clearSearchHistory()
      searchHistory.value = []
      localStorage.removeItem('search_history')
      return true
    } catch (error) {
      console.error('清空搜索历史失败:', error)
      throw error
    }
  }
  
  // 清空搜索建议
  const clearSearchSuggestions = () => {
    searchSuggestions.value = []
  }
  
  // 从本地存储加载搜索历史
  const loadSearchHistoryFromStorage = () => {
    try {
      const savedHistory = localStorage.getItem('search_history')
      if (savedHistory) {
        searchHistory.value = JSON.parse(savedHistory)
      }
    } catch (e) {
      console.error('从本地存储加载搜索历史失败:', e)
      searchHistory.value = []
    }
  }
  
  // 计算属性：是否有搜索结果
// 计算属性：是否有搜索结果
const hasResults = computed(() => {
  const total = searchResults.value.total || 0
  console.log('📊 hasResults 计算: total =', total)
  return total > 0
})
  
  // 计算属性：是否正在搜索
  const isSearching = computed(() => searchLoading.value)
  
  // 初始化
  loadSearchHistoryFromStorage()
  
  return {
    // 状态
    searchLoading,
    searchError,
    searchResults,
    searchHistory,
    hotKeywords,
    hotKeywordsLoading,
    searchSuggestions,
    suggestionsLoading,
    currentKeyword,
    searchType,
    pagination,
    
    // 计算属性
    hasResults,
    isSearching,
    
    // 方法
    clearSearchResults,
    fullSearch,
    searchArticles,
    searchUsers,
    searchTags,
    advancedSearch,
    fetchHotKeywords,
    fetchSearchSuggestions,
    fetchSearchHistory,
    addToSearchHistory,
    clearSearchHistory,
    clearSearchSuggestions,
    loadSearchHistoryFromStorage
  }
})