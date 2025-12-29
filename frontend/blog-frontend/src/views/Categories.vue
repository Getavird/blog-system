<template>
  <div class="categories-page">
    <Header />
    
    <div class="categories-container">
      <div class="container">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1>文章分类</h1>
          <p class="page-subtitle">按主题浏览所有文章分类</p>
        </div>

        <!-- 分类列表 -->
        <div class="categories-list">
          <div v-if="categoryStore.loading" class="loading-state">
            <el-skeleton :rows="6" animated />
          </div>
          
          <div v-else-if="categories.length > 0" class="categories-grid">
            <div 
              v-for="category in categories" 
              :key="category.id"
              class="category-card"
              @click="viewCategory(category.id)"
            >
              <div class="category-icon" :style="{ backgroundColor: getCategoryColor(category.id) }">
                <el-icon :size="32" color="white">
                  <component :is="getCategoryIcon(category.id)" />
                </el-icon>
              </div>
              <div class="category-content">
                <h3 class="category-title">{{ category.name }}</h3>
                <p class="category-description">{{ category.description || '暂无描述' }}</p>
              </div>
              <div class="category-arrow">
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
          
          <div v-else class="empty-state">
            <div class="empty-content">
              <el-icon :size="60" color="#c0c4cc">
                <Folder />
              </el-icon>
              <h3>暂无分类</h3>
              <p v-if="isAdmin">作为管理员，你可以创建分类来组织文章</p>
              <p v-else>管理员还没有创建任何分类</p>
              <el-button v-if="isAdmin" type="primary" class="create-button" @click="createCategory">
                创建分类
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Footer />
    
    <!-- 创建分类对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建分类" width="500px">
      <el-form :model="newCategory" :rules="categoryRules" ref="categoryFormRef">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="newCategory.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类描述" prop="description">
          <el-input v-model="newCategory.description" type="textarea" rows="3" placeholder="请输入分类描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" :loading="creating" @click="handleCreateCategory">创建</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCategoryStore } from '@/stores/category'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { 
  ArrowRight, Folder,
  Grid, Collection, Flag, Star, 
  Setting, Share, Help, ChatDotRound 
} from '@element-plus/icons-vue'

// 组件导入
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'

const router = useRouter()

// Pinia Stores
const categoryStore = useCategoryStore()
const userStore = useUserStore()

// 状态
const showCreateDialog = ref(false)
const creating = ref(false)
const categoryFormRef = ref(null)

// 新分类数据
const newCategory = ref({
  name: '',
  description: ''
})

// 验证规则
const categoryRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在2到20个字符', trigger: 'blur' }
  ]
}

// 计算属性
const categories = computed(() => categoryStore.categories || [])
const isAdmin = computed(() => userStore.user?.role === 1)

// 获取分类图标（基于分类ID选择）
const getCategoryIcon = (id) => {
  const icons = [
    Collection, Flag, Star, Grid,
    Setting, Share, Help, ChatDotRound,
    Folder
  ]
  return icons[id % icons.length]
}

// 获取分类颜色
const getCategoryColor = (id) => {
  const colors = [
    '#409eff', '#67c23a', '#e6a23c', '#f56c6c',
    '#909399', '#ff69b4', '#9b30ff', '#00bfff',
    '#32cd32', '#ff4500'
  ]
  return colors[id % colors.length]
}

// 组件挂载
onMounted(async () => {
  await loadCategories()
})

// 加载分类数据
const loadCategories = async () => {
  try {
    await categoryStore.fetchCategories()
  } catch (error) {
    console.error('加载分类列表失败:', error)
    ElMessage.error('加载分类列表失败')
  }
}

// 查看分类详情
const viewCategory = (categoryId) => {
  router.push(`/category/${categoryId}`)
}

// 创建分类
const createCategory = () => {
  if (!isAdmin.value) {
    ElMessage.warning('需要管理员权限')
    return
  }
  showCreateDialog.value = true
}

// 处理创建分类
const handleCreateCategory = async () => {
  if (!categoryFormRef.value) return
  
  try {
    // 表单验证
    await categoryFormRef.value.validate()
    
    creating.value = true
    await categoryStore.createCategory(newCategory.value)
    
    ElMessage.success('创建分类成功')
    showCreateDialog.value = false
    resetCategoryForm()
    
    // 刷新列表
    await loadCategories()
  } catch (error) {
    console.error('创建分类失败:', error)
    ElMessage.error(error.message || '创建分类失败')
  } finally {
    creating.value = false
  }
}

// 重置表单
const resetCategoryForm = () => {
  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
  newCategory.value = {
    name: '',
    description: ''
  }
}
</script>

<style scoped>
.categories-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.categories-container {
  flex: 1;
  padding: 20px 0 40px;
  background: #f8f9fa;
}

.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页面标题 */
.page-header {
  margin-bottom: 40px;
  text-align: center;
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

/* 分类列表 */
.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.category-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  overflow: hidden;
}

.category-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.category-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  background: var(--category-color, #409eff);
}

.category-icon {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.category-content {
  flex: 1;
  min-width: 0;
}

.category-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.category-description {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.category-arrow {
  flex-shrink: 0;
  color: #c0c4cc;
  transition: all 0.3s;
}

.category-card:hover .category-arrow {
  color: #409eff;
  transform: translateX(4px);
}

/* 加载状态 */
.loading-state {
  background: white;
  border-radius: 12px;
  padding: 40px;
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
}

.empty-content .create-button {
  margin-top: 15px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .categories-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 768px) {
  .page-header h1 {
    font-size: 28px;
  }
  
  .category-card {
    padding: 20px;
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }
  
  .category-arrow {
    position: absolute;
    bottom: 15px;
    right: 15px;
  }
  
  .category-card::before {
    width: 100%;
    height: 4px;
    top: auto;
    bottom: 0;
  }

  .categories-grid {
    grid-template-columns: 1fr;
  }
}
</style>