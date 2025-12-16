import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface Article {
  id: number
  title: string
  content: string
  summary?: string
  coverImage?: string
  authorId: number
  authorName: string
  authorAvatar?: string
  categoryId?: number
  categoryName?: string
  tags?: string[]
  viewCount: number
  likeCount: number
  commentCount: number
  isLiked?: boolean
  createTime: string
  updateTime: string
  status: number  // 0: 草稿, 1: 已发布, 2: 审核中
}

export const useArticleStore = defineStore('article', () => {
  const articles = ref<Article[]>([])
  const currentArticle = ref<Article | null>(null)
  const loading = ref(false)
  
  const setArticles = (newArticles: Article[]) => {
    articles.value = newArticles
  }
  
  const setCurrentArticle = (article: Article) => {
    currentArticle.value = article
  }
  
  const clearCurrentArticle = () => {
    currentArticle.value = null
  }
  
  const setLoading = (value: boolean) => {
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