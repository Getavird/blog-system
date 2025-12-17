// 检查是否登录
export const isLoggedIn = () => {
  const token = localStorage.getItem('blog_token')
  return !!token
}

// 获取当前用户信息
export const getCurrentUser = () => {
  const userStr = localStorage.getItem('blog_user')
  if (userStr) {
    try {
      return JSON.parse(userStr)
    } catch (error) {
      console.error('解析用户信息失败:', error)
      return null
    }
  }
  return null
}

// 获取 token
export const getToken = () => {
  return localStorage.getItem('blog_token')
}

// 设置用户信息和 token
export const setAuth = (user, token) => {
  localStorage.setItem('blog_user', JSON.stringify(user))
  localStorage.setItem('blog_token', token)
}

// 清除用户信息和 token
export const clearAuth = () => {
  localStorage.removeItem('blog_user')
  localStorage.removeItem('blog_token')
}