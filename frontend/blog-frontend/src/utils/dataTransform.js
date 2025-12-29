// src/utils/dataTransform.js

/**
 * 转换文章数据格式，适配前端组件
 * @param {Object} apiData - 后端返回的文章数据
 * @returns {Object} - 前端需要的文章格式
 */
export const transformArticle = (apiData) => {
  if (!apiData) {
    console.warn('transformArticle: apiData为空')
    return null
  }
  
  console.log('转换文章原始数据:', apiData)
  
  // 处理tags字段：可能已经是数组，也可能是逗号分隔的字符串
  let tagsArray = []
  if (apiData.tags) {
    if (Array.isArray(apiData.tags)) {
      tagsArray = apiData.tags
    } else if (typeof apiData.tags === 'string') {
      tagsArray = apiData.tags.split(',').map(tag => tag.trim()).filter(tag => tag)
    }
  }
  
  // 构建转换后的对象
  const transformed = {
    id: apiData.id || apiData.articleId || 0,
    title: apiData.title || '无标题',
    content: apiData.content || '',
    summary: apiData.summary || apiData.content?.substring(0, 100) || '暂无摘要',
    coverImage: apiData.coverImage 
      ? (apiData.coverImage.startsWith('http') ? apiData.coverImage : `/uploads/${apiData.coverImage}`)
      : '',
    status: apiData.status || 1,
    viewCount: apiData.viewCount || apiData.viewCount || 0,
    likeCount: apiData.likeCount || apiData.likeCount || 0,
    commentCount: apiData.commentCount || 0,
    categoryId: apiData.categoryId || apiData.categoryId,
    categoryName: apiData.categoryName || apiData.category?.name || '未分类',
    // 作者信息可能有多种字段名
    authorName: apiData.authorName || apiData.author?.name || apiData.username || apiData.user?.username || '未知作者',
    authorAvatar: apiData.authorAvatar 
      ? (apiData.authorAvatar.startsWith('http') ? apiData.authorAvatar : `/uploads/${apiData.authorAvatar}`)
      : (apiData.author?.avatar 
          ? (apiData.author.avatar.startsWith('http') ? apiData.author.avatar : `/uploads/${apiData.author.avatar}`)
          : ''),
    authorId: apiData.userId || apiData.authorId || apiData.user?.id || 0,
    tags: tagsArray,
    isTop: apiData.isTop === 1 || apiData.isTop === true,
    allowComment: apiData.allowComment === 1 || apiData.allowComment === true || apiData.allowComment === undefined,
    // 时间字段可能有多种名称
    createTime: apiData.createTime || apiData.createdAt || apiData.createdTime || new Date().toISOString(),
    updateTime: apiData.updateTime || apiData.updatedAt || apiData.updatedTime || apiData.createTime,
    publishTime: apiData.publishTime || apiData.publishedAt || apiData.createTime
  }
  
  console.log('转换后的文章:', transformed)
  return transformed
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
  console.log('transformArticles输入:', articles)
  
  if (!Array.isArray(articles)) {
    console.warn('transformArticles: 输入不是数组', articles)
    return []
  }
  
  const transformed = articles
    .map(transformArticle)
    .filter(article => article !== null) // 过滤掉转换失败的文章
  
  console.log('transformArticles输出:', transformed)
  return transformed
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