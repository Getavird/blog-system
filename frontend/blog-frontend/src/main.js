// src/main.js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 初始化 Pinia store
const pinia = createPinia()
app.use(pinia)

// 初始化用户状态
import { useUserStore } from './stores/user'
const userStore = useUserStore()
userStore.initFromStorage()

app.use(router)
app.use(ElementPlus)

app.mount('#app')