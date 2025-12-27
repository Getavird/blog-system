import request from '@/utils/request'

// 文章评论列表
export const getArticleComments = (articleId, params = {}) => {
  return request.get(`/api/comments/article/${articleId}`, { 
    params: { page: 1, size: 20, ...params }
  })
}

// 新增评论
export const createComment = (data) => {
  return request.post('/api/comments', data)
}

// 删除评论
export const deleteComment = (id) => {
  return request.delete(`/api/comments/${id}`)
}

// 评论点赞/取消
export const toggleCommentLike = (id, isLike) => {
  return request.post(`/api/comments/${id}/like`, { isLike })
}