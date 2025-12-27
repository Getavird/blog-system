// api/user.js
import request from '@/utils/request'

// 获取用户详情
export const getUserInfo = (id) => {
  return request.get(`/api/user/info/${id}`)
}

// 更新用户基本信息
export const updateUserInfo = (id, userData) => {
  return request.put(`/api/user/info/${id}`, userData)
}

// 上传用户头像
export const uploadAvatar = (file) => {
  // 构建表单数据（文件上传专用）
  const formData = new FormData()
  formData.append('avatar', file)
  return request.post('/api/user/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data' // 指定请求头
    }
  })
}

// 获取用户发布的文章
export const getUserArticles = (userId, params = {}) => {
  // 默认分页参数
  const defaultParams = { page: 1, size: 10, ...params }
  return request.get(`/api/user/articles/${userId}`, {
    params: defaultParams
  })
}

// 获取用户统计信息（文章数、点赞数等）
export const getUserStats = (userId) => {
  return request.get(`/api/user/stats/${userId}`)
}

// 获取用户登录历史
export const getLoginHistory = (userId, params = {}) => {
  const defaultParams = { page: 1, size: 10, ...params }
  return request.get(`/api/user/history/${userId}`, {
    params: defaultParams
  })
}

// 获取用户点赞的文章
export const getUserLikedArticles = (userId, params = {}) => {
  const defaultParams = { page: 1, size: 10, ...params }
  return request.get(`/api/user/liked/${userId}`, {
    params: defaultParams
  })
}