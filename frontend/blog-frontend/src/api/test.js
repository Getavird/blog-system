import request from '@/utils/request'

// 测试后端状态
export const getBackendStatus = () => {
  return request.get('/api/test/status')
}

// 数据库状态
export const getDbStatus = () => {
  return request.get('/api/debug/db-status')
}