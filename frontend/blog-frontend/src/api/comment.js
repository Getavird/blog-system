import request from '@/utils/request'

// 获取文章评论
export const getArticleComments = (articleId) => {
  return request.get(`/api/comments/article/${articleId}`)
}

// 发布评论
export const createComment = (data) => {
  return request.post('/api/comments', data)
}

// 删除评论
export const deleteComment = (id) => {
  return request.delete(`/api/comments/${id}`)
}

// 点赞评论
export const likeComment = (id) => {
  return request.post(`/api/comments/${id}/like`)
}

// 取消点赞评论
export const unlikeComment = (id) => {
  return request.delete(`/api/comments/${id}/like`)
}