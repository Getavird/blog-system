<template>
  <div style="padding: 20px;">
    <h1>Vite 代理连接测试</h1>
    <el-button type="primary" @click="testProxy" :loading="loading">
      开始测试
    </el-button>
    
    <div v-if="loading" style="margin-top: 20px;">
      <el-alert title="正在测试中..." type="info" show-icon />
    </div>
    
    <div v-if="testResult" style="margin-top: 20px;">
      <h3>测试结果:</h3>
      
      <!-- 测试项结果展示 -->
      <div v-for="(item, index) in testResult" :key="index" style="margin-bottom: 15px;">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span style="font-weight: bold;">{{ item.url }}</span>
              <el-tag :type="item.success ? 'success' : 'danger'">
                {{ item.success ? '✅ 成功' : '❌ 失败' }}
              </el-tag>
            </div>
          </template>
          
          <div v-if="item.success">
            <p><strong>状态码:</strong> {{ item.status }}</p>
            <p><strong>响应类型:</strong> {{ item.contentType }}</p>
            <p><strong>响应大小:</strong> {{ item.size }} 字节</p>
            <div v-if="item.data">
              <p><strong>响应预览:</strong></p>
              <pre style="background: #f5f7fa; padding: 10px; border-radius: 4px; overflow: auto; max-height: 200px;">
{{ item.data }}
              </pre>
            </div>
          </div>
          
          <div v-else>
            <p><strong>错误:</strong> {{ item.error }}</p>
            <p v-if="item.status"><strong>状态码:</strong> {{ item.status }}</p>
          </div>
        </el-card>
      </div>
    </div>
    
    <!-- 后端状态信息 -->
    <div v-if="backendStatus" style="margin-top: 20px;">
      <h3>后端状态检查</h3>
      <el-button @click="checkBackendDirectly" type="warning">直接检查后端</el-button>
      <div v-if="directCheckResult" style="margin-top: 10px;">
        <pre style="background: #e8f4f8; padding: 10px; border-radius: 4px;">
{{ directCheckResult }}
        </pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const testResult = ref([])
const backendStatus = ref(false)
const directCheckResult = ref('')

// 要测试的URL列表
const testUrls = [
  { name: '基础接口测试', url: '/api/test/hello', type: 'json' },
  { name: '数据库连接测试', url: '/api/test/db/connection', type: 'json' },
  { name: '传统Servlet登录页', url: '/traditional/login', type: 'html' },
  { name: '数据库状态调试', url: '/api/debug/db-status', type: 'json' },
  { name: '用户信息接口', url: '/api/user/info', type: 'json', expected: 401 },
  { name: '文章列表接口', url: '/api/articles', type: 'json' }
]

const testProxy = async () => {
  loading.value = true
  testResult.value = []
  
  console.log('=== 开始测试代理 ===')
  console.log('前端地址: http://localhost:3000')
  console.log('后端地址: http://localhost:8080')
  
  for (const testItem of testUrls) {
    console.log(`\n测试: ${testItem.name}`)
    console.log(`请求URL: ${testItem.url}`)
    console.log(`期望类型: ${testItem.type}`)
    
    try {
      const startTime = Date.now()
      
      // 使用 fetch 进行请求
      const response = await fetch(testItem.url, {
        method: 'GET',
        headers: {
          'Accept': testItem.type === 'json' ? 'application/json' : 'text/html',
          'Content-Type': 'application/json'
        },
        // 添加超时处理
        signal: AbortSignal.timeout(10000)
      })
      
      const endTime = Date.now()
      const duration = endTime - startTime
      
      console.log(`响应状态: ${response.status} ${response.statusText}`)
      console.log(`响应时间: ${duration}ms`)
      
      // 获取响应内容
      let data = null
      let contentType = response.headers.get('content-type') || ''
      
      if (response.ok || (testItem.expected && response.status === testItem.expected)) {
        if (contentType.includes('application/json')) {
          data = await response.json()
        } else if (contentType.includes('text/html')) {
          data = await response.text()
          // 只显示前500个字符
          data = data.substring(0, 500) + (data.length > 500 ? '...' : '')
        } else {
          data = await response.text()
        }
      }
      
      const result = {
        name: testItem.name,
        url: testItem.url,
        success: response.ok || (testItem.expected && response.status === testItem.expected),
        status: response.status,
        statusText: response.statusText,
        contentType: contentType,
        size: response.headers.get('content-length') || '未知',
        duration: duration + 'ms',
        data: data,
        error: null
      }
      
      console.log('结果:', result.success ? '✅ 成功' : '❌ 失败')
      
      testResult.value.push(result)
      
      // 短暂延迟，避免请求太快
      await new Promise(resolve => setTimeout(resolve, 300))
      
    } catch (error) {
      console.error(`请求失败:`, error)
      
      const result = {
        name: testItem.name,
        url: testItem.url,
        success: false,
        status: null,
        statusText: null,
        contentType: null,
        size: null,
        duration: null,
        data: null,
        error: error.message
      }
      
      testResult.value.push(result)
    }
  }
  
  loading.value = false
  
  // 检查整体结果
  const successCount = testResult.value.filter(r => r.success).length
  const totalCount = testResult.value.length
  
  console.log(`\n=== 测试完成 ===`)
  console.log(`成功: ${successCount}/${totalCount}`)
  
  if (successCount === 0) {
    ElMessage.error('所有测试都失败了，请检查代理配置和后端服务')
    backendStatus.value = true
  } else if (successCount < totalCount) {
    ElMessage.warning(`部分测试成功 (${successCount}/${totalCount})`)
  } else {
    ElMessage.success('所有测试都成功了！')
  }
}

// 直接检查后端状态
const checkBackendDirectly = async () => {
  try {
    console.log('直接检查后端...')
    
    // 尝试直接访问后端
    const response = await fetch('http://localhost:8080/api/test/hello', {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      },
      mode: 'cors' // 需要后端支持CORS
    })
    
    if (response.ok) {
      const data = await response.text()
      directCheckResult.value = `✅ 后端服务正常\nURL: http://localhost:8080\n状态码: ${response.status}\n响应: ${data}`
    } else {
      directCheckResult.value = `❌ 后端响应异常\n状态码: ${response.status}\n状态文本: ${response.statusText}`
    }
  } catch (error) {
    directCheckResult.value = `❌ 无法连接到后端\n错误: ${error.message}\n\n可能的原因:\n1. 后端服务未启动\n2. 端口8080被占用\n3. 防火墙阻止了连接`
  }
}

// 页面加载时自动运行一次测试
setTimeout(() => {
  testProxy()
}, 1000)
</script>

<style scoped>
pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  line-height: 1.4;
}
</style>