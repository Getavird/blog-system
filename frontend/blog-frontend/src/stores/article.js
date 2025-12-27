import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as articleApi from '@/api/article'
import { transformArticles, transformArticle } from '@/utils/dataTransform'  // 导入数据转换函数

export const useArticleStore = defineStore('article', () => {
  // 状态
  const articles = ref([])
  const currentArticle = ref(null)
  const hotArticles = ref([])
  const newestArticles = ref([])
  const myArticles = ref({ list: [], total: 0 }) // 我的文章（含分页）
  const myDrafts = ref({ list: [], total: 0 })   // 我的草稿（含分页）
  const total = ref(0)
  
  // 拆分的loading状态
  const articlesLoading = ref(false)       // 文章列表加载
  const detailLoading = ref(false)         // 文章详情加载
  const hotLoading = ref(false)            // 热门文章加载
  const newestLoading = ref(false)         // 最新文章加载
  const myArticlesLoading = ref(false)     // 我的文章加载
  const myDraftsLoading = ref(false)       // 我的草稿加载
  const createLoading = ref(false)         // 创建文章加载
  const updateLoading = ref(false)         // 更新文章加载
  const deleteLoading = ref(false)         // 删除文章加载
  const likeLoading = ref(false)           // 点赞操作加载
  const publishLoading = ref(false)        // 发布草稿加载

  // 获取文章列表
  const fetchArticles = async (params = {}) => {
    try {
      articlesLoading.value = true
      const data = await articleApi.getArticles(params)
      articles.value = transformArticles(data.list || [])
      total.value = data.total || 0
      return data
    } catch (error) {
      console.error('获取文章列表失败:', error)
      throw error
    } finally {
      articlesLoading.value = false
    }
  }

  // 获取文章详情
  const fetchArticleDetail = async (id) => {
    try {
      detailLoading.value = true
      const data = await articleApi.getArticleById(id)
      currentArticle.value = transformArticle(data)
      return data
    } catch (error) {
      console.error('获取文章详情失败:', error)
      throw error
    } finally {
      detailLoading.value = false
    }
  }

  // 创建文章
  const createArticle = async (articleData) => {
    try {
      createLoading.value = true
      const newArticle = await articleApi.createArticle(articleData)
      // 创建成功后同步更新状态
      const transformedArticle = transformArticle(newArticle)
      articles.value.unshift(transformedArticle)
      total.value += 1
      return newArticle
    } catch (error) {
      console.error('创建文章失败:', error)
      throw error
    } finally {
      createLoading.value = false
    }
  }

  // 更新文章
  const updateArticle = async (id, articleData) => {
    try {
      updateLoading.value = true
      const data = await articleApi.updateArticle(id, articleData)

      // 更新本地状态
      if (currentArticle.value && currentArticle.value.id === id) {
        currentArticle.value = transformArticle({ ...currentArticle.value, ...articleData })
      }
      
      // 更新列表中的文章
      const index = articles.value.findIndex(article => article.id === id)
      if (index !== -1) {
        articles.value[index] = transformArticle({ ...articles.value[index], ...articleData })
      }

      return data
    } catch (error) {
      console.error('更新文章失败:', error)
      throw error
    } finally {
      updateLoading.value = false
    }
  }

  // 删除文章
  const deleteArticle = async (id) => {
    try {
      deleteLoading.value = true
      const data = await articleApi.deleteArticle(id)

      // 从列表中移除
      articles.value = articles.value.filter(article => article.id !== id)
      total.value = Math.max(0, total.value - 1)
      
      // 从我的文章列表中移除
      myArticles.value.list = myArticles.value.list.filter(article => article.id !== id)
      myArticles.value.total = Math.max(0, myArticles.value.total - 1)
      
      if (currentArticle.value && currentArticle.value.id === id) {
        currentArticle.value = null
      }

      return data
    } catch (error) {
      console.error('删除文章失败:', error)
      throw error
    } finally {
      deleteLoading.value = false
    }
  }

  // 文章阅读量+1
  const incrementViewCount = async (id) => {
    try {
      await articleApi.incrementArticleView(id)

      // 更新本地状态
      if (currentArticle.value && currentArticle.value.id === id) {
        currentArticle.value.viewCount += 1
      }
      
      // 更新列表中的阅读量
      const index = articles.value.findIndex(article => article.id === id)
      if (index !== -1) {
        articles.value[index].viewCount += 1
      }
    } catch (error) {
      console.error('增加阅读量失败:', error)
    }
  }

  // 点赞/取消点赞文章
  const toggleLike = async (id, isLike) => {
    try {
      likeLoading.value = true
      await articleApi.toggleArticleLike(id, isLike)

      // 更新本地状态
      if (currentArticle.value && currentArticle.value.id === id) {
        if (isLike) {
          currentArticle.value.likeCount += 1
        } else {
          currentArticle.value.likeCount = Math.max(0, currentArticle.value.likeCount - 1)
        }
      }
      
      // 更新列表中的点赞数
      const index = articles.value.findIndex(article => article.id === id)
      if (index !== -1) {
        if (isLike) {
          articles.value[index].likeCount += 1
        } else {
          articles.value[index].likeCount = Math.max(0, articles.value[index].likeCount - 1)
        }
      }
    } catch (error) {
      console.error('操作点赞失败:', error)
      throw error
    } finally {
      likeLoading.value = false
    }
  }

  // 获取热门文章
  const fetchHotArticles = async (limit = 10) => {
    try {
      hotLoading.value = true
      const data = await articleApi.getHotArticles(limit)
      hotArticles.value = transformArticles(data)
      return data
    } catch (error) {
      console.error('获取热门文章失败:', error)
      throw error
    } finally {
      hotLoading.value = false
    }
  }

  // 获取最新文章
  const fetchNewestArticles = async (limit = 10) => {
    try {
      newestLoading.value = true
      const data = await articleApi.getNewestArticles(limit)
      newestArticles.value = transformArticles(data)
      return data
    } catch (error) {
      console.error('获取最新文章失败:', error)
      throw error
    } finally {
      newestLoading.value = false
    }
  }

  // 搜索文章
  const searchArticles = async (keyword, params = {}) => {
    try {
      articlesLoading.value = true
      const data = await articleApi.searchArticles(keyword, params)
      articles.value = transformArticles(data.list || [])
      total.value = data.total || 0  // 补充total更新
      return data
    } catch (error) {
      console.error('搜索文章失败:', error)
      throw error
    } finally {
      articlesLoading.value = false
    }
  }

  // 获取我的文章
  const fetchMyArticles = async (params = {}) => {
    try {
      myArticlesLoading.value = true
      const data = await articleApi.getMyArticles(params)
      myArticles.value = { 
        list: transformArticles(data.list || []), 
        total: data.total || 0 
      }
      return data
    } catch (error) {
      console.error('获取我的文章失败:', error)
      throw error
    } finally {
      myArticlesLoading.value = false
    }
  }

  // 清除当前文章
  const clearCurrentArticle = () => {
    currentArticle.value = null
  }

  // 设置文章列表（用于从分类Store同步数据）
  const setArticles = (newArticles) => {
    articles.value = transformArticles(newArticles)
  }

  // 获取我的草稿列表
  const fetchMyDrafts = async (params = {}) => {
    try {
      myDraftsLoading.value = true
      const data = await articleApi.getMyDrafts(params)
      myDrafts.value = { 
        list: transformArticles(data.list || []), 
        total: data.total || 0 
      }
      return data
    } catch (error) {
      console.error('获取草稿列表失败:', error)
      throw error
    } finally {
      myDraftsLoading.value = false
    }
  }

  // 发布草稿
  const publishDraft = async (id) => {
    try {
      publishLoading.value = true
      const data = await articleApi.publishDraft(id)
      
      // 从草稿列表移除
      myDrafts.value.list = myDrafts.value.list.filter(draft => draft.id !== id)
      myDrafts.value.total = Math.max(0, myDrafts.value.total - 1)
      
      // 添加到文章列表
      if (data.article) {
        const newArticle = transformArticle(data.article)
        articles.value.unshift(newArticle)
        total.value += 1
        myArticles.value.list.unshift(newArticle)
        myArticles.value.total += 1
      }
      
      return data
    } catch (error) {
      console.error('发布草稿失败:', error)
      throw error
    } finally {
      publishLoading.value = false
    }
  }

  return {
    // 状态
    articles,
    currentArticle,
    hotArticles,
    newestArticles,
    myArticles,
    myDrafts,
    total,
    
    // 加载状态
    articlesLoading,
    detailLoading,
    hotLoading,
    newestLoading,
    myArticlesLoading,
    myDraftsLoading,
    createLoading,
    updateLoading,
    deleteLoading,
    likeLoading,
    publishLoading,

    // 方法
    setArticles,
    fetchArticles,
    fetchArticleDetail,
    createArticle,
    updateArticle,
    deleteArticle,
    incrementViewCount,
    toggleLike,
    fetchHotArticles,
    fetchNewestArticles,
    searchArticles,
    fetchMyArticles,
    clearCurrentArticle,
    fetchMyDrafts,
    publishDraft
  }
})