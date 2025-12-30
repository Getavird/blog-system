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
                <div class="stat-item" @click="showArticles">
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
                <div class="stat-item" @click="showFollowers">
                  <div class="stat-number">{{ publicUserStats.followerCount || 0 }}</div>
                  <div class="stat-label">粉丝</div>
                </div>
                <div class="stat-item" @click="showFollowing">
                  <div class="stat-number">{{ publicUserStats.followingCount || 0 }}</div>
                  <div class="stat-label">关注</div>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="user-actions">
                <el-button v-if="showFollowButton" :type="publicUser.isFollowed ? 'default' : 'primary'"
                  @click="toggleFollow" :loading="followLoading" :disabled="followLoading || !isLoggedIn" round>
                  <el-icon>
                    <Star />
                  </el-icon>
                  {{ publicUser.isFollowed ? '已关注' : '关注' }}
                  <span v-if="publicUser.isFollowed" style="margin-left: 5px">✓</span>
                </el-button>

                <!-- 如果是自己的主页，显示编辑按钮 -->
                <el-button v-if="isMyProfile" type="primary" @click="goToMyProfile" round>
                  <el-icon>
                    <Edit />
                  </el-icon>
                  编辑个人资料
                </el-button>

                <!-- 如果未登录，显示登录提示 -->
                <el-button v-if="!isLoggedIn && !isMyProfile" type="text" @click="showLoginPrompt" size="small">
                  <el-icon>
                    <User />
                  </el-icon>
                  登录后关注
                </el-button>
              </div>
            </div>
          </div>

          <!-- 用户不存在或错误 -->
          <div v-else-if="!publicUserLoading" class="error-state">
            <el-result icon="error" title="用户不存在" :sub-title="errorMessage || '请检查用户名是否正确'">
              <template #extra>
                <el-button type="primary" @click="goToHome">返回首页</el-button>
                <el-button @click="reloadPage">重新加载</el-button>
              </template>
            </el-result>
          </div>

          <!-- 注册时间 -->
          <div v-if="publicUser.createTime" class="user-register-time">
            <el-icon>
              <Calendar />
            </el-icon>
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
              <div class="skeleton-content"></div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else-if="publicUserArticles.length === 0 && !publicUserLoading" class="empty-state">
            <div class="empty-content">
              <el-icon :size="60" color="#c0c4cc">
                <Document />
              </el-icon>
              <h3>暂无文章</h3>
              <p>{{ publicUser.username }} 还没有发布过文章</p>
              <el-button v-if="isMyProfile" type="primary" @click="goToCreateArticle" class="create-article-btn">
                去写第一篇文章
              </el-button>
            </div>
          </div>

          <!-- 文章列表 -->
          <div v-else class="articles-list">
            <div v-for="article in publicUserArticles" :key="article.id" class="article-item"
              @click="viewArticle(article.id)">
              <div class="article-content">
                <div class="article-header">
                  <h3 class="article-title">{{ article.title }}</h3>
                  <div class="article-status">
                    <el-tag v-if="article.status === 0" type="info" size="small">草稿</el-tag>
                    <el-tag v-if="article.status === 2" type="warning" size="small">待审核</el-tag>
                  </div>
                </div>

                <div class="article-meta">
                  <span class="meta-item">
                    <el-icon>
                      <Calendar />
                    </el-icon>
                    {{ formatTime(article.createTime) }}
                  </span>
                  <span class="meta-item">
                    <el-icon>
                      <View />
                    </el-icon>
                    {{ article.viewCount || 0 }}
                  </span>
                  <span class="meta-item">
                    <el-icon>
                      <Star />
                    </el-icon>
                    {{ article.likeCount || 0 }}
                  </span>
                  <span class="meta-item">
                    <el-icon>
                      <ChatDotRound />
                    </el-icon>
                    {{ article.commentCount || 0 }}
                  </span>
                </div>

                <div v-if="article.summary" class="article-summary">
                  {{ article.summary }}
                </div>

                <div class="article-footer">
                  <div class="article-tags">
                  </div>
                  <div class="article-category" v-if="article.categoryName">
                    <el-icon>
                      <Folder />
                    </el-icon>
                    {{ article.categoryName }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="publicUserArticles.length > 0 && publicUserTotal > pageSize" class="pagination-wrapper">
            <el-pagination :current-page="currentPage" :page-size="pageSize" :total="publicUserTotal"
              :page-sizes="[10, 20, 30, 50]" layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange" @current-change="handlePageChange" :hide-on-single-page="true" />
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
import { useAuthStore } from '@/stores/auth'
import { useFollowStore } from '@/stores/follow'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Star,
  Edit,
  Calendar,
  View,
  ChatDotRound,
  Document,
  Folder,
  User
} from '@element-plus/icons-vue'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const authStore = useAuthStore()
const followStore = useFollowStore()
// 路由参数
const username = ref(route.params.username)

// 分页状态
const currentPage = ref(1)
const pageSize = ref(10)
const followLoading = ref(false)
const errorMessage = ref('')

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
  return !isMyProfile.value && publicUser.value.id
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
    if (newUsername && newUsername !== username.value) {
      username.value = newUsername
      currentPage.value = 1
      await loadPublicUserData()
    }
  },
  { immediate: true }
)

// 加载公开用户数据
const loadPublicUserData = async () => {
  try {
    // 清空之前的数据和错误
    userStore.clearPublicUserData()
    errorMessage.value = ''

    console.log('开始加载用户数据，用户名:', username.value)

    if (!username.value) {
      errorMessage.value = '用户名不能为空'
      return
    }

    // 1. 加载用户信息
    const userInfo = await userStore.fetchPublicUserInfo(username.value)
    console.log('获取用户信息返回:', userInfo)

    // 检查用户是否存在
    if (!publicUser.value || !publicUser.value.id) {
      errorMessage.value = '用户不存在或已被删除'
      ElMessage.error('用户不存在')
      return
    }

    console.log('用户信息加载成功:', publicUser.value)

    // 2. 并行加载文章和统计
    await Promise.all([
      loadUserArticles(),
      loadUserStats()
    ])

    // 3. 如果已登录且不是自己的主页，检查关注状态
    if (showFollowButton.value && isLoggedIn.value) {
      await checkFollowStatus()
    }

  } catch (error) {
    console.error('加载用户数据失败:', error)

    // 根据错误类型显示不同提示
    if (error.response?.status === 404) {
      errorMessage.value = '用户不存在'
      ElMessage.error('用户不存在')
    } else if (error.response?.status === 401) {
      errorMessage.value = '需要登录才能查看'
      ElMessage.warning('登录后可查看完整信息')
    } else if (error.message) {
      errorMessage.value = error.message
      ElMessage.error('加载用户数据失败，请稍后重试')
    } else {
      errorMessage.value = '网络错误或服务器异常'
      ElMessage.error('加载用户数据失败，请稍后重试')
    }
  }
}

// 加载用户文章
const loadUserArticles = async () => {
  try {
    console.log('加载用户文章，参数:', {
      username: username.value,
      page: currentPage.value,
      size: pageSize.value
    })

    const response = await userStore.fetchPublicUserArticles(username.value, {
      page: currentPage.value,
      size: pageSize.value
    })
    
    console.log('文章API返回原始数据:', response)
    
    // 处理分页数据 - 根据后端实际返回的数据结构调整
    let articles = []
    let total = 0
    
    // 情况1: 如果返回的是 {total, page, size, data, totalPages} 格式
    if (response && response.data && Array.isArray(response.data)) {
      articles = response.data
      total = response.total || response.data.length
    }
    // 情况2: 如果后端返回的是 Result 格式 {code: 200, data: {...}, message: '...'}
    else if (response && response.code === 200) {
      const resultData = response.data
      console.log('Result格式的data字段:', resultData)
      
      if (resultData) {
        // 检查是否有data字段
        if (resultData.data && Array.isArray(resultData.data)) {
          articles = resultData.data
          total = resultData.total || resultData.data.length
        }
        // 检查是否有list字段
        else if (resultData.list && Array.isArray(resultData.list)) {
          articles = resultData.list
          total = resultData.total || resultData.list.length
        }
        // 直接是数组
        else if (Array.isArray(resultData)) {
          articles = resultData
          total = resultData.length
        }
      }
    }
    // 情况3: 直接返回数组
    else if (Array.isArray(response)) {
      articles = response
      total = response.length
    }
    
    console.log('提取的文章数据:', articles)
    console.log('文章总数:', total)
    
    // 转换文章数据格式 - 确保所有必要字段都有值
    const transformedArticles = articles.map(article => {
      // 先检查数据结构
      console.log('单篇文章原始数据:', article)
      
      const transformed = {
        id: article.id || article.articleId || 0,
        title: article.title || '无标题',
        content: article.content || '',
        summary: article.summary || article.content?.substring(0, 100) || '',
        coverImage: article.coverImage 
          ? (article.coverImage.startsWith('http') ? article.coverImage : `/uploads/${article.coverImage}`)
          : '',
        status: article.status || 1,
        viewCount: article.viewCount || 0,
        likeCount: article.likeCount || 0,
        commentCount: article.commentCount || 0,
        categoryName: article.categoryName || article.category?.name || '未分类',
        authorName: article.authorName || article.user?.username || username.value,
        authorAvatar: article.authorAvatar 
          ? (article.authorAvatar.startsWith('http') ? article.authorAvatar : `/uploads/avatars/${article.authorAvatar}`)
          : (article.user?.avatar 
              ? (article.user.avatar.startsWith('http') ? article.user.avatar : `/uploads/avatars/${article.user.avatar}`)
              : '/static/images/default-avatars/default_avatar.png'),
        createTime: article.createTime || article.createdAt || new Date().toISOString(),
        updateTime: article.updateTime || article.updatedAt || article.createTime,
        publishTime: article.publishTime || article.publishedAt || article.createTime
      }
      
      // 处理tags字段
      if (article.tags) {
        if (Array.isArray(article.tags)) {
          transformed.tags = article.tags
        } else if (typeof article.tags === 'string') {
          transformed.tags = article.tags.split(',').map(tag => tag.trim()).filter(tag => tag)
        }
      } else {
        transformed.tags = []
      }
      
      return transformed
    })
    
    console.log('转换后的文章数据:', transformedArticles)
    
    // 更新store中的数据
    userStore.setPublicUserArticles(transformedArticles)
    userStore.setPublicUserTotal(total)
    
    console.log('文章加载成功，数量:', transformedArticles.length)
    
    // 同时更新统计信息中的文章数量
    if (total > 0) {
      userStore.setPublicUserStats({
        ...publicUserStats.value,
        articleCount: total
      })
    }
    
    return transformedArticles
  } catch (error) {
    console.error('加载用户文章失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    if (error.response?.status !== 404) {
      ElMessage.warning('文章加载失败，部分内容可能无法显示')
    }
    return []
  }
}


// 加载用户统计
const loadUserStats = async () => {
  try {
    console.log('开始加载用户统计')
    
    const response = await userStore.fetchPublicUserStats(username.value)
    console.log('统计API返回原始数据:', response)
    
    // 处理统计数据 - 根据后端实际返回的数据结构调整
    let stats = {}
    
    if (response) {
      // 如果返回的是 Result 格式 {code: 200, data: {...}, message: '...'}
      if (response.code === 200 && response.data) {
        stats = response.data
      }
      // 直接返回对象
      else {
        stats = response
      }
    }
    
    console.log('提取的统计数据:', stats)
    
    // 确保所有必需的统计字段都存在
    const defaultStats = {
      articleCount: 0,
      likeCount: 0,
      viewCount: 0,
      followerCount: 0,
      followingCount: 0,
      commentCount: 0,
      collectionCount: 0
    }
    
    // 合并数据，优先使用API返回的数据
    const finalStats = { ...defaultStats, ...stats }
    
    console.log('最终用户统计:', finalStats)
    
    // 更新store中的统计数据
    userStore.setPublicUserStats(finalStats)
    
    return finalStats
  } catch (error) {
    console.error('加载用户统计失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    // 使用默认统计数据
    const defaultStats = {
      articleCount: 0,
      likeCount: 0,
      viewCount: 0,
      followerCount: 0,
      followingCount: 0,
      commentCount: 0,
      collectionCount: 0
    }
    userStore.setPublicUserStats(defaultStats)
    return defaultStats
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

    // 使用followStore检查关注状态
    const isFollowing = await followStore.checkFollowStatus(publicUser.value.id)
    
    // 更新本地状态
    publicUser.value.isFollowed = isFollowing
    console.log('关注状态检查结果:', isFollowing)

  } catch (error) {
    console.error('检查关注状态失败:', error)
    // 关注状态检查失败不影响主要功能
  }
}


// 切换关注状态
const toggleFollow = async () => {
  if (!isLoggedIn.value) {
    showLoginPrompt()
    return
  }

  if (!publicUser.value.id) {
    ElMessage.error('无法操作，用户ID不存在')
    return
  }

  try {
    followLoading.value = true

    console.log('切换关注状态，用户ID:', publicUser.value.id, '当前状态:', publicUser.value.isFollowed)

    // 使用followStore切换关注状态
    const newFollowStatus = await followStore.toggleFollow(publicUser.value.id, publicUser.value.isFollowed)
    
    // 只更新关注状态，不更新粉丝数
    publicUser.value.isFollowed = newFollowStatus
    
    // 移除以下更新粉丝数的代码，因为已经在 followStore 中更新了
    // if (newFollowStatus) {
    //   publicUserStats.value.followerCount = (publicUserStats.value.followerCount || 0) + 1
    //   ElMessage.success('关注成功')
    // } else {
    //   publicUserStats.value.followerCount = Math.max(0, (publicUserStats.value.followerCount || 1) - 1)
    //   ElMessage.success('已取消关注')
    // }
    
    // 显示操作成功消息
    if (newFollowStatus) {
      ElMessage.success('关注成功')
    } else {
      ElMessage.success('已取消关注')
    }

    console.log('关注状态切换成功，新状态:', publicUser.value.isFollowed)

  } catch (error) {
    console.error('操作关注失败:', error)

    if (error.response?.status === 401) {
      ElMessage.error('请先登录')
    } else if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error(error.message || '操作失败，请稍后重试')
    }
  } finally {
    followLoading.value = false
  }
}

// 显示登录提示
const showLoginPrompt = () => {
  ElMessage.warning({
    message: '请先登录',
    duration: 2000,
    onClose: () => {
      router.push({
        path: '/',
        query: { showLogin: true }
      })
    }
  })
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '未知'

  try {
    const date = new Date(time)
    const now = new Date()
    const diff = now.getTime() - date.getTime()

    // 如果是今年，显示月日，否则显示年月日
    if (date.getFullYear() === now.getFullYear()) {
      return date.toLocaleDateString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    } else {
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      })
    }
  } catch (error) {
    console.error('格式化时间失败:', error, time)
    return time
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

// 跳转到创建文章
const goToCreateArticle = () => {
  router.push('/article/create')
}

// 返回首页
const goToHome = () => {
  router.push('/')
}

// 显示粉丝列表
const showFollowers = () => {
  ElMessage.info('粉丝列表功能开发中')
  // TODO: 实现粉丝列表弹窗
}

// 显示关注列表
const showFollowing = () => {
  ElMessage.info('关注列表功能开发中')
  // TODO: 实现关注列表弹窗
}

// 显示文章列表（滚动到文章区域）
const showArticles = () => {
  const articlesSection = document.querySelector('.user-articles-section')
  if (articlesSection) {
    articlesSection.scrollIntoView({ behavior: 'smooth' })
  }
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
.create-article-btn {
  margin-top: 20px;
}

.user-actions .el-button {
  transition: all 0.3s;
}

.user-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.skeleton-item {
  padding: 20px;
  border-radius: 8px;
  background: #f8f9fa;
  margin-bottom: 10px;
}

.skeleton-title {
  height: 24px;
  width: 60%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  margin-bottom: 12px;
  animation: loading 1.5s infinite;
}

.skeleton-meta {
  height: 16px;
  width: 40%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  margin-bottom: 12px;
  animation: loading 1.5s infinite;
}

.skeleton-content {
  height: 16px;
  width: 80%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  animation: loading 1.5s infinite;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }

  100% {
    background-position: -200% 0;
  }
}


.article-status {
  display: flex;
  gap: 8px;
}

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
  0% {
    background-position: 200% 0;
  }

  100% {
    background-position: -200% 0;
  }
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
  justify-content: space-between;
  align-items: flex-start;
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