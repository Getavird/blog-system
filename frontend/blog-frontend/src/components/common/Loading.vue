<template>
  <div class="loading-container" v-if="visible">
    <div class="loading-content">
      <el-icon :size="size" class="loading-icon" :class="{ 'is-loading': true }">
        <Loading />
      </el-icon>
      <span v-if="text" class="loading-text">{{ text }}</span>
    </div>
  </div>
</template>

<script setup>
import { Loading } from '@element-plus/icons-vue'

defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  text: {
    type: String,
    default: ''
  },
  size: {
    type: Number,
    default: 40
  }
})
</script>

<style scoped>
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.loading-icon {
  color: #409eff;
  animation: rotating 2s linear infinite;
}

.loading-text {
  color: #666;
  font-size: 14px;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>