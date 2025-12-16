import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref('')
  
  const initFromStorage = () => {
    const storedToken = localStorage.getItem('blog_token')
    const storedUser = localStorage.getItem('blog_user')
    
    if (storedToken) {
      token.value = storedToken
    }
    
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch (error) {
        console.error('解析用户数据失败:', error)
        user.value = null
      }
    }
  }
  
  const login = (userData, userToken) => {
    user.value = userData
    token.value = userToken
    
    localStorage.setItem('blog_user', JSON.stringify(userData))
    localStorage.setItem('blog_token', userToken)
  }
  
  const logout = () => {
    user.value = null
    token.value = ''
    
    localStorage.removeItem('blog_user')
    localStorage.removeItem('blog_token')
  }
  
  const isLoggedIn = () => {
    return !!token.value
  }
  
  return {
    user,
    token,
    initFromStorage,
    login,
    logout,
    isLoggedIn
  }
})