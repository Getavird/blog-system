<template>
  <div class="user-public-page">
    <Header />
    
    <div class="user-container">
      <div class="container">
        <!-- 用户信息卡片 -->
        <div class="user-profile-card">
          <!-- 加载状态 -->
          <div v-if="publicUserLoading && !publicUser.username" class="loading-state">
            <div class="skeleton-avatar"></div>
            <div class="skeleton-info">
              <div class="skeleton-line large"></div>
              <div class="skeleton-line medium"></div>
              <div class="skeleton-line small"></div>
            </div>
          </div>

          <!-- 用户信息 -->
          <div v-else-if="publicUser.username" class="user-basic-info">
            <!-- 头像 -->
            <div class="user-avatar">
              <img v-if="publicUser.avatar" :src="publicUser.avatar" alt="用户头像" class="avatar-image" />
              <div v-else class="avatar-placeholder">
                {{ publicUser.username.charAt(0).toUpperCase() }}
              </div>
            </div>
            
            <!-- 用户详情 -->
            <div class="user-details">
              <div class="user-header">
                <h1 class="username">{{ publicUser.username }}</h1>
                <div class="user-meta">
                  <span class="user-bio">{{ publicUser.bio || '这个人很懒，什么都没有写～' }}</span>
                </div>
              </div>
              
              <!-- 统计信息 -->
              <div class="user-stats">
                <div class="stat-item">
                  <div class="stat-number">{{ publicUserStats.articleCount || 0 }}</div>
                  <div class="stat-label">文章</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">{{ publicUserStats.likeCount || 0 }}</div>
                  <div class="stat-label">获赞</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">{{ publicUserStats.viewCount || 0 }}</div>
                  <div class="stat-label">阅读</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">{{ publicUserStats.followerCount || 0 }}</div>
                  <div class="stat-label">粉丝</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">{{ publicUserStats.followingCount || 0 }}</div>
                  <div class="stat-label">关注</div>
                </div>
              </div>
              
              <!-- 操作按钮 -->
              <div class="user-actions">
                <el-button 
                  v-if="showFollowButton" 
                  :type="publicUser.isFollowed ? 'default' : 'primary'" 
                  @click="toggleFollow"
                  :loading="followLoading"
                  :disabled="followLoading"
                >
                  <el-icon><Star /></el-icon>
                  {{ publicUser.isFollowed ? '已关注' : '关注' }}
                </el-button>
                
                <!-- 如果是自己的主页，显示编辑按钮 -->
                <el-button 
                  v-if="isMyProfile" 
                  type="primary" 
                  @click="goToMyProfile"
                >
                  <el-icon><Edit /></el-icon>
                  编辑个人资料
                </el-button>
              </div>
            </div>
          </div>

          <!-- 用户不存在或错误 -->
          <div v-else class="error-state">
            <el-result
              icon="error"
              title="用户不存在"
              sub-title="请检查用户名是否正确"
            >
              <template #extra>
                <el-button type="primary" @click="goToHome">返回首页</el-button>
              </template>
            </el-result>
          </div>
          
          <!-- 注册时间 -->
          <div v-if="publicUser.createTime" class="user-register-time">
            <el-icon><Calendar /></el-icon>
            注册于 {{ formatTime(publicUser.createTime) }}
          </div>
        </div>

        <!-- 文章区域 -->
        <div v-if="publicUser.username" class="user-articles-section">
          <div class="section-header">
            <h2>文章列表</h2>
            <div class="article-count">共 {{ publicUserTotal }} 篇文章</div>
          </div>

          <!-- 文章加载状态 -->
          <div v-if="publicUserLoading && publicUserArticles.length === 0" class="loading-state">
            <div class="skeleton-item" v-for="n in 5" :key="n">
              <div class="skeleton-title"></div>
              <div class="skeleton-meta"></div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else-if="publicUserArticles.length === 0" class="empty-state">
            <div class="empty-content">
              <el-icon :size="60" color="#c0c4cc">
                <Document />
              </el-icon>
              <h3>暂无文章</h3>
              <p>{{ publicUser.username }} 还没有发布过文章</p>
            </div>
          </div>

          <!-- 文章列表 -->
          <div v-else class="articles-list">
            <div 
              v-for="article in publicUserArticles" 
              :key="article.id"
              class="article-item"
              @click="viewArticle(article.id)"
            >
              <div class="article-content">
                <div class="article-header">
                  <h3 class="article-title">{{ article.title }}</h3>
                  <el-tag v-if="article.status === 0" type="info" size="small">草稿</el-tag>
                </div>
                
                <div class="article-meta">
                  <span class="meta-item">
                    <el-icon><Calendar /></el-icon>
                    {{ formatTime(article.createTime) }}
                  </span>
                  <span class="meta-item">
                    <el-icon><View /></el-icon>
                    {{ article.viewCount || 0 }}
                  </span>
                  <span class="meta-item">
                    <el-icon><Star /></el-icon>
                    {{ article.likeCount || 0 }}
                  </span>
                  <span class="meta-item">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ article.commentCount || 0 }}
                  </span>
                </div>
                
                <div v-if="article.summary" class="article-summary">
                  {{ article.summary }}
                </div>
                
                <div class="article-footer">
                  <div class="article-tags">
                    <el-tag 
                      v-for="tag in article.tags || []" 
                      :key="tag"
                      size="small"
                      class="tag-item"
                      @click.stop="goToTag(tag)"
                    >
                      {{ tag }}
                    </el-tag>
                  </div>
                  <div class="article-category" v-if="article.categoryName">
                    <el-icon><Folder /></el-icon>
                    {{ article.categoryName }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="publicUserArticles.length > 0 && publicUserTotal > pageSize" class="pagination-wrapper">
            <el-pagination
              :current-page="currentPage"
              :page-size="pageSize"
              :total="publicUserTotal"
              :page-sizes="[10, 20, 30, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  Star,
  Edit,
  Calendar,
  View,
  ChatDotRound,
  Document,
  Folder
} from '@element-plus/icons-vue'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 路由参数
const username = ref(route.params.username)

// 分页状态
const currentPage = ref(1)
const pageSize = ref(10)
const followLoading = ref(false)

// 计算属性：从store获取数据
const publicUser = computed(() => userStore.publicUser)
const publicUserArticles = computed(() => userStore.publicUserArticles)
const publicUserStats = computed(() => userStore.publicUserStats)
const publicUserTotal = computed(() => userStore.publicUserTotal)
const publicUserLoading = computed(() => userStore.publicUserLoading)

// 登录状态
const isLoggedIn = computed(() => userStore.isLoggedIn())
const currentUser = computed(() => userStore.user)

// 是否是自己的主页
const isMyProfile = computed(() => {
  return isLoggedIn.value && currentUser.value?.username === username.value
})

// 是否显示关注按钮
const showFollowButton = computed(() => {
  return isLoggedIn.value && !isMyProfile.value && publicUser.value.id
})

// 生命周期
onMounted(async () => {
  // 初始化用户状态
  userStore.initFromStorage()
  
  // 加载公开用户数据
  await loadPublicUserData()
})

// 监听路由变化
watch(
  () => route.params.username,
  async (newUsername) => {
    if (newUsername) {
      username.value = newUsername
      currentPage.value = 1
      await loadPublicUserData()
    }
  }
)

// 加载公开用户数据
const loadPublicUserData = async () => {
  try {
    // 清空之前的数据
    userStore.clearPublicUserData()
    
    console.log('开始加载用户数据，用户名:', username.value)
    
    // 1. 加载用户信息
    const userInfo = await userStore.fetchPublicUserInfo(username.value)
    console.log('获取用户信息返回:', userInfo)
    
    // 检查用户是否存在
    if (!publicUser.value.id && !publicUser.value.username) {
      // 如果用户信息为空，可能是用户不存在
      if (userInfo && userInfo.code === 404) {
        ElMessage.error('用户不存在')
      } else if (userInfo && userInfo.message) {
        ElMessage.warning(userInfo.message)
      }
      return
    }
    
    console.log('用户信息加载成功:', publicUser.value)
    
    // 2. 并行加载文章和统计
    await Promise.all([
      loadUserArticles(),
      loadUserStats()
    ])
    
    // 3. 如果已登录且不是自己的主页，检查关注状态
    if (showFollowButton.value) {
      await checkFollowStatus()
    }
    
  } catch (error) {
    console.error('加载用户数据失败:', error)
    
    // 根据错误类型显示不同提示
    if (error.response?.status === 404) {
      ElMessage.error('用户不存在')
    } else if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('加载用户数据失败，请稍后重试')
    }
  }
}

// 加载用户文章
const loadUserArticles = async () => {
  try {
    console.log('开始加载用户文章，参数:', {
      username: username.value,
      page: currentPage.value,
      size: pageSize.value
    })
    
    await userStore.fetchPublicUserArticles(username.value, {
      page: currentPage.value,
      size: pageSize.value
    })
    
    console.log('文章加载成功，数量:', publicUserArticles.value.length)
  } catch (error) {
    console.error('加载用户文章失败:', error)
    // 如果文章加载失败，只提示但不阻止页面显示
    if (error.response?.status !== 404) {
      ElMessage.warning('文章加载失败')
    }
  }
}

// 加载用户统计
const loadUserStats = async () => {
  try {
    console.log('开始加载用户统计')
    
    const statsData = await userStore.fetchPublicUserStats(username.value)
    console.log('获取用户统计返回:', statsData)
    
    // 如果后端返回null或空，使用默认值
    if (!statsData && !publicUserStats.value.articleCount) {
      publicUserStats.value = {
        articleCount: 0,
        likeCount: 0,
        viewCount: 0,
        followerCount: 0,
        followingCount: 0
      }
    }
    
    console.log('用户统计加载成功:', publicUserStats.value)
  } catch (error) {
    console.error('加载用户统计失败:', error)
    // 统计加载失败不影响页面显示
  }
}

// 检查关注状态
const checkFollowStatus = async () => {
  try {
    // 确保有用户ID
    if (!publicUser.value?.id) {
      console.warn('无法检查关注状态：用户ID不存在')
      return
    }
    
    console.log('检查关注状态，用户ID:', publicUser.value.id)
    
    // 调用API检查关注状态
    const data = await userStore.checkFollowStatus(publicUser.value.id)
    console.log('关注状态返回:', data)
    
    // 根据返回的数据结构设置关注状态
    if (data && typeof data.isFollowing === 'boolean') {
      publicUser.value.isFollowed = data.isFollowing
    } else if (data?.data && typeof data.data.isFollowing === 'boolean') {
      publicUser.value.isFollowed = data.data.isFollowing
    } else if (data?.isFollowing !== undefined) {
      publicUser.value.isFollowed = data.isFollowing
    }
    
    console.log('最终关注状态:', publicUser.value.isFollowed)
    
  } catch (error) {
    console.error('检查关注状态失败:', error)
  }
}

// 切换关注状态
const toggleFollow = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  
  if (!publicUser.value.id) {
    ElMessage.error('无法操作，用户ID不存在')
    return
  }
  
  try {
    followLoading.value = true
    
    console.log('切换关注状态，用户ID:', publicUser.value.id, '当前状态:', publicUser.value.isFollowed)
    
    if (publicUser.value.isFollowed) {
      await userStore.unfollowUser(publicUser.value.id)
      ElMessage.success('已取消关注')
    } else {
      await userStore.followUser(publicUser.value.id)
      ElMessage.success('关注成功')
    }
    
    // 更新本地状态
    publicUser.value.isFollowed = !publicUser.value.isFollowed
    
    // 更新粉丝数统计
    if (publicUser.value.isFollowed) {
      publicUserStats.value.followerCount = (publicUserStats.value.followerCount || 0) + 1
    } else {
      publicUserStats.value.followerCount = Math.max(0, (publicUserStats.value.followerCount || 1) - 1)
    }
    
    console.log('关注状态切换成功，新状态:', publicUser.value.isFollowed)
    
  } catch (error) {
    console.error('操作关注失败:', error)
    
    if (error.response?.status === 401) {
      ElMessage.error('请先登录')
    } else if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('操作失败，请稍后重试')
    }
  } finally {
    followLoading.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  
  try {
    const date = new Date(time)
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    
    if (days === 0) {
      // 今天
      return '今天 ' + date.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    } else if (days === 1) {
      return '昨天'
    } else if (days < 7) {
      return `${days}天前`
    } else if (days < 30) {
      const weeks = Math.floor(days / 7)
      return `${weeks}周前`
    } else if (days < 365) {
      const months = Math.floor(days / 30)
      return `${months}个月前`
    } else {
      return date.toLocaleDateString('zh-CN')
    }
  } catch (error) {
    console.error('格式化时间失败:', error, time)
    return ''
  }
}

// 查看文章
const viewArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

// 跳转到标签页
const goToTag = (tagName) => {
  router.push(`/tag/${encodeURIComponent(tagName)}`)
}

// 跳转到个人中心
const goToMyProfile = () => {
  router.push('/user/profile')
}

// 返回首页
const goToHome = () => {
  router.push('/')
}

// 分页处理
const handlePageChange = async (page) => {
  currentPage.value = page
  await loadUserArticles()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 每页数量改变
const handleSizeChange = async (size) => {
  pageSize.value = size
  currentPage.value = 1
  await loadUserArticles()
}

// 重新加载页面
const reloadPage = () => {
  window.location.reload()
}
</script>

<style scoped>
.user-public-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.user-container {
  flex: 1;
  padding: 20px 0 40px;
}

.container {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 用户信息卡片 */
.user-profile-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  position: relative;
  min-height: 200px;
}

/* 加载状态 */
.loading-state {
  display: flex;
  gap: 30px;
  align-items: center;
}

.skeleton-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
}

.skeleton-info {
  flex: 1;
}

.skeleton-line {
  height: 20px;
  background: #f0f0f0;
  border-radius: 4px;
  margin-bottom: 10px;
}

.skeleton-line.large {
  width: 60%;
  height: 28px;
}

.skeleton-line.medium {
  width: 80%;
}

.skeleton-line.small {
  width: 40%;
  height: 16px;
}

@keyframes loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 错误状态 */
.error-state {
  text-align: center;
  padding: 40px 0;
}

/* 用户信息 */
.user-basic-info {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
}

.user-avatar {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: bold;
  color: white;
  border: 3px solid white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-details {
  flex: 1;
}

.user-header {
  margin-bottom: 20px;
}

.username {
  font-size: 32px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 600;
}

.user-bio {
  color: #666;
  font-size: 16px;
  line-height: 1.5;
}

/* 统计信息 */
.user-stats {
  display: flex;
  gap: 40px;
  margin-bottom: 25px;
  flex-wrap: wrap;
}

.stat-item {
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-item:hover {
  transform: translateY(-2px);
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

/* 操作按钮 */
.user-actions {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

/* 注册时间 */
.user-register-time {
  color: #999;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

/* 文章区域 */
.user-articles-section {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.section-header h2 {
  font-size: 20px;
  color: #333;
  margin: 0;
}

.article-count {
  color: #666;
  font-size: 14px;
}

/* 文章列表 */
.articles-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.article-item {
  padding: 20px;
  border-radius: 8px;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.article-item:hover {
  background: white;
  border-color: #e1e4e8;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.article-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.article-title {
  font-size: 18px;
  color: #333;
  margin: 0;
  font-weight: 500;
  flex: 1;
}

.article-meta {
  display: flex;
  gap: 20px;
  color: #999;
  font-size: 13px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-summary {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 15px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-item {
  cursor: pointer;
  transition: transform 0.2s;
}

.tag-item:hover {
  transform: translateY(-2px);
}

.article-category {
  color: #409eff;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 空状态 */
.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.empty-content h3 {
  font-size: 18px;
  color: #333;
  margin: 15px 0 8px;
}

.empty-content p {
  color: #666;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-basic-info {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .user-avatar {
    margin: 0 auto;
  }
  
  .user-stats {
    justify-content: center;
    gap: 20px;
  }
  
  .stat-item {
    min-width: 60px;
  }
  
  .article-meta {
    gap: 10px;
  }
  
  .article-footer {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .article-category {
    align-self: flex-start;
  }
  
  .user-actions {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .user-profile-card {
    padding: 20px;
  }
  
  .user-articles-section {
    padding: 20px;
  }
  
  .username {
    font-size: 24px;
  }
  
  .user-bio {
    font-size: 14px;
  }
  
  .stat-number {
    font-size: 20px;
  }
  
  .article-title {
    font-size: 16px;
  }
}
</style>