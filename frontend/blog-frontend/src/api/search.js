// api/search.js
import request from '@/utils/request'

/**
 * 全文搜索（文章+用户+标签）
 * @param {string} keyword - 搜索关键词
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export const fullSearch = (keyword, params = {}) => {
  const defaultParams = { page: 1, size: 20 }
  return request.get('/api/search/full', {
    params: { keyword, ...defaultParams, ...params }
  })
}

/**
 * 搜索文章
 * @param {string} keyword - 搜索关键词
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export const searchArticles = (keyword, params = {}) => {
  const defaultParams = { page: 1, size: 10 }
  return request.get('/api/search/articles', {
    params: { keyword, ...defaultParams, ...params }
  })
}

/**
 * 搜索用户
 * @param {string} keyword - 搜索关键词
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export const searchUsers = (keyword, params = {}) => {
  const defaultParams = { page: 1, size: 10 }
  return request.get('/api/search/users', {
    params: { keyword, ...defaultParams, ...params }
  })
}

/**
 * 搜索标签
 * @param {string} keyword - 搜索关键词
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export const searchTags = (keyword, params = {}) => {
  const defaultParams = { page: 1, size: 10 }
  return request.get('/api/search/tags', {
    params: { keyword, ...defaultParams, ...params }
  })
}

/**
 * 高级搜索文章
 * @param {Object} searchParams - 高级搜索参数
 * @returns {Promise}
 */
export const advancedSearchArticles = (searchParams = {}) => {
  const defaultParams = { page: 1, size: 10 }
  return request.get('/api/search/articles/advanced', {
    params: { ...defaultParams, ...searchParams }
  })
}

/**
 * 获取热门搜索关键词
 * @param {number} limit - 返回数量限制
 * @returns {Promise}
 */
export const getHotKeywords = (limit = 10) => {
  return request.get('/api/search/hot-keywords', {
    params: { limit }
  })
}

/**
 * 获取搜索建议（自动补全）
 * @param {string} prefix - 输入的前缀
 * @param {number} limit - 建议数量限制
 * @returns {Promise}
 */
export const getSearchSuggestions = (prefix, limit = 5) => {
  return request.get('/api/search/suggest', {
    params: { prefix, limit }
  })
}

/**
 * 获取当前用户的搜索历史
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export const getSearchHistory = (params = {}) => {
  const defaultParams = { limit: 20 }
  return request.get('/api/search/history', {
    params: { ...defaultParams, ...params }
  })
}

/**
 * 清空搜索记录（管理员权限）
 * @returns {Promise}
 */
export const clearSearchHistory = () => {
  return request.delete('/api/search/history')
}