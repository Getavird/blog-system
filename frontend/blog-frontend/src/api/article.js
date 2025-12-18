import request from '@/utils/request'

// 获取文章列表
export const getArticles = (params) => {
  return request.get('/api/articles', { params })
}

// 获取文章详情
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

// 点赞文章
export const likeArticle = (id) => {
  return request.post(`/api/articles/${id}/like`)
}

// 取消点赞
export const unlikeArticle = (id) => {
  return request.delete(`/api/articles/${id}/like`)
}

// 搜索文章
export const searchArticles = (keyword, params) => {
  return request.get('/api/articles/search', { 
    params: { keyword, ...params }
  })
}

// 获取我的文章
export const getMyArticles = (params) => {
  return request.get('/api/articles/my', { params })
}