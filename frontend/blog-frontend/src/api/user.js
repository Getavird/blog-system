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

// 用户公开信息相关的API
// ===============================================

/**
 * 获取公开用户信息（通过用户名）
 * 后端路径：GET /api/user/public/{username}
 * 返回：Result<UserPublicVO>
 */
export const getPublicUserInfo = (username) => {
  return request.get(`/api/user/public/${username}`)  
}

/**
 * 获取用户公开文章列表
 * 后端路径：GET /api/user/public/{username}/articles
 * 返回：Result<PageResult<ArticlePublicVO>>
 */
export const getPublicUserArticles = (username, params = {}) => {
  const defaultParams = { page: 1, size: 10, ...params }
  return request.get(`/api/user/public/${username}/articles`, { 
    params: defaultParams
  })
}

/**
 * 获取用户公开统计
 * 后端路径：GET /api/user/public/{username}/stats
 * 返回：Result<UserStatsVO>
 */
export const getPublicUserStats = (username) => {
  return request.get(`/api/user/public/${username}/stats`) 
}

/**
 * 检查是否关注用户（需要登录）
 * @param {string|number} userId - 用户ID
 * @returns Promise
 */
export const checkFollowStatus = (userId) => {
  return request.get(`/api/follow/check/${userId}`)
}

/**
 * 关注用户（需要登录）
 * @param {string|number} userId - 用户ID
 * @returns Promise
 */
export const followUser = (userId) => {
  return request.post(`/api/follow/${userId}`)
}

/**
 * 取消关注用户（需要登录）
 * @param {string|number} userId - 用户ID
 * @returns Promise
 */
export const unfollowUser = (userId) => {
  return request.delete(`/api/follow/${userId}`)
}

/**
 * 获取用户关注/粉丝数量（公开）
 * @param {string|number} userId - 用户ID
 * @returns Promise
 */
export const getFollowCounts = (userId) => {
  return request.get(`/api/follow/counts/${userId}`)
}