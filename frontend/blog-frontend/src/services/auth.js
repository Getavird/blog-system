import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

export const useAuth = () => {
  const userStore = useUserStore()
  
  // 处理登录
  const handleLogin = async (loginForm, loginLoading, showLoginDialog) => {
    try {
      loginLoading.value = true
      
      const response = await userStore.login(
        loginForm.username,
        loginForm.password
      )
      
      if (response.success) {
        ElMessage.success('登录成功')
        showLoginDialog.value = false
        return true
      }
    } catch (error) {
      const errorMsg = error.response?.data?.message || '登录失败，请检查用户名和密码'
      ElMessage.error(errorMsg)
      return false
    } finally {
      loginLoading.value = false
    }
  }
  
  // 处理注册
  const handleRegister = async (registerForm, registerLoading, activeTab) => {
    try {
      registerLoading.value = true
      
      const response = await userStore.register({
        username: registerForm.username,
        email: registerForm.email,
        password: registerForm.password
      })
      
      if (response.success) {
        ElMessage.success('注册成功')
        activeTab.value = 'login'
        return true
      }
    } catch (error) {
      const errorMsg = error.response?.data?.message || '注册失败'
      ElMessage.error(errorMsg)
      return false
    } finally {
      registerLoading.value = false
    }
  }
  
  // 检查登录状态
  const checkAuth = () => {
    return userStore.isLoggedIn()
  }
  
  // 获取当前用户
  const getCurrentUser = () => {
    return userStore.user
  }
  
  return {
    handleLogin,
    handleRegister,
    checkAuth,
    getCurrentUser
  }
}