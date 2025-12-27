import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as tagApi from '@/api/tag'

// 提取加载状态处理工具函数
const withLoading = (loadingRef, fn) => {
  return async (...args) => {
    try {
      loadingRef.value = true
      return await fn(...args)
    } catch (error) {
      console.error(`标签操作失败:`, error)
      throw error // 继续抛出错误供调用者处理
    } finally {
      loadingRef.value = false
    }
  }
}

export const useTagStore = defineStore('tag', () => {
  // 状态
  const tags = ref([])
  const currentTag = ref(null)
  const loading = ref(false)

  // 获取所有标签
  const fetchTags = withLoading(loading, async () => {
    const data = await tagApi.getAllTags()
    tags.value = data
    return data
  })

  // 获取标签详情
  const fetchTagDetail = withLoading(loading, async (id) => {
    const data = await tagApi.getTagDetail(id)
    currentTag.value = data
    return data
  })

  // 获取标签下的文章
  const fetchTagArticles = withLoading(loading, async (id, params = {}) => {
    return await tagApi.getTagArticles(id, params)
  })

  // 根据名称搜索标签（优先使用后端接口）
  const searchTagByName = withLoading(loading, async (name) => {
    return await tagApi.searchTagByName(name)
  })

  // 创建标签
  const createTag = withLoading(loading, async (name) => {
    const data = await tagApi.createTag(name)
    tags.value.push(data)
    return data
  })

  // 更新标签
  const updateTag = withLoading(loading, async (id, name) => {
    const data = await tagApi.updateTag(id, name)

    // 更新本地状态
    const index = tags.value.findIndex(tag => tag.id === id)
    if (index !== -1) {
      tags.value[index].name = name
    }

    if (currentTag.value?.id === id) {
      currentTag.value.name = name
    }

    return data
  })

  // 删除标签
  const deleteTag = withLoading(loading, async (id) => {
    const data = await tagApi.deleteTag(id)

    // 从列表中移除
    tags.value = tags.value.filter(tag => tag.id !== id)
    if (currentTag.value?.id === id) {
      currentTag.value = null
    }

    return data
  })

  // 获取标签云数据（包含文章数量统计）
  const fetchTagCloud = withLoading(loading, async () => {
    // 若后端有专门的标签云接口，建议替换为 tagApi.getTagCloud()
    const data = await tagApi.getAllTags()
    tags.value = data.map(tag => ({
      ...tag,
      count: tag.articleCount || 0 // 确保有count字段用于标签云渲染
    }))
    return tags.value
  })

  // 搜索标签（优先使用后端接口）
  const searchTags = withLoading(loading, async (keyword) => {
    // 若后端有搜索接口，替换为：return await tagApi.searchTags(keyword)
    const allTags = await tagApi.getAllTags()
    return allTags.filter(tag =>
      tag.name.toLowerCase().includes(keyword.toLowerCase())
    )
  })

  return {
    // 状态
    tags,
    currentTag,
    loading,

    // 方法
    fetchTags,
    fetchTagCloud,
    fetchTagDetail,
    fetchTagArticles,
    searchTags,
    createTag,
    updateTag,
    deleteTag
  }
})