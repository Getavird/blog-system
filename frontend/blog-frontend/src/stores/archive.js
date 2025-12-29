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
      try {
        const response = await archiveApi.getAllArchives()
        
        const yearMap = new Map()
        
        if (Array.isArray(response)) {
          response.forEach(monthData => {
            const year = parseInt(monthData.year) || new Date().getFullYear()
            let month = 0
            
            if (monthData.month && typeof monthData.month === 'string') {
              const match = monthData.month.match(/(\d+)/)
              month = match ? parseInt(match[1]) : 0
            }
            
            if (month === 0) month = 1
            
            if (!yearMap.has(year)) {
              yearMap.set(year, {
                year: year,
                total: 0,
                viewCount: 0,
                likeCount: 0,
                expanded: true, // 年份默认展开
                months: []
              })
            }
            
            const yearData = yearMap.get(year)
            yearData.total += monthData.articleCount || 0
            yearData.viewCount += monthData.viewCount || 0
            yearData.likeCount += monthData.likeCount || 0
            
            yearData.months.push({
              month: month,
              count: monthData.articleCount || 0,
              articles: monthData.articles || [],
              expanded: false, // 月份默认收起（文章多时更合适）
              monthName: monthData.monthName || monthData.month || `${month}月`
            })
          })
        }
        
        // 转换为数组并排序
        const formattedData = Array.from(yearMap.values())
        formattedData.forEach(year => {
          year.months.sort((a, b) => b.month - a.month)
        })
        formattedData.sort((a, b) => b.year - a.year)
        
        // 默认展开最近一年的所有月份，其他年份的月份收起
        if (formattedData.length > 0) {
          const currentYear = new Date().getFullYear()
          formattedData.forEach(year => {
            if (year.year === currentYear) {
              year.months.forEach(month => {
                month.expanded = true // 当前年份的月份默认展开
              })
            }
          })
        }
        
        archives.value = formattedData
        return archives.value
        
      } catch (error) {
        console.error('获取归档数据失败:', error)
        throw error
      }
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
      const response = await archiveApi.getArchiveYears()
      
      // 提取 data 字段
      let data = response
      if (response && typeof response === 'object' && 'data' in response) {
        data = response.data
      }
      
      // 确保是数组
      archiveYears.value = Array.isArray(data) ? data : []
      
      // 如果年份接口返回空，从归档数据中提取年份
      if (archiveYears.value.length === 0 && archives.value.length > 0) {
        archiveYears.value = archives.value.map(year => year.year).sort((a, b) => b - a)
      }
      
      console.log('📦 年份数据:', archiveYears.value)
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