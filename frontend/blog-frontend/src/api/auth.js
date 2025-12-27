// api/auth.js
import request from '@/utils/request' // 假设项目已有统一请求封装

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

// 获取当前用户信息（对应后端 UserController.getCurrentUser，路径统一调整）
export const getCurrentUser = () => {
  return request.get('/api/user/current')
}

// 修改密码接口（对应后端 UserController.updatePassword）
export const updatePassword = (oldPassword, newPassword) => {
  return request.put('/api/user/password', {
    oldPassword,
    newPassword
  })
}