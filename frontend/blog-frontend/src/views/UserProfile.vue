<template>
  <div class="user-profile-page">
    <Header />
    
    <div class="profile-container">
      <div class="container">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1>个人中心</h1>
          <p class="page-subtitle">管理您的个人信息和账户设置</p>
        </div>

        <div class="profile-content">
          <!-- 左侧：用户信息卡片 -->
          <div class="profile-card">
            <!-- 头像区域 -->
            <div class="avatar-section">
              <div class="avatar-wrapper">
                <div class="avatar-preview">
                  <img v-if="userForm.avatar" :src="userForm.avatar" alt="用户头像" />
                  <div v-else class="avatar-placeholder">
                    {{ avatarPlaceholder }}
                  </div>
                </div>
                
                <div class="avatar-actions">
                  <el-upload
                    class="avatar-uploader"
                    action="#"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :http-request="uploadAvatar"
                  >
                    <el-button type="primary" size="small" :loading="uploadingAvatar">
                      {{ uploadingAvatar ? '上传中...' : '更换头像' }}
                    </el-button>
                  </el-upload>
                  <el-button 
                    v-if="userForm.avatar" 
                    type="text" 
                    size="small" 
                    @click="userForm.avatar = ''"
                  >
                    移除头像
                  </el-button>
                </div>
                <p class="upload-tip">支持 JPG、PNG 格式，大小不超过 2MB</p>
              </div>
            </div>

            <!-- 用户信息表单 -->
            <div class="info-form">
              <h3>基本信息</h3>
              <el-form
                ref="infoFormRef"
                :model="userForm"
                :rules="infoRules"
                label-width="80px"
                class="info-form-content"
              >
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="userForm.username" placeholder="请输入用户名" />
                </el-form-item>
                
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="userForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                
                <el-form-item label="个人简介" prop="bio">
                  <el-input
                    v-model="userForm.bio"
                    type="textarea"
                    :rows="3"
                    placeholder="介绍一下你自己吧..."
                    :maxlength="200"
                    show-word-limit
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-button 
                    type="primary" 
                    @click="saveUserInfo"
                    :loading="savingInfo"
                    class="save-btn"
                  >
                    {{ savingInfo ? '保存中...' : '保存修改' }}
                  </el-button>
                  <el-button @click="resetInfoForm">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>

          <!-- 右侧：其他设置 -->
          <div class="settings-section">
            <!-- 账户统计 -->
            <div class="stats-card">
              <h3>账户统计</h3>
              <div class="stats-grid">
                <div class="stat-item">
                  <div class="stat-icon">
                    <el-icon><Document /></el-icon>
                  </div>
                  <div class="stat-content">
                    <div class="stat-number">{{ userStats.articleCount || 0 }}</div>
                    <div class="stat-label">文章</div>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon">
                    <el-icon><Star /></el-icon>
                  </div>
                  <div class="stat-content">
                    <div class="stat-number">{{ userStats.likeCount || 0 }}</div>
                    <div class="stat-label">获赞</div>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon">
                    <el-icon><View /></el-icon>
                  </div>
                  <div class="stat-content">
                    <div class="stat-number">{{ userStats.viewCount || 0 }}</div>
                    <div class="stat-label">阅读</div>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon">
                    <el-icon><User /></el-icon>
                  </div>
                  <div class="stat-content">
                    <div class="stat-number">{{ userStats.followerCount || 0 }}</div>
                    <div class="stat-label">粉丝</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 修改密码 -->
            <div class="password-card">
              <h3>修改密码</h3>
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="80px"
                class="password-form"
              >
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    placeholder="请输入原密码"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    placeholder="请输入新密码"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-button 
                    type="primary" 
                    @click="changePassword"
                    :loading="changingPassword"
                    class="change-password-btn"
                  >
                    {{ changingPassword ? '修改中...' : '修改密码' }}
                  </el-button>
                </el-form-item>
              </el-form>
            </div>

            <!-- 账户安全 -->
            <div class="security-card">
              <h3>账户安全</h3>
              <div class="security-options">
                <div class="security-item">
                  <div class="security-info">
                    <h4>登录记录</h4>
                    <p>最近登录时间：{{ formatTime(lastLoginTime) }}</p>
                    <p>最近登录IP：{{ lastLoginIp || '未知' }}</p>
                  </div>
                  <el-button type="text" @click="viewLoginHistory">查看详情</el-button>
                </div>
                
                <div class="security-item">
                  <div class="security-info">
                    <h4>账户状态</h4>
                    <p>注册时间：{{ formatTime(registerTime) }}</p>
                    <p>账户状态：正常</p>
                  </div>
                  <el-button type="text" @click="showAccountStatus">查看详情</el-button>
                </div>
              </div>
            </div>

            <!-- 快速操作 -->
            <div class="quick-actions">
              <h3>快速操作</h3>
              <div class="action-buttons">
                <el-button @click="goToArticles" class="action-btn">
                  <el-icon><Document /></el-icon>
                  我的文章
                </el-button>
                <el-button @click="goToCreateArticle" type="primary" class="action-btn">
                  <el-icon><Edit /></el-icon>
                  写文章
                </el-button>
                <el-button @click="logout" type="danger" class="action-btn">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAuthStore } from '@/stores/auth'
import { useArticleStore } from '@/stores/article'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  Star,
  View,
  User,
  Edit,
  SwitchButton,
  Search
} from '@element-plus/icons-vue'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'

const router = useRouter()

// Pinia Store
const userStore = useUserStore()
const authStore = useAuthStore()
const articleStore = useArticleStore()

// 用户信息表单
const infoFormRef = ref(null)
const userForm = reactive({
  username: '',
  email: '',
  bio: '',
  avatar: ''
})

const savingInfo = ref(false)

// 表单验证规则
const infoRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度为2-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  bio: [
    { max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' }
  ]
})

// 密码表单
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const changingPassword = ref(false)

// 密码验证规则
const passwordRules = reactive({
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 头像上传
const uploadingAvatar = ref(false)

// 用户统计信息
const userStats = reactive({
  articleCount: 0,
  likeCount: 0,
  viewCount: 0,
  followerCount: 0
})

// 账户安全信息
const lastLoginTime = ref('')
const lastLoginIp = ref('')
const registerTime = ref('')

// 计算属性
const avatarPlaceholder = computed(() => {
  return userForm.username ? userForm.username.charAt(0).toUpperCase() : 'U'
})

// 组件挂载
onMounted(async () => {
  // 初始化用户状态
  userStore.initFromStorage()
  
  // 加载用户信息
  await loadUserInfo()
  
  // 加载用户统计
  await loadUserStats()
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    if (!userStore.user) {
      // 如果store中没有用户信息，尝试从API获取
      await authStore.fetchCurrentUser()
    }
    
    // 更新表单数据
    if (userStore.user) {
      Object.assign(userForm, {
        username: userStore.user.username || '',
        email: userStore.user.email || '',
        bio: userStore.user.bio || '',
        avatar: userStore.user.avatar || ''
      })
      
      // 设置注册时间
      registerTime.value = userStore.user.createTime || ''
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  }
}

// 加载用户统计
const loadUserStats = async () => {
  try {
    // TODO: 这里需要后端提供用户统计接口
    // 暂时使用默认值
    userStats.articleCount = 0
    userStats.likeCount = 0
    userStats.viewCount = 0
    userStats.followerCount = 0
    
    // 加载用户文章获取文章数
    const result = await articleStore.fetchMyArticles({ page: 1, size: 1 })
    if (result) {
      userStats.articleCount = result.total || 0
    }
    
    // 登录历史（需要后端接口）
    // lastLoginTime.value = userStore.user?.lastLoginTime || ''
    // lastLoginIp.value = userStore.user?.lastLoginIp || ''
    
  } catch (error) {
    console.error('加载用户统计失败:', error)
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '未知'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 头像上传前的验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 上传头像
const uploadAvatar = async (file) => {
  try {
    uploadingAvatar.value = true
    
    const result = await userStore.uploadAvatar(file.file)
    
    if (result && result.avatar) {
      userForm.avatar = result.avatar
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error('头像上传失败')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    ElMessage.error('头像上传失败')
  } finally {
    uploadingAvatar.value = false
  }
}

// 保存用户信息
const saveUserInfo = async () => {
  try {
    // 表单验证
    await infoFormRef.value.validate()
    
    savingInfo.value = true
    
    // 调用API更新用户信息
    await userStore.updateUserInfo(userStore.user.id, {
      username: userForm.username,
      email: userForm.email,
      bio: userForm.bio,
      avatar: userForm.avatar
    })
    
    ElMessage.success('用户信息更新成功')
  } catch (error) {
    if (error instanceof Error) {
      console.error('保存用户信息失败:', error)
      ElMessage.error(error.message || '保存失败')
    }
  } finally {
    savingInfo.value = false
  }
}

// 重置用户信息表单
const resetInfoForm = () => {
  if (userStore.user) {
    Object.assign(userForm, {
      username: userStore.user.username || '',
      email: userStore.user.email || '',
      bio: userStore.user.bio || '',
      avatar: userStore.user.avatar || ''
    })
  }
}

// 修改密码
const changePassword = async () => {
  try {
    // 表单验证
    await passwordFormRef.value.validate()
    
    changingPassword.value = true
    
    // 调用API修改密码
    await authStore.changePassword(
      passwordForm.oldPassword,
      passwordForm.newPassword
    )
    
    ElMessage.success('密码修改成功')
    
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.message || '修改密码失败')
  } finally {
    changingPassword.value = false
  }
}

// 查看登录历史（需要后端接口）
const viewLoginHistory = () => {
  ElMessage.info('登录历史功能待实现')
}

// 查看账户状态
const showAccountStatus = () => {
  ElMessage.info('账户状态功能待实现')
}

// 跳转到我的文章
const goToArticles = () => {
  router.push('/user/articles')
}

// 跳转到创建文章
const goToCreateArticle = () => {
  router.push('/article/create')
}

// 退出登录
const logout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    await authStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/')
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
      ElMessage.error('退出登录失败')
    }
  }
}
</script>

<style scoped>
.user-profile-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.profile-container {
  flex: 1;
  padding: 20px 0 40px;
}

.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.page-subtitle {
  color: #666;
  font-size: 14px;
}

.profile-content {
  display: flex;
  gap: 30px;
}

.profile-card {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
}

.settings-section {
  width: 400px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 头像区域 */
.avatar-section {
  margin-bottom: 30px;
}

.avatar-wrapper {
  text-align: center;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  margin: 0 auto 20px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: bold;
  color: white;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  text-align: center;
}

/* 信息表单 */
.info-form h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.info-form-content {
  padding-top: 10px;
}

.save-btn {
  min-width: 100px;
}

/* 统计卡片 */
.stats-card, .password-card, .security-card, .quick-actions {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
}

.stats-card h3, .password-card h3, .security-card h3, .quick-actions h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-item:hover {
  background: #e9ecef;
  transform: translateY(-2px);
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #666;
}

/* 密码表单 */
.password-form {
  padding-top: 10px;
}

.change-password-btn {
  width: 100%;
}

/* 安全设置 */
.security-options {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.security-info h4 {
  font-size: 14px;
  color: #333;
  margin-bottom: 6px;
}

.security-info p {
  font-size: 12px;
  color: #666;
  margin: 2px 0;
}

/* 快速操作 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  width: 100%;
  justify-content: flex-start;
  padding: 12px 20px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .profile-content {
    flex-direction: column;
  }
  
  .settings-section {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .profile-card {
    padding: 20px;
  }
  
  .stats-card, .password-card, .security-card, .quick-actions {
    padding: 20px;
  }
}
</style>