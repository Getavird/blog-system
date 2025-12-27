// utils/auth.js（或者保持原路径）
// 这个文件现在只作为辅助工具函数，真正的认证状态管理在 Pinia Store 中

import { useUserStore } from '@/stores/user'

// 检查是否登录（兼容旧代码，推荐使用 userStore.isLoggedIn()）
export const isLoggedIn = () => {
  try {
    // 方法1：检查localStorage（简单但不够实时）
    const userStr = localStorage.getItem('blog_user')
    if (!userStr) return false
    
    const user = JSON.parse(userStr)
    return user && user.id && user.username
    
  } catch (error) {
    console.error('检查登录状态失败:', error)
    return false
  }
}

// 获取当前用户信息（兼容旧代码，推荐使用 userStore.user）
export const getCurrentUser = () => {
  try {
    const userStr = localStorage.getItem('blog_user')
    if (userStr) {
      return JSON.parse(userStr)
    }
    return null
  } catch (error) {
    console.error('获取用户信息失败:', error)
    return null
  }
}

// 设置用户信息（兼容旧代码，推荐使用 userStore.setUser(user)）
export const setAuth = (user) => {
  try {
    if (user) {
      localStorage.setItem('blog_user', JSON.stringify(user))
    } else {
      localStorage.removeItem('blog_user')
    }
  } catch (error) {
    console.error('设置用户信息失败:', error)
  }
}

// 清除用户信息（兼容旧代码，推荐使用 userStore.clearUser()）
export const clearAuth = () => {
  try {
    // 清除用户信息
    localStorage.removeItem('blog_user')
    
    // 如果还有旧token，一并清除
    localStorage.removeItem('blog_token')
    
    // 同时清除Pinia状态
    const userStore = useUserStore()
    userStore.clearUser()
    
  } catch (error) {
    console.error('清除认证信息失败:', error)
  }
}

// 验证登录状态（可选的扩展功能）
export const validateAuth = async () => {
  try {
    const userStore = useUserStore()
    
    // 如果store中已有用户，直接返回
    if (userStore.isLoggedIn()) {
      return true
    }
    
    // 否则尝试从localStorage恢复
    const user = getCurrentUser()
    if (user) {
      userStore.setUser(user)
      return true
    }
    
    return false
    
  } catch (error) {
    console.error('验证认证状态失败:', error)
    return false
  }
}

// 检查Session是否有效（需要后端接口支持）
export const checkSession = async () => {
  try {
    // 这里可以调用后端验证Session的接口
    // 例如：fetch('/api/user/check-session')
    // 如果没有专门的接口，可以尝试调用需要登录的接口
    
    const response = await fetch('/api/user/info', {
      method: 'GET',
      credentials: 'include' // 携带Cookie
    })
    
    return response.ok
    
  } catch (error) {
    console.error('检查Session失败:', error)
    return false
  }
}

// 设置/清除Cookie的辅助函数（如果需要）
export const cookieHelper = {
  // 获取Cookie
  get: (name) => {
    const value = `; ${document.cookie}`
    const parts = value.split(`; ${name}=`)
    if (parts.length === 2) return parts.pop().split(';').shift()
    return null
  },
  
  // 设置Cookie
  set: (name, value, days = 7) => {
    const expires = new Date(Date.now() + days * 24 * 60 * 60 * 1000).toUTCString()
    document.cookie = `${name}=${value}; expires=${expires}; path=/`
  },
  
  // 删除Cookie
  remove: (name) => {
    document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`
  }
}

// 监听认证状态变化（可选）
export const setupAuthListener = (callback) => {
  // 监听storage事件（跨标签页同步）
  window.addEventListener('storage', (event) => {
    if (event.key === 'blog_user') {
      const userStore = useUserStore()
      if (event.newValue) {
        try {
          userStore.setUser(JSON.parse(event.newValue))
        } catch (error) {
          console.error('同步用户信息失败:', error)
        }
      } else {
        userStore.clearUser()
      }
      
      if (callback) {
        callback(event.newValue ? JSON.parse(event.newValue) : null)
      }
    }
  })
}

export default {
  isLoggedIn,
  getCurrentUser,
  setAuth,
  clearAuth,
  validateAuth,
  checkSession,
  cookieHelper,
  setupAuthListener
}