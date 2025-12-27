// store/user.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as userApi from '@/api/user'

// 本地存储键名常量（仅保留用户信息）
const STORAGE_KEYS = {
  USER: 'blog_user'
}

// 工具函数：统一处理加载状态和错误捕获
const withLoading = (loadingRef, fn) => {
  return async (...args) => {
    try {
      loadingRef.value = true
      return await fn(...args)
    } catch (error) {
      console.error(`用户操作失败:`, error)
      throw error // 继续抛出，供上层组件处理
    } finally {
      loadingRef.value = false
    }
  }
}

export const useUserStore = defineStore('user', () => {
  // 状态：仅保留用户信息和加载状态
  const user = ref(null)
  const loading = ref(false)

  // 从本地存储初始化用户信息
  const initFromStorage = () => {
    const storedUser = localStorage.getItem(STORAGE_KEYS.USER)
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch (error) {
        console.error('解析用户数据失败，已清除无效数据:', error)
        localStorage.removeItem(STORAGE_KEYS.USER)
        user.value = null
      }
    }
  }

  // 初始化时自动加载本地数据
  initFromStorage()

  // 获取用户详情
  const fetchUserInfo = withLoading(loading, async (id) => {
    return await userApi.getUserInfo(id)
  })

  // 更新用户基本信息
  const updateUserInfo = withLoading(loading, async (id, userData) => {
    const data = await userApi.updateUserInfo(id, userData)
    // 同步更新本地用户信息
    if (user.value && user.value.id === id) {
      user.value = { ...user.value, ...userData }
      localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user.value))
    }
    return data
  })

  // 上传用户头像
  const uploadAvatar = withLoading(loading, async (file) => {
    const data = await userApi.uploadAvatar(file)
    // 同步更新本地头像
    if (user.value && data.avatar) {
      user.value.avatar = data.avatar
      localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user.value))
    }
    return data
  })

  // 获取用户发布的文章
  const fetchUserArticles = withLoading(loading, async (userId, params = {}) => {
    return await userApi.getUserArticles(userId, params)
  })

  // 获取用户统计信息
  const fetchUserStats = withLoading(loading, async (userId) => {
    return await userApi.getUserStats(userId)
  })

  // 获取用户登录历史
  const fetchLoginHistory = withLoading(loading, async (userId, params = {}) => {
    return await userApi.getLoginHistory(userId, params)
  })

  // 获取用户点赞的文章
  const fetchUserLikedArticles = withLoading(loading, async (userId, params = {}) => {
    return await userApi.getUserLikedArticles(userId, params)
  })

  // 设置用户信息（同步到本地存储）
  const setUser = (userData) => {
    user.value = userData
    if (userData) {
      localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(userData))
    } else {
      localStorage.removeItem(STORAGE_KEYS.USER)
    }
  }

  // 清除用户信息（退出登录时调用）
  const clearUser = () => {
    user.value = null
    localStorage.removeItem(STORAGE_KEYS.USER)
  }

  // 检查是否登录（基于用户信息是否存在）
  const isLoggedIn = () => {
    return !!user.value
  }

  return {
    // 状态
    user,
    loading,
    // 方法
    initFromStorage,
    fetchUserInfo,
    updateUserInfo,
    uploadAvatar,
    fetchUserArticles,
    fetchUserStats,
    fetchLoginHistory,
    fetchUserLikedArticles,
    setUser,
    clearUser,
    isLoggedIn
  }
})