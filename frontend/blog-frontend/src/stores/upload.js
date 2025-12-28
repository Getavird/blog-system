// stores/upload.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as fileApi from '@/api/files'
import * as avatarApi from '@/api/avatar'

export const useUploadStore = defineStore('upload', () => {
  // 通用上传状态
  const uploadLoading = ref(false)
  const uploadError = ref(null)
  const uploadProgress = ref(0)
  
  // 文件列表状态
  const userFiles = ref([])
  const fileTotal = ref(0)
  const fileLoading = ref(false)
  
  // 上传配置
  const uploadConfig = ref({
    maxFileSize: 10485760, // 10MB
    allowedTypes: ['jpg', 'jpeg', 'png', 'gif', 'webp', 'pdf', 'doc', 'docx', 'txt'],
    maxFileSizeFormatted: '10MB'
  })
  
  // 头像上传状态
  const avatarUploading = ref(false)
  const avatarError = ref(null)
  
  // 默认头像列表
  const defaultAvatars = ref([])
  
  // 工具函数：格式化文件大小
  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + 'B'
    else if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
    else if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + 'MB'
    else return (bytes / (1024 * 1024 * 1024)).toFixed(1) + 'GB'
  }
  
  // 上传文件
  const uploadFile = async (file, usageType = 'general') => {
    try {
      uploadLoading.value = true
      uploadError.value = null
      uploadProgress.value = 0
      
      // 文件验证
      const fileExtension = file.name.split('.').pop().toLowerCase()
      if (!uploadConfig.value.allowedTypes.includes(fileExtension)) {
        throw new Error(`不支持的文件类型，仅支持: ${uploadConfig.value.allowedTypes.join(', ')}`)
      }
      
      if (file.size > uploadConfig.value.maxFileSize) {
        throw new Error(`文件大小不能超过 ${uploadConfig.value.maxFileSizeFormatted}`)
      }
      
      // 创建 FormData
      const formData = new FormData()
      formData.append('file', file)
      if (usageType) {
        formData.append('usageType', usageType)
      }
      
      // 模拟上传进度（实际项目中可能有进度事件）
      const interval = setInterval(() => {
        uploadProgress.value = Math.min(95, uploadProgress.value + 10)
      }, 200)
      
      const data = await fileApi.uploadFile(formData)
      
      clearInterval(interval)
      uploadProgress.value = 100
      
      // 添加到文件列表
      if (data) {
        userFiles.value.unshift(data)
        fileTotal.value += 1
      }
      
      return data
    } catch (error) {
      uploadError.value = error.response?.data?.message || error.message || '上传失败'
      console.error('上传文件失败:', error)
      throw error
    } finally {
      uploadLoading.value = false
      setTimeout(() => {
        uploadProgress.value = 0
      }, 500)
    }
  }
  
  // 批量上传
  const uploadMultipleFiles = async (files, usageType = 'general') => {
    try {
      uploadLoading.value = true
      uploadError.value = null
      
      const formData = new FormData()
      files.forEach(file => {
        formData.append('files', file)
      })
      formData.append('usageType', usageType)
      
      const data = await fileApi.batchUpload(formData)
      
      // 处理结果
      const successFiles = data.success || data.files || []
      const errors = data.errors || {}
      
      // 成功文件添加到列表
      if (successFiles.length > 0) {
        userFiles.value = [...successFiles, ...userFiles.value]
        fileTotal.value += successFiles.length
      }
      
      return {
        success: successFiles,
        errors: errors,
        total: files.length,
        successCount: successFiles.length,
        errorCount: Object.keys(errors).length
      }
    } catch (error) {
      uploadError.value = error.response?.data?.message || error.message || '批量上传失败'
      console.error('批量上传失败:', error)
      throw error
    } finally {
      uploadLoading.value = false
    }
  }
  
  // 获取用户文件列表
  const fetchUserFiles = async (params = {}) => {
    try {
      fileLoading.value = true
      const data = await fileApi.getUserFiles(params)
      
      if (data) {
        userFiles.value = data.files || data.list || data.data || []
        fileTotal.value = data.total || data.count || userFiles.value.length
      }
      
      return data
    } catch (error) {
      console.error('获取文件列表失败:', error)
      throw error
    } finally {
      fileLoading.value = false
    }
  }
  
  // 删除文件
  const deleteFile = async (id) => {
    try {
      await fileApi.deleteFile(id)
      
      // 从列表中移除
      userFiles.value = userFiles.value.filter(file => file.id !== id)
      fileTotal.value = Math.max(0, fileTotal.value - 1)
      
      return true
    } catch (error) {
      console.error('删除文件失败:', error)
      throw error
    }
  }
  
  // 获取上传配置
  const fetchUploadConfig = async () => {
    try {
      const config = await fileApi.getUploadConfig()
      if (config) {
        uploadConfig.value = {
          ...uploadConfig.value,
          ...config,
          maxFileSizeFormatted: formatFileSize(config.maxFileSize || uploadConfig.value.maxFileSize)
        }
      }
      return uploadConfig.value
    } catch (error) {
      console.error('获取上传配置失败:', error)
      return uploadConfig.value
    }
  }
  
  // 编辑器上传（富文本编辑器专用）
  const uploadForEditor = async (file) => {
    try {
      uploadLoading.value = true
      uploadError.value = null
      
      const formData = new FormData()
      formData.append('file', file)
      
      const data = await fileApi.editorUpload(formData)
      
      // 处理编辑器期望的响应格式
      if (data.errno === 0) {
        return {
          success: true,
          url: data.data?.url,
          alt: data.data?.alt
        }
      } else {
        throw new Error(data.message || '上传失败')
      }
    } catch (error) {
      uploadError.value = error.response?.data?.message || error.message || '编辑器上传失败'
      console.error('编辑器上传失败:', error)
      throw error
    } finally {
      uploadLoading.value = false
    }
  }
  
  // 上传头像
  const uploadAvatar = async (avatarFile) => {
    try {
      avatarUploading.value = true
      avatarError.value = null
      
      const data = await avatarApi.uploadAvatar(avatarFile)
      
      return data
    } catch (error) {
      avatarError.value = error.response?.data?.message || error.message || '头像上传失败'
      console.error('头像上传失败:', error)
      throw error
    } finally {
      avatarUploading.value = false
    }
  }
  
  // 获取头像信息
  const fetchAvatarInfo = async () => {
    try {
      const data = await avatarApi.getAvatarInfo()
      return data
    } catch (error) {
      console.error('获取头像信息失败:', error)
      throw error
    }
  }
  
  // 重置为默认头像
  const resetToDefaultAvatar = async () => {
    try {
      avatarUploading.value = true
      const data = await avatarApi.resetToDefaultAvatar()
      return data
    } catch (error) {
      console.error('重置头像失败:', error)
      throw error
    } finally {
      avatarUploading.value = false
    }
  }
  
  // 获取默认头像列表
  const fetchDefaultAvatars = async () => {
    try {
      const data = await avatarApi.getDefaultAvatars()
      defaultAvatars.value = data.defaults || data.list || data.data || []
      return defaultAvatars.value
    } catch (error) {
      console.error('获取默认头像列表失败:', error)
      return []
    }
  }
  
  // 清空上传状态
  const clearUploadState = () => {
    uploadError.value = null
    avatarError.value = null
    uploadProgress.value = 0
  }
  
  // 清空文件列表
  const clearFileList = () => {
    userFiles.value = []
    fileTotal.value = 0
  }
  
  return {
    // 状态
    uploadLoading,
    uploadError,
    uploadProgress,
    userFiles,
    fileTotal,
    fileLoading,
    uploadConfig,
    avatarUploading,
    avatarError,
    defaultAvatars,
    
    // 方法
    uploadFile,
    uploadMultipleFiles,
    fetchUserFiles,
    deleteFile,
    fetchUploadConfig,
    uploadForEditor,
    uploadAvatar,
    fetchAvatarInfo,
    resetToDefaultAvatar,
    fetchDefaultAvatars,
    clearUploadState,
    clearFileList,
    formatFileSize
  }
})