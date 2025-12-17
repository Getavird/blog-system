import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import ArticleDetail from '../views/ArticleDetail.vue'
import ArticleEdit from '../views/ArticleEdit.vue'
import UserArticles from '../views/UserArticles.vue'
import UserProfile from '../views/UserProfile.vue'
import NotFound from '../views/NotFound.vue'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: ArticleDetail
  },
  {
    path: '/article/create',
    name: 'ArticleCreate',
    component: ArticleEdit,
    meta: { requiresAuth: true }
  },
  {
    path: '/article/edit/:id',
    name: 'ArticleEdit',
    component: ArticleEdit,
    meta: { requiresAuth: true }
  },
  {
    path: '/user/articles',
    name: 'UserArticles',
    component: UserArticles,
    meta: { requiresAuth: true }
  },
  {
    path: '/user/profile',  // 新增路由
    name: 'UserProfile',
    component: UserProfile,
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 权限验证
router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    const token = localStorage.getItem('blog_token')
    if (!token) {
      ElMessage.warning('请先登录')
      next('/')
      return
    }
  }
  next()
})

export default router