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
      
      const data = await searchApi.fullSearch(keyword, page, size)
      
      // 处理返回数据
      if (data) {
        searchResults.value = {
          articles: data.articles || [],
          users: data.users || [],
          tags: data.tags || [],
          total: data.total || 0
        }
        
        pagination.value = {
          page: data.page || page,
          size: data.size || size,
          total: data.total || 0,
          totalPages: Math.ceil((data.total || 0) / (data.size || size))
        }
      }
      
      // 保存到搜索历史
      if (keyword.trim()) {
        addToSearchHistory(keyword)
      }
      
      return data
    } catch (error) {
      searchError.value = error.response?.data?.message || error.message || '搜索失败'
      console.error('全文搜索失败:', error)
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
      
      const data = await searchApi.searchUsers(keyword, page, size)
      
      if (data) {
        searchResults.value = {
          articles: [],
          users: data.list || data.users || data.data || [],
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
      
      return data
    } catch (error) {
      searchError.value = error.response?.data?.message || error.message || '搜索用户失败'
      console.error('搜索用户失败:', error)
      throw error
    } finally {
      searchLoading.value = false
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
  const hasResults = computed(() => {
    return searchResults.value.total > 0
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