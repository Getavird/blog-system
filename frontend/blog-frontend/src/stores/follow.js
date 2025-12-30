// stores/follow.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as followApi from '@/api/follow'
import { useUserStore } from './user'

export const useFollowStore = defineStore('follow', () => {
  // 依赖其他store
  const userStore = useUserStore()
  
  // 关注状态
  const followingLoading = ref(false)
  const unfollowLoading = ref(false)
  const followError = ref(null)
  
  // 关注列表
  const followingList = ref([])      // 我关注的人
  const followerList = ref([])       // 关注我的人
  
  // 分页信息
  const followingPagination = ref({
    page: 1,
    size: 10,
    total: 0,
    totalPages: 0
  })
  
  const followerPagination = ref({
    page: 1,
    size: 10,
    total: 0,
    totalPages: 0
  })
  
  // 关注统计
  const followStats = ref({
    followingCount: 0,
    followerCount: 0
  })
  
  // 关注关系缓存（用户ID -> 是否关注）
  const followStatusCache = ref({})
  
  // 检查是否已登录
  const checkLogin = () => {
  if (!userStore.user || !userStore.user.id) {
    throw new Error('请先登录')
  }
}
  
  // 关注用户
const followUser = async (userId) => {
  try {
    checkLogin()
    followingLoading.value = true
    followError.value = null
    
    // 添加参数验证
    if (!userId) {
      throw new Error('用户ID不能为空')
    }
    
    const data = await followApi.followUser(userId)
    
    // 更新缓存
    followStatusCache.value[userId] = true
    
    // 更新统计
    followStats.value.followingCount += 1
    
    // ✅ 修复：移除 .value，直接访问 publicUser
    // 如果需要，更新用户store中的公开用户状态
    if (userStore.publicUser && userStore.publicUser.id === userId) {
      userStore.publicUser.isFollowed = true
      // 确保 userStore.publicUserStats 存在
      if (userStore.publicUserStats) {
        userStore.publicUserStats.followerCount = (userStore.publicUserStats.followerCount || 0) + 1
      }
    }
    
    return data
  } catch (error) {
    followError.value = error.response?.data?.message || error.message || '关注失败'
    console.error('关注用户失败:', error)
    throw error
  } finally {
    followingLoading.value = false
  }
}
  
// 取消关注
const unfollowUser = async (userId) => {
  try {
    checkLogin()
    unfollowLoading.value = true
    followError.value = null
    
    // 添加参数验证
    if (!userId) {
      throw new Error('用户ID不能为空')
    }
    
    const data = await followApi.unfollowUser(userId)
    
    // 更新缓存
    followStatusCache.value[userId] = false
    
    // 更新统计
    followStats.value.followingCount = Math.max(0, followStats.value.followingCount - 1)
    
    // 从关注列表中移除
    followingList.value = followingList.value.filter(user => user.id !== userId)
    
    // ✅ 修复：移除 .value，直接访问 publicUser
    // 如果需要，更新用户store中的公开用户状态
    if (userStore.publicUser && userStore.publicUser.id === userId) {
      userStore.publicUser.isFollowed = false
      // 确保 userStore.publicUserStats 存在
      if (userStore.publicUserStats) {
        userStore.publicUserStats.followerCount = Math.max(0, (userStore.publicUserStats.followerCount || 1) - 1)
      }
    }
    
    return data
  } catch (error) {
    followError.value = error.response?.data?.message || error.message || '取消关注失败'
    console.error('取消关注失败:', error)
    throw error
  } finally {
    unfollowLoading.value = false
  }
}
  
 // 检查关注状态
const checkFollowStatus = async (userId) => {
  try {
    checkLogin()
    
    // 添加参数验证
    if (!userId) {
      console.warn('用户ID为空，无法检查关注状态')
      return false
    }
    
    // 先检查缓存
    if (followStatusCache.value[userId] !== undefined) {
      return followStatusCache.value[userId]
    }
    
    const data = await followApi.checkFollowing(userId)
    const isFollowing = data?.isFollowing || data?.following || false
    
    // 更新缓存
    followStatusCache.value[userId] = isFollowing
    
    return isFollowing
  } catch (error) {
    console.error('检查关注状态失败:', error)
    return false
  }
}
  
  // 获取关注列表（我关注的人）
  const fetchFollowingList = async (page = 1, size = 10) => {
    try {
      checkLogin()
      followingLoading.value = true
      
      const data = await followApi.getFollowingList({ page, size })
      
      // 处理返回数据
      if (data) {
        followingList.value = data.list || data.following || data.data || []
        
        followingPagination.value = {
          page: data.page || page,
          size: data.size || size,
          total: data.total || data.count || 0,
          totalPages: Math.ceil((data.total || data.count || 0) / (data.size || size))
        }
        
        // 更新缓存
        followingList.value.forEach(user => {
          followStatusCache.value[user.id] = true
        })
      }
      
      return data
    } catch (error) {
      console.error('获取关注列表失败:', error)
      throw error
    } finally {
      followingLoading.value = false
    }
  }
  
  // 获取粉丝列表（关注我的人）
  const fetchFollowerList = async (page = 1, size = 10) => {
    try {
      checkLogin()
      followingLoading.value = true
      
      const data = await followApi.getFollowerList({ page, size })
      
      if (data) {
        followerList.value = data.list || data.followers || data.data || []
        
        followerPagination.value = {
          page: data.page || page,
          size: data.size || size,
          total: data.total || data.count || 0,
          totalPages: Math.ceil((data.total || data.count || 0) / (data.size || size))
        }
      }
      
      return data
    } catch (error) {
      console.error('获取粉丝列表失败:', error)
      throw error
    } finally {
      followingLoading.value = false
    }
  }
  
  // 获取关注统计
  const fetchFollowStats = async () => {
    try {
      checkLogin()
      
      const data = await followApi.getFollowCounts()
      
      if (data) {
        followStats.value = {
          followingCount: data.followingCount || data.following || 0,
          followerCount: data.followerCount || data.followers || 0
        }
      }
      
      return data
    } catch (error) {
      console.error('获取关注统计失败:', error)
      throw error
    }
  }
  
  // 获取指定用户的关注统计（公开）
  const fetchUserFollowStats = async (userId) => {
    try {
      const data = await followApi.getUserFollowCounts(userId)
      
      if (data) {
        return {
          followingCount: data.followingCount || data.following || 0,
          followerCount: data.followerCount || data.followers || 0
        }
      }
      
      return { followingCount: 0, followerCount: 0 }
    } catch (error) {
      console.error('获取用户关注统计失败:', error)
      return { followingCount: 0, followerCount: 0 }
    }
  }
  
// 切换关注状态
const toggleFollow = async (userId, currentStatus = null) => {
  try {
    // 参数验证
    if (!userId) {
      throw new Error('用户ID不能为空')
    }
    
    // 检查登录状态
    checkLogin()
    
    // 获取当前状态
    if (currentStatus === null) {
      currentStatus = await checkFollowStatus(userId)
    }
    
    // 根据状态执行操作
    if (currentStatus) {
      await unfollowUser(userId)
      return false
    } else {
      await followUser(userId)
      return true
    }
  } catch (error) {
    console.error('切换关注状态失败:', error)
    throw error
  }
}
  
  // 清空关注列表
  const clearFollowLists = () => {
    followingList.value = []
    followerList.value = []
    followingPagination.value = {
      page: 1,
      size: 10,
      total: 0,
      totalPages: 0
    }
    followerPagination.value = {
      page: 1,
      size: 10,
      total: 0,
      totalPages: 0
    }
  }
  
  // 清空缓存
  const clearCache = () => {
    followStatusCache.value = {}
  }
  
  // 计算属性：是否正在关注操作中
  const isFollowingLoading = computed(() => followingLoading.value || unfollowLoading.value)
  
  return {
    // 状态
    followingLoading,
    unfollowLoading,
    followError,
    followingList,
    followerList,
    followingPagination,
    followerPagination,
    followStats,
    
    // 计算属性
    isFollowingLoading,
    
    // 方法
    followUser,
    unfollowUser,
    checkFollowStatus,
    fetchFollowingList,
    fetchFollowerList,
    fetchFollowStats,
    fetchUserFollowStats,
    toggleFollow,
    clearFollowLists,
    clearCache
  }
})