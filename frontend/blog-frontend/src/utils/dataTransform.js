// src/utils/dataTransform.js

/**
 * 转换文章数据格式，适配前端组件
 * @param {Object} apiData - 后端返回的文章数据
 * @returns {Object} - 前端需要的文章格式
 */
export const transformArticle = (apiData) => {
  return {
    id: apiData.id,
    title: apiData.title,
    content: apiData.content,
    summary: apiData.summary,
    coverImage: apiData.coverImage ? `/uploads/${apiData.coverImage}` : '',
    status: apiData.status,
    viewCount: apiData.viewCount || 0,
    likeCount: apiData.likeCount || 0,
    commentCount: apiData.commentCount || 0,
    categoryId: apiData.categoryId,
    categoryName: apiData.categoryName,
    authorName: apiData.authorName || apiData.username,
    authorAvatar: apiData.authorAvatar ? `/uploads/${apiData.authorAvatar}` : '',
    authorId: apiData.userId,
    tags: apiData.tags ? apiData.tags.split(',').map(tag => tag.trim()) : [],
    isTop: apiData.isTop === 1,
    allowComment: apiData.allowComment === 1,
    createTime: apiData.createTime,
    updateTime: apiData.updateTime || apiData.createTime,
    publishTime: apiData.publishTime || apiData.createTime
  }
}

/**
 * 转换用户数据格式
 * @param {Object} apiData - 后端返回的用户数据
 * @returns {Object} - 前端需要的用户格式
 */
export const transformUser = (apiData) => {
  return {
    id: apiData.id,
    username: apiData.username,
    email: apiData.email,
    avatar: apiData.avatar ? `/uploads/${apiData.avatar}` : '',
    role: apiData.role || 0,
    status: apiData.status || 1,
    bio: apiData.bio || '',
    createTime: apiData.createTime,
    updateTime: apiData.updateTime || apiData.createTime
  }
}

/**
 * 转换分类数据格式
 * @param {Object} apiData - 后端返回的分类数据
 * @returns {Object} - 前端需要的分类格式
 */
export const transformCategory = (apiData) => {
  return {
    id: apiData.id,
    name: apiData.name,
    description: apiData.description || '',
    orderNum: apiData.orderNum || 0,
    articleCount: apiData.articleCount || 0,
    icon: apiData.icon || 'folder',
    color: apiData.color || '#409eff'
  }
}

/**
 * 转换评论数据格式
 * @param {Object} apiData - 后端返回的评论数据
 * @returns {Object} - 前端需要的评论格式
 */
export const transformComment = (apiData) => {
  return {
    id: apiData.id,
    content: apiData.content,
    articleId: apiData.articleId,
    userId: apiData.userId,
    parentId: apiData.parentId || 0,
    replyUserId: apiData.replyUserId,
    likeCount: apiData.likeCount || 0,
    status: apiData.status || 1,
    createTime: apiData.createTime,
    updateTime: apiData.updateTime || apiData.createTime,
    userName: apiData.username,
    userAvatar: apiData.userAvatar ? `/uploads/${apiData.userAvatar}` : '',
    replyUserName: apiData.replyUsername,
    // 递归转换子评论
    childComments: (apiData.childComments || []).map(transformComment)
  }
}

/**
 * 批量转换文章数据
 * @param {Array} articles - 文章数组
 * @returns {Array}
 */
export const transformArticles = (articles) => {
  return Array.isArray(articles) 
    ? articles.map(transformArticle)
    : []
}

/**
 * 批量转换分类数据
 * @param {Array} categories - 分类数组
 * @returns {Array}
 */
export const transformCategories = (categories) => {
  return Array.isArray(categories)
    ? categories.map(transformCategory)
    : []
}

/**
 * 格式化日期时间
 * @param {string} dateString - ISO日期字符串
 * @returns {string} - 格式化后的日期
 */
export const formatDateTime = (dateString) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    // 今天，显示时间
    return date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}