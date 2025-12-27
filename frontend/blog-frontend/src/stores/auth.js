// store/auth.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth'
import { useUserStore } from './user'
import { getActivePinia } from 'pinia'

export const useAuthStore = defineStore('auth', () => {
  // 状态：仅保留加载状态和错误信息
  const loading = ref(false)
  const error = ref(null)

  // 登录方法
  const login = async (username, password) => {
    // 动态获取userStore，避免初始化依赖问题
    const userStore = useUserStore(getActivePinia())
    try {
      loading.value = true
      error.value = null

      const data = await authApi.login(username, password)
      // 仅保存用户信息（无Token，适配Session认证）
      userStore.setUser(data.user || data)
      return data
    } catch (err) {
      // 精准提取错误信息
      error.value = err.response?.data?.message || err.message || '登录失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  // 注册方法（注册后自动登录）
  const register = async (registerData) => {
    const userStore = useUserStore(getActivePinia())
    try {
      loading.value = true
      error.value = null

      const data = await authApi.register(registerData)
      // 注册成功后保存用户信息
      if (data.user || data) {
        userStore.setUser(data.user || data)
      }
      return data
    } catch (err) {
      error.value = err.response?.data?.message || err.message || '注册失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  // 登出方法（修复变量名冲突）
  const logout = async () => {
    const userStore = useUserStore(getActivePinia())
    try {
      loading.value = true
      await authApi.logout()
    } catch (err) {
      console.error('登出失败:', err)
    } finally {
      // 清除用户信息，退出登录
      userStore.clearUser()
      loading.value = false
    }
  }

  // 获取当前用户（失败时清除无效状态）
  const fetchCurrentUser = async () => {
    const userStore = useUserStore(getActivePinia())
    try {
      loading.value = true
      error.value = null

      const data = await authApi.getCurrentUser()
      userStore.setUser(data)
      return data
    } catch (err) {
      error.value = err.response?.data?.message || err.message || '获取用户信息失败'
      // 清除无效登录状态
      userStore.clearUser()
      throw err
    } finally {
      loading.value = false
    }
  }

  // 修改密码
  const changePassword = async (oldPassword, newPassword) => {
    try {
      loading.value = true
      error.value = null

      const data = await authApi.updatePassword(oldPassword, newPassword)
      return data
    } catch (err) {
      error.value = err.response?.data?.message || err.message || '修改密码失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  // 登录状态判断（依赖userStore，响应式更新）
  const isLoggedIn = computed(() => {
    const userStore = useUserStore(getActivePinia())
    return userStore.isLoggedIn()
  })

  return {
    // 状态
    loading,
    error,
    // 计算属性
    isLoggedIn,
    // 方法
    login,
    register,
    logout,
    fetchCurrentUser,
    changePassword
  }
})