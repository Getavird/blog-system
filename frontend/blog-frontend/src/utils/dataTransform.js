// src/utils/dataTransform.js

/**
 * 转换文章数据格式，适配前端组件
 * @param {Object} apiData - 后端返回的文章数据
 * @returns {Object} - 前端需要的文章格式
 */
// src/utils/dataTransform.js

export const transformArticle = (apiData) => {
  if (!apiData) {
    console.warn('transformArticle: apiData为空')
    return null
  }
  
  console.log('转换文章原始数据:', apiData)
  
  // 新增：处理头像的完整URL函数
  const getFullAvatarUrl = (avatarPath, username) => {
    if (!avatarPath || avatarPath === 'null' || avatarPath === 'undefined' || avatarPath.trim() === '') {
      return `http://localhost:8080/static/images/default-avatars/default_avatar.png?t=${Date.now()}&u=${username || 'default'}`
    }
    
    // 已经是完整URL直接返回
    if (avatarPath.startsWith('http://') || 
        avatarPath.startsWith('https://') || 
        avatarPath.startsWith('data:')) {
      const separator = avatarPath.includes('?') ? '&' : '?'
      return avatarPath + separator + 't=' + Date.now() + '&u=' + (username || 'user')
    }
    
    let fullUrl = avatarPath
    
    // 情况1：路径以/uploads/avatars/开头（相对路径）
    if (fullUrl.startsWith('/uploads/avatars/')) {
      fullUrl = 'http://localhost:8080' + fullUrl
    }
    // 情况2：只有文件名（如default_avatar.png）
    else if (!fullUrl.includes('/') && !fullUrl.includes('\\')) {
      if (fullUrl.includes('default_avatar')) {
        fullUrl = 'http://localhost:8080/static/images/default-avatars/default_avatar.png'
      } else {
        fullUrl = 'http://localhost:8080/uploads/avatars/' + fullUrl
      }
    }
    // 情况3：其他格式的路径
    else if (fullUrl.startsWith('/')) {
      fullUrl = 'http://localhost:8080' + fullUrl
    }
    // 情况4：Windows风格的路径或其他
    else {
      fullUrl = fullUrl.replace(/\\/g, '/')
      if (fullUrl.startsWith('uploads/avatars/')) {
        fullUrl = 'http://localhost:8080/' + fullUrl
      } else if (fullUrl.startsWith('/')) {
        fullUrl = 'http://localhost:8080' + fullUrl
      }
    }
    
    const separator = fullUrl.includes('?') ? '&' : '?'
    return fullUrl + separator + 't=' + Date.now() + '&u=' + (username || 'user')
  }
  
  // 获取用户名
  const username = apiData.username || apiData.user?.username || apiData.authorName || apiData.author?.username || '未知作者'
  
  // 处理作者头像
  let authorAvatar = ''
  if (apiData.authorAvatar) {
    authorAvatar = getFullAvatarUrl(apiData.authorAvatar, username)
  } else if (apiData.author?.avatar) {
    authorAvatar = getFullAvatarUrl(apiData.author.avatar, username)
  } else if (apiData.user?.avatar) {
    authorAvatar = getFullAvatarUrl(apiData.user.avatar, username)
  } else {
    authorAvatar = getFullAvatarUrl(null, username)
  }
  
  const article = {
    id: apiData.id || apiData.articleId || 0,
    title: apiData.title || '无标题',
    content: apiData.content || '',
    summary: apiData.summary || apiData.content?.substring(0, 100) || '',
    coverImage: apiData.coverImage 
      ? (apiData.coverImage.startsWith('http') ? apiData.coverImage : `/uploads/${apiData.coverImage}`)
      : '',
    status: apiData.status,
    viewCount: apiData.viewCount || 0,
    likeCount: apiData.likeCount || 0,
    commentCount: apiData.commentCount || 0,
    categoryId: apiData.categoryId || 0,
    categoryName: apiData.categoryName || apiData.category?.name || '未分类',
    
    // 作者信息
    username: username, // 确保有用户名
    authorName: username, // 显示名
    authorAvatar: authorAvatar, // 修正的头像URL
    authorId: apiData.userId || apiData.authorId || apiData.user?.id || 0,
    
    // 作者统计信息 - 默认值
    articleCount: apiData.author?.articleCount || apiData.user?.articleCount || 0,
    totalLikeCount: apiData.author?.likeCount || apiData.user?.likeCount || 0,
    fansCount: apiData.author?.followerCount || apiData.user?.followerCount || 0,
    isFollowing: apiData.isFollowing || false,
    
    // 处理标签
    tags: [],
    isTop: apiData.isTop === 1 || apiData.isTop === true,
    allowComment: apiData.allowComment === 1 || apiData.allowComment === true || apiData.allowComment === undefined,
    // 时间字段可能有多种名称
    createTime: apiData.createTime || apiData.createdAt || apiData.createdTime || new Date().toISOString(),
    updateTime: apiData.updateTime || apiData.updatedAt || apiData.updatedTime || apiData.createTime,
    publishTime: apiData.publishTime || apiData.publishedAt || apiData.createTime
  }
  
  // 处理tags字段：可能已经是数组，也可能是逗号分隔的字符串
  if (apiData.tags) {
    if (Array.isArray(apiData.tags)) {
      article.tags = apiData.tags
    } else if (typeof apiData.tags === 'string') {
      article.tags = apiData.tags.split(',').map(tag => tag.trim()).filter(tag => tag)
    }
  }
  
  console.log('转换后的文章:', article)
  return article
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