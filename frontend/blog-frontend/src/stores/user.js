// stores/user.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as userApi from '@/api/user'

// 公开用户信息的数据结构
const createDefaultPublicUser = () => ({
  id: null,
  username: '',
  avatar: '',
  bio: '',
  createTime: '',
  isFollowed: false  // 当前登录用户是否关注了该用户
})

export const useUserStore = defineStore('user', () => {
  // 状态：当前登录用户信息
  const user = ref(null)
  const loading = ref(false)
  
  // 状态：公开用户信息（用于用户公开主页）
  const publicUser = ref(createDefaultPublicUser())
  const publicUserArticles = ref([])
  const publicUserStats = ref({
    articleCount: 0,
    likeCount: 0,
    viewCount: 0,
    followerCount: 0,
    followingCount: 0
  })
  const publicUserTotal = ref(0)
  const publicUserLoading = ref(false)

  // 初始化方法
  const initFromStorage = () => {
    const storedUser = localStorage.getItem('blog_user')
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch (error) {
        console.error('解析用户数据失败:', error)
        clearUser()
      }
    }
  }

  // 清空用户信息
  const clearUser = () => {
    user.value = null
    localStorage.removeItem('blog_user')
  }

  // 检查是否登录
  const isLoggedIn = () => {
    return !!user.value
  }

  // 设置用户信息
  const setUser = (userData) => {
    user.value = userData
    if (userData) {
      localStorage.setItem('blog_user', JSON.stringify(userData))
    } else {
      clearUser()
    }
  }

  // stores/user.js - 修改公开用户相关方法

// 获取公开用户信息
const fetchPublicUserInfo = async (username) => {
  try {
    publicUserLoading.value = true
    const data = await userApi.getPublicUserInfo(username)
    
    // 后端返回的是Result格式，需要处理data字段
    if (data && typeof data === 'object' && 'id' in data) {
      publicUser.value = { ...createDefaultPublicUser(), ...data }
    } else {
      // 如果后端返回的是Result包装，提取data
      publicUser.value = { ...createDefaultPublicUser(), ...(data?.data || data) }
    }
    
    return data
  } catch (error) {
    console.error('获取公开用户信息失败:', error)
    throw error
  } finally {
    publicUserLoading.value = false
  }
}

// 获取用户公开文章
const fetchPublicUserArticles = async (username, params = {}) => {
  try {
    publicUserLoading.value = true
    const data = await userApi.getPublicUserArticles(username, params)
    
    // 处理返回的数据结构
    const result = data?.data || data
    publicUserArticles.value = result.list || result.articles || result.data || []
    publicUserTotal.value = result.total || result.count || 0
    
    return data
  } catch (error) {
    console.error('获取用户公开文章失败:', error)
    throw error
  } finally {
    publicUserLoading.value = false
  }
}

// 获取用户公开统计
const fetchPublicUserStats = async (username) => {
  try {
    const data = await userApi.getPublicUserStats(username)
    
    // 处理返回的数据结构
    const stats = data?.data || data
    publicUserStats.value = { ...publicUserStats.value, ...stats }
    
    return data
  } catch (error) {
    console.error('获取用户统计失败:', error)
    throw error
  }
}

  // 检查关注状态
  const checkFollowStatus = async (userId) => {
    try {
      const data = await userApi.checkFollowStatus(userId)
      return data
    } catch (error) {
      console.error('检查关注状态失败:', error)
      throw error
    }
  }

  // 关注用户
  const followUser = async (userId) => {
    try {
      const data = await userApi.followUser(userId)
      // 更新本地状态
      if (publicUser.value.id === userId) {
        publicUser.value.isFollowed = true
        publicUserStats.value.followerCount = (publicUserStats.value.followerCount || 0) + 1
      }
      return data
    } catch (error) {
      console.error('关注用户失败:', error)
      throw error
    }
  }

  // 取消关注用户
  const unfollowUser = async (userId) => {
    try {
      const data = await userApi.unfollowUser(userId)
      // 更新本地状态
      if (publicUser.value.id === userId) {
        publicUser.value.isFollowed = false
        publicUserStats.value.followerCount = Math.max(0, (publicUserStats.value.followerCount || 1) - 1)
      }
      return data
    } catch (error) {
      console.error('取消关注失败:', error)
      throw error
    }
  }

  // 获取关注数量
  const fetchFollowCounts = async (userId) => {
    try {
      const data = await userApi.getFollowCounts(userId)
      return data
    } catch (error) {
      console.error('获取关注数量失败:', error)
      throw error
    }
  }

  // 清空公开用户数据
  const clearPublicUserData = () => {
    publicUser.value = createDefaultPublicUser()
    publicUserArticles.value = []
    publicUserStats.value = {
      articleCount: 0,
      likeCount: 0,
      viewCount: 0,
      followerCount: 0,
      followingCount: 0
    }
    publicUserTotal.value = 0
  }

  // 上传用户头像
const uploadAvatar = async (file) => {
  try {
    loading.value = true
    const formData = new FormData()
    formData.append('avatar', file)
    
    const data = await userApi.uploadAvatar(formData)
    
    // 更新本地用户信息
    if (user.value) {
      user.value.avatar = data.avatar || data.url || ''
    }
    
    return data
  } catch (error) {
    console.error('上传头像失败:', error)
    throw error
  } finally {
    loading.value = false
  }
}

// 更新用户基本信息
const updateUserInfo = async (id, userData) => {
  try {
    loading.value = true
    const data = await userApi.updateProfile(userData)
    
    // 更新本地用户信息
    if (user.value && user.value.id === id) {
      Object.assign(user.value, userData)
    }
    
    return data
  } catch (error) {
    console.error('更新用户信息失败:', error)
    throw error
  } finally {
    loading.value = false
  }
}
  return {
    // 当前用户状态
    user,
    loading,
    
    // 公开用户状态
    publicUser,
    publicUserArticles,
    publicUserStats,
    publicUserTotal,
    publicUserLoading,
    
    // 方法
    uploadAvatar,
    updateUserInfo,
    initFromStorage,
    clearUser,
    isLoggedIn,
    setUser,
    
    // 公开用户方法
    fetchPublicUserInfo,
    fetchPublicUserArticles,
    fetchPublicUserStats,
    checkFollowStatus,
    followUser,
    unfollowUser,
    fetchFollowCounts,
    clearPublicUserData
  }
})