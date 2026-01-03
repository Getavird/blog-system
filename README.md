# 📝 Blog System - 个人博客系统

一个基于 Spring Boot 3 和 Vue 3 构建的全栈博客系统，提供完整的文章管理、用户认证、评论互动和富文本编辑功能。

## ✨ 核心特性

- 🔐 **基于会话的用户认证系统** - 注册、登录、权限管理
- 📄 **文章管理** - 完整的 CRUD 操作，支持草稿与发布工作流
- 🖼️ **富文本编辑器** - 集成 WangEditor，支持图片上传与处理
- 💬 **评论系统** - 文章评论与互动功能
- 🔍 **搜索功能** - 全文搜索文章、用户和标签
- 🏷️ **分类与标签** - 灵活的文章组织方式
- 👤 **用户中心** - 个人资料管理、头像上传、密码修改
- 👥 **用户关系** - 关注/取消关注其他用户
- 📊 **数据统计** - 文章数、阅读数、点赞数、粉丝数等
- 📱 **响应式设计** - 基于 Element Plus 的现代化 UI

## 🛠️ 技术栈

### 后端
- **框架**: Spring Boot 3.5.8
- **Java 版本**: JDK 21
- **持久层**: MyBatis 3.0.5
- **数据库**: MySQL 8.0+
- **模板引擎**: Thymeleaf (用于部分传统Servlet集成)
- **其他**: Lombok, Jakarta Servlet API, Jsoup (HTML解析)

### 前端
- **框架**: Vue 3.4.0 (Composition API)
- **构建工具**: Vite 5.4.21
- **状态管理**: Pinia 2.1.7
- **路由**: Vue Router 4.2.5
- **HTTP 客户端**: Axios 1.6.2
- **UI 组件库**: Element Plus 2.4.1
- **富文本编辑器**: WangEditor 5.1.23
- **图标库**: @element-plus/icons-vue

## 📂 项目结构
blog-system/

├── backend/blog-system/ # Spring Boot 后端

│ ├── src/main/java/com/blog/ # Java 源码

│ │ ├── common/ # 通用组件

│ │ ├── config/ # 配置类

│ │ ├── controller/ # 控制器层

│ │ ├── dao/ # 数据访问层接口

│ │ ├── entity/ # 实体类

│ │ │ └── vo/ # 视图对象

│ │ ├── filter/ # 过滤器

│ │ ├── interceptor/ # 拦截器

│ │ ├── service/ # 业务逻辑层

│ │ │ └── impl/ # 业务实现类

│ │ ├── servlet/ # 传统Servlet

│ │ └── utils/ # 工具类

│ ├── src/main/resources/ # 资源文件

│ │ ├── mapper/ # MyBatis XML映射文件

│ │ ├── sql/ # SQL脚本

│ │ │ ├── schema.sql # 数据库建表脚本

│ │ │ └── data.sql # 初始化数据脚本

│ │ ├── static/ # 静态资源

│ │ └── application.properties # 应用配置

│ └── pom.xml # Maven依赖配置

│

└── frontend/blog-frontend/ # Vue 3 前端

├── src/

│ ├── api/ # API接口封装

│ ├── components/ # 可复用组件

│ │ ├── article/ # 文章相关组件

│ │ ├── common/ # 通用组件

│ │ └── layout/ # 布局组件

│ ├── router/ # 路由配置

│ ├── stores/ # Pinia状态管理

│ ├── utils/ # 工具函数

│ └── views/ # 页面视图

│ └── admin/ # 管理后台页面

├── index.html # HTML入口

├── package.json # NPM依赖配置

└── vite.config.js # Vite构建配置

## 🚀 快速开始

### 前置要求
- JDK 21+
- Node.js 18+
- MySQL 8.0+
- Maven 3.6+

### 数据库配置

1. **创建数据库** (可选，系统可自动创建)
   ```sql
   CREATE DATABASE IF NOT EXISTS blog_system 
   CHARACTER SET utf8mb4 
   COLLATE utf8mb4_unicode_ci;
修改配置文件

编辑 backend/blog-system/src/main/resources/application.properties：

   ```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/blog_system?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=your_password
   ```
# 自动执行SQL脚本
   ```properties
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:sql/schema.sql
spring.sql.init.data-locations=classpath:sql/data.sql
   ```
后端启动
   ```bash
# 进入后端目录
cd backend/blog-system

# 方式1：使用Maven Wrapper运行
./mvnw spring-boot:run

# 方式2：先打包再运行
./mvnw clean package
java -jar target/blog-system-0.0.1-SNAPSHOT.jar
   ```
后端服务将在 http://localhost:8080 启动
前端启动
   ```
bash
# 进入前端目录
cd frontend/blog-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 生产构建
npm run build
   ```
前端应用将在 http://localhost:3000 启动
📖 主要功能模块
用户认证模块
用户注册、登录、注销

基于Session的认证机制

密码加密存储（BCrypt）

全局认证拦截器

文章管理模块
文章的创建、编辑、删除、发布

草稿箱功能

文章分类和标签管理

文章搜索（标题、内容、标签）

文章点赞、收藏、评论

用户中心模块
个人资料编辑

头像上传与管理

密码修改

我的文章管理

关注/粉丝管理

评论系统
多级评论回复

评论点赞

评论管理

文件上传模块
头像上传

文章封面图上传

富文本编辑器图片上传

文件类型验证和大小限制

🔧 开发说明
API 接口规范
后端API遵循RESTful设计原则，响应格式统一：

   ```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
   ```
跨域配置
开发环境已配置CORS，允许前端开发服务器访问：

   ```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
   ```
路由守卫
前端路由包含认证守卫，保护需要登录的页面：

   ```javascript
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const requiresAuth = to.meta.requiresAuth || false;
  
  if (requiresAuth && !authStore.isLoggedIn) {
    next('/?showLogin=true');
  } else {
    next();
  }
});
   ```
📦 生产部署
后端部署
打包应用：

   ```bash
cd backend/blog-system
./mvnw clean package -DskipTests
   ```
运行应用：

   ```bash
java -jar target/blog-system-0.0.1-SNAPSHOT.jar
   ```
使用PM2管理进程（推荐）：
   ```bash
npm install -g pm2
pm2 start java --name "blog-system" -- -jar blog-system-0.0.1-SNAPSHOT.jar
   ```

前端部署
构建生产版本：

   ```bash
cd frontend/blog-frontend
npm run build
   ```
配置Nginx：

   ```nginx
server {
    listen 80;
    server_name yourdomain.com;
    
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /uploads {
        alias /path/to/backend/uploads;
    }
}
   ```
🔍 常见问题
Q: 数据库表无法自动创建
A: 确保配置文件中 spring.sql.init.mode=always，并且数据库用户有创建表的权限。

Q: 前端无法连接到后端API
A: 检查CORS配置，确保前端地址已添加到允许列表中。

Q: 上传文件失败
A: 检查上传目录权限，确保应用有读写权限。

Q: 头像显示不正确
A: 检查头像URL处理逻辑，确保路径正确拼接。

-----Start by 2025.12.15

-----Still a WIP. I’ll keep refining it if time ever decides to spare me.

