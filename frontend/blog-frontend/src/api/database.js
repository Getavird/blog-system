// api/database.js
import request from '@/utils/request'

/**
 * 检查数据库连接状态
 * @returns {Promise}
 */
export const checkDatabaseStatus = () => {
  return request.get('/api/debug/db-status')
}

/**
 * 测试数据库连接（HTML格式）
 * @returns {Promise}
 */
export const testDatabaseConnection = () => {
  return request.get('/api/debug/db-test')
}