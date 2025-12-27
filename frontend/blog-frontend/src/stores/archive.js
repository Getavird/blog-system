import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as archiveApi from '@/api/archive'


// 工具函数：包装异步操作，自动管理 loading 状态
const withLoading = async (loadingRef, asyncFunc, errorMessage) => {
  loadingRef.value = true
  try {
    const result = await asyncFunc()
    return result
  } catch (error) {
    handleError(errorMessage, error)
    throw error // 继续抛出，方便上层捕获
  } finally {
    loadingRef.value = false
  }
}

export const useArchiveStore = defineStore('archive', () => {
  // 状态：拆分 loading 为独立状态
  const archives = ref([])
  const archiveYears = ref([])
  const archiveStats = ref({})
  const currentYearArchives = ref([])
  const monthArticles = ref([])
  const searchedArticles = ref({ list: [], total: 0 })
  
  // 独立的 loading 状态
  const loadingAll = ref(false)
  const loadingStats = ref(false)
  const loadingYears = ref(false)
  const loadingByYear = ref(false)
  const loadingByMonth = ref(false)
  const loadingSearch = ref(false)


  // 获取所有归档数据（带格式化）
  const fetchAllArchives = async () => {
    return withLoading(
      loadingAll,
      async () => {
        const data = await archiveApi.getAllArchives()
        // 严格校验数据类型，避免非数组导致的 map 报错
        const safeData = Array.isArray(data) ? data : []
        archives.value = safeData.map(year => ({
          ...(year || {}), // 处理 year 为 null/undefined 的情况
          expanded: true,
          months: Array.isArray(year?.months) ? year.months : [],
          total: year?.total || 0,
          viewCount: year?.viewCount || 0,
          likeCount: year?.likeCount || 0
        }))
        return archives.value
      },
      '获取归档数据失败：'
    )
  }

  // 获取归档统计
  const fetchArchiveStats = async () => {
    return withLoading(
      loadingStats,
      async () => {
        const data = await archiveApi.getArchiveStats()
        archiveStats.value = typeof data === 'object' && data !== null ? data : {}
        return archiveStats.value
      },
      '获取归档统计失败：'
    )
  }

  // 获取可用年份
  const fetchArchiveYears = async () => {
    return withLoading(
      loadingYears,
      async () => {
        const data = await archiveApi.getArchiveYears()
        archiveYears.value = Array.isArray(data) ? data : []
        return archiveYears.value
      },
      '获取归档年份失败：'
    )
  }

  // 按年份查归档（增加参数校验）
  const fetchArchivesByYear = async (year) => {
    // 校验年份是否为有效数字
    if (typeof year !== 'number' || year < 1970 || year > new Date().getFullYear() + 1) {
      throw new Error(`无效的年份：${year}（需为1970至${new Date().getFullYear() + 1}之间的数字）`)
    }

    return withLoading(
      loadingByYear,
      async () => {
        const data = await archiveApi.getArchivesByYear(year)
        currentYearArchives.value = Array.isArray(data) ? data : []
        return currentYearArchives.value
      },
      `按年份${year}获取归档失败：`
    )
  }

  // 按年月查文章（增加参数校验）
  const fetchArticlesByMonth = async (year, month) => {
    // 校验年份和月份
    if (typeof year !== 'number' || year < 1970 || year > new Date().getFullYear() + 1) {
      throw new Error(`无效的年份：${year}`)
    }
    if (typeof month !== 'number' || month < 1 || month > 12) {
      throw new Error(`无效的月份：${month}（需为1-12之间的数字）`)
    }

    return withLoading(
      loadingByMonth,
      async () => {
        const data = await archiveApi.getArticlesByMonth(year, month)
        monthArticles.value = Array.isArray(data) ? data : []
        return monthArticles.value
      },
      `按${year}年${month}月获取文章失败：`
    )
  }

  // 搜索归档文章（参数校验）
  const searchArchiveArticles = async (keyword, page = 1, size = 20) => {
    // 校验分页参数
    if (typeof page !== 'number' || page < 1) page = 1
    if (typeof size !== 'number' || size < 1 || size > 100) size = 20 // 限制最大每页条数

    return withLoading(
      loadingSearch,
      async () => {
        const data = await archiveApi.searchArchiveArticles(keyword, page, size)
        // 确保返回结构符合预期
        searchedArticles.value = {
          list: Array.isArray(data?.list) ? data.list : [],
          total: typeof data?.total === 'number' ? data.total : 0
        }
        return searchedArticles.value
      },
      `搜索归档文章（关键词：${keyword}）失败：`
    )
  }

  return {
    // 状态
    archives,
    archiveYears,
    archiveStats,
    currentYearArchives,
    monthArticles,
    searchedArticles,
    // 独立 loading 状态
    loadingAll,
    loadingStats,
    loadingYears,
    loadingByYear,
    loadingByMonth,
    loadingSearch,

    // 方法
    fetchAllArchives,
    fetchArchiveStats,
    fetchArchiveYears,
    fetchArchivesByYear,
    fetchArticlesByMonth,
    searchArchiveArticles
  }
})

// 归档数据结构说明（同原注释）
// [
//   {
//     year: 2024,
//     total: 120,
//     viewCount: 50000,
//     likeCount: 3000,
//     months: [
//       {
//         month: 1,
//         count: 10,
//         articles: [
//           {
//             id: 1,
//             title: '文章标题',
//             createTime: '2024-01-15T10:30:00',
//             viewCount: 1000,
//             likeCount: 50,
//             commentCount: 20,
//             status: 1
//           }
//         ]
//       }
//     ]
//   }
// ]