import request from '@/utils/request'

// 统一的响应处理函数
const handleResponse = (response) => {
  // 检查是否是标准格式 {code, message, data}
  if (response && typeof response === 'object' && 'code' in response) {
    console.log(`📊 接口返回标准格式: code=${response.code}`)
    if (response.code === 200) {
      return response.data
    } else {
      throw new Error(response.message || '请求失败')
    }
  }
  // 如果不是标准格式，直接返回
  return response
}

export const getAllArchives = () => {
  return request.get('/api/archives').then(handleResponse)
}

export const getArchiveStats = () => {
  return request.get('/api/archives/stats').then(handleResponse)
}

export const getArchiveOverview = () => {
  return request.get('/api/archives/overview').then(handleResponse)
}

export const getArchiveYears = () => {
  return request.get('/api/archives/years').then(handleResponse)
}

export const getArchivesByYear = (year) => {
  return request.get(`/api/archives/year/${year}`).then(handleResponse)
}

export const getYearStats = (year) => {
  return request.get(`/api/archives/year/${year}/stats`).then(handleResponse)
}

export const getArticlesByMonth = (year, month) => {
  return request.get(`/api/archives/${year}/${month}`).then(handleResponse)
}

export const getMonthArchiveDetail = (year, month) => {
  return request.get(`/api/archives/${year}/${month}/detail`).then(handleResponse)
}

export const getMostActiveYear = () => {
  return request.get('/api/archives/most-active-year').then(handleResponse)
}

export const getRecentYearStats = (limit = 5) => {
  return request.get('/api/archives/recent-stats', { params: { limit } }).then(handleResponse)
}

export const getYearComparison = () => {
  return request.get('/api/archives/year-comparison').then(handleResponse)
}

export const searchArchiveArticles = (keyword, page = 1, size = 20) => {
  return request.get('/api/archives/search', { 
    params: { keyword, page, size }
  }).then(handleResponse)
}