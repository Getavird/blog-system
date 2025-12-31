// stores/ui.js - 全局UI状态管理
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUIStore = defineStore('ui', () => {
  // 登录弹窗状态
  const showLoginDialog = ref(false)
  const loginDialogTab = ref('login') // 'login' 或 'register'
  
  // 显示登录弹窗
  const openLoginDialog = (tab = 'login') => {
    loginDialogTab.value = tab
    showLoginDialog.value = true
  }
  
  // 关闭登录弹窗
  const closeLoginDialog = () => {
    showLoginDialog.value = false
  }
  
  return {
    showLoginDialog,
    loginDialogTab,
    openLoginDialog,
    closeLoginDialog
  }
})