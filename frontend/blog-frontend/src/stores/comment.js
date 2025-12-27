import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as commentApi from '@/api/comment'

export const useCommentStore = defineStore('comment', () => {
  // 状态：拆分loading为具体操作的状态，避免冲突
  const comments = ref([])
  const fetchLoading = ref(false) // 获取评论加载中
  const createLoading = ref(false) // 创建评论加载中
  const deleteLoading = ref(false) // 删除评论加载中
  const likeLoading = ref(false) // 点赞操作加载中
  const total = ref(0) // 评论总数（用于分页）

  // 获取文章评论（支持分页加载更多）
  const fetchArticleComments = async (articleId, params = {}) => {
    try {
      fetchLoading.value = true
      const data = await commentApi.getArticleComments(articleId, params)
      const newComments = data.list || []
      // 分页逻辑：如果是第一页则覆盖，否则追加
      if (params.page === 1 || !params.page) {
        comments.value = newComments
      } else {
        comments.value = [...comments.value, ...newComments]
      }
      total.value = data.total || 0 // 记录总数
      return data
    } catch (error) {
      console.error('获取文章评论失败:', error)
      throw error
    } finally {
      fetchLoading.value = false
    }
  }

  // 创建评论
  const createComment = async (commentData) => {
    try {
      createLoading.value = true
      const newComment = await commentApi.createComment(commentData)
      // 添加到列表头部（最新评论在前）
      comments.value.unshift(newComment)
      total.value += 1 // 总数+1
      return newComment
    } catch (error) {
      console.error('创建评论失败:', error)
      throw error
    } finally {
      createLoading.value = false
    }
  }

  // 删除评论
  const deleteComment = async (id) => {
    try {
      deleteLoading.value = true
      await commentApi.deleteComment(id)
      // 从列表中移除
      comments.value = comments.value.filter(comment => comment.id !== id)
      total.value -= 1 // 总数-1
    } catch (error) {
      console.error('删除评论失败:', error)
      throw error
    } finally {
      deleteLoading.value = false
    }
  }

  // 点赞/取消点赞评论（同步更新isLiked状态）
  const toggleCommentLike = async (id, isLike) => {
    try {
      likeLoading.value = true
      await commentApi.toggleCommentLike(id, isLike)
      
      // 更新本地状态（包含likeCount和isLiked）
      const comment = comments.value.find(comment => comment.id === id)
      if (comment) {
        comment.isLiked = isLike // 同步更新是否点赞状态
        if (isLike) {
          comment.likeCount += 1
        } else {
          comment.likeCount = Math.max(0, comment.likeCount - 1)
        }
      }
    } catch (error) {
      console.error('操作评论点赞失败:', error)
      throw error
    } finally {
      likeLoading.value = false
    }
  }

  // 清除评论（切换文章时使用）
  const clearComments = () => {
    comments.value = []
    total.value = 0 // 清空总数
  }

  return {
    // 状态
    comments,
    total,
    fetchLoading,
    createLoading,
    deleteLoading,
    likeLoading,
    
    // 方法
    fetchArticleComments,
    createComment,
    deleteComment,
    toggleCommentLike,
    clearComments
  }
})