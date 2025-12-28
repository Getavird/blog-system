<template>
  <div class="article-edit-page">
    <Header />
    
    <div class="edit-container">
      <div class="container">
        <!-- 文章编辑头部 -->
        <div class="edit-header">
          <div class="header-left">
            <h1 v-if="articleId">编辑文章</h1>
            <h1 v-else>写文章</h1>
          </div>
          <div class="header-right">
            <el-button @click="saveDraft" :loading="saving" :disabled="!title || !content">
              {{ saving ? '保存中...' : '保存草稿' }}
            </el-button>
            <el-button type="primary" @click="publishArticle" :loading="publishing" :disabled="!title || !content">
              {{ publishing ? '发布中...' : '发布文章' }}
            </el-button>
          </div>
        </div>

        <!-- 编辑区域 -->
        <div class="edit-content">
          <!-- 左侧：编辑器 -->
          <div class="editor-section">
            <!-- 标题输入 -->
            <div class="title-input">
              <el-input
                v-model="title"
                placeholder="请输入文章标题..."
                :maxlength="100"
                show-word-limit
                size="large"
                @input="handleTitleInput"
              />
            </div>

            <!-- WangEditor 工具栏 -->
            <Toolbar
              v-if="!loading"
              :editor="editorRef"
              :defaultConfig="{}"
              style="border-bottom: 1px solid #ccc;"
            />

            <!-- WangEditor 编辑器 -->
            <Editor
              v-if="!loading"
              v-model="content"
              :defaultConfig="editorConfig"
              style="height: 500px; overflow-y: hidden;"
              @onCreated="handleCreated"
              @onChange="handleEditorChange"
            />

            <!-- 加载状态 -->
            <div v-if="loading" class="editor-loading">
              <el-icon class="loading-icon"><Loading /></el-icon>
              加载中...
            </div>
          </div>

          <!-- 右侧：设置面板 -->
          <div class="settings-section">
            <!-- 封面图片 -->
            <div class="cover-card">
              <h3>封面图片</h3>
              <div class="cover-upload">
                <div v-if="coverImage" class="cover-image-preview">
                  <img :src="coverImage" class="cover-image" />
                  <el-button type="text" @click="removeCover" class="remove-cover">移除</el-button>
                </div>
                <el-upload
                  v-else
                  class="cover-uploader"
                  :show-file-list="false"
                  :http-request="uploadCover"
                  :before-upload="beforeCoverUpload"
                >
                  <el-icon><Plus /></el-icon>
                  <div class="upload-text">点击上传封面</div>
                </el-upload>
                <p class="upload-tip">支持 JPG/PNG，最大 2MB</p>
              </div>
            </div>

            <!-- 分类选择 -->
            <div class="category-card">
              <h3>文章分类</h3>
              <el-select 
                v-model="categoryId" 
                placeholder="选择分类" 
                class="category-select" 
                size="large"
                filterable
                clearable
              >
                <el-option 
                  v-for="category in categories" 
                  :key="category.id" 
                  :label="category.name" 
                  :value="category.id" 
                />
              </el-select>
            </div>

            <!-- 标签管理 -->
            <div class="tags-card">
              <h3>文章标签</h3>
              <div class="tags-input">
                <el-select
                  v-model="selectedTags"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  placeholder="输入标签，按回车添加"
                  size="large"
                  class="tags-select"
                  @change="handleTagsChange"
                >
                  <el-option 
                    v-for="tag in allTags" 
                    :key="tag" 
                    :label="tag" 
                    :value="tag" 
                  />
                </el-select>
              </div>
              <div v-if="selectedTags.length > 0" class="selected-tags">
                <span class="tags-count">已选择 {{ selectedTags.length }} 个标签</span>
              </div>
            </div>

            <!-- 发布设置 -->
            <div class="publish-card">
              <h3>发布设置</h3>
              <div class="publish-options">
                <el-radio-group v-model="status">
                  <el-radio :label="0">草稿</el-radio>
                  <el-radio :label="1">发布</el-radio>
                </el-radio-group>
                
                <div class="option-item" v-if="status === 1">
                  <el-checkbox v-model="isPublic">公开文章</el-checkbox>
                  <el-tooltip content="不公开的文章只有作者自己可见" placement="top">
                    <el-icon class="option-tip"><InfoFilled /></el-icon>
                  </el-tooltip>
                </div>

                <div class="option-item">
                  <el-checkbox v-model="allowComment">允许评论</el-checkbox>
                  <el-tooltip content="关闭后读者无法评论此文章" placement="top">
                    <el-icon class="option-tip"><InfoFilled /></el-icon>
                  </el-tooltip>
                </div>
              </div>
            </div>
            
            <!-- 自动保存提示 -->
            <div v-if="hasUnsavedChanges" class="save-tip">
              <el-icon><Warning /></el-icon>
              内容尚未保存
            </div>
          </div>
        </div>
      </div>
    </div>

    <Footer />
    
    <!-- 离开确认对话框 -->
    <el-dialog
      v-model="showLeaveConfirm"
      title="离开确认"
      width="400px"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <p>文章内容尚未保存，确定要离开吗？</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelLeave">取消</el-button>
          <el-button type="primary" @click="confirmLeave">确定离开</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, onUnmounted } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { useCategoryStore } from '@/stores/category'
import { useTagStore } from '@/stores/tag'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, 
  Loading,
  InfoFilled,
  Warning
} from '@element-plus/icons-vue'

// 导入 Vue 3 版本的 WangEditor
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'

const route = useRoute()
const router = useRouter()

// Pinia Store
const articleStore = useArticleStore()
const categoryStore = useCategoryStore()
const tagStore = useTagStore()
const userStore = useUserStore()

// 文章ID（编辑模式才有）
const articleId = ref(parseInt(route.params.id) || 0)
const isEditMode = computed(() => !!articleId.value)

// 表单数据
const title = ref('')
const content = ref('')
const coverImage = ref('')
const categoryId = ref('')
const selectedTags = ref([])
const status = ref(0) // 0:草稿, 1:发布
const isPublic = ref(true)
const allowComment = ref(true)

// 状态管理
const saving = ref(false)
const publishing = ref(false)
const loading = ref(false)
const hasUnsavedChanges = ref(false)
const showLeaveConfirm = ref(false)
const nextRoute = ref(null)

// 分类和标签数据
const categories = computed(() => categoryStore.categories || [])
const allTags = computed(() => {
  const existingTags = tagStore.tags?.map(tag => tag.name) || []
  const currentTags = selectedTags.value || []
  
  // 合并现有标签和当前选中的标签（去重）
  return [...new Set([...existingTags, ...currentTags])]
})

// WangEditor 相关
const editorRef = ref(null)

// 编辑器配置
const editorConfig = ref({
  placeholder: '开始写作...',
  scroll: false, // 编辑器区域是否支持滚动
  MENU_CONF: {
    // 配置上传图片
    uploadImage: {
      server: '/api/files/editor/upload',
      fieldName: 'file',
      maxFileSize: 2 * 1024 * 1024, // 2M
      allowedFileTypes: ['image/*'],
      headers: {
        // 如果需要认证，这里可以添加
      },
      // 自定义上传回调
      customUpload: async (file, insertFn) => {
        try {
          const formData = new FormData()
          formData.append('file', file)
          
          // 使用统一的 request 上传
          const { default: request } = await import('@/utils/request')
          const response = await request.post('/api/files/editor/upload', formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          })
          
          // WangEditor 编辑器返回格式
          if (response.errno === 0 && response.data?.url) {
            insertFn(response.data.url)
            ElMessage.success('图片上传成功')
          } else {
            throw new Error(response.message || '上传失败')
          }
        } catch (error) {
          console.error('图片上传失败:', error)
          ElMessage.error('图片上传失败')
          throw error
        }
      }
    },
    // 禁用视频上传
    uploadVideo: {
      disabled: true
    }
  }
})

// 组件挂载
onMounted(async () => {
  // 检查登录状态
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/')
    return
  }
  
  // 加载分类和标签数据
  try {
    loading.value = true
    await Promise.all([
      categoryStore.fetchCategories(),
      tagStore.fetchTags()
    ])
  } catch (error) {
    console.error('加载分类或标签失败:', error)
    ElMessage.error('加载分类或标签失败')
  } finally {
    loading.value = false
  }
  
  // 如果是编辑模式，加载文章数据
  if (isEditMode.value) {
    await loadArticleData()
  }
  
  // 添加页面离开提示
  window.addEventListener('beforeunload', beforeUnloadHandler)
})

// 加载文章数据
const loadArticleData = async () => {
  try {
    loading.value = true
    
    const article = await articleStore.fetchArticleDetail(articleId.value)
    
    if (article) {
      // 填充表单数据
      title.value = article.title || ''
      content.value = article.content || ''
      coverImage.value = article.coverImage || article.cover || ''
      categoryId.value = article.categoryId || article.category?.id || ''
      status.value = article.status || 0
      isPublic.value = article.isPublic !== false
      allowComment.value = article.allowComment !== false
      
      // 处理标签
      if (article.tags && Array.isArray(article.tags)) {
        selectedTags.value = article.tags.map(tag => {
          return typeof tag === 'string' ? tag : tag.name
        }).filter(tag => tag) // 过滤掉空值
      }
      
      // 重置未保存状态
      hasUnsavedChanges.value = false
    } else {
      ElMessage.error('文章不存在或无权访问')
      router.push('/user/articles')
    }
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('加载文章失败')
    router.push('/user/articles')
  } finally {
    loading.value = false
  }
}

// 编辑器创建时的回调
const handleCreated = (editor) => {
  editorRef.value = editor
  
  // 如果是从编辑模式加载的文章，设置编辑器内容
  if (content.value && isEditMode.value) {
    editor.setHtml(content.value)
  }
}

// 编辑器内容变化
const handleEditorChange = () => {
  if (editorRef.value) {
    content.value = editorRef.value.getHtml()
    if (!hasUnsavedChanges.value && content.value) {
      hasUnsavedChanges.value = true
    }
  }
}

// 标题输入变化
const handleTitleInput = () => {
  if (!hasUnsavedChanges.value && title.value.trim()) {
    hasUnsavedChanges.value = true
  }
}

// 标签变化
const handleTagsChange = () => {
  if (!hasUnsavedChanges.value && selectedTags.value.length > 0) {
    hasUnsavedChanges.value = true
  }
}

// 组件销毁
onBeforeUnmount(() => {
  if (editorRef.value) {
    editorRef.value.destroy()
    editorRef.value = null
  }
})

onUnmounted(() => {
  window.removeEventListener('beforeunload', beforeUnloadHandler)
})

// 封面图片上传前的验证
const beforeCoverUpload = (file) => {
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

// 上传封面图片
const uploadCover = async (options) => {
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    formData.append('usageType', 'article-cover')
    
    const { default: request } = await import('@/utils/request')
    const response = await request.post('/api/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response && response.url) {
      coverImage.value = response.url
      hasUnsavedChanges.value = true
      ElMessage.success('封面图片上传成功')
      options.onSuccess({ code: 200, data: response })
    } else {
      ElMessage.error('上传失败：未获取到图片地址')
      options.onError(new Error('上传失败'))
    }
  } catch (error) {
    console.error('上传封面失败:', error)
    ElMessage.error('封面图片上传失败')
    options.onError(error)
  }
}

// 移除封面图片
const removeCover = () => {
  coverImage.value = ''
  hasUnsavedChanges.value = true
}

// 保存草稿
const saveDraft = async () => {
  if (!validateForm()) return
  
  try {
    saving.value = true
    
    const articleData = {
      title: title.value.trim(),
      content: getEditorContent(),
      coverImage: coverImage.value,
      categoryId: categoryId.value ? parseInt(categoryId.value) : null,
      tags: selectedTags.value,
      status: 0, // 草稿状态
      isPublic: isPublic.value,
      allowComment: allowComment.value
    }
    
    let result
    
    if (isEditMode.value) {
      // 更新草稿
      result = await articleStore.updateArticle(articleId.value, articleData)
      ElMessage.success('草稿保存成功')
      
      // 更新文章ID
      if (result && result.id) {
        articleId.value = result.id
      }
    } else {
      // 创建草稿
      result = await articleStore.createArticle(articleData)
      ElMessage.success('草稿保存成功')
      
      // 保存成功后，更新文章ID以便后续编辑
      if (result && result.id) {
        articleId.value = result.id
        // 更新URL但不刷新页面
        router.replace(`/article/edit/${result.id}`)
      }
    }
    
    // 重置未保存状态
    hasUnsavedChanges.value = false
    
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error(error.message || '保存草稿失败')
  } finally {
    saving.value = false
  }
}

// 发布文章
const publishArticle = async () => {
  if (!validateForm()) return
  
  try {
    publishing.value = true
    
    const articleData = {
      title: title.value.trim(),
      content: getEditorContent(),
      coverImage: coverImage.value,
      categoryId: categoryId.value ? parseInt(categoryId.value) : null,
      tags: selectedTags.value,
      status: 1, // 发布状态
      isPublic: isPublic.value,
      allowComment: allowComment.value
    }
    
    let result
    
    if (isEditMode.value) {
      result = await articleStore.updateArticle(articleId.value, articleData)
      ElMessage.success('文章发布成功')
    } else {
      result = await articleStore.createArticle(articleData)
      ElMessage.success('文章发布成功')
    }
    
    // 发布成功后跳转到文章详情页
    if (result && result.id) {
      hasUnsavedChanges.value = false
      setTimeout(() => {
        router.push(`/article/${result.id}`)
      }, 500)
    } else if (articleId.value) {
      router.push(`/article/${articleId.value}`)
    }
    
  } catch (error) {
    console.error('发布文章失败:', error)
    ElMessage.error(error.message || '发布文章失败')
  } finally {
    publishing.value = false
  }
}

// 验证表单
const validateForm = () => {
  if (!title.value.trim()) {
    ElMessage.warning('请输入文章标题')
    return false
  }
  
  const editorContent = getEditorContent()
  if (!editorContent.trim() || editorContent === '<p><br></p>') {
    ElMessage.warning('请输入文章内容')
    return false
  }
  
  return true
}

// 获取编辑器内容
const getEditorContent = () => {
  if (editorRef.value) {
    return editorRef.value.getHtml()
  }
  return content.value
}

// 页面离开提示处理
const beforeUnloadHandler = (e) => {
  if (hasUnsavedChanges.value) {
    e.preventDefault()
    e.returnValue = '文章内容尚未保存，确定要离开吗？'
  }
}

// 路由离开守卫
onBeforeRouteLeave((to, from, next) => {
  if (!hasUnsavedChanges.value) {
    next()
    return
  }
  
  // 如果正在保存或发布，阻止离开
  if (saving.value || publishing.value) {
    ElMessage.warning('正在保存，请稍后...')
    next(false)
    return
  }
  
  // 显示确认对话框
  showLeaveConfirm.value = true
  nextRoute.value = next
})

// 取消离开
const cancelLeave = () => {
  showLeaveConfirm.value = false
  nextRoute.value = null
}

// 确认离开
const confirmLeave = () => {
  showLeaveConfirm.value = false
  if (nextRoute.value) {
    hasUnsavedChanges.value = false // 重置状态
    nextRoute.value()
    nextRoute.value = null
  }
}
</script>

<style scoped>
.article-edit-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.edit-container {
  flex: 1;
  padding: 20px 0 40px;
}

.container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.header-left h1 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.edit-content {
  display: flex;
  gap: 30px;
}

.editor-section {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.settings-section {
  width: 320px;
  flex-shrink: 0;
}

.title-input {
  margin-bottom: 20px;
}

.editor-loading {
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #666;
  flex-direction: column;
  gap: 10px;
}

/* 右侧卡片 */
.cover-card,
.category-card,
.tags-card,
.publish-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.cover-card h3,
.category-card h3,
.tags-card h3,
.publish-card h3 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #333;
}

/* 封面图片上传样式 */
.cover-uploader {
  width: 100%;
  height: 150px;
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  transition: border-color 0.3s;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-image-preview {
  position: relative;
  width: 100%;
}

.cover-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 6px;
}

.remove-cover {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
}

.remove-cover:hover {
  background: rgba(0, 0, 0, 0.7);
}

.upload-text {
  margin-top: 10px;
  font-size: 14px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
  text-align: center;
}

.category-select, .tags-select {
  width: 100%;
}

.publish-options {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.loading-icon {
  animation: rotate 1s linear infinite;
}

.selected-tags {
  margin-top: 10px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 4px;
}

.tags-count {
  font-size: 12px;
  color: #666;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.option-tip {
  color: #999;
  cursor: help;
}

.save-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 15px;
  background: #fff6ec;
  border: 1px solid #ffd8b2;
  border-radius: 4px;
  color: #e6a23c;
  font-size: 14px;
  margin-top: 20px;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 992px) {
  .edit-content {
    flex-direction: column;
  }
  
  .settings-section {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .edit-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-right {
    align-self: stretch;
    display: flex;
    gap: 10px;
  }
  
  .header-right .el-button {
    flex: 1;
  }
}

/* 调整 WangEditor 样式 */
:deep(.w-e-bar) {
  background-color: #fff !important;
  border-bottom: 1px solid #e8e8e8 !important;
}

:deep(.w-e-text-container) {
  background-color: #fff !important;
}

:deep(.w-e-text) {
  padding: 20px !important;
  min-height: 400px !important;
}
/* 全屏模式下的调整 */
:deep(.w-e-full-screen-editor) {
  z-index: 1000 !important;
}

:deep(.w-e-full-screen-container) {
  z-index: 1000 !important;
}
</style>