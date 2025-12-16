import request from '@/utils/request'

// 获取文章列表
export const getArticles = (params?: any) => {
  return request.get('/api/articles', { params })
}

// 获取文章详情
export const getArticleById = (id: number) => {
  return request.get(`/api/articles/${id}`)
}

// 创建文章
export const createArticle = (data: any) => {
  return request.post('/api/articles', data)
}

// 更新文章
export const updateArticle = (id: number, data: any) => {
  return request.put(`/api/articles/${id}`, data)
}

// 删除文章
export const deleteArticle = (id: number) => {
  return request.delete(`/api/articles/${id}`)
}

// 点赞文章
export const likeArticle = (id: number) => {
  return request.post(`/api/articles/${id}/like`)
}

// 取消点赞
export const unlikeArticle = (id: number) => {
  return request.delete(`/api/articles/${id}/like`)
}

// 获取热门文章
export const getHotArticles = () => {
  return request.get('/api/articles/hot')
}

// 搜索文章
export const searchArticles = (keyword: string) => {
  return request.get('/api/articles/search', { params: { keyword } })
}