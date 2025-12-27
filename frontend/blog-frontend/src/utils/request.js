// utils/request.js
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
  withCredentials: true // 携带cookie，用于Session认证
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // ⚠️ Session认证不需要手动设置Authorization Header
    // Cookie会自动携带，后端通过Session识别用户
    
    // 调试日志
    console.log(`请求: ${config.method} ${config.url}`)
    console.log('withCredentials:', config.withCredentials)
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    console.log(`响应: ${response.status} ${response.config.url}`)
    
    const res = response.data

    // 如果是文件上传请求，直接返回原响应
    if (response.config.url.includes('/api/files/upload') || 
        response.config.url.includes('/api/avatar/upload') ||
        response.config.url.includes('/api/user/avatar')) {
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
        // Session认证：清除用户信息
        localStorage.removeItem('blog_user')
        
        // 显示错误信息
        ElMessage.warning(message || '请先登录')
        
        // 跳转到首页
        if (router.currentRoute.value.path !== '/') {
          router.push('/')
        }
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
    console.error('响应错误:', error)
    console.error('错误详情:', {
      status: error.response?.status,
      data: error.response?.data,
      headers: error.response?.headers
    })
    
    // HTTP错误
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          // Session认证：清除用户信息
          localStorage.removeItem('blog_user')
          
          ElMessage.error(data?.message || '未授权，请重新登录')
          
          // 跳转到首页
          if (router.currentRoute.value.path !== '/') {
            router.push('/')
          }
          break
        case 403:
          ElMessage.error(data?.message || '权限不足')
          break
        case 500:
          ElMessage.error(data?.message || '服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || `请求失败 (${status})`)
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