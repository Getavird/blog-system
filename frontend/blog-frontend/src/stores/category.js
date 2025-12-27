import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as categoryApi from '@/api/category'

export const useCategoryStore = defineStore('category', () => {
  // 状态
  const categories = ref([])
  const currentCategory = ref(null)
  const loading = ref(false)
  
  // 获取分类列表
  const fetchCategories = async () => {
    try {
      loading.value = true
      const data = await categoryApi.getCategories()
      categories.value = data
      return data
    } catch (error) {
      console.error('获取分类列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 获取分类详情
  const fetchCategoryDetail = async (id) => {
    try {
      loading.value = true
      const data = await categoryApi.getCategoryDetail(id)
      currentCategory.value = data
      return data
    } catch (error) {
      console.error('获取分类详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 获取分类下的文章
  const fetchCategoryArticles = async (id, params = {}) => {
    try {
      loading.value = true
      const data = await categoryApi.getCategoryArticles(id, params)
      return data
    } catch (error) {
      console.error('获取分类文章失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 创建分类
  const createCategory = async (categoryData) => {
    try {
      loading.value = true
      const data = await categoryApi.createCategory(categoryData)
      categories.value.push(data)
      return data
    } catch (error) {
      console.error('创建分类失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 更新分类
  const updateCategory = async (id, categoryData) => {
    try {
      loading.value = true
      const data = await categoryApi.updateCategory(id, categoryData)
      
      // 更新本地状态
      const index = categories.value.findIndex(cat => cat.id === id)
      if (index !== -1) {
        categories.value[index] = { ...categories.value[index], ...categoryData }
      }
      
      if (currentCategory.value && currentCategory.value.id === id) {
        currentCategory.value = { ...currentCategory.value, ...categoryData }
      }
      
      return data
    } catch (error) {
      console.error('更新分类失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 删除分类
  const deleteCategory = async (id) => {
    try {
      loading.value = true
      const data = await categoryApi.deleteCategory(id)
      
      // 从列表中移除
      categories.value = categories.value.filter(cat => cat.id !== id)
      if (currentCategory.value && currentCategory.value.id === id) {
        currentCategory.value = null
      }
      
      return data
    } catch (error) {
      console.error('删除分类失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  

  return {
    // 状态
    categories,
    currentCategory,
    loading,
    
    // 方法
    fetchCategories,
    fetchCategoryDetail,
    fetchCategoryArticles,
    createCategory,
    updateCategory,
    deleteCategory
  }
})