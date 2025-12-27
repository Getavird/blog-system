import request from '@/utils/request'

// 文章列表（分页）
export const getArticles = (params = {}) => {
  const defaultParams = { page: 1, size: 10, sort: 'createTime' }
  return request.get('/api/articles', { 
    params: { ...defaultParams, ...params }
  })
}

// 文章详情
export const getArticleById = (id) => {
  return request.get(`/api/articles/${id}`)
}

// 创建文章
export const createArticle = (data) => {
  return request.post('/api/articles', data)
}

// 更新文章
export const updateArticle = (id, data) => {
  return request.put(`/api/articles/${id}`, data)
}

// 删除文章
export const deleteArticle = (id) => {
  return request.delete(`/api/articles/${id}`)
}

// 文章阅读量 +1
export const incrementArticleView = (id) => {
  return request.post(`/api/articles/${id}/view`)
}

// 文章点赞/取消
export const toggleArticleLike = (id, isLike) => {
  return request.post(`/api/articles/${id}/like`, { isLike })
}

// 热门文章
export const getHotArticles = (limit = 10) => {
  return request.get('/api/articles/hot', { params: { limit } })
}

// 最新文章
export const getNewestArticles = (limit = 10) => {
  return request.get('/api/articles/newest', { params: { limit } })
}

// 搜索文章
export const searchArticles = (keyword, params = {}) => {
  return request.get('/api/articles/search', { 
    params: { keyword, ...params }
  })
}

// 根据标签ID获取文章
export const getArticlesByTag = (tagId, params = {}) => {
  return request.get('/api/articles', { 
    params: { tagId, ...params }
  })
}

// 获取我的文章
export const getMyArticles = (params = {}) => {
  return request.get('/api/articles/my', { params })
}

// 获取我的草稿列表
export const getMyDrafts = (params = {}) => {
  return request.get('/api/articles/my-drafts', { params })
}

// 发布草稿
export const publishDraft = (id) => {
  return request.post(`/api/articles/${id}/publish`)
}

// 删除草稿
export const deleteDraft = (id) => {
  return request.delete(`/api/articles/draft/${id}`)
}