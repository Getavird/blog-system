// api/files.js
import request from '@/utils/request'

/**
 * 上传文件
 * @param {FormData} formData - 包含文件的FormData
 * @param {string} usageType - 文件用途类型
 * @returns {Promise}
 */
export const uploadFile = (formData, usageType = 'general') => {
  // 如果formData中没有usageType，则添加
  if (!formData.has('usageType')) {
    formData.append('usageType', usageType)
  }
  
  return request.post('/api/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量上传文件
 * @param {FormData} formData - 包含多个文件的FormData
 * @param {string} usageType - 文件用途类型
 * @returns {Promise}
 */
export const batchUpload = (formData, usageType = 'general') => {
  if (!formData.has('usageType')) {
    formData.append('usageType', usageType)
  }
  
  return request.post('/api/files/upload/batch', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取文件详情
 * @param {number} id - 文件ID
 * @returns {Promise}
 */
export const getFileById = (id) => {
  return request.get(`/api/files/${id}`)
}

/**
 * 获取用户文件列表（分页）
 * @param {Object} params - 分页参数
 * @returns {Promise}
 */
export const getUserFiles = (params = {}) => {
  const defaultParams = { page: 1, size: 20 }
  return request.get('/api/files/my-files', {
    params: { ...defaultParams, ...params }
  })
}

/**
 * 删除文件
 * @param {number} id - 文件ID
 * @returns {Promise}
 */
export const deleteFile = (id) => {
  return request.delete(`/api/files/${id}`)
}

/**
 * 获取上传配置
 * @returns {Promise}
 */
export const getUploadConfig = () => {
  return request.get('/api/files/upload-config')
}

/**
 * 编辑器上传（wangEditor等富文本编辑器专用）
 * @param {File} file - 文件对象
 * @returns {Promise} - 返回编辑器期望的格式
 */
export const editorUpload = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post('/api/files/editor/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}