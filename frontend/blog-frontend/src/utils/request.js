import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
// 注意：使用相对路径，让Vite代理处理
const request = axios.create({
  baseURL: '/', // 使用相对路径，代理会转发到后端
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true // 携带cookie，用于Session认证
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 调试日志 - 简化
    console.log(`请求: ${config.method} ${config.url}`)
    
    // 确保所有API请求都以/api开头
    if (config.url && !config.url.startsWith('/api') && !config.url.includes('http')) {
      config.url = '/api' + config.url
    }
    
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
    if (response.config.url.includes('/upload') || 
        response.config.url.includes('/avatar')) {
      return res
    }
    
    // 如果后端返回的是Result对象格式
    if (res && (res.code !== undefined || res.success !== undefined)) {
      const { code, success, message, data } = res
      
      // 成功状态码：200或201，或者success为true
      if (code === 200 || code === 201 || success === true) {
        // 返回实际数据（可能嵌套在data字段中）
        return data !== undefined ? data : res
      }
      
      // 业务错误处理
      if (code === 401) {
        // Session认证：清除用户信息
        localStorage.removeItem('blog_user')
        
        // 显示错误信息
        ElMessage.warning(message || '请先登录')
        
        // 跳转到首页
        if (router.currentRoute.value.path !== '/') {
          router.push({
            path: '/',
            query: {
              showLogin: true,
              redirect: router.currentRoute.value.fullPath
            }
          })
        }
        return Promise.reject(new Error(message || '请先登录'))
      }
      
      // 其他错误
      ElMessage.error(message || '操作失败')
      return Promise.reject(new Error(message || '操作失败'))
    }
    
    // 如果没有code/success字段，直接返回数据
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      console.error('HTTP错误详情:', {
        status: status,
        data: data,
        url: error.config?.url
      })
      
      switch (status) {
        case 401:
          // Session认证：清除用户信息
          localStorage.removeItem('blog_user')
          
          ElMessage.error(data?.message || '请先登录')
          
          // 跳转到首页
          if (router.currentRoute.value.path !== '/') {
            router.push({
              path: '/',
              query: {
                showLogin: true,
                redirect: router.currentRoute.value.fullPath
              }
            })
          }
          break
        case 403:
          ElMessage.error(data?.message || '权限不足')
          break
        case 404:
          // 对于特定的API错误，不显示提示
          if (error.config.url?.includes('/view')) {
            console.warn('阅读量接口不存在，跳过')
          } else {
            ElMessage.error(data?.message || '请求的资源不存在')
          }
          break
        case 500:
          ElMessage.error(data?.message || '服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || `请求失败 (${status})`)
      }
    } else if (error.code === 'ERR_NETWORK') {
      ElMessage.error('网络连接失败，请检查后端服务是否启动')
    } else if (error.message === 'Network Error') {
      ElMessage.error('网络错误，请检查代理配置和后端服务')
    } else {
      ElMessage.error(error.message || '请求发送失败')
    }
    
    return Promise.reject(error)
  }
)

export default request