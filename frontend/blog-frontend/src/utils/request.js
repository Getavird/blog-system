import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true // 携带cookie
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('blog_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 根据后端格式调整
request.interceptors.response.use(
  response => {
    const res = response.data

    // 如果是文件上传请求，直接返回原响应
    if (response.config.url.includes('/api/files/upload') || 
        response.config.url.includes('/api/avatar/upload')) {
      return res
    }
    
    const { code, message, data } = res
    
    // 成功状态码：200或201
    if (code === 200 || code === 201) {
      // 返回实际数据
      return data
    }
    
    // 业务错误处理
    switch (code) {
      case 401:
        // 未登录，清除登录状态
        localStorage.removeItem('blog_token')
        localStorage.removeItem('blog_user')
        ElMessage.warning(message || '请先登录')
        router.push('/')
        break
      case 403:
        ElMessage.error(message || '权限不足')
        break
      case 404:
        ElMessage.error(message || '资源不存在')
        break
      default:
        ElMessage.error(message || '请求失败')
    }
    
    return Promise.reject(new Error(message || '请求失败'))
  },
  error => {
    // HTTP错误
    if (error.response) {
      const { status } = error.response
      switch (status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          router.push('/')
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error('请求失败')
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error('请求发送失败')
    }
    
    return Promise.reject(error)
  }
)

export default request