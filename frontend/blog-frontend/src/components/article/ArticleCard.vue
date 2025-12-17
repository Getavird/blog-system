<template>
  <div class="article-card" @click="handleClick">
    <div class="card-content">
      <!-- 封面图片 -->
      <div v-if="showCover && article.coverImage" class="card-cover">
        <img :src="article.coverImage" :alt="article.title" />
      </div>
      
      <!-- 文章信息 -->
      <div class="card-body">
        <!-- 标题 -->
        <h3 class="card-title">
          {{ article.title }}
          <el-tag v-if="showStatus && article.status === 0" type="info" size="small">
            草稿
          </el-tag>
        </h3>
        
        <!-- 摘要 -->
        <p v-if="showSummary" class="card-summary">
          {{ article.summary || '暂无摘要' }}
        </p>
        
        <!-- 元信息 -->
        <div class="card-meta">
          <!-- 作者 -->
          <div v-if="showAuthor" class="meta-item author">
            <el-icon><User /></el-icon>
            <span>{{ article.authorName || '匿名' }}</span>
          </div>
          
          <!-- 分类 -->
          <div v-if="showCategory && article.categoryName" class="meta-item category">
            <el-icon><Folder /></el-icon>
            <span>{{ article.categoryName }}</span>
          </div>
          
          <!-- 时间 -->
          <div v-if="showTime" class="meta-item time">
            <el-icon><Clock /></el-icon>
            <span>{{ formatTime(article.createTime) }}</span>
          </div>
          
          <!-- 阅读量 -->
          <div v-if="showViews" class="meta-item views">
            <el-icon><View /></el-icon>
            <span>{{ article.viewCount || 0 }}</span>
          </div>
          
          <!-- 点赞数 -->
          <div v-if="showLikes" class="meta-item likes">
            <el-icon><Star /></el-icon>
            <span>{{ article.likeCount || 0 }}</span>
          </div>
          
          <!-- 评论数 -->
          <div v-if="showComments" class="meta-item comments">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ article.commentCount || 0 }}</span>
          </div>
        </div>
        
        <!-- 标签 -->
        <div v-if="showTags && article.tags && article.tags.length > 0" class="card-tags">
          <el-tag
            v-for="tag in article.tags.slice(0, 3)"
            :key="tag.id || tag"
            size="small"
            class="tag-item"
          >
            {{ typeof tag === 'string' ? tag : tag.name }}
          </el-tag>
          <span v-if="article.tags.length > 3" class="more-tags">+{{ article.tags.length - 3 }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import {
  User,
  Folder,
  Clock,
  View,
  Star,
  ChatDotRound
} from '@element-plus/icons-vue'

const props = defineProps({
  article: {
    type: Object,
    required: true,
    default: () => ({})
  },
  // 显示选项
  showCover: {
    type: Boolean,
    default: false
  },
  showSummary: {
    type: Boolean,
    default: true
  },
  showAuthor: {
    type: Boolean,
    default: true
  },
  showCategory: {
    type: Boolean,
    default: false
  },
  showTime: {
    type: Boolean,
    default: true
  },
  showViews: {
    type: Boolean,
    default: true
  },
  showLikes: {
    type: Boolean,
    default: false
  },
  showComments: {
    type: Boolean,
    default: false
  },
  showTags: {
    type: Boolean,
    default: false
  },
  showStatus: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

// 点击事件
const handleClick = () => {
  emit('click', props.article.id)
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    return '今天'
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}
</script>

<style scoped>
.article-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 16px;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.card-cover {
  margin-bottom: 16px;
  border-radius: 6px;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  line-height: 1.4;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-summary {
  color: #666;
  line-height: 1.6;
  margin-bottom: 16px;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 12px;
  color: #999;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.tag-item {
  cursor: default;
}

.more-tags {
  font-size: 12px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card-meta {
    gap: 12px;
  }
  
  .article-card {
    padding: 16px;
  }
}
</style>