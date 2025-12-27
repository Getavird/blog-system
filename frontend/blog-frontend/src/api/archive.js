import request from '@/utils/request'

export const getAllArchives = () => {
  return request.get('/api/archives')
}

export const getArchiveStats = () => {
  return request.get('/api/archives/stats')
}

export const getArchiveOverview = () => {
  return request.get('/api/archives/overview')
}

export const getArchiveYears = () => {
  return request.get('/api/archives/years')
}

export const getArchivesByYear = (year) => {
  return request.get(`/api/archives/year/${year}`)
}

export const getYearStats = (year) => {
  return request.get(`/api/archives/year/${year}/stats`)
}

export const getArticlesByMonth = (year, month) => {
  return request.get(`/api/archives/${year}/${month}`)
}

export const getMonthArchiveDetail = (year, month) => {
  return request.get(`/api/archives/${year}/${month}/detail`)
}

export const getMostActiveYear = () => {
  return request.get('/api/archives/most-active-year')
}

export const getRecentYearStats = (limit = 5) => {
  return request.get('/api/archives/recent-stats', { params: { limit } })
}

export const getYearComparison = () => {
  return request.get('/api/archives/year-comparison')
}

export const searchArchiveArticles = (keyword, page = 1, size = 20) => {
  return request.get('/api/archives/search', { 
    params: { keyword, page, size }
  })
}