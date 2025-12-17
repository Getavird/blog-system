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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  Star,
  View,
  User,
  Edit,
  SwitchButton
} from '@element-plus/icons-vue'
import Header from '../components/layout/Header.vue'
import Footer from '../components/layout/Footer.vue'

const router = useRouter()

// 用户信息表单
const infoFormRef = ref(null)
const userForm = ref({
  username: '',
  email: '',
  bio: '',
  avatar: ''
})

// 密码表单
const passwordFormRef = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 用户统计
const userStats = ref({
  articleCount: 0,
  likeCount: 0,
  viewCount: 0,
  followerCount: 0
})

// 状态
const uploadingAvatar = ref(false)
const savingInfo = ref(false)
const changingPassword = ref(false)

// 用户信息验证规则
const infoRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  bio: [
    { max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' }
  ]
}

// 密码验证规则
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能小于6位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 模拟数据
const lastLoginTime = ref('2024-01-16 14:30:00')
const lastLoginIp = ref('192.168.1.100')
const registerTime = ref('2024-01-01 10:00:00')

// 计算属性
const avatarPlaceholder = computed(() => {
  return userForm.value.username ? userForm.value.username.charAt(0).toUpperCase() : 'U'
})

// 生命周期
onMounted(() => {
  loadUserData()
})

// 加载用户数据
const loadUserData = () => {
  // 从localStorage获取用户信息
  const userStr = localStorage.getItem('blog_user')
  if (userStr) {
    try {
      const userData = JSON.parse(userStr)
      userForm.value = {
        username: userData.username || '',
        email: userData.email || '',
        bio: userData.bio || '',
        avatar: userData.avatar || ''
      }
      
      // 加载用户统计
      userStats.value = {
        articleCount: userData.articleCount || 5,
        likeCount: userData.likeCount || 23,
        viewCount: userData.viewCount || 156,
        followerCount: userData.followerCount || 12
      }
    } catch (error) {
      console.error('解析用户数据失败:', error)
      ElMessage.error('加载用户数据失败')
    }
  }
}

// 头像上传
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
    return false
  }
  return true
}

const uploadAvatar = async (options) => {
  uploadingAvatar.value = true
  
  try {
    // 模拟上传
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟上传成功，生成一个占位图
    const mockAvatarUrl = `https://via.placeholder.com/300x300/409eff/ffffff?text=${userForm.value.username.charAt(0)}`
    
    userForm.value.avatar = mockAvatarUrl
    
    // 更新localStorage中的用户信息
    updateLocalStorage()
    
    ElMessage.success('头像上传成功')
  } catch (error) {
    ElMessage.error('头像上传失败')
  } finally {
    uploadingAvatar.value = false
  }
}

// 保存用户信息
const saveUserInfo = async () => {
  if (!infoFormRef.value) return
  
  try {
    await infoFormRef.value.validate()
    savingInfo.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 更新localStorage
    updateLocalStorage()
    
    ElMessage.success('用户信息保存成功')
  } catch (error) {
    ElMessage.error('保存失败，请检查表单')
  } finally {
    savingInfo.value = false
  }
}

// 重置表单
const resetInfoForm = () => {
  loadUserData()
  if (infoFormRef.value) {
    infoFormRef.value.clearValidate()
  }
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 清空密码表单
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
    
    ElMessage.success('密码修改成功')
  } catch (error) {
    ElMessage.error('密码修改失败')
  } finally {
    changingPassword.value = false
  }
}

// 更新localStorage中的用户信息
const updateLocalStorage = () => {
  const userStr = localStorage.getItem('blog_user')
  if (userStr) {
    try {
      const userData = JSON.parse(userStr)
      const updatedUser = {
        ...userData,
        ...userForm.value,
        articleCount: userStats.value.articleCount,
        likeCount: userStats.value.likeCount,
        viewCount: userStats.value.viewCount,
        followerCount: userStats.value.followerCount
      }
      localStorage.setItem('blog_user', JSON.stringify(updatedUser))
    } catch (error) {
      console.error('更新用户数据失败:', error)
    }
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 查看登录历史
const viewLoginHistory = () => {
  ElMessageBox.alert('登录历史功能开发中...', '提示', {
    confirmButtonText: '知道了'
  })
}

// 显示账户状态
const showAccountStatus = () => {
  ElMessageBox.alert('账户状态正常，无异常登录记录。', '账户状态', {
    confirmButtonText: '确定'
  })
}

// 导航方法
const goToArticles = () => {
  router.push('/user/articles')
}

const goToCreateArticle = () => {
  router.push('/article/create')
}

const logout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '退出确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    localStorage.removeItem('blog_token')
    localStorage.removeItem('blog_user')
    ElMessage.success('已退出登录')
    router.push('/')
  })
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