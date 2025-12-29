import request from '@/utils/request'

// 获取所有标签
export const getAllTags = () => {
  return request.get('/api/tags')
}

// 标签详情（通过ID）
export const getTagDetail = (id) => {
  return request.get(`/api/tags/${id}`)
}

// 标签详情（通过名称） - 新增
export const getTagDetailByName = (name) => {
  return request.get(`/api/tags/name/${name}/detail`)
}

// 标签下的文章（通过ID）
export const getTagArticles = (id, params = {}) => {
  return request.get(`/api/tags/${id}/articles`, { 
    params: { page: 1, size: 10, ...params }
  })
}

// 标签下的文章（通过名称） - 新增
export const getTagArticlesByName = (name, params = {}) => {
  // 映射排序参数
  const sortMapping = {
    createTime: 'latest',
    viewCount: 'hot',
    likeCount: 'likes'
  }
  
  const mappedParams = {
    page: params.page || 1,
    size: params.size || 15,
    sort: sortMapping[params.sort] || 'latest',
    ...params
  }
  
  return request.get(`/api/tags/name/${name}/articles`, { 
    params: mappedParams
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

// 获取标签云数据
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