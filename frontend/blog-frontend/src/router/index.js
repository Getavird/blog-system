import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import Home from '../views/Home.vue'
import ArticleDetail from '../views/ArticleDetail.vue'
import ArticleEdit from '../views/ArticleEdit.vue'
import UserArticles from '../views/UserArticles.vue'
import UserProfile from '../views/UserProfile.vue'
import NotFound from '../views/NotFound.vue'
import Category from '../views/Category.vue'
import Search from '../views/Search.vue'
import Archives from '../views/Archives.vue'
import Categories from '../views/Categories.vue'
import Tags from '../views/Tags.vue'
import Tag from '../views/Tag.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { 
      title: '首页',
      requiresAuth: false 
    }
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: ArticleDetail,
    meta: { 
      title: '文章详情',
      requiresAuth: false 
    }
  },
  {
    path: '/article/create',
    name: 'ArticleCreate',
    component: ArticleEdit,
    meta: { 
      requiresAuth: true,
      title: '写文章'
    }
  },
  {
    path: '/article/edit/:id',
    name: 'ArticleEdit',
    component: ArticleEdit,
    meta: { 
      requiresAuth: true,
      title: '编辑文章'
    }
  },
  {
    path: '/user/articles',
    name: 'UserArticles',
    component: UserArticles,
    meta: { 
      requiresAuth: true,
      title: '我的文章'
    }
  },
  {
    path: '/user/profile',
    name: 'UserProfile',
    component: UserProfile,
    meta: { 
      requiresAuth: true,
      title: '个人中心'
    }
  },
  {
    path: '/category/:id',
    name: 'Category',
    component: Category,
    meta: { 
      title: '分类文章',
      requiresAuth: false 
    }
  },
  {
    path: '/categories',
    name: 'Categories',
    component: Categories,
    meta: { 
      title: '分类',
      requiresAuth: false 
    }
  },
  {
    path: '/search',
    name: 'Search',
    component: Search,
    meta: { 
      title: '搜索',
      requiresAuth: false 
    }
  },
  {
    path: '/archives',
    name: 'Archives',
    component: Archives,
    meta: { 
      title: '归档',
      requiresAuth: false 
    }
  },
  {
    path: '/tags',
    name: 'Tags',
    component: Tags,
    meta: { 
      title: '标签',
      requiresAuth: false 
    }
  },
  {
    path: '/tag/:name',
    name: 'Tag',
    component: Tag,
    meta: { 
      title: '标签文章',
      requiresAuth: false 
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: { 
      title: '页面未找到',
      requiresAuth: false 
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0, behavior: 'smooth' }
    }
  }
})

// 检查登录状态的函数 - 不依赖Pinia
const checkLoginStatus = () => {
  try {
    // 检查localStorage中是否有用户信息
    const userStr = localStorage.getItem('blog_user')
    if (!userStr) {
      return false
    }
    
    // 尝试解析用户信息
    const user = JSON.parse(userStr)
    
    // 检查用户信息是否完整
    if (!user || !user.id || !user.username) {
      localStorage.removeItem('blog_user')
      return false
    }
    
    return true
  } catch (error) {
    console.error('检查登录状态失败:', error)
    localStorage.removeItem('blog_user')
    return false
  }
}

// 显示登录提示的函数
const showLoginPrompt = (to) => {
  ElMessage.warning({
    message: '请先登录',
    duration: 2000,
    onClose: () => {
      // 跳转到首页并传递showLogin参数
      router.push({
        path: '/',
        query: { 
          showLogin: true,
          redirect: to.fullPath // 保存要跳转的路径
        }
      })
    }
  })
}

// 路由守卫
router.beforeEach((to, from, next) => {
  console.log(`路由跳转: ${from.path} -> ${to.path}`)
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 博客系统`
  } else {
    document.title = '博客系统'
  }
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    const isLoggedIn = checkLoginStatus()
    
    if (!isLoggedIn) {
      console.log('未登录，需要登录才能访问:', to.path)
      showLoginPrompt(to)
      return // 阻止路由跳转，showLoginPrompt会处理跳转
    }
  }
  
  next()
})

// 全局后置钩子
router.afterEach((to, from) => {
  // 可以在这里添加统计代码等
  console.log('路由导航完成:', to.path)
})

// 导出路由和检查登录状态的函数
export { checkLoginStatus }
export default router