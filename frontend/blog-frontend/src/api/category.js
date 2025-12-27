import request from '@/utils/request'

export const getCategories = () => {
  return request.get('/api/categories')
}

export const getCategoryDetail = (id) => {
  return request.get(`/api/categories/${id}`)
}

// 获取分类下的文章
export const getCategoryArticles = (id, params = {}) => {
  return request.get(`/api/categories/${id}/articles`, { 
    params: { page: 1, size: 10, sort: 'createTime', ...params }
  })
}

export const createCategory = (data) => {
  return request.post('/api/categories', data)
}

export const updateCategory = (id, data) => {
  return request.put(`/api/categories/${id}`, data)
}

export const deleteCategory = (id) => {
  return request.delete(`/api/categories/${id}`)
}
