<template>

    <div class="test-api-page">
        <Header />

        <div class="container">
            <h1>API 连接测试</h1>

            <div class="test-cards">
                <!-- 后端状态测试 -->
                <el-card class="test-card">
                    <template #header>
                        <div class="card-header">
                            <span>后端连接测试</span>
                        </div>
                    </template>
                    <div class="card-content">
                        <p>测试后端服务是否正常运行</p>
                        <el-button @click="testBackendConnection" :loading="testing.backend">
                            测试连接
                        </el-button>
                        <div v-if="testResult.backend" class="test-result">
                            <p :class="testResult.backend.success ? 'success' : 'error'">
                                {{ testResult.backend.message }}
                            </p>
                        </div>
                    </div>
                </el-card>

                <!-- 登录测试 -->
                <el-card class="test-card">
                    <template #header>
                        <div class="card-header">
                            <span>用户登录测试</span>
                        </div>
                    </template>
                    <div class="card-content">
                        <el-form :model="loginForm" label-width="80px">
                            <el-form-item label="用户名">
                                <el-input v-model="loginForm.username" placeholder="admin" />
                            </el-form-item>
                            <el-form-item label="密码">
                                <el-input v-model="loginForm.password" type="password" placeholder="123456" />
                            </el-form-item>
                        </el-form>
                        <el-button @click="testLoginConnection" :loading="testing.login" type="primary">
                            测试登录
                        </el-button>
                        <div v-if="testResult.login" class="test-result">
                            <p :class="testResult.login.success ? 'success' : 'error'">
                                {{ testResult.login.message }}
                            </p>
                        </div>
                    </div>
                </el-card>

                <!-- 文章列表测试 -->
                <el-card class="test-card">
                    <template #header>
                        <div class="card-header">
                            <span>文章列表测试</span>
                        </div>
                    </template>
                    <div class="card-content">
                        <p>测试获取文章列表API</p>
                        <el-button @click="testArticlesConnection" :loading="testing.articles">
                            获取文章列表
                        </el-button>
                        <div v-if="testResult.articles" class="test-result">
                            <p :class="testResult.articles.success ? 'success' : 'error'">
                                {{ testResult.articles.message }}
                            </p>
                            <div v-if="testResult.articles.data" class="data-preview">
                                <p>获取到 {{ testResult.articles.data.length }} 篇文章</p>
                                <ul>
                                    <li v-for="article in testResult.articles.data.slice(0, 3)" :key="article.id">
                                        {{ article.title }}
                                    </li>
                                    <li v-if="testResult.articles.data.length > 3">...</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </el-card>

                <!-- 数据库测试 -->
                <el-card class="test-card">
                    <template #header>
                        <div class="card-header">
                            <span>数据库状态测试</span>
                        </div>
                    </template>
                    <div class="card-content">
                        <p>测试数据库连接状态</p>
                        <!-- 修改按钮名称和点击事件 -->
                        <el-button @click="testDbStatusConnection" :loading="testing.database">
                            测试数据库状态
                        </el-button>
                        <div v-if="testResult.database" class="test-result">
                            <p :class="testResult.database.success ? 'success' : 'error'">
                                {{ testResult.database.message }}
                            </p>
                            <!-- 显示数据库状态详情 -->
                            <div v-if="testResult.database.details" class="data-preview">
                                <pre>{{ JSON.stringify(testResult.database.details, null, 2) }}</pre>
                            </div>
                        </div>
                    </div>
                </el-card>
            </div>
        </div>

        <Footer />
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'
import { getBackendStatus, getDbStatus } from '@/api/test' 
import { login } from '@/api/user'
import { getArticles } from '@/api/article'


const testing = ref({
    backend: false,
    login: false,
    articles: false,
    database: false
})

const testResult = ref({
    backend: null,
    login: null,
    articles: null,
    database: null
})

const loginForm = ref({
    username: 'admin',
    password: '123456'
})

const testBackendConnection = async () => {
    testing.value.backend = true
    testResult.value.backend = null

    try {
        const result = await getBackendStatus()
        testResult.value.backend = {
            success: true,
            message: `✅ 后端服务正常: ${result.status || 'running'}`
        }
        ElMessage.success('后端服务正常')
    } catch (error) {
        testResult.value.backend = {
            success: false,
            message: `❌ 后端服务异常: ${error.message}`
        }
        ElMessage.error('后端连接失败')
    } finally {
        testing.value.backend = false
    }
}

const testLoginConnection = async () => {
    if (!loginForm.value.username || !loginForm.value.password) {
        ElMessage.warning('请输入用户名和密码')
        return
    }

    testing.value.login = true
    testResult.value.login = null

    try {
        const result = await login(loginForm.value)
        testResult.value.login = {
            success: true,
            message: `✅ 登录成功: ${result.username || '用户'}`
        }
        ElMessage.success('登录测试成功')
    } catch (error) {
        testResult.value.login = {
            success: false,
            message: `❌ 登录失败: ${error.message}`
        }
        ElMessage.error('登录测试失败')
    } finally {
        testing.value.login = false
    }
}

const testArticlesConnection = async () => {
    testing.value.articles = true
    testResult.value.articles = null

    try {
        // 注意：page参数可能需要调整，根据文档是从1开始
        const result = await getArticles({ page: 1, size: 5 })

        // result现在是data数组
        const articles = Array.isArray(result) ? result : []

        testResult.value.articles = {
            success: true,
            message: `✅ 文章列表获取成功，共${articles.length}篇文章`,
            data: articles
        }
        ElMessage.success('文章列表获取成功')
    } catch (error) {
        testResult.value.articles = {
            success: false,
            message: `❌ 文章列表获取失败: ${error.message}`
        }
        ElMessage.error('文章列表获取失败')
    } finally {
        testing.value.articles = false
    }
}

const testDbStatusConnection = async () => {
  testing.value.database = true
  testResult.value.database = null
  
  try {
    const result = await getDbStatus()
    testResult.value.database = {
      success: true,
      message: '✅ 数据库状态获取成功',
      details: result // 显示详细信息
    }
    ElMessage.success('数据库状态获取成功')
  } catch (error) {
    testResult.value.database = {
      success: false,
      message: `❌ 数据库状态获取失败: ${error.message}`
    }
    ElMessage.error('数据库状态获取失败')
  } finally {
    testing.value.database = false
  }
}
</script>

<style scoped>
.test-api-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
}

.data-preview pre {
  margin-top: 10px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.4;
  max-height: 200px;
  overflow-y: auto;
}

.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    flex: 1;
}

h1 {
    text-align: center;
    margin: 30px 0;
    color: #333;
}

.test-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 20px;
    margin-top: 30px;
}

.test-card {
    height: 100%;
}

.card-header {
    font-weight: bold;
    color: #333;
}

.card-content {
    min-height: 200px;
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.test-result {
    margin-top: 15px;
    padding: 10px;
    border-radius: 4px;
    background: #f5f7fa;
}

.test-result .success {
    color: #67c23a;
    font-weight: 500;
}

.test-result .error {
    color: #f56c6c;
    font-weight: 500;
}

.data-preview {
    margin-top: 10px;
    font-size: 14px;
    color: #666;
}

.data-preview ul {
    margin: 8px 0;
    padding-left: 20px;
}

.data-preview li {
    margin: 4px 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
</style>