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
      // 调用真实API
      const response = await api.login({ username, password })

      // 假设后端返回格式：{ code: 200, message: 'success', data: { token, user } }
      const { token, user } = response

      if (token && user) {
        user.value = user
        token.value = token

        localStorage.setItem('blog_user', JSON.stringify(user))
        localStorage.setItem('blog_token', token)

        return { success: true, data: response }
      } else {
        throw new Error('登录响应格式错误')
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
      // 如果获取失败，清除登录状态
      logout()
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
    api.logout().catch(() => { })
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