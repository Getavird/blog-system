// api/follow.js
import request from '@/utils/request'

/**
 * 关注用户
 * @param {number} userId - 要关注的用户ID
 * @returns {Promise}
 */
export const followUser = (userId) => {
  return request.post(`/api/follow/${userId}`)
}

/**
 * 取消关注
 * @param {number} userId - 要取消关注的用户ID
 * @returns {Promise}
 */
export const unfollowUser = (userId) => {
  return request.delete(`/api/follow/${userId}`)
}

/**
 * 检查是否关注某用户
 * @param {number} userId - 要检查的用户ID
 * @returns {Promise}
 */
export const checkFollowing = (userId) => {
  return request.get(`/api/follow/check/${userId}`)
}

/**
 * 获取关注列表（我关注的人）
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export const getFollowingList = (params = {}) => {
  const defaultParams = { page: 1, size: 10 }
  return request.get('/api/follow/following', {
    params: { ...defaultParams, ...params }
  })
}

/**
 * 获取粉丝列表（关注我的人）
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export const getFollowerList = (params = {}) => {
  const defaultParams = { page: 1, size: 10 }
  return request.get('/api/follow/followers', {
    params: { ...defaultParams, ...params }
  })
}

/**
 * 获取当前用户的关注数量统计
 * @returns {Promise}
 */
export const getFollowCounts = () => {
  return request.get('/api/follow/counts')
}

/**
 * 获取指定用户的关注数量统计（公开接口）
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export const getUserFollowCounts = (userId) => {
  return request.get(`/api/follow/counts/${userId}`)
}