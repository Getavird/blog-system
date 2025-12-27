<template>
  <div class="tag-cloud" ref="tagCloudRef">
    <!-- 标签云显示区域 -->
    <div 
      v-for="tag in displayedTags" 
      :key="tag.id"
      :class="['tag-item', `tag-level-${tag.level}`]"
      :style="{
        left: `${tag.position.x}%`,
        top: `${tag.position.y}%`,
        fontSize: `${tag.fontSize}px`,
        opacity: tag.opacity,
        transform: `scale(${tag.scale})`
      }"
      @click="handleTagClick(tag.id, tag.name)"
    >
      <span class="tag-text">{{ tag.name }}</span>
      <span class="tag-count">({{ tag.count }})</span>
    </div>
    
    <!-- 查看更多按钮 -->
    <div v-if="showMoreButton && tags.length > maxDisplayCount" class="more-tags">
      <el-button type="text" @click="showAllTags = !showAllTags">
        {{ showAllTags ? '收起' : `查看更多(${tags.length - maxDisplayCount})` }}
        <el-icon :class="{ 'rotate': showAllTags }">
          <ArrowDown />
        </el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useTagStore } from '@/stores/tag'
import { ArrowDown } from '@element-plus/icons-vue'

const props = defineProps({
  // 标签云配置
  minFontSize: {
    type: Number,
    default: 12
  },
  maxFontSize: {
    type: Number,
    default: 32
  },
  // 显示模式：'cloud'（云状）或 'list'（列表）
  mode: {
    type: String,
    default: 'cloud'
  },
  // 最大显示数量
  maxDisplayCount: {
    type: Number,
    default: 20
  },
  // 是否显示查看更多
  showMoreButton: {
    type: Boolean,
    default: true
  },
  // 颜色方案
  colorScheme: {
    type: Array,
    default: () => ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
  }
})

const emit = defineEmits(['tag-click'])

const router = useRouter()
const tagStore = useTagStore()
const tagCloudRef = ref(null)
const showAllTags = ref(false)
const tags = ref([])

// 组件挂载时加载标签数据
onMounted(async () => {
  try {
    await tagStore.fetchTags()
    tags.value = tagStore.tags || []
  } catch (error) {
    console.error('加载标签失败:', error)
  }
})

// 计算显示的标签
const displayedTags = computed(() => {
  if (!tags.value || tags.value.length === 0) return []
  
  const tagsToShow = showAllTags.value ? tags.value : tags.value.slice(0, props.maxDisplayCount)
  
  if (props.mode === 'cloud') {
    return tagsToShow.map(tag => ({
      ...tag,
      level: getTagLevel(tag.articleCount || 0),
      fontSize: calculateFontSize(tag.articleCount || 0),
      opacity: calculateOpacity(tag.articleCount || 0),
      scale: 1,
      position: tags.value.length > 1 ? calculatePosition() : { x: 50, y: 50 }
    }))
  } else {
    // 列表模式
    return tagsToShow.map(tag => ({
      ...tag,
      level: getTagLevel(tag.articleCount || 0)
    }))
  }
})

// 计算标签级别（1-5级）
const getTagLevel = (count) => {
  const counts = tags.value.map(t => t.articleCount || 0)
  const maxCount = Math.max(...counts, 1)
  const minCount = Math.min(...counts, 0)
  
  if (maxCount === minCount) return 3
  
  const normalized = (count - minCount) / (maxCount - minCount)
  if (normalized < 0.2) return 1
  if (normalized < 0.4) return 2
  if (normalized < 0.6) return 3
  if (normalized < 0.8) return 4
  return 5
}

// 计算字体大小
const calculateFontSize = (count) => {
  const counts = tags.value.map(t => t.articleCount || 0)
  const maxCount = Math.max(...counts, 1)
  const minCount = Math.min(...counts, 0)
  
  if (maxCount === minCount) return (props.minFontSize + props.maxFontSize) / 2
  
  const normalized = (count - minCount) / (maxCount - minCount)
  return props.minFontSize + (props.maxFontSize - props.minFontSize) * normalized
}

// 计算不透明度
const calculateOpacity = (count) => {
  const counts = tags.value.map(t => t.articleCount || 0)
  const maxCount = Math.max(...counts, 1)
  const minCount = Math.min(...counts, 0)
  
  if (maxCount === minCount) return 0.8
  
  const normalized = (count - minCount) / (maxCount - minCount)
  return 0.4 + normalized * 0.6 // 0.4-1.0
}

// 计算位置（简单的随机分布）
const calculatePosition = () => {
  // 为避免标签重叠，可以改进为更智能的布局算法
  return {
    x: Math.random() * 80 + 10, // 10-90%
    y: Math.random() * 80 + 10  // 10-90%
  }
}

// 标签点击事件
const handleTagClick = (tagId, tagName) => {
  emit('tag-click', tagId, tagName)
  // 跳转到标签页面
  router.push(`/tag/${encodeURIComponent(tagName)}`)
}
</script>

<style scoped>
.tag-cloud {
  position: relative;
  min-height: 300px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.tag-cloud:hover {
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.12);
}

/* 标签云模式 */
.tag-item {
  position: absolute;
  padding: 8px 16px;
  background: white;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
  user-select: none;
  z-index: 1;
}

.tag-item:hover {
  transform: translateY(-4px) scale(1.1);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

/* 标签级别颜色 */
.tag-level-1 {
  color: #909399;
  border: 2px solid #dcdfe6;
  background: #f5f7fa;
}
.tag-level-1:hover {
  background: #e4e7ed;
  border-color: #c0c4cc;
}

.tag-level-2 {
  color: #67c23a;
  border: 2px solid #c2e7b0;
  background: #f0f9eb;
}
.tag-level-2:hover {
  background: #e1f3d8;
  border-color: #a4da89;
}

.tag-level-3 {
  color: #409eff;
  border: 2px solid #b3d8ff;
  background: #ecf5ff;
}
.tag-level-3:hover {
  background: #d9ecff;
  border-color: #8cc5ff;
}

.tag-level-4 {
  color: #e6a23c;
  border: 2px solid #f5dab1;
  background: #fdf6ec;
}
.tag-level-4:hover {
  background: #faecd8;
  border-color: #f3d19e;
}

.tag-level-5 {
  color: #f56c6c;
  border: 2px solid #f9c7c7;
  background: #fef0f0;
}
.tag-level-5:hover {
  background: #fde2e2;
  border-color: #f8b4b4;
}

.tag-text {
  font-weight: 500;
}

.tag-count {
  font-size: 0.85em;
  opacity: 0.7;
}

/* 查看更多按钮 */
.more-tags {
  position: absolute;
  bottom: 10px;
  right: 20px;
  z-index: 5;
}

.more-tags .el-button {
  color: #666;
  font-size: 14px;
}

.more-tags .el-button:hover {
  color: #409eff;
}

.rotate {
  transform: rotate(180deg);
  transition: transform 0.3s;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tag-cloud {
    min-height: 200px;
    padding: 15px;
  }
  
  .tag-item {
    padding: 6px 12px;
    font-size: 12px !important;
  }
  
  .tag-count {
    font-size: 0.8em;
  }
}

/* 列表模式样式 */
.tag-cloud.list-mode {
  min-height: auto;
  background: white;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 20px;
}

.list-mode .tag-item {
  position: static;
  opacity: 1 !important;
  transform: none !important;
  font-size: 14px !important;
  box-shadow: none;
  border-width: 1px;
}

.list-mode .tag-item:hover {
  transform: translateY(-2px) !important;
}
</style>