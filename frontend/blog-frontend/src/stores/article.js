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

const prepareArticleDataForApi = (articleData) => {
  const data = { ...articleData };
  
  // 调试：打印原始数据
  console.log('prepareArticleDataForApi 原始数据:', articleData);
  
  // 确保布尔值转换为整数
  if (typeof data.isPublic === 'boolean') {
    data.isPublic = data.isPublic ? 1 : 0;
  }
  
  if (typeof data.allowComment === 'boolean') {
    data.allowComment = data.allowComment ? 1 : 0;
  }
  
  if (typeof data.isTop === 'boolean') {
    data.isTop = data.isTop ? 1 : 0;
  }
  
  // 确保标签是字符串格式（逗号分隔）
  if (Array.isArray(data.tags)) {
    data.tags = data.tags.length > 0 ? data.tags.join(',') : null;
  } else if (data.tags === '' || data.tags === undefined) {
    data.tags = null;
  }
  
  // 确保数字字段是数字类型
  if (data.categoryId !== undefined && data.categoryId !== null) {
    data.categoryId = parseInt(data.categoryId);
  }
  
  if (data.status !== undefined && data.status !== null) {
    data.status = parseInt(data.status);
  }
  
  // 确保字符串字段正确（去除多余空格）
  if (typeof data.title === 'string') {
    data.title = data.title.trim();
  }
  
  if (typeof data.content === 'string') {
    data.content = data.content.trim();
  }
  
  // 确保封面图片为空时是null而不是空字符串
  if (data.coverImage === '') {
    data.coverImage = null;
  }
  
  console.log('prepareArticleDataForApi 转换后:', data);
  return data;
}
  
  // 获取文章列表
  const fetchArticles = async (params = {}) => {
    try {
      articlesLoading.value = true
      console.log('fetchArticles 参数:', params)

      const response = await articleApi.getArticles(params)
      console.log('fetchArticles 响应数据:', response)
      console.log('响应数据类型:', typeof response)
      console.log('响应是否数组:', Array.isArray(response))

      // 检查返回的数据结构
      let list = []
      let totalCount = 0

      // 情况1: 如果返回的是数组
      if (Array.isArray(response)) {
        list = response
        totalCount = response.length
      }
      // 情况2: 如果返回的是对象且有 data 字段
      else if (response && response.data && Array.isArray(response.data)) {
        list = response.data
        totalCount = response.total || response.data.length
      }
      // 情况3: 如果返回的是对象且有 list 字段
      else if (response && response.list && Array.isArray(response.list)) {
        list = response.list
        totalCount = response.total || response.list.length
      }
      // 情况4: 如果返回的是 Result 格式 (code, data, message)
      else if (response && response.code === 200 && response.data) {
        // 检查 data 是数组还是包含数组的对象
        if (Array.isArray(response.data)) {
          list = response.data
          totalCount = response.data.length
        } else if (response.data.list && Array.isArray(response.data.list)) {
          list = response.data.list
          totalCount = response.data.total || response.data.list.length
        }
      }

      console.log('提取的文章列表:', list)
      console.log('提取的文章数量:', list.length)

      // 转换数据
      articles.value = transformArticles(list)
      total.value = totalCount

      console.log('转换后的文章:', articles.value)

      return response
    } catch (error) {
      console.error('获取文章列表失败:', error)
      throw error
    } finally {
      articlesLoading.value = false
    }
  }

  // 获取文章详情
const fetchArticleDetail = async (id, params = {}) => {
  try {
    detailLoading.value = true
    const data = await articleApi.getArticleById(id, params)
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
    
    // 准备发送给后端的数据
    const preparedData = prepareArticleDataForApi(articleData);
    console.log('createArticle 发送的数据:', preparedData);
    
    const newArticle = await articleApi.createArticle(preparedData);
    
    // 创建成功后同步更新状态
    const transformedArticle = transformArticle(newArticle);
    articles.value.unshift(transformedArticle);
    total.value += 1;
    
    // 如果当前正在查看我的文章，也添加到我的文章列表
    if (myArticles.value.list.length > 0) {
      myArticles.value.list.unshift(transformedArticle);
      myArticles.value.total += 1;
    }
    
    console.log('创建文章成功:', transformedArticle);
    return newArticle;
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
    
    // 准备发送给后端的数据
    const preparedData = prepareArticleDataForApi(articleData);
    console.log('updateArticle 发送的数据 (id=' + id + '):', preparedData);
    
    const data = await articleApi.updateArticle(id, preparedData);

    // 更新本地状态
    if (currentArticle.value && currentArticle.value.id === id) {
      currentArticle.value = transformArticle({ ...currentArticle.value, ...articleData })
    }

    // 更新列表中的文章
    const index = articles.value.findIndex(article => article.id === id)
    if (index !== -1) {
      articles.value[index] = transformArticle({ ...articles.value[index], ...articleData })
    }
    
    // 更新我的文章列表
    const myIndex = myArticles.value.list.findIndex(article => article.id === id)
    if (myIndex !== -1) {
      myArticles.value.list[myIndex] = transformArticle({ ...myArticles.value.list[myIndex], ...articleData })
    }
    
    // 更新草稿列表
    const draftIndex = myDrafts.value.list.findIndex(draft => draft.id === id)
    if (draftIndex !== -1) {
      myDrafts.value.list[draftIndex] = transformArticle({ ...myDrafts.value.list[draftIndex], ...articleData })
    }

    console.log('更新文章成功:', data);
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
    
    // 从草稿列表中移除
    myDrafts.value.list = myDrafts.value.list.filter(draft => draft.id !== id)
    myDrafts.value.total = Math.max(0, myDrafts.value.total - 1)

    if (currentArticle.value && currentArticle.value.id === id) {
      currentArticle.value = null
    }

    console.log('删除文章成功:', data);
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
      // 由于后端已经在获取文章详情时更新了阅读量
      // 我们只需要更新本地状态，不需要调用额外的API

      // 更新本地状态
      if (currentArticle.value && currentArticle.value.id === id) {
        currentArticle.value.viewCount += 1
      }

      // 更新列表中的阅读量
      const index = articles.value.findIndex(article => article.id === id)
      if (index !== -1) {
        articles.value[index].viewCount += 1
      }

      // 注意：这里我们不调用 API，因为后端已经在 GET /api/articles/{id} 中处理了阅读量
      // 如果你的后端确实需要单独的接口增加阅读量，可以取消下面的注释
      // await articleApi.incrementArticleView(id)
    } catch (error) {
      console.error('更新阅读量状态失败:', error)
      // 这里不抛出错误，因为只是本地状态更新失败
    }
  }

  // 点赞/取消点赞文章
  const toggleLike = async (id) => {
    try {
      likeLoading.value = true
      const response = await articleApi.toggleArticleLike(id)

      console.log('点赞API返回:', response) // 添加这行查看实际返回结构

      // 检查响应是否成功
      if (response && response.code === 200) {
        // 获取返回的数据
        const result = response.data

        if (result && result.success !== false) {
          // 更新本地状态
          if (currentArticle.value && currentArticle.value.id === id) {
            currentArticle.value.isLiked = result.isLiked || true
            currentArticle.value.likeCount = result.likeCount || 0
          }

          // 更新列表中的点赞数
          const index = articles.value.findIndex(article => article.id === id)
          if (index !== -1) {
            articles.value[index].isLiked = result.isLiked || true
            articles.value[index].likeCount = result.likeCount || 0
          }

          return {
            success: true,
            isLiked: result.isLiked,
            likeCount: result.likeCount,
            message: result.message || '操作成功'
          }
        } else {
          // 如果success为false，抛出错误信息
          throw new Error(result?.message || '操作失败')
        }
      } else {
        // 响应code不是200，抛出错误
        throw new Error(response?.message || '操作失败')
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
      console.log('fetchHotArticles 调用，limit:', limit)

      const response = await articleApi.getHotArticles(limit)
      console.log('fetchHotArticles 响应:', response)

      // 检查数据结构
      let list = []

      if (Array.isArray(response)) {
        list = response
      } else if (response && response.data && Array.isArray(response.data)) {
        list = response.data
      } else if (response && response.list && Array.isArray(response.list)) {
        list = response.list
      } else if (response && response.code === 200 && response.data) {
        if (Array.isArray(response.data)) {
          list = response.data
        } else if (response.data.list && Array.isArray(response.data.list)) {
          list = response.data.list
        }
      }

      console.log('提取的热门文章列表:', list)
      hotArticles.value = transformArticles(list)
      console.log('转换后的热门文章:', hotArticles.value)

      return response
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
    console.log('🔄 获取我的文章参数:', params)
    
    const data = await articleApi.getMyArticles(params)
    console.log('📋 我的文章API返回:', data)
    
    // ✅ 根据实际数据结构提取文章列表
    let articlesList = []
    
    if (data.articles && Array.isArray(data.articles)) {
      articlesList = data.articles
    } else if (data.list && Array.isArray(data.list)) {
      articlesList = data.list
    }
    
    console.log('📊 提取的文章列表:', articlesList)
    
    myArticles.value = {
      list: transformArticles(articlesList),
      total: data.total || articlesList.length
    }
    
    console.log('✅ 转换后的文章数据:', myArticles.value)
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
    console.log('🔄 获取草稿参数:', params)
    
    const data = await articleApi.getMyDrafts(params)
    console.log('📋 草稿API返回:', data)
    
    // ✅ 根据实际数据结构提取草稿列表
    // 注意：response.data 已经被响应拦截器提取出来了
    // 所以 data 应该是 {total:1, size:10, drafts:[...], page:1}
    let draftsList = []
    
    if (data.drafts && Array.isArray(data.drafts)) {
      draftsList = data.drafts
    } else if (data.list && Array.isArray(data.list)) {
      draftsList = data.list
    }
    
    console.log('📊 提取的草稿列表:', draftsList)
    
    myDrafts.value = {
      list: transformArticles(draftsList),
      total: data.total || draftsList.length
    }
    
    console.log('✅ 转换后的草稿数据:', myDrafts.value)
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
    
    // 先获取草稿的当前状态
    const draft = myDrafts.value.list.find(d => d.id === id);
    if (!draft) {
      throw new Error('草稿不存在');
    }
    
    // 准备发布数据
    const publishData = {
      ...draft,
      status: 1 // 发布状态
    };
    
    // 使用 updateArticle 来发布草稿
    const data = await updateArticle(id, publishData);
    
    console.log('发布草稿成功:', data);
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
    prepareArticleDataForApi,
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