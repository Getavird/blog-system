// stores/avatar.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as avatarApi from '@/api/avatar'

export const useAvatarStore = defineStore('avatar', () => {
  // 上传状态
  const uploading = ref(false)
  const uploadError = ref(null)
  
  // 头像信息
  const avatarInfo = ref(null)
  const avatarInfoLoading = ref(false)
  
  // 默认头像
  const defaultAvatars = ref([])
  const defaultAvatarsLoading = ref(false)
  
  // 上传头像
  const uploadAvatar = async (avatarFile) => {
    try {
      uploading.value = true
      uploadError.value = null
      
      const data = await avatarApi.uploadAvatar(avatarFile)
      
      // 更新本地头像信息
      if (data) {
        avatarInfo.value = data
      }
      
      return data
    } catch (error) {
      uploadError.value = error.response?.data?.message || error.message || '头像上传失败'
      console.error('头像上传失败:', error)
      throw error
    } finally {
      uploading.value = false
    }
  }
  
  // 获取头像信息
  const fetchAvatarInfo = async () => {
    try {
      avatarInfoLoading.value = true
      const data = await avatarApi.getAvatarInfo()
      avatarInfo.value = data
      return data
    } catch (error) {
      console.error('获取头像信息失败:', error)
      throw error
    } finally {
      avatarInfoLoading.value = false
    }
  }
  
  // 重置为默认头像
  const resetToDefault = async () => {
    try {
      uploading.value = true
      const data = await avatarApi.resetToDefaultAvatar()
      
      // 更新本地头像信息
      if (data) {
        avatarInfo.value = data
      }
      
      return data
    } catch (error) {
      console.error('重置头像失败:', error)
      throw error
    } finally {
      uploading.value = false
    }
  }
  
  // 获取默认头像列表
  const fetchDefaultAvatars = async () => {
    try {
      defaultAvatarsLoading.value = true
      const data = await avatarApi.getDefaultAvatars()
      
      // 处理返回数据
      if (data.defaults) {
        defaultAvatars.value = Object.entries(data.defaults).map(([name, url]) => ({
          name,
          url
        }))
      } else if (Array.isArray(data)) {
        defaultAvatars.value = data
      } else if (data.list) {
        defaultAvatars.value = data.list
      } else if (data.data) {
        defaultAvatars.value = data.data
      }
      
      return defaultAvatars.value
    } catch (error) {
      console.error('获取默认头像列表失败:', error)
      return []
    } finally {
      defaultAvatarsLoading.value = false
    }
  }
  
  // 清除错误信息
  const clearError = () => {
    uploadError.value = null
  }
  
  // 清除头像信息
  const clearAvatarInfo = () => {
    avatarInfo.value = null
  }
  
  return {
    // 状态
    uploading,
    uploadError,
    avatarInfo,
    avatarInfoLoading,
    defaultAvatars,
    defaultAvatarsLoading,
    
    // 方法
    uploadAvatar,
    fetchAvatarInfo,
    resetToDefault,
    fetchDefaultAvatars,
    clearError,
    clearAvatarInfo
  }
})