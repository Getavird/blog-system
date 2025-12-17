import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as api from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref('')
  
  const initFromStorage = () => {
    const storedToken = localStorage.getItem('blog_token')
    const storedUser = localStorage.getItem('blog_user')
    
    if (storedToken) {
      token.value = storedToken
    }
    
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch (error) {
        console.error('解析用户数据失败:', error)
        user.value = null
      }
    }
  }
  
  // 登录方法
  const login = async (username, password) => {
    try {
      const response = await api.login({ username, password })
      
      if (response.token && response.user) {
        user.value = response.user
        token.value = response.token
        
        localStorage.setItem('blog_user', JSON.stringify(response.user))
        localStorage.setItem('blog_token', response.token)
        return { success: true, data: response }
      }
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }
  
  // 注册方法
  const register = async (data) => {
    try {
      const response = await api.register(data)
      return { success: true, data: response }
    } catch (error) {
      console.error('注册失败:', error)
      throw error
    }
  }
  
  // 获取用户信息
  const getUserInfo = async () => {
    try {
      const response = await api.getUserInfo()
      user.value = response
      localStorage.setItem('blog_user', JSON.stringify(response))
      return response
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  }
  
  // 退出登录
  const logout = () => {
    user.value = null
    token.value = ''
    
    localStorage.removeItem('blog_user')
    localStorage.removeItem('blog_token')
    
    // 调用后端登出API
    api.logout().catch(() => {})
  }
  
  const isLoggedIn = () => {
    return !!token.value
  }
  
  return {
    user,
    token,
    initFromStorage,
    login,
    register,
    getUserInfo,
    logout,
    isLoggedIn
  }
})