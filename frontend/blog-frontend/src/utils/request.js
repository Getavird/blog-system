// src/utils/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
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

// 响应拦截器
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    // 统一错误处理
    if (error.response) {
      const { status, data } = error.response
      
      if (status === 401) {
        // 未授权，清除登录状态
        localStorage.removeItem('blog_token')
        localStorage.removeItem('blog_user')
        ElMessage.error('登录已过期，请重新登录')
        // 跳转到登录页面
        window.location.href = '/'
      } else if (status === 403) {
        ElMessage.error('权限不足')
      } else if (status === 500) {
        ElMessage.error('服务器内部错误')
      } else {
        ElMessage.error(data.message || '请求失败')
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