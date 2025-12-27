// src/api/upload.js
import request from '@/utils/request'

export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post('/api/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}