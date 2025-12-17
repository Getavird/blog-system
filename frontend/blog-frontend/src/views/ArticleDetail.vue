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
            
            <!-- 相关文章 -->
            <div class="related-articles">
              <h3><el-icon><Link /></el-icon> 相关文章</h3>
              <div class="related-list">
                <div 
                  v-for="related in relatedArticles" 
                  :key="related.id"
                  class="related-item"
                  @click="viewArticle(related.id)"
                >
                  <div class="related-title">{{ related.title }}</div>
                  <div class="related-meta">
                    <span>{{ related.authorName }}</span>
                    <span>{{ formatTime(related.createTime) }}</span>
                  </div>
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
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { defineAsyncComponent } from 'vue'
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
  Link
} from '@element-plus/icons-vue'

const Header = defineAsyncComponent(() => import('../components/layout/Header.vue'))
const Footer = defineAsyncComponent(() => import('../components/layout/Footer.vue'))

const route = useRoute()
const router = useRouter()

// 状态管理
const loading = ref(true)
const article = ref(null)
const comments = ref([])
const relatedArticles = ref([])
const tocItems = ref([])
const likeLoading = ref(false)
const collectLoading = ref(false)
const followLoading = ref(false)
const commentLoading = ref(false)
const commentContent = ref('')
const showLogin = ref(false)

// 模拟数据（修复：使用 userId 而不是 isCurrentUser）
const mockArticle = {
  id: 1,
  title: 'Vue 3 新特性详解与实战指南',
  content: `
    <h2>Vue 3 的新特性</h2>
    <p>Vue 3 带来了许多令人兴奋的新特性，其中最重要的包括：</p>
    
    <h3>1. Composition API</h3>
    <p>Composition API 是 Vue 3 的核心特性之一，它提供了更好的代码组织和复用能力。</p>
    <pre><code>import { ref, computed } from 'vue'

export default {
  setup() {
    const count = ref(0)
    const doubleCount = computed(() => count.value * 2)
    
    return { count, doubleCount }
  }
}</code></pre>
    
    <h3>2. 更好的 TypeScript 支持</h3>
    <p>Vue 3 完全使用 TypeScript 重写，提供了更好的类型推断。</p>
    
    <h3>3. 性能优化</h3>
    <ul>
      <li>更快的虚拟 DOM</li>
      <li>更小的包体积</li>
      <li>更好的 tree-shaking</li>
    </ul>
  `,
  summary: '深入解析 Vue 3 的新特性和使用技巧，带你快速上手 Vue 3 开发...',
  authorName: '李四',
  authorId: 2, // 添加作者ID
  authorAvatar: '',
  authorBio: '前端开发工程师，专注于 Vue 和 React 技术栈',
  authorArticleCount: 25,
  authorLikeCount: 156,
  authorFansCount: 89,
  category: { id: 1, name: '技术' },
  tags: [
    { id: 1, name: 'Vue' },
    { id: 2, name: '前端' },
    { id: 3, name: 'JavaScript' }
  ],
  coverImage: '',
  viewCount: 320,
  likeCount: 42,
  collectCount: 18,
  commentCount: 12,
  isLiked: false,
  isCollected: false,
  isFollowing: false,
  createTime: '2024-01-14 14:20:00',
  updateTime: '2024-01-15 09:30:00'
}

const mockComments = [
  {
    id: 1,
    userId: 1, // 张三的用户ID
    userName: '张三',
    userAvatar: '',
    content: '这篇文章写得很好，特别是 Composition API 的部分，讲得很清楚！',
    likeCount: 5,
    isLiked: false,
    createTime: '2024-01-15 10:30:00'
  },
  {
    id: 2,
    userId: 2, // 李四的用户ID
    userName: '李四',
    userAvatar: '',
    content: '期待更多关于 Vue 3 实战的文章！',
    likeCount: 2,
    isLiked: false,
    createTime: '2024-01-15 11:45:00'
  }
]

const mockRelatedArticles = [
  {
    id: 2,
    title: 'Spring Boot入门教程',
    authorName: '张三',
    createTime: '2024-01-15 10:30:00'
  },
  {
    id: 3,
    title: '数据库设计规范',
    authorName: '王五',
    createTime: '2024-01-13 09:15:00'
  }
]

// 计算属性
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('blog_token')
})

const currentUser = computed(() => {
  const userStr = localStorage.getItem('blog_user')
  return userStr ? JSON.parse(userStr) : null
})

const currentUserName = computed(() => {
  return currentUser.value ? currentUser.value.username : ''
})

const currentUserAvatar = computed(() => {
  return currentUser.value ? currentUser.value.avatar : ''
})

const isArticleAuthor = computed(() => {
  if (!article.value || !currentUser.value) return false
  return currentUser.value.id === article.value.authorId
})

const showToc = computed(() => {
  return tocItems.value.length > 0
})

// 检查评论是否是当前用户的
const checkCommentOwnership = (comment) => {
  if (!isLoggedIn.value || !currentUser.value) return false
  return comment.userId === currentUser.value.id
}

// 登录检查方法
const checkLogin = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    showLogin.value = true
    return false
  }
  return true
}

// 跳转到登录页面
const toLoginPage = () => {
  showLogin.value = false
  // 触发 Header 组件的登录事件
  const event = new CustomEvent('showLogin')
  window.dispatchEvent(event)
}

// 生命周期
onMounted(() => {
  loadArticleData()
})

// 方法
const loadArticleData = () => {
  loading.value = true
  
  // 模拟 API 调用
  setTimeout(() => {
    article.value = mockArticle
    comments.value = mockComments
    relatedArticles.value = mockRelatedArticles
    
    // 生成目录
    generateToc()
    
    // 增加阅读量
    increaseViewCount()
    
    loading.value = false
  }, 800)
}

const generateToc = () => {
  if (!article.value || !article.value.content) return
  
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = article.value.content
  
  const headings = tempDiv.querySelectorAll('h1, h2, h3, h4, h5, h6')
  tocItems.value = Array.from(headings)
    .filter(h => h.id || h.textContent.trim())
    .map((h, index) => {
      if (!h.id) h.id = `heading-${index}`
      return {
        id: h.id,
        text: h.textContent.trim(),
        level: parseInt(h.tagName.charAt(1))
      }
    })
}

const increaseViewCount = () => {
  console.log('增加阅读量')
}

const formatTime = (time) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    return '今天'
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 编辑文章（只有作者才能编辑）
const editArticle = () => {
  if (!checkLogin()) return
  
  if (!isArticleAuthor.value) {
    ElMessage.warning('只有作者才能编辑文章')
    return
  }
  
  router.push(`/article/edit/${article.value.id}`)
}

// 点赞文章（需要登录）
const toggleLike = async () => {
  if (!checkLogin()) return
  
  likeLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 300))
    
    if (article.value.isLiked) {
      article.value.isLiked = false
      article.value.likeCount--
      ElMessage.success('已取消点赞')
    } else {
      article.value.isLiked = true
      article.value.likeCount++
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    likeLoading.value = false
  }
}

// 收藏文章（需要登录）
const toggleCollect = async () => {
  if (!checkLogin()) return
  
  collectLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 300))
    
    if (article.value.isCollected) {
      article.value.isCollected = false
      article.value.collectCount--
      ElMessage.success('已取消收藏')
    } else {
      article.value.isCollected = true
      article.value.collectCount++
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    collectLoading.value = false
  }
}

// 关注作者（不能关注自己，需要登录）
const toggleFollow = async () => {
  if (!checkLogin()) return
  
  // 不能关注自己
  if (isArticleAuthor.value) {
    ElMessage.warning('不能关注自己')
    return
  }
  
  followLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 300))
    
    if (article.value.isFollowing) {
      article.value.isFollowing = false
      article.value.authorFansCount--
      ElMessage.success('已取消关注')
    } else {
      article.value.isFollowing = true
      article.value.authorFansCount++
      ElMessage.success('关注成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    followLoading.value = false
  }
}

const goToCategory = (categoryId) => {
  router.push(`/category/${categoryId}`)
}

const goToTag = (tagId) => {
  router.push(`/tag/${tagId}`)
}

const viewArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

const scrollToHeading = (headingId) => {
  const element = document.getElementById(headingId)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

// 发表评论（需要登录）
const submitComment = async () => {
  if (!checkLogin()) return
  
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  commentLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const newComment = {
      id: Date.now(),
      userId: currentUser.value.id, // 使用当前用户的ID
      userName: currentUserName.value,
      userAvatar: currentUserAvatar.value,
      content: commentContent.value,
      likeCount: 0,
      isLiked: false,
      createTime: new Date().toISOString()
    }
    
    comments.value.unshift(newComment)
    article.value.commentCount++
    commentContent.value = ''
    
    ElMessage.success('评论发表成功')
  } catch (error) {
    ElMessage.error('评论发表失败')
  } finally {
    commentLoading.value = false
  }
}

const cancelComment = () => {
  commentContent.value = ''
}

// 编辑评论（只能编辑自己的评论）
const editComment = (comment) => {
  if (!checkLogin()) return
  
  if (!checkCommentOwnership(comment)) {
    ElMessage.warning('只能编辑自己的评论')
    return
  }
  
  ElMessageBox.prompt('编辑评论', '编辑', {
    inputValue: comment.content,
    confirmButtonText: '保存',
    cancelButtonText: '取消'
  }).then(({ value }) => {
    if (value && value.trim()) {
      comment.content = value.trim()
      ElMessage.success('评论已更新')
    }
  })
}

// 删除评论（只能删除自己的评论）
const deleteComment = (commentId) => {
  if (!checkLogin()) return
  
  const comment = comments.value.find(c => c.id === commentId)
  if (!comment) return
  
  if (!checkCommentOwnership(comment)) {
    ElMessage.warning('只能删除自己的评论')
    return
  }
  
  ElMessageBox.confirm('确定要删除这条评论吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    comments.value = comments.value.filter(c => c.id !== commentId)
    article.value.commentCount--
    ElMessage.success('评论已删除')
  })
}

const replyToComment = (comment) => {
  if (!checkLogin()) return
  commentContent.value = `@${comment.userName} `
}

const likeComment = async (comment) => {
  if (!checkLogin()) return
  
  if (comment.isLiked) {
    comment.isLiked = false
    comment.likeCount--
  } else {
    comment.isLiked = true
    comment.likeCount++
  }
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
.toc-card,
.related-articles {
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

/* 相关文章 */
.related-articles h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  margin-bottom: 15px;
  color: #333;
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.related-item {
  cursor: pointer;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.related-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.related-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 6px;
  line-height: 1.4;
  transition: color 0.2s;
}

.related-title:hover {
  color: #409eff;
}

.related-meta {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 12px;
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