<template>
  <div class="tags-page">
    <Header />
    
    <div class="tags-container">
      <div class="container">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1>标签云</h1>
          <p class="page-subtitle">探索文章的不同主题和分类</p>
        </div>

        <!-- 标签统计 -->
        <div class="tags-stats">
          <div class="stat-card">
            <div class="stat-number">{{ tags.length }}</div>
            <div class="stat-label">标签总数</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ totalArticleCount }}</div>
            <div class="stat-label">文章数量</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ mostUsedTag.name }}</div>
            <div class="stat-label">最热标签</div>
          </div>
        </div>

        <!-- 标签搜索 -->
        <div class="tags-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索标签..."
            clearable
            @input="handleSearch"
            @clear="handleSearch"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-button 
            type="text" 
            @click="toggleViewMode"
            class="view-toggle"
          >
            {{ viewMode === 'cloud' ? '切换到列表视图' : '切换到云图视图' }}
          </el-button>
        </div>

        <!-- 标签云/列表 -->
        <div class="tags-content">
          <!-- 加载状态 -->
          <div v-if="tagStore.loading" class="loading-state">
            <el-skeleton :rows="5" animated />
          </div>

          <!-- 空状态 -->
          <div v-else-if="filteredTags.length === 0" class="empty-state">
            <div class="empty-content">
              <el-icon :size="60" color="#c0c4cc">
                <PriceTag />
              </el-icon>
              <h3>没有找到相关标签</h3>
              <p>尝试其他搜索关键词</p>
              <el-button type="text" @click="clearSearch">清除搜索</el-button>
            </div>
          </div>

          <!-- 标签云模式 -->
          <div v-else-if="viewMode === 'cloud'" class="tag-cloud-mode">
            <div class="tag-cloud-wrapper">
              <div 
                v-for="tag in filteredTags" 
                :key="tag.id || tag.name"
                :class="[
                  'tag-cloud-item',
                  `tag-level-${getTagLevel(tag)}`
                ]"
                :style="{
                  fontSize: `${getTagFontSize(tag)}px`,
                  opacity: 0.6 + (getTagCount(tag) / 200),
                  transform: `rotate(${Math.random() * 10 - 5}deg)`
                }"
                @click="viewTagArticles(tag)"
              >
                {{ tag.name }}
                <span class="tag-count">{{ getTagCount(tag) }}</span>
              </div>
            </div>
          </div>

          <!-- 列表模式 -->
          <div v-else class="tag-list-mode">
            <div class="tags-table">
              <div class="table-header">
                <div class="header-cell">标签名称</div>
                <div class="header-cell">文章数量</div>
                <div class="header-cell">最后更新</div>
                <div class="header-cell">操作</div>
              </div>
              
              <div class="table-body">
                <div 
                  v-for="tag in filteredTags" 
                  :key="tag.id || tag.name"
                  class="table-row"
                >
                  <div class="table-cell">
                    <el-tag :type="getTagType(tag)" size="medium" class="tag-cell">
                      {{ tag.name }}
                    </el-tag>
                  </div>
                  <div class="table-cell">
                    <span class="article-count">{{ getTagCount(tag) }} 篇</span>
                  </div>
                  <div class="table-cell">
                    {{ formatTime(tag.updatedAt || tag.createTime) }}
                  </div>
                  <div class="table-cell">
                    <el-button 
                      type="primary" 
                      size="small" 
                      @click="viewTagArticles(tag)"
                    >
                      查看文章
                    </el-button>
                    <el-button 
                      v-if="userStore.user?.role === 1"
                      type="danger" 
                      size="small" 
                      @click="deleteTag(tag)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 热门标签 -->
        <div v-if="topTags.length > 0" class="hot-tags">
          <h3>热门标签</h3>
          <div class="hot-tags-list">
            <el-tag
              v-for="tag in topTags"
              :key="tag.id || tag.name"
              :type="getTagType(tag)"
              size="large"
              class="hot-tag-item"
              @click="viewTagArticles(tag)"
            >
              {{ tag.name }} ({{ getTagCount(tag) }})
            </el-tag>
          </div>
        </div>

        <!-- 创建标签按钮（管理员） -->
        <div v-if="userStore.user?.role === 1" class="create-tag-section">
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            创建新标签
          </el-button>
        </div>
      </div>
    </div>

    <Footer />
    
    <!-- 创建标签对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建标签" width="500px">
      <el-form :model="newTag" :rules="tagRules" ref="tagFormRef">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="newTag.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="标签描述" prop="description">
          <el-input v-model="newTag.description" type="textarea" rows="3" placeholder="请输入标签描述" />
        </el-form-item>
        <el-form-item label="标签颜色" prop="color">
          <el-color-picker v-model="newTag.color" show-alpha :predefine="predefineColors" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" :loading="creating" @click="handleCreateTag">创建</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useTagStore } from '@/stores/tag'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, PriceTag, Plus } from '@element-plus/icons-vue'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'

const router = useRouter()

// Pinia Stores
const tagStore = useTagStore()
const userStore = useUserStore()

// 状态
const loading = ref(false)
const searchKeyword = ref('')
const viewMode = ref('cloud') // 'cloud' 或 'list'
const showCreateDialog = ref(false)
const creating = ref(false)
const tagFormRef = ref(null)

// 新标签数据
const newTag = ref({
  name: '',
  description: '',
  color: '#409eff'
})

// 预定义颜色
const predefineColors = ref([
  '#409eff', '#67c23a', '#e6a23c', '#f56c6c',
  '#909399', '#ff69b4', '#9b30ff', '#00bfff',
  '#32cd32', '#ff4500'
])

// 验证规则
const tagRules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在2到20个字符', trigger: 'blur' }
  ]
}

// 计算属性
const tags = computed(() => {
  return tagStore.tags.map(tag => ({
    ...tag,
    // 统一文章数量字段
    articleCount: tag.articleCount || tag.count || 0
  }))
})

// 过滤后的标签
const filteredTags = computed(() => {
  let result = tags.value
  
  // 按关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(tag => 
      tag.name.toLowerCase().includes(keyword) ||
      (tag.description && tag.description.toLowerCase().includes(keyword))
    )
  }
  
  return result
})

// 获取标签的文章数量
const getTagCount = (tag) => {
  return tag.articleCount || tag.count || 0
}

// 获取标签级别（用于云图）
const getTagLevel = (tag) => {
  const count = getTagCount(tag)
  if (count >= 100) return 5
  if (count >= 50) return 4
  if (count >= 20) return 3
  if (count >= 10) return 2
  return 1
}

// 获取标签字体大小
const getTagFontSize = (tag) => {
  const count = getTagCount(tag)
  const baseSize = 14
  const maxSize = 32
  const increment = 0.5
  
  // 对数计算，避免大小差异过大
  const size = baseSize + Math.log2(count + 1) * increment
  return Math.min(size, maxSize)
}

// 总文章数
const totalArticleCount = computed(() => {
  return tags.value.reduce((sum, tag) => sum + getTagCount(tag), 0)
})

// 最热标签（文章数最多的标签）
const mostUsedTag = computed(() => {
  if (tags.value.length === 0) return { name: '无' }
  
  const sortedTags = [...tags.value].sort((a, b) => 
    getTagCount(b) - getTagCount(a)
  )
  
  return sortedTags[0] || { name: '无' }
})

// 热门标签（前10个）
const topTags = computed(() => {
  return [...tags.value]
    .sort((a, b) => getTagCount(b) - getTagCount(a))
    .slice(0, 10)
})

// 生命周期
onMounted(async () => {
  await loadTags()
})

// 加载标签
const loadTags = async () => {
  try {
    await tagStore.fetchTags()
  } catch (error) {
    console.error('加载标签失败:', error)
    ElMessage.error('加载标签失败')
  }
}

// 搜索标签
const handleSearch = () => {
  // 搜索逻辑由filteredTags计算属性处理
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
}

// 切换视图模式
const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'cloud' ? 'list' : 'cloud'
}

// 查看标签文章
const viewTagArticles = (tag) => {
  router.push(`/tag/${encodeURIComponent(tag.name)}`)
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '暂无'
  
  try {
    const date = new Date(time)
    if (isNaN(date.getTime())) return '暂无'
    
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const diffDays = Math.floor(diff / (1000 * 60 * 60 * 24))
    
    if (diffDays === 0) return '今天'
    if (diffDays === 1) return '昨天'
    if (diffDays < 7) return `${diffDays}天前`
    if (diffDays < 30) return `${Math.floor(diffDays / 7)}周前`
    if (diffDays < 365) return `${Math.floor(diffDays / 30)}月前`
    
    return date.toLocaleDateString('zh-CN')
  } catch (error) {
    console.warn('时间格式化失败:', time, error)
    return '暂无'
  }
}

// 根据文章数量获取标签类型（用于样式）
const getTagType = (tag) => {
  const count = getTagCount(tag)
  if (count >= 100) return 'danger'
  if (count >= 50) return 'warning'
  if (count >= 20) return 'success'
  if (count >= 10) return 'primary'
  return 'info'
}

// 创建标签
const handleCreateTag = async () => {
  if (!tagFormRef.value) return
  
  try {
    // 表单验证
    await tagFormRef.value.validate()
    
    creating.value = true
    await tagStore.createTag(newTag.value)
    
    ElMessage.success('创建标签成功')
    showCreateDialog.value = false
    resetTagForm()
    
    // 刷新标签列表
    await loadTags()
  } catch (error) {
    console.error('创建标签失败:', error)
    ElMessage.error(error.message || '创建标签失败')
  } finally {
    creating.value = false
  }
}

// 删除标签
const deleteTag = async (tag) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除标签 "${tag.name}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await tagStore.deleteTag(tag.id || tag.name)
    
    ElMessage.success('删除标签成功')
    
    // 刷新标签列表
    await loadTags()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除标签失败:', error)
      ElMessage.error('删除标签失败')
    }
  }
}

// 重置表单
const resetTagForm = () => {
  if (tagFormRef.value) {
    tagFormRef.value.resetFields()
  }
  newTag.value = {
    name: '',
    description: '',
    color: '#409eff'
  }
}
</script>

<style scoped>
.tags-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.tags-container {
  flex: 1;
  padding: 20px 0 40px;
}

.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 32px;
  color: #333;
  margin-bottom: 12px;
}

.page-subtitle {
  color: #666;
  font-size: 16px;
}

/* 标签统计 */
.tags-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.tags-stats .stat-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  cursor: pointer;
}

.tags-stats .stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.tags-stats .stat-card:nth-child(1):hover .stat-number {
  color: #409eff;
}

.tags-stats .stat-card:nth-child(2):hover .stat-number {
  color: #67c23a;
}

.tags-stats .stat-card:nth-child(3):hover .stat-number {
  color: #e6a23c;
}

.tags-stats .stat-number {
  font-size: 36px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 8px;
  transition: color 0.3s;
}

.tags-stats .stat-card:nth-child(2) .stat-number {
  color: #67c23a;
}

.tags-stats .stat-card:nth-child(3) .stat-number {
  color: #e6a23c;
  font-size: 28px;
  word-break: break-all;
}

.tags-stats .stat-label {
  color: #666;
  font-size: 14px;
}

/* 标签搜索 */
.tags-search {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 30px;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.search-input {
  flex: 1;
}

.view-toggle {
  white-space: nowrap;
}

/* 标签内容 */
.tags-content {
  margin-bottom: 40px;
}

/* 加载状态 */
.loading-state {
  background: white;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
}

/* 空状态 */
.empty-state {
  background: white;
  border-radius: 12px;
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
  margin-bottom: 15px;
}

/* 标签云模式 */
.tag-cloud-mode {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  min-height: 500px;
}

.tag-cloud-wrapper {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 15px;
  text-align: center;
}

.tag-cloud-item {
  padding: 10px 20px;
  margin: 5px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 20px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #333;
  background: #f5f7fa;
  border: 2px solid transparent;
  user-select: none;
}

.tag-cloud-item:hover {
  transform: translateY(-5px) scale(1.1);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

/* 标签级别样式 */
.tag-level-1 {
  border-color: #dcdfe6;
  background: #f5f7fa;
  color: #909399;
}
.tag-level-1:hover {
  border-color: #c0c4cc;
  background: #e4e7ed;
}

.tag-level-2 {
  border-color: #b3e19d;
  background: #f0f9eb;
  color: #67c23a;
}
.tag-level-2:hover {
  border-color: #95d475;
  background: #e1f3d8;
}

.tag-level-3 {
  border-color: #a0cfff;
  background: #ecf5ff;
  color: #409eff;
}
.tag-level-3:hover {
  border-color: #79bbff;
  background: #d9ecff;
}

.tag-level-4 {
  border-color: #f3d19e;
  background: #fdf6ec;
  color: #e6a23c;
}
.tag-level-4:hover {
  border-color: #eebe77;
  background: #faecd8;
}

.tag-level-5 {
  border-color: #fab6b6;
  background: #fef0f0;
  color: #f56c6c;
}
.tag-level-5:hover {
  border-color: #f89898;
  background: #fde2e2;
}

.tag-count {
  font-size: 0.8em;
  opacity: 0.8;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.7);
  padding: 1px 6px;
  border-radius: 10px;
  margin-left: 4px;
}

/* 列表模式 */
.tag-list-mode {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.tags-table {
  width: 100%;
}

.table-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 2fr;
  background: #f8f9fa;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
}

.header-cell {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.table-body {
  padding: 0;
}

.table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 2fr;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  align-items: center;
  transition: all 0.3s;
}

.table-row:hover {
  background: #f8f9fa;
}

.table-cell {
  color: #333;
}

.tag-cell {
  cursor: pointer;
  transition: all 0.3s;
}

.tag-cell:hover {
  transform: scale(1.05);
}

.article-count {
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.table-cell .el-button {
  margin-right: 8px;
}

.table-cell .el-button:last-child {
  margin-right: 0;
}

/* 热门标签 */
.hot-tags {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.hot-tags h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.hot-tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.hot-tag-item {
  cursor: pointer;
  transition: all 0.3s;
  padding: 8px 16px;
}

.hot-tag-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 创建标签区域 */
.create-tag-section {
  text-align: center;
  margin-top: 30px;
}

.create-tag-section .el-button {
  padding: 12px 32px;
  font-size: 16px;
}

.create-tag-section .el-icon {
  margin-right: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header h1 {
    font-size: 28px;
  }
  
  .tags-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .tags-search {
    flex-direction: column;
    align-items: stretch;
  }
  
  .tag-cloud-mode {
    padding: 20px;
  }
  
  .tag-cloud-item {
    padding: 8px 16px;
  }
  
  .table-header {
    display: none;
  }
  
  .table-row {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 20px;
    border-bottom: 2px solid #eee;
  }
  
  .table-cell {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .table-cell::before {
    content: attr(data-label);
    font-weight: 600;
    color: #666;
    font-size: 14px;
  }
  
  .table-cell[data-label="标签名称"]::before {
    content: "标签名称";
  }
  
  .table-cell[data-label="文章数量"]::before {
    content: "文章数量";
  }
  
  .table-cell[data-label="最后更新"]::before {
    content: "最后更新";
  }
  
  .table-cell[data-label="操作"]::before {
    content: "操作";
  }
  
  .table-cell .el-button {
    margin-right: 5px;
    margin-bottom: 5px;
  }
}

@media (max-width: 480px) {
  .tags-stats {
    grid-template-columns: 1fr;
  }
  
  .tags-stats .stat-card {
    padding: 20px;
  }
  
  .tags-stats .stat-number {
    font-size: 28px;
  }
  
  .tags-stats .stat-card:nth-child(3) .stat-number {
    font-size: 24px;
  }
  
  .hot-tag-item {
    padding: 6px 12px;
    font-size: 12px;
  }
}
</style>