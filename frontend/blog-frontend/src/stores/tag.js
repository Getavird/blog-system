import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as tagApi from '@/api/tag'

// 工具函数：包装异步操作，自动管理 loading 状态
const withLoading = (loadingRef, fn) => {
  return async (...args) => {
    try {
      loadingRef.value = true
      return await fn(...args)
    } catch (error) {
      console.error(`标签操作失败:`, error)
      throw error
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

  // 通过ID获取标签详情
  const fetchTagDetail = withLoading(loading, async (id) => {
    const data = await tagApi.getTagDetail(id)
    currentTag.value = data
    return data
  })

  // 通过名称获取标签详情（新增）
  const fetchTagDetailByName = withLoading(loading, async (name) => {
    const data = await tagApi.getTagDetailByName(name)
    currentTag.value = data
    return data
  })

  // 通过ID获取标签下的文章
  const fetchTagArticles = withLoading(loading, async (id, params = {}) => {
    return await tagApi.getTagArticles(id, params)
  })

  // 通过名称获取标签下的文章（新增）
  const fetchTagArticlesByName = withLoading(loading, async (name, params = {}) => {
    const data = await tagApi.getTagArticlesByName(name, params)
    return data
  })

  // 搜索标签
  const searchTags = withLoading(loading, async (keyword) => {
    return await tagApi.searchTags(keyword)
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

  return {
    // 状态
    tags,
    currentTag,
    loading,
    
    // 方法
    fetchTags,
    fetchTagDetail,
    fetchTagDetailByName,  // 新增
    fetchTagArticles,
    fetchTagArticlesByName, // 新增
    searchTags,
    createTag,
    updateTag,
    deleteTag
  }
})