import request from '@/utils/request'

// 获取分类列表
export const getCategories = () => {
  return request.get('/api/categories')
}

// 创建分类
export const createCategory = (data: any) => {
  return request.post('/api/categories', data)
}

// 更新分类
export const updateCategory = (id: number, data: any) => {
  return request.put(`/api/categories/${id}`, data)
}

// 删除分类
export const deleteCategory = (id: number) => {
  return request.delete(`/api/categories/${id}`)
}