<template>
  <div class="empty-container">
    <div class="empty-content">
      <el-icon :size="iconSize" :color="iconColor" class="empty-icon">
        <component :is="icon" />
      </el-icon>
      <h3 v-if="title" class="empty-title">{{ title }}</h3>
      <p v-if="description" class="empty-description">{{ description }}</p>
      <slot>
        <el-button v-if="buttonText" :type="buttonType" @click="handleClick" class="empty-button">
          {{ buttonText }}
        </el-button>
      </slot>
    </div>
  </div>
</template>

<script setup>
import { Document, Search, Folder } from '@element-plus/icons-vue'

const props = defineProps({
  title: {
    type: String,
    default: '暂无数据'
  },
  description: {
    type: String,
    default: '这里还没有任何内容'
  },
  icon: {
    type: String,
    default: 'Document'
  },
  iconSize: {
    type: Number,
    default: 60
  },
  iconColor: {
    type: String,
    default: '#c0c4cc'
  },
  buttonText: {
    type: String,
    default: ''
  },
  buttonType: {
    type: String,
    default: 'primary'
  }
})

const emit = defineEmits(['click'])

const handleClick = () => {
  emit('click')
}
</script>

<style scoped>
.empty-container {
  padding: 60px 20px;
  text-align: center;
}

.empty-content {
  display: inline-block;
}

.empty-icon {
  margin-bottom: 20px;
}

.empty-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 10px;
}

.empty-description {
  color: #666;
  margin-bottom: 25px;
  font-size: 14px;
  line-height: 1.6;
}

.empty-button {
  margin-top: 10px;
}
</style>