import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import ArticleDetail from '../views/ArticleDetail.vue'
import ArticleEdit from '../views/ArticleEdit.vue'
import UserArticles from '../views/UserArticles.vue'
import UserProfile from '../views/UserProfile.vue'
import NotFound from '../views/NotFound.vue'
import TestApi from '../views/TestApi.vue'
import Category from'../views/Category.vue'
import Search from '../views/Search.vue'
import Archives from '../views/Archives.vue'
import Categories from '../views/Categories.vue'
import Tags from '../views/Tags.vue'
import Tag from '../views/Tag.vue'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/test',  // 新增测试页面
    name: 'TestApi',
    component: TestApi
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
    path: '/user/profile', 
    name: 'UserProfile',
    component: UserProfile,
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound
  },
  {
    path: '/category/:id',
    name: 'Category',
    component: Category
  },
  {
    path: '/categories',
    name: 'Categories',
    component: Categories
  },
  {
    path: '/search',
    name: 'Search',
    component: Search
  },
  {
    path: '/archives',
    name: 'Archives',
    component: Archives
  },
  {
    path: '/tags',
    name: 'Tags',
    component: Tags
  },
  {
    path: '/tag/:name',
    name: 'Tag',
    component: Tag
  },
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