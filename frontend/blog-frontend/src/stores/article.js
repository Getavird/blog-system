import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * @typedef {Object} Article
 * @property {number} id
 * @property {string} title
 * @property {string} content
 * @property {string} [summary]
 * @property {string} [coverImage]
 * @property {number} authorId
 * @property {string} authorName
 * @property {string} [authorAvatar]
 * @property {number} [categoryId]
 * @property {string} [categoryName]
 * @property {string[]} [tags]
 * @property {number} viewCount
 * @property {number} likeCount
 * @property {number} commentCount
 * @property {boolean} [isLiked]
 * @property {string} createTime
 * @property {string} updateTime
 * @property {number} status  // 0: 草稿, 1: 已发布, 2: 审核中
 */

export const useArticleStore = defineStore('article', () => {
  const articles = ref(/** @type {Article[]} */ ([]))
  const currentArticle = ref(/** @type {Article|null} */ (null))
  const loading = ref(false)
  
  const setArticles = (newArticles) => {
    articles.value = newArticles
  }
  
  const setCurrentArticle = (article) => {
    currentArticle.value = article
  }
  
  const clearCurrentArticle = () => {
    currentArticle.value = null
  }
  
  const setLoading = (value) => {
    loading.value = value
  }
  
  return {
    articles,
    currentArticle,
    loading,
    setArticles,
    setCurrentArticle,
    clearCurrentArticle,
    setLoading
  }
})