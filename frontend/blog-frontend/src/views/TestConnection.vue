<script setup>
import { ref } from 'vue'
import axios from 'axios'

const backendResult = ref('')
const dbResult = ref('')
const servletResult = ref('')
const filterResult = ref('')
const allTestsPassed = ref(false)

// 添加请求拦截器来查看实际请求的URL
axios.interceptors.request.use(config => {
  console.log('请求URL:', config.url)
  console.log('请求方法:', config.method)
  console.log('请求头:', config.headers)
  return config
})

axios.interceptors.response.use(response => {
  console.log('响应数据:', response.data)
  return response
}, error => {
  console.error('响应错误:', error.message)
  console.error('错误详情:', error.response?.data)
  return Promise.reject(error)
})

const testBackend = async () => {
  try {
    // 使用相对路径，Vite代理会自动处理
    const response = await axios.get('/api/test/hello')
    backendResult.value = response.data.message
    console.log('后端测试成功:', response.data)
    return true
  } catch (error) {
    backendResult.value = `连接失败：${error.message}`
    if (error.response) {
      console.error('HTTP状态码:', error.response.status)
      console.error('响应数据:', error.response.data)
      backendResult.value += ` (状态码: ${error.response.status})`
    }
    return false
  }
}

const testDatabase = async () => {
  try {
    const response = await axios.get('/api/test/db')
    dbResult.value = response.data.status
    console.log('数据库测试成功:', response.data)
    return true
  } catch (error) {
    dbResult.value = `数据库连接失败：${error.message}`
    if (error.response) {
      console.error('数据库测试错误:', error.response.status)
      dbResult.value += ` (状态码: ${error.response.status})`
    }
    return false
  }
}

const testTraditionalServlet = async () => {
  try {
    // 注意：Servlet返回的是HTML，不是JSON
    const response = await axios.get('/traditional/login', {
      headers: {
        'Accept': 'text/html'
      }
    })
    servletResult.value = 'Servlet工作正常'
    console.log('Servlet响应类型:', typeof response.data)
    return true
  } catch (error) {
    servletResult.value = `Servlet测试失败：${error.message}`
    if (error.response) {
      console.error('Servlet状态码:', error.response.status)
      servletResult.value += ` (状态码: ${error.response.status})`
    }
    return false
  }
}

const testFilter = async () => {
  try {
    // 测试一个需要登录的接口
    const response = await axios.get('/api/user/info')
    filterResult.value = 'Filter工作正常，已登录'
    return true
  } catch (error) {
    if (error.response && error.response.status === 401) {
      filterResult.value = 'Filter拦截成功（未登录状态）'
      console.log('Filter正确拦截未登录请求')
      return true
    } else {
      filterResult.value = `Filter测试失败：${error.message}`
      if (error.response) {
        console.error('Filter状态码:', error.response.status)
        filterResult.value += ` (状态码: ${error.response.status})`
      }
      return false
    }
  }
}

// 运行所有测试
const runAllTests = async () => {
  console.log('开始运行所有测试...')
  
  const results = await Promise.all([
    testBackend(),
    testDatabase(),
    testTraditionalServlet(),
    testFilter()
  ])
  
  allTestsPassed.value = results.every(r => r === true)
  console.log('所有测试结果:', results)
  console.log('全部通过:', allTestsPassed.value)
}

// 页面加载时自动运行测试
runAllTests()
</script>