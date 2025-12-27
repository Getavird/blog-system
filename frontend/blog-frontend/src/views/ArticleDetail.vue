<template>
  <div class="article-detail-page">
    <Header />
    
    <!-- 文章加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-content">
        <el-skeleton :rows="5" animated />
        <el-skeleton style="margin-top: 20px;" :rows="10" animated />
      </div>
    </div>
    
    <!-- 文章内容 -->
    <div v-else-if="article" class="article-container">
      <div class="container">
        <!-- 文章头部 -->
        <div class="article-header">
          <!-- 面包屑导航 -->
          <div class="breadcrumb">
            <router-link to="/">首页</router-link>
            <el-icon><ArrowRight /></el-icon>
            <span v-if="article.category">{{ article.category.name }}</span>
            <el-icon><ArrowRight /></el-icon>
            <span class="current">{{ article.title }}</span>
          </div>
          
          <!-- 文章标题 -->
          <h1 class="article-title">{{ article.title }}</h1>
          
          <!-- 文章元信息 -->
          <div class="article-meta">
            <div class="author-info">
              <div class="author-avatar">
                <img v-if="article.authorAvatar" :src="article.authorAvatar" alt="作者头像">
                <div v-else class="avatar-placeholder">
                  {{ article.authorName ? article.authorName.charAt(0) : 'A' }}
                </div>
              </div>
              <div class="author-details">
                <div class="author-name">{{ article.authorName }}</div>
                <div class="meta-items">
                  <span class="meta-item">
                    <el-icon><Calendar /></el-icon>
                    {{ formatTime(article.createTime) }}
                  </span>
                  <span class="meta-item">
                    <el-icon><View /></el-icon>
                    阅读 {{ article.viewCount || 0 }}
                  </span>
                  <span class="meta-item">
                    <el-icon><ChatDotRound /></el-icon>
                    评论 {{ article.commentCount || 0 }}
                  </span>
                </div>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button 
                v-if="isArticleAuthor && isLoggedIn"
                type="primary" 
                size="small"
                @click="editArticle"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button 
                :type="article.isLiked ? 'danger' : 'default'" 
                size="small"
                @click="toggleLike"
                :loading="likeLoading"
                :disabled="!isLoggedIn"
              >
                <el-icon><Star /></el-icon>
                {{ article.isLiked ? '已点赞' : '点赞' }} ({{ article.likeCount || 0 }})
              </el-button>
              <el-button 
                :type="article.isCollected ? 'warning' : 'default'" 
                size="small"
                @click="toggleCollect"
                :loading="collectLoading"
                :disabled="!isLoggedIn"
              >
                <el-icon><Collection /></el-icon>
                {{ article.isCollected ? '已收藏' : '收藏' }} ({{ article.collectCount || 0 }})
              </el-button>
            </div>
          </div>
          
          <!-- 文章封面 -->
          <div v-if="article.coverImage" class="article-cover">
            <img :src="article.coverImage" :alt="article.title" />
          </div>
        </div>
        
        <!-- 文章主体 -->
        <div class="article-body">
          <div class="content-wrapper">
            <!-- 文章分类和标签 -->
            <div class="article-tags">
              <el-tag 
                v-if="article.category"
                type="primary" 
                size="large"
                @click="goToCategory(article.category.id)"
                class="category-tag"
              >
                <el-icon><Folder /></el-icon>
                {{ article.category.name }}
              </el-tag>
              <el-tag
                v-for="tag in article.tags"
                :key="tag.id"
                size="large"
                @click="goToTag(tag.id)"
                class="tag-item"
              >
                #{{ tag.name }}
              </el-tag>
            </div>
            
            <!-- 文章内容 -->
            <div class="article-content" v-html="article.content"></div>
            
            <!-- 文章底部信息 -->
            <div class="article-footer">
              <div class="update-info">
                最后更新于 {{ formatTime(article.updateTime || article.createTime) }}
              </div>
              
              <!-- 版权声明 -->
              <div class="copyright">
                <p>© 本文由 {{ article.authorName }} 发布，转载请注明出处</p>
              </div>
            </div>
            
            <!-- 互动操作 -->
            <div class="interaction-actions">
              <div class="action-group">
                <el-button 
                  :type="article.isLiked ? 'danger' : 'primary'" 
                  size="large"
                  @click="toggleLike"
                  :loading="likeLoading"
                  class="action-btn"
                  :disabled="!isLoggedIn"
                >
                  <el-icon><Star /></el-icon>
                  {{ article.isLiked ? '已点赞' : '点赞' }} ({{ article.likeCount || 0 }})
                </el-button>
                <el-button 
                  :type="article.isCollected ? 'warning' : 'primary'" 
                  size="large"
                  @click="toggleCollect"
                  :loading="collectLoading"
                  class="action-btn"
                  :disabled="!isLoggedIn"
                >
                  <el-icon><Collection /></el-icon>
                  {{ article.isCollected ? '已收藏' : '收藏' }} ({{ article.collectCount || 0 }})
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 侧边栏 -->
          <aside class="sidebar">
            <!-- 作者信息卡片 -->
            <div class="author-card">
              <div class="author-header">
                <div class="author-avatar-large">
                  <img v-if="article.authorAvatar" :src="article.authorAvatar" alt="作者头像">
                  <div v-else class="avatar-placeholder-large">
                    {{ article.authorName ? article.authorName.charAt(0) : 'A' }}
                  </div>
                </div>
                <h3 class="author-name">{{ article.authorName }}</h3>
                <p class="author-bio" v-if="article.authorBio">{{ article.authorBio }}</p>
              </div>
              <div class="author-stats">
                <div class="stat-item">
                  <div class="stat-number">{{ article.authorArticleCount || 0 }}</div>
                  <div class="stat-label">文章</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">{{ article.authorLikeCount || 0 }}</div>
                  <div class="stat-label">获赞</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">{{ article.authorFansCount || 0 }}</div>
                  <div class="stat-label">粉丝</div>
                </div>
              </div>
              <el-button 
                v-if="!isArticleAuthor && isLoggedIn"
                :type="article.isFollowing ? 'default' : 'primary'"
                size="small"
                @click="toggleFollow"
                :loading="followLoading"
                class="follow-btn"
              >
                {{ article.isFollowing ? '已关注' : '关注作者' }}
              </el-button>
            </div>
            
            <!-- 目录导航 -->
            <div v-if="showToc" class="toc-card">
              <h3 class="toc-title">
                <el-icon><Menu /></el-icon>
                目录
              </h3>
              <div class="toc-content">
                <div 
                  v-for="(item, index) in tocItems" 
                  :key="index"
                  :class="['toc-item', `toc-level-${item.level}`]"
                  @click="scrollToHeading(item.id)"
                >
                  {{ item.text }}
                </div>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </div>
    
    <!-- 文章不存在 -->
    <div v-else class="not-found-container">
      <div class="error-content">
        <h1>文章不存在</h1>
        <p>抱歉，您要访问的文章可能已被删除或不存在</p>
        <el-button type="primary" @click="$router.push('/')">
          返回首页
        </el-button>
      </div>
    </div>
    
    <!-- 评论区域 -->
    <div v-if="article && !loading" class="comments-section">
      <div class="container">
        <div class="comments-wrapper">
          <h2 class="comments-title">
            <el-icon><ChatDotRound /></el-icon>
            评论 ({{ article.commentCount || 0 }})
          </h2>
          
          <!-- 登录提示 -->
          <div v-if="!isLoggedIn" class="login-prompt">
            <p>请先<a href="javascript:;" @click="showLogin = true">登录</a>后发表评论</p>
          </div>
          
          <!-- 发表评论 -->
          <div v-else class="comment-form-card">
            <div class="form-header">
              <div class="user-avatar">
                <img v-if="currentUserAvatar" :src="currentUserAvatar" alt="用户头像">
                <div v-else class="avatar-placeholder-small">
                  {{ currentUserName ? currentUserName.charAt(0) : 'U' }}
                </div>
              </div>
              <div class="form-title">发表评论</div>
            </div>
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="4"
              placeholder="写下你的评论..."
              resize="none"
              class="comment-textarea"
            />
            <div class="form-actions">
              <el-button @click="cancelComment">取消</el-button>
              <el-button 
                type="primary" 
                @click="submitComment"
                :loading="commentLoading"
                :disabled="!commentContent.trim()"
              >
                发表评论
              </el-button>
            </div>
          </div>
          
          <!-- 评论列表 -->
          <div v-if="comments.length > 0" class="comments-list">
            <div 
              v-for="comment in comments" 
              :key="comment.id"
              class="comment-item"
            >
              <div class="comment-header">
                <div class="comment-author">
                  <div class="comment-avatar">
                    <img v-if="comment.userAvatar" :src="comment.userAvatar" alt="用户头像">
                    <div v-else class="avatar-placeholder-small">
                      {{ comment.userName ? comment.userName.charAt(0) : 'U' }}
                    </div>
                  </div>
                  <div class="comment-author-info">
                    <div class="comment-author-name">{{ comment.userName }}</div>
                    <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
                  </div>
                </div>
                <!-- 关键修复：使用 checkCommentOwnership 方法 -->
                <div v-if="checkCommentOwnership(comment)" class="comment-actions">
                  <el-button type="text" @click="editComment(comment)">编辑</el-button>
                  <el-button type="text" @click="deleteComment(comment.id)">删除</el-button>
                </div>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-footer">
                <el-button 
                  type="text" 
                  size="small"
                  @click="replyToComment(comment)"
                  :disabled="!isLoggedIn"
                >
                  回复
                </el-button>
                <el-button 
                  type="text" 
                  size="small"
                  :class="{ 'liked': comment.isLiked }"
                  @click="likeComment(comment)"
                  :disabled="!isLoggedIn"
                >
                  <el-icon><Star /></el-icon>
                  {{ comment.likeCount || 0 }}
                </el-button>
              </div>
            </div>
          </div>
          <div v-else class="no-comments">
            <p>还没有评论，快来抢沙发吧～</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 登录弹窗 -->
    <el-dialog
      v-model="showLogin"
      title="用户登录"
      width="400px"
      :close-on-click-modal="false"
    >
      <div style="text-align: center; padding: 20px;">
        <p>请先登录才能进行此操作</p>
        <el-button type="primary" @click="toLoginPage">去登录</el-button>
      </div>
    </el-dialog>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { useCommentStore } from '@/stores/comment'
import { useUserStore } from '@/stores/user'
import { useAuthStore } from '@/stores/auth'
import { useCategoryStore } from '@/stores/category'
import { useTagStore } from '@/stores/tag'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowRight,
  Calendar,
  View,
  ChatDotRound,
  Edit,
  Star,
  Collection,
  Folder,
  Menu,
  Search
} from '@element-plus/icons-vue'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'

const route = useRoute()
const router = useRouter()

// Pinia Store
const articleStore = useArticleStore()
const commentStore = useCommentStore()
const userStore = useUserStore()
const authStore = useAuthStore()
const categoryStore = useCategoryStore()
const tagStore = useTagStore()

// 路由参数
const articleId = ref(parseInt(route.params.id) || 0)

// 文章数据
const article = computed(() => articleStore.currentArticle)
const loading = ref(false)
const likeLoading = ref(false)
const collectLoading = ref(false)
const followLoading = ref(false)

// 登录状态
const isLoggedIn = computed(() => userStore.isLoggedIn())
const currentUser = computed(() => userStore.user)
const currentUserName = computed(() => currentUser.value?.username || '')
const currentUserAvatar = computed(() => currentUser.value?.avatar || '')

// 检查是否为文章作者
const isArticleAuthor = computed(() => {
  if (!isLoggedIn.value || !article.value) return false
  return currentUser.value?.id === article.value.authorId
})

// 评论相关
const comments = computed(() => commentStore.comments || [])
const commentContent = ref('')
const commentLoading = ref(false)
const showLogin = ref(false)

// 目录相关
const tocItems = ref([])
const showToc = computed(() => tocItems.value.length > 0)

// 组件挂载
onMounted(async () => {
  // 初始化用户状态
  userStore.initFromStorage()
  
  // 加载文章详情
  if (articleId.value) {
    await loadArticleDetail()
  }
})

// 监听路由参数变化
watch(
  () => route.params.id,
  async (newId) => {
    if (newId) {
      articleId.value = parseInt(newId)
      await loadArticleDetail()
    }
  }
)

// 加载文章详情
const loadArticleDetail = async () => {
  try {
    loading.value = true
    
    // 1. 加载文章详情
    await articleStore.fetchArticleDetail(articleId.value)
    
    // 2. 增加阅读量
    await articleStore.incrementViewCount(articleId.value)
    
    // 3. 加载文章评论
    await loadArticleComments()
    
    // 4. 生成目录
    generateToc()
    
  } catch (error) {
    console.error('加载文章详情失败:', error)
    ElMessage.error('文章加载失败')
  } finally {
    loading.value = false
  }
}

// 加载文章评论
const loadArticleComments = async () => {
  try {
    await commentStore.fetchArticleComments(articleId.value, {
      page: 1,
      size: 20
    })
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

// 生成目录
const generateToc = () => {
  nextTick(() => {
    const contentElement = document.querySelector('.article-content')
    if (!contentElement) return
    
    const headings = contentElement.querySelectorAll('h1, h2, h3, h4, h5, h6')
    tocItems.value = Array.from(headings).map((heading, index) => {
      const id = heading.id || `heading-${index}`
      heading.id = id
      return {
        id,
        text: heading.textContent || '',
        level: parseInt(heading.tagName.charAt(1))
      }
    })
  })
}

// 滚动到标题
const scrollToHeading = (id) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 点赞文章
const toggleLike = async () => {
  if (!isLoggedIn.value) {
    showLogin.value = true
    return
  }
  
  try {
    likeLoading.value = true
    const isLike = !article.value.isLiked
    await articleStore.toggleLike(articleId.value, isLike)
    
    if (isLike) {
      ElMessage.success('点赞成功')
    } else {
      ElMessage.info('已取消点赞')
    }
  } catch (error) {
    console.error('操作点赞失败:', error)
    ElMessage.error('操作失败')
  } finally {
    likeLoading.value = false
  }
}

// 收藏文章（需要后端接口支持）
const toggleCollect = async () => {
  if (!isLoggedIn.value) {
    showLogin.value = true
    return
  }
  
  try {
    collectLoading.value = true
    // 这里需要调用收藏API，暂时模拟
    ElMessage.info('收藏功能待实现')
    // TODO: 调用收藏API
  } catch (error) {
    console.error('操作收藏失败:', error)
    ElMessage.error('操作失败')
  } finally {
    collectLoading.value = false
  }
}

// 关注作者（需要后端接口支持）
const toggleFollow = async () => {
  if (!isLoggedIn.value) {
    showLogin.value = true
    return
  }
  
  try {
    followLoading.value = true
    // 这里需要调用关注API，暂时模拟
    ElMessage.info('关注功能待实现')
    // TODO: 调用关注API
  } catch (error) {
    console.error('操作关注失败:', error)
    ElMessage.error('操作失败')
  } finally {
    followLoading.value = false
  }
}

// 编辑文章
const editArticle = () => {
  router.push(`/article/edit/${articleId.value}`)
}

// 跳转到分类
const goToCategory = (categoryId) => {
  if (categoryId) {
    router.push(`/category/${categoryId}`)
  }
}

// 跳转到标签
const goToTag = (tagId) => {
  if (tagId) {
    // 先获取标签名称
    const tag = tagStore.tags.find(t => t.id === tagId)
    if (tag) {
      router.push(`/tag/${encodeURIComponent(tag.name)}`)
    }
  }
}

// 提交评论
const submitComment = async () => {
  if (!isLoggedIn.value) {
    showLogin.value = true
    return
  }
  
  const content = commentContent.value.trim()
  if (!content) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  try {
    commentLoading.value = true
    
    await commentStore.createComment({
      articleId: articleId.value,
      content: content
    })
    
    ElMessage.success('评论成功')
    commentContent.value = ''
    
    // 更新文章评论数
    if (article.value) {
      article.value.commentCount = (article.value.commentCount || 0) + 1
    }
    
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('评论失败')
  } finally {
    commentLoading.value = false
  }
}

// 取消评论
const cancelComment = () => {
  commentContent.value = ''
}

// 编辑评论
const editComment = (comment) => {
  // 这里可以打开编辑对话框
  ElMessage.info('编辑评论功能待实现')
}

// 删除评论
const deleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    await commentStore.deleteComment(commentId)
    ElMessage.success('删除成功')
    
    // 更新文章评论数
    if (article.value) {
      article.value.commentCount = Math.max(0, (article.value.commentCount || 1) - 1)
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 回复评论
const replyToComment = (comment) => {
  if (!isLoggedIn.value) {
    showLogin.value = true
    return
  }
  
  commentContent.value = `@${comment.userName} `
  // 聚焦到评论框
  nextTick(() => {
    const textarea = document.querySelector('.comment-textarea textarea')
    if (textarea) {
      textarea.focus()
    }
  })
}

// 点赞评论
const likeComment = async (comment) => {
  if (!isLoggedIn.value) {
    showLogin.value = true
    return
  }
  
  try {
    const isLike = !comment.isLiked
    await commentStore.toggleCommentLike(comment.id, isLike)
    
    if (isLike) {
      ElMessage.success('点赞成功')
    } else {
      ElMessage.info('已取消点赞')
    }
  } catch (error) {
    console.error('操作评论点赞失败:', error)
    ElMessage.error('操作失败')
  }
}

// 检查评论所有权（作者或管理员）
const checkCommentOwnership = (comment) => {
  if (!isLoggedIn.value) return false
  
  // 1. 评论作者
  if (comment.userId === currentUser.value?.id) return true
  
  // 2. 文章作者
  if (article.value && article.value.authorId === currentUser.value?.id) return true
  
  // 3. 管理员（假设角色1为管理员）
  if (currentUser.value?.role === 1) return true
  
  return false
}

// 跳转到登录页
const toLoginPage = () => {
  showLogin.value = false
  router.push('/')
  // 这里可以触发父组件的登录弹窗
}
</script>

<style scoped>
.article-detail-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

/* 加载状态 */
.loading-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.loading-content {
  width: 100%;
  max-width: 800px;
}

/* 文章容器 */
.article-container {
  padding-top: 20px;
}

.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 面包屑导航 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
  margin-bottom: 20px;
}

.breadcrumb a {
  color: #666;
  text-decoration: none;
}

.breadcrumb a:hover {
  color: #409eff;
}

.breadcrumb .current {
  color: #333;
  font-weight: 500;
}

/* 文章标题 */
.article-title {
  font-size: 32px;
  font-weight: bold;
  line-height: 1.4;
  color: #333;
  margin-bottom: 24px;
}

/* 文章元信息 */
.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-details {
  flex: 1;
}

.author-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.meta-items {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

/* 文章封面 */
.article-cover {
  margin-bottom: 30px;
  border-radius: 8px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
}

/* 文章主体 */
.article-body {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
}

.content-wrapper {
  flex: 1;
  min-width: 0;
}

.sidebar {
  width: 320px;
  flex-shrink: 0;
}

/* 文章标签 */
.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 30px;
}

.category-tag,
.tag-item {
  cursor: pointer;
  transition: all 0.3s;
}

.category-tag:hover,
.tag-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 文章内容 */
.article-content {
  line-height: 1.8;
  color: #333;
  margin-bottom: 40px;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3),
.article-content :deep(h4),
.article-content :deep(h5),
.article-content :deep(h6) {
  margin: 1.5em 0 0.8em;
  color: #333;
  font-weight: 600;
}

.article-content :deep(h1) {
  font-size: 28px;
}
.article-content :deep(h2) {
  font-size: 24px;
}
.article-content :deep(h3) {
  font-size: 20px;
}
.article-content :deep(h4) {
  font-size: 18px;
}

.article-content :deep(p) {
  margin: 1em 0;
}

.article-content :deep(ul),
.article-content :deep(ol) {
  margin: 1em 0;
  padding-left: 2em;
}

.article-content :deep(li) {
  margin: 0.5em 0;
}

.article-content :deep(pre) {
  background: #f6f8fa;
  border-radius: 6px;
  padding: 16px;
  overflow: auto;
  margin: 1.5em 0;
}

.article-content :deep(code) {
  background: #f6f8fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: "Consolas", "Monaco", monospace;
  font-size: 14px;
}

.article-content :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 16px;
  margin: 1.5em 0;
  color: #666;
  font-style: italic;
}

/* 文章底部 */
.article-footer {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  color: #666;
  font-size: 14px;
}

.update-info {
  margin-bottom: 10px;
}

.copyright p {
  color: #999;
  font-size: 13px;
}

/* 互动操作 */
.interaction-actions {
  margin: 40px 0;
  text-align: center;
}

.action-group {
  display: inline-flex;
  gap: 20px;
}

.action-btn {
  min-width: 120px;
}

/* 侧边栏卡片 */
.author-card,
.toc-card{
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.author-header {
  text-align: center;
  margin-bottom: 20px;
}

.author-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin: 0 auto 15px;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: bold;
  overflow: hidden;
}

.author-avatar-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
}

.author-bio {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 0;
}

.author-stats {
  display: flex;
  justify-content: space-around;
  margin: 20px 0;
  padding: 20px 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.follow-btn {
  width: 100%;
}

/* 目录导航 */
.toc-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  margin-bottom: 15px;
  color: #333;
}

.toc-content {
  max-height: 300px;
  overflow-y: auto;
}

.toc-item {
  padding: 8px 0;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  line-height: 1.5;
}

.toc-item:hover {
  color: #409eff;
  transform: translateX(4px);
}

.toc-level-2 {
  padding-left: 16px;
}
.toc-level-3 {
  padding-left: 32px;
}
.toc-level-4 {
  padding-left: 48px;
}
.toc-level-5 {
  padding-left: 64px;
}
.toc-level-6 {
  padding-left: 80px;
}

/* 文章不存在 */
.not-found-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}

.error-content {
  text-align: center;
}

.error-content h1 {
  font-size: 48px;
  color: #333;
  margin-bottom: 20px;
}

.error-content p {
  font-size: 16px;
  color: #666;
  margin-bottom: 30px;
}

/* 评论区域 */
.comments-section {
  background: white;
  border-top: 1px solid #eee;
  padding: 40px 0;
}

.comments-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

.comments-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  margin-bottom: 30px;
  color: #333;
}

/* 评论表单 */
.comment-form-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 30px;
  border: 1px solid #eee;
}

.form-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.form-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.comment-textarea {
  margin-bottom: 20px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 登录提示样式 */
.login-prompt {
  text-align: center;
  padding: 30px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 30px;
  border: 1px solid #e9ecef;
}

.login-prompt a {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
}


.login-prompt a:hover {
  text-decoration: underline;
}

/* 评论列表 */
.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.comment-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  overflow: hidden;
}

.comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-author-info {
  flex: 1;
}

.comment-author-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

/* 未登录时的按钮样式 */
.action-buttons .el-button:disabled,
.comment-footer .el-button:disabled,
.interaction-actions .el-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.action-buttons .el-button:disabled:hover,
.comment-footer .el-button:disabled:hover,
.interaction-actions .el-button:disabled:hover {
  background: initial;
  border-color: initial;
  color: initial;
}

.comment-actions {
  display: flex;
  gap: 8px;
}
.comment-actions .el-button {
  font-size: 12px;
  padding: 0 6px;
}

.comment-content {
  line-height: 1.6;
  color: #333;
  margin-bottom: 15px;
}

.comment-footer {
  display: flex;
  gap: 20px;
}

.comment-footer .liked {
  color: #409eff;
}

/* 禁用状态的点赞/收藏按钮样式 */
.liked {
  color: #409eff;
}

.comment-footer .el-button:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.comment-footer .el-button:disabled:hover {
  background: transparent;
  border-color: transparent;
}

.no-comments {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

/* 头像占位符 */
.avatar-placeholder,
.avatar-placeholder-large,
.avatar-placeholder-small {
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.avatar-placeholder {
  font-size: 20px;
}
.avatar-placeholder-large {
  font-size: 32px;
}
.avatar-placeholder-small {
  font-size: 14px;
}
/* 登录弹窗样式 */
:deep(.el-dialog__header) {
  padding: 20px 20px 10px;
}

:deep(.el-dialog__body) {
  padding: 10px 20px 20px;
}


/* 响应式设计 */
@media (max-width: 992px) {
  .article-body {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    margin-top: 40px;
  }

  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .action-buttons {
    align-self: flex-end;
  }
}

@media (max-width: 768px) {
  .article-title {
    font-size: 24px;
  }

  .author-stats {
    padding: 15px 0;
  }

  .action-group {
    flex-direction: column;
    width: 100%;
  }

  .action-btn {
    width: 100%;
  }

  .comments-title {
    font-size: 18px;
  }
  .comment-actions {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .comment-actions .el-button {
    font-size: 11px;
    padding: 0 4px;
  }

}
</style>