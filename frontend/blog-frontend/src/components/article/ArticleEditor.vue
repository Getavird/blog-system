<template>
  <div class="article-editor">
    <!-- 标题输入 -->
    <div class="editor-field title-field">
      <el-input
        v-model="title"
        placeholder="请输入文章标题..."
        :maxlength="100"
        show-word-limit
        size="large"
        class="title-input"
      />
    </div>
    
    <!-- 编辑器标签页 -->
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
        :rows="15"
        :placeholder="placeholder"
        resize="vertical"
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
    <div v-if="showSummary" class="editor-field summary-field">
      <h3>文章摘要</h3>
      <el-input
        v-model="summary"
        type="textarea"
        :rows="3"
        placeholder="请输入文章摘要，留空将自动生成..."
        :maxlength="300"
        show-word-limit
        class="summary-input"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, defineProps, defineEmits } from 'vue'
import { marked } from 'marked'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true,
    default: () => ({
      title: '',
      content: '',
      summary: ''
    })
  },
  placeholder: {
    type: String,
    default: `开始写作...支持 Markdown 语法：
# 标题
## 二级标题
**粗体** *斜体*
- 列表项
1. 有序列表
> 引用
\`代码\``
  },
  showSummary: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue'])

// 本地数据
const title = ref(props.modelValue.title || '')
const content = ref(props.modelValue.content || '')
const summary = ref(props.modelValue.summary || '')
const activeTab = ref('edit')

// 计算属性
const previewContent = computed(() => {
  if (!content.value) return ''
  return marked(content.value)
})

// 监听数据变化并更新父组件
watch([title, content, summary], () => {
  emit('update:modelValue', {
    title: title.value,
    content: content.value,
    summary: summary.value
  })
}, { deep: true })

// 监听父组件数据变化
watch(() => props.modelValue, (newValue) => {
  title.value = newValue.title || ''
  content.value = newValue.content || ''
  summary.value = newValue.summary || ''
}, { deep: true })
</script>

<style scoped>
.article-editor {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.editor-field {
  margin-bottom: 20px;
}

.title-field {
  margin-bottom: 10px;
}

.title-input :deep(.el-input__inner) {
  font-size: 24px;
  font-weight: 600;
  border: none;
  padding: 10px 0;
}

.editor-tabs {
  margin-bottom: 10px;
}

.editor-wrapper, .preview-wrapper {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
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
  min-height: 300px;
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
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

.summary-field h3 {
  margin-bottom: 10px;
  font-size: 16px;
  color: #333;
}

.summary-input :deep(.el-textarea__inner) {
  font-size: 14px;
  line-height: 1.6;
}
</style>