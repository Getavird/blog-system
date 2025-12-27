import request from '@/utils/request'

// 获取所有标签
export const getAllTags = () => {
  return request.get('/api/tags')
}

// 标签详情
export const getTagDetail = (id) => {
  return request.get(`/api/tags/${id}`)
}

// 标签下的文章
export const getTagArticles = (id, params = {}) => {
  return request.get(`/api/tags/${id}/articles`, { 
    params: { page: 1, size: 10, ...params }
  })
}

// 创建标签
export const createTag = (name) => {
  return request.post('/api/tags', { name })
}

// 修改标签
export const updateTag = (id, name) => {
  return request.put(`/api/tags/${id}`, { name })
}

// 删除标签
export const deleteTag = (id) => {
  return request.delete(`/api/tags/${id}`)
}

// 获取标签云数据（如果后端有专门接口）
export const getTagCloud = () => {
  return request.get('/api/tags/cloud')
}

// 搜索标签
export const searchTags = (keyword) => {
  return request.get('/api/tags/search', { params: { keyword } })
}

// 根据标签名称搜索标签
export const searchTagByName = (name) => {
  return request.get('/api/tags/search', { params: { name } })
}