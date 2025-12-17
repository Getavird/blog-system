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
              />
            </div>

            <!-- 编辑器切换标签 -->
            <div class="editor-tabs">
              <el-tabs v-model="activeTab">
                <el-tab-pane label="编辑" name="edit" />
                <el-tab-pane label="预览" name="preview" />
              </el-tabs>
            </div>

            <!-- 编辑器 -->
            <div v-show="activeTab === 'edit'" class="editor-wrapper">
              <el-input
                v-model="content"
                type="textarea"
                :rows="20"
                placeholder="开始写作...支持 Markdown 语法：
# 标题
## 二级标题
**粗体** *斜体*
- 列表项
1. 有序列表
> 引用
`代码`"
                resize="none"
                class="editor-textarea"
              />
            </div>

            <!-- 预览 -->
            <div v-show="activeTab === 'preview'" class="preview-wrapper">
              <div class="preview-content" v-html="previewContent"></div>
              <div v-if="!content" class="preview-empty">
                <p>还没有内容，请切换到编辑模式开始写作</p>
              </div>
            </div>

            <!-- 摘要 -->
            <div class="summary-section">
              <h3>文章摘要</h3>
              <el-input
                v-model="summary"
                type="textarea"
                :rows="3"
                placeholder="请输入文章摘要，留空将自动生成..."
                :maxlength="300"
                show-word-limit
              />
            </div>
          </div>

          <!-- 右侧：设置面板 -->
          <div class="settings-section">
            <!-- 封面图片（简化版） -->
            <div class="cover-card">
              <h3>封面图片</h3>
              <div class="cover-upload">
                <div v-if="coverImage" class="cover-image-preview">
                  <img :src="coverImage" class="cover-image" />
                  <el-button type="text" @click="coverImage = ''" class="remove-cover">移除</el-button>
                </div>
                <el-upload
                  v-else
                  class="cover-uploader"
                  action="/api/upload"
                  :show-file-list="false"
                  :on-success="handleCoverSuccess"
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
              <el-select v-model="categoryId" placeholder="选择分类" class="category-select" size="large">
                <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
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
                >
                  <el-option v-for="tag in allTags" :key="tag" :label="tag" :value="tag" />
                </el-select>
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
                
                <div class="visibility-option" v-if="status === 1">
                  <el-checkbox v-model="isPublic">公开文章</el-checkbox>
                </div>

                <div class="comment-option">
                  <el-checkbox v-model="allowComment">允许评论</el-checkbox>
                </div>
              </div>
            </div>

            <!-- 文章信息 -->
            <div class="info-card">
              <h3>文章信息</h3>
              <div class="info-item">
                <span class="info-label">字数：</span>
                <span class="info-value">{{ wordCount }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">预计阅读：</span>
                <span class="info-value">{{ readingTime }} 分钟</span>
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
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { marked } from 'marked'
import Header from '../components/layout/Header.vue'
import Footer from '../components/layout/Footer.vue'

const route = useRoute()
const router = useRouter()

// 文章ID
const articleId = ref(route.params.id || null)

// 文章数据
const title = ref('')
const content = ref('')
const summary = ref('')
const coverImage = ref('')
const categoryId = ref('')
const selectedTags = ref([])
const status = ref(0)
const isPublic = ref(true)
const allowComment = ref(true)

// 状态
const activeTab = ref('edit')
const saving = ref(false)
const publishing = ref(false)

// 模拟数据
const categories = ref([
  { id: 1, name: '技术' },
  { id: 2, name: '生活' },
  { id: 3, name: '学习' }
])

const allTags = ref(['Vue', 'React', 'JavaScript', 'CSS', 'HTML', 'Node.js', 'Spring Boot', 'Java', 'Python', '数据库'])

// 计算属性
const wordCount = computed(() => {
  return content.value ? content.value.length : 0
})

const readingTime = computed(() => {
  const words = wordCount.value
  return Math.ceil(words / 300) || 1
})

const previewContent = computed(() => {
  if (!content.value) return ''
  return marked(content.value)
})

// 如果是编辑模式，加载文章数据
if (articleId.value) {
  // 模拟加载
  setTimeout(() => {
    const mockArticle = {
      title: '我的第一篇文章',
      content: `# 欢迎使用博客系统

这是一个简单的 Markdown 编辑器示例。

## 功能特点
- 支持 Markdown 语法
- 实时预览
- 草稿保存
- 文章发布

## 代码示例
\`\`\`javascript
console.log('Hello, World!')
\`\`\`

> 开始写作吧！`,
      summary: '这是我的第一篇文章，欢迎阅读！',
      categoryId: 1,
      tags: ['Vue', 'JavaScript'],
      coverImage: ''
    }
    
    title.value = mockArticle.title
    content.value = mockArticle.content
    summary.value = mockArticle.summary
    categoryId.value = mockArticle.categoryId
    selectedTags.value = mockArticle.tags
    coverImage.value = mockArticle.coverImage
  }, 500)
}

// 方法
const saveDraft = async () => {
  saving.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const articleData = getArticleData()
    console.log('保存草稿:', articleData)
    
    ElMessage.success('草稿保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const publishArticle = async () => {
  publishing.value = true
  try {
    if (!title.value.trim()) {
      ElMessage.warning('请输入文章标题')
      return
    }
    
    if (!content.value || content.value.trim().length < 10) {
      ElMessage.warning('文章内容太短')
      return
    }
    
    // 确认发布
    if (status.value === 1) {
      const confirm = await ElMessageBox.confirm(
        '确定要发布文章吗？发布后其他人将可以看到这篇文章。',
        '发布确认',
        { confirmButtonText: '确定发布', cancelButtonText: '取消', type: 'warning' }
      ).catch(() => false)
      
      if (!confirm) {
        publishing.value = false
        return
      }
    }
    
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    const articleData = getArticleData()
    console.log('发布文章:', articleData)
    
    ElMessage.success(status.value === 1 ? '文章发布成功' : '文章保存成功')
    
    if (status.value === 1) {
      router.push('/article/1')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    publishing.value = false
  }
}

const getArticleData = () => {
  return {
    id: articleId.value,
    title: title.value,
    content: content.value,
    summary: summary.value || content.value.substring(0, 100) + '...',
    coverImage: coverImage.value,
    categoryId: categoryId.value,
    tags: selectedTags.value,
    status: status.value,
    isPublic: isPublic.value,
    allowComment: allowComment.value,
    wordCount: wordCount.value
  }
}

const beforeCoverUpload = (file) => {
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

const handleCoverSuccess = (response) => {
  // 模拟上传成功
  coverImage.value = 'https://via.placeholder.com/1200x600/409eff/ffffff?text=文章封面'
  ElMessage.success('封面图片上传成功')
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
}

.settings-section {
  width: 320px;
  flex-shrink: 0;
}

.title-input {
  margin-bottom: 20px;
}

.editor-tabs {
  margin-bottom: 20px;
}

.editor-wrapper, .preview-wrapper {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.editor-textarea {
  border: none;
}

.editor-textarea :deep(.el-textarea__inner) {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 14px;
  line-height: 1.6;
  border: none;
  padding: 20px;
}

.preview-content {
  padding: 20px;
  min-height: 400px;
  line-height: 1.8;
}

.preview-content h1 {
  font-size: 28px;
  margin: 20px 0 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #eee;
}

.preview-content h2 {
  font-size: 24px;
  margin: 18px 0 12px;
}

.preview-content h3 {
  font-size: 20px;
  margin: 16px 0 10px;
}

.preview-content p {
  margin: 10px 0;
}

.preview-content ul, .preview-content ol {
  padding-left: 2em;
  margin: 10px 0;
}

.preview-content li {
  margin: 5px 0;
}

.preview-content blockquote {
  border-left: 4px solid #409eff;
  padding-left: 16px;
  margin: 15px 0;
  color: #666;
  background: #f8f9fa;
}

.preview-content pre {
  background: #2d2d2d;
  color: #f8f8f2;
  padding: 15px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 15px 0;
}

.preview-content code {
  background: #f6f8fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 0.9em;
}

.preview-empty {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

.summary-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.summary-section h3 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #333;
}

/* 右侧卡片 */
.cover-card,
.category-card,
.tags-card,
.publish-card,
.info-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.cover-card h3,
.category-card h3,
.tags-card h3,
.publish-card h3,
.info-card h3 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #333;
}

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
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-image-preview {
  position: relative;
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

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #666;
  font-size: 14px;
}

.info-value {
  color: #333;
  font-weight: 500;
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
</style>