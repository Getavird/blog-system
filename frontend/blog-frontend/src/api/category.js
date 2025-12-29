import request from '@/utils/request'

export const getCategories = () => {
  return request.get('/api/categories')
}

export const getCategoryDetail = (id) => {
  return request.get(`/api/categories/${id}/detail`)
}

// 获取分类下的文章
export const getCategoryArticles = (id, params = {}) => {
  const requestParams = {
    page: params.page || 1,
    size: params.size || 15,
    sort: params.sort || 'latest'
  }
  
  console.log('发送分类文章请求，参数:', requestParams) 
  
  return request.get(`/api/categories/${id}/articles`, { 
    params: requestParams
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
