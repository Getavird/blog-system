// api/avatar.js
import request from '@/utils/request'

/**
 * 上传头像
 * @param {File} avatarFile - 头像文件
 * @returns {Promise}
 */
export const uploadAvatar = (avatarFile) => {
  const formData = new FormData()
  formData.append('avatar', avatarFile)
  
  return request.post('/api/avatar/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取当前用户的头像信息
 * @returns {Promise}
 */
export const getAvatarInfo = () => {
  return request.get('/api/avatar/info')
}

/**
 * 重置为默认头像
 * @returns {Promise}
 */
export const resetToDefaultAvatar = () => {
  return request.post('/api/avatar/reset')
}

/**
 * 获取默认头像列表
 * @returns {Promise}
 */
export const getDefaultAvatars = () => {
  return request.get('/api/avatar/defaults')
}