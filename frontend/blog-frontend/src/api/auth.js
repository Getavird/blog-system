// api/auth.js
import request from '@/utils/request'

// 登录接口（对应后端 UserController.login）
export const login = (username, password) => {
  return request.post('/api/user/login', {
    username,
    password
  })
}

// 注册接口（对应后端 UserController.register）
export const register = (registerData) => {
  return request.post('/api/user/register', registerData)
}

// 登出接口（对应后端 UserController.logout）
export const logout = () => {
  return request.post('/api/user/logout')
}

// 获取当前用户信息（对应后端 UserController.getCurrentUser）
export const getCurrentUser = () => {
  return request.get('/api/user/info')  // 注意：后端是 /api/user/info
}

// 修改密码接口（对应后端 UserController.changePassword）
export const updatePassword = (oldPassword, newPassword) => {
  return request.post('/api/user/change-password', {
    oldPassword,
    newPassword
  })
}

// 更新用户信息（对应后端 UserController.updateProfile）
export const updateProfile = (userData) => {
  return request.put('/api/user/profile', userData)
}

// 获取用户统计信息（对应后端 UserController.getUserStats）
export const getUserStats = () => {
  return request.get('/api/user/stats')
}

// 获取账户状态（对应后端 UserController.getUserStatus）
export const getUserStatus = () => {
  return request.get('/api/user/status')
}

// 更新个人简介（对应后端 UserController.updateBio）
export const updateBio = (bio) => {
  return request.put('/api/user/bio', null, {
    params: { bio }
  })
}