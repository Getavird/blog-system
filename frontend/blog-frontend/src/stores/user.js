import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as userApi from '@/api/user'

// 头像URL处理工具函数
const normalizeAvatarUrl = (avatarPath) => {
  if (!avatarPath || avatarPath === 'null' || avatarPath === 'undefined' || avatarPath.trim() === '') {
    return 'http://localhost:8080/static/images/default-avatars/default_avatar.png'
  }
  
  // 已经是完整URL直接返回
  if (avatarPath.startsWith('http://') || 
      avatarPath.startsWith('https://') || 
      avatarPath.startsWith('data:')) {
    return avatarPath
  }
  
  let fullUrl = avatarPath
  
  // 情况1：路径以/uploads/avatars/开头（相对路径）
  if (fullUrl.startsWith('/uploads/avatars/')) {
    fullUrl = 'http://localhost:8080' + fullUrl
  }
  // 情况2：只有文件名（如default_avatar.png）
  else if (!fullUrl.includes('/') && !fullUrl.includes('\\')) {
    if (fullUrl.includes('default_avatar')) {
      fullUrl = 'http://localhost:8080/static/images/default-avatars/default_avatar.png'
    } else {
      fullUrl = 'http://localhost:8080/uploads/avatars/' + fullUrl
    }
  }
  // 情况3：其他格式的路径
  else if (fullUrl.startsWith('/')) {
    fullUrl = 'http://localhost:8080' + fullUrl
  }
  // 情况4：Windows风格的路径或其他
  else {
    fullUrl = fullUrl.replace(/\\/g, '/')
    if (fullUrl.startsWith('uploads/avatars/')) {
      fullUrl = 'http://localhost:8080/' + fullUrl
    } else if (fullUrl.startsWith('/')) {
      fullUrl = 'http://localhost:8080' + fullUrl
    }
  }
  
  return fullUrl
}

// 创建默认公开用户
const createDefaultPublicUser = () => ({
  id: null,
  username: '',
  avatar: '',
  bio: '',
  createTime: '',
  isFollowed: false
})

export const useUserStore = defineStore('user', () => {
  // 当前登录用户信息
  const user = ref(null)
  const loading = ref(false)
  
  // 公开用户信息
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
        const parsedUser = JSON.parse(storedUser)
        // 确保存储的头像URL是完整的
        if (parsedUser.avatar) {
          parsedUser.avatar = normalizeAvatarUrl(parsedUser.avatar)
        }
        user.value = parsedUser
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

  // 设置用户信息（关键方法）
  const setUser = (userData) => {
    if (!userData) {
      clearUser()
      return
    }
    
    // 处理头像URL
    const processedUser = { ...userData }
    if (processedUser.avatar) {
      processedUser.avatar = normalizeAvatarUrl(processedUser.avatar)
    }
    
    user.value = processedUser
    
    // 保存到localStorage
    const userForStorage = {
      id: processedUser.id,
      username: processedUser.username,
      avatar: processedUser.avatar,
      email: processedUser.email,
      bio: processedUser.bio,
      createTime: processedUser.createTime
    }
    
    localStorage.setItem('blog_user', JSON.stringify(userForStorage))
  }

  // 上传头像
  const uploadAvatar = async (file) => {
    try {
      loading.value = true
      const formData = new FormData()
      formData.append('avatar', file)
      
      const response = await userApi.uploadAvatar(formData)
      const data = response?.data || response
      
      // 确保返回完整URL
      if (data && (data.avatar || data.url)) {
        const avatarPath = data.avatar || data.url
        const fullAvatarUrl = normalizeAvatarUrl(avatarPath)
        
        // 更新当前用户信息
        if (user.value) {
          user.value.avatar = fullAvatarUrl
          // 重新保存到storage
          localStorage.setItem('blog_user', JSON.stringify(user.value))
        }
        
        return { ...data, avatar: fullAvatarUrl }
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
      const response = await userApi.updateUserInfo(userData)
      const data = response?.data || response
      
      // 更新本地用户信息
      if (user.value && user.value.id === id) {
        user.value = { ...user.value, ...userData }
        localStorage.setItem('blog_user', JSON.stringify(user.value))
      }
      
      return data
    } catch (error) {
      console.error('更新用户信息失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取公开用户信息
  const fetchPublicUserInfo = async (username, forceRefresh = false) => {
    try {
      publicUserLoading.value = true
      
      // 如果强制刷新，添加时间戳避免缓存
      const params = forceRefresh ? { t: Date.now() } : {}
      
      const response = await userApi.getPublicUserInfo(username, params)
      
      console.log('fetchPublicUserInfo 响应:', response)
      
      let userData = null
      
      // 处理返回的数据格式
      if (response && response.code === 200) {
        userData = response.data
      } else if (response) {
        userData = response
      }
      
      if (userData) {
        // 使用统一的 normalizeAvatarUrl 函数处理头像
        userData.avatar = normalizeAvatarUrl(userData.avatar)
        
        // 添加时间戳避免缓存
        const separator = userData.avatar.includes('?') ? '&' : '?'
        userData.avatar = userData.avatar + separator + 't=' + Date.now()
        
        publicUser.value = userData
      }
      
      return userData
    } catch (error) {
      console.error('获取公开用户信息失败:', error)
      throw error
    } finally {
      publicUserLoading.value = false
    }
  }

  // 获取用户公开文章
  const fetchPublicUserArticles = async (username, params) => {
    try {
      publicUserLoading.value = true
      const data = await userApi.getPublicUserArticles(username, params)
      return data
    } catch (error) {
      console.error('获取公开用户文章失败:', error)
      throw error
    } finally {
      publicUserLoading.value = false
    }
  }

  // 获取用户公开统计
  const fetchPublicUserStats = async (username, forceRefresh = false) => {
    try {
      publicUserLoading.value = true
      
      // 如果强制刷新，添加时间戳避免缓存
      const params = forceRefresh ? { t: Date.now() } : {}
      
      const response = await userApi.getPublicUserStats(username, params)
      
      let statsData = null
      
      // 处理返回的数据格式
      if (response && response.code === 200) {
        statsData = response.data
      } else if (response) {
        statsData = response
      }
      
      if (statsData) {
        publicUserStats.value = statsData
      }
      
      return statsData
    } catch (error) {
      console.error('获取公开用户统计失败:', error)
      throw error
    } finally {
      publicUserLoading.value = false
    }
  }

  // 清除公开用户统计缓存
  const clearPublicUserStats = () => {
    console.log('清除公开用户统计缓存')
    publicUserStats.value = {
      articleCount: 0,
      likeCount: 0,
      viewCount: 0,
      followerCount: 0,
      followingCount: 0
    }
  }

  // 清除公开用户信息缓存
  const clearPublicUserInfo = () => {
    console.log('清除公开用户信息缓存')
    publicUser.value = createDefaultPublicUser()
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

  // 获取当前用户统计信息
  const fetchCurrentUserStats = async () => {
    try {
      const response = await userApi.getCurrentUserStats()
      return response?.data || response
    } catch (error) {
      console.error('获取当前用户统计失败:', error)
      throw error
    }
  }

  // 获取当前用户状态
  const fetchCurrentUserStatus = async () => {
    try {
      const response = await userApi.getCurrentUserStatus()
      return response?.data || response
    } catch (error) {
      console.error('获取当前用户状态失败:', error)
      throw error
    }
  }

  // 设置用户头像
  const setAvatar = (avatarUrl) => {
    if (user.value) {
      user.value.avatar = avatarUrl
      saveUserToStorage()
    }
  }

  // 直接更新用户头像（Pinia共享状态最佳实践）
  const updateUserAvatar = (avatarUrl) => {
    if (user.value) {
      user.value.avatar = avatarUrl
      // 兼容本地存储
      localStorage.setItem('blog_user', JSON.stringify({
        ...user.value,
        avatar: avatarUrl
      }))
    }
  }

  // 清除头像
  const clearAvatar = () => {
    if (user.value) {
      user.value.avatar = null
      saveUserToStorage()
    }
  }

  // 更新用户头像
  const updateAvatar = async (avatarFile) => {
    try {
      const formData = new FormData()
      formData.append('avatar', avatarFile)
      
      const response = await userApi.updateAvatar(formData)
      
      if (response.data) {
        // 更新本地用户信息
        if (user.value) {
          user.value.avatar = response.data.avatar
          saveUserToStorage()
        }
        return response.data
      }
    } catch (error) {
      console.error('更新头像失败:', error)
      throw error
    }
  }

  // 设置公开用户文章
  const setPublicUserArticles = (articles) => {
    publicUserArticles.value = articles
  }

  // 设置公开用户统计
  const setPublicUserStats = (stats) => {
    publicUserStats.value = stats
  }

  // 设置公开用户文章总数
  const setPublicUserTotal = (total) => {
    publicUserTotal.value = total
  }

  // 同步用户统计信息
  const syncUserStats = async (userId) => {
    try {
      console.log('同步用户统计信息，用户ID:', userId)
      
      // 获取用户store
      const userStore = useUserStore()
      
      // 1. 如果是当前用户
      if (userStore.user && userStore.user.id === userId) {
        console.log('同步当前用户统计')
        
        // 并行获取多种统计信息
        await Promise.allSettled([
          userStore.fetchCurrentUserStats(),
          userStore.fetchCurrentUserStatus(),
        ])
      }
      
      // 2. 如果是公开用户
      if (userStore.publicUser.value && userStore.publicUser.value.id === userId) {
        const username = userStore.publicUser.value.username
        if (username) {
          console.log('同步公开用户统计，用户名:', username)
          
          // 并行获取公开用户统计信息
          await Promise.allSettled([
            userStore.fetchPublicUserStats(username, true), // 强制刷新
            userStore.fetchPublicUserInfo(username, true)   // 强制刷新
          ])
        }
      }
      
      console.log('用户统计信息同步完成')
      
      // 触发事件通知其他组件
      window.dispatchEvent(new CustomEvent('user-stats-updated', {
        detail: { userId }
      }))
      
      return true
    } catch (error) {
      console.error('同步用户统计信息失败:', error)
      return false
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

  // 保存用户信息到本地存储
  const saveUserToStorage = () => {
    if (user.value) {
      localStorage.setItem('blog_user', JSON.stringify(user.value))
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
    syncUserStats,
    
    // 公开用户方法
    setPublicUserArticles,
    setPublicUserStats,
    setPublicUserTotal,
    fetchPublicUserInfo,
    fetchPublicUserArticles,
    fetchPublicUserStats,
    checkFollowStatus,
    followUser,
    unfollowUser,
    fetchFollowCounts,
    clearPublicUserData,
    
    // 新增清理方法
    clearPublicUserStats,
    clearPublicUserInfo,
    
    // 当前用户方法
    fetchCurrentUserStats,
    fetchCurrentUserStatus,
    setAvatar,
    clearAvatar,
    updateAvatar,
    updateUserAvatar,
    
    // 其他方法
    saveUserToStorage
  }
})