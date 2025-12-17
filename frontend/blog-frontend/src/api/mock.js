//临时API模拟
import { mockArticles, mockUsers, mockCategories } from '@/utils/mock'

export const getMockArticles = (params) => {
  // 模拟API延迟
  return new Promise(resolve => {
    setTimeout(() => {
      resolve({
        data: mockArticles,
        total: mockArticles.length
      })
    }, 500)
  })
}