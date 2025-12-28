// api/integration.js
import request from '@/utils/request'

/**
 * 测试Session共享
 * @returns {Promise}
 */
export const testSessionShare = () => {
  return request.get('/api/integration/session-share')
}

/**
 * 显示所有Session属性
 * @returns {Promise}
 */
export const dumpSession = () => {
  return request.get('/api/integration/session-dump')
}