添加 bio 字段
INSERT INTO `user` (username, password, email, role, bio) VALUES  -- 添加 bio
('admin', MD5('admin123'), 'admin@blog.com', 1, '系统管理员'),
('testuser', MD5('test123'), 'test@blog.com', 0, '测试用户'),
('zhangsan', MD5('zhangsan123'), 'zhangsan@blog.com', 0, '普通用户张三');

-- 分类数据添加 color 字段
INSERT INTO `category` (name, slug, description, icon, color) VALUES  -- 添加 color
('技术分享', 'technology', '编程技术相关文章', 'code', '#409eff'),
('生活随笔', 'life', '日常生活分享', 'coffee', '#67c23a'),
('学习笔记', 'study', '学习心得记录', 'book', '#5cdbd3');

-- 文章数据添加 tags 字段
INSERT INTO `article` (title, slug, content, summary, tags, user_id, category_id) VALUES  -- 添加 tags
('Spring Boot入门教程', 'spring-boot-tutorial', 'Spring Boot 是 Pivotal 团队在 2013 年推出的开源框架，它基于 Spring 4.0 设计，核心目标是把“配置地狱”变成“开箱即用”。传统 Spring 项目需要写大量 XML 或 JavaConfig，还要考虑依赖版本、容器启动、外部资源绑定等细节，而 Spring Boot 通过“约定优于配置”的理念，把这些模板化的工作封装进一套自动配置（Auto Configuration）机制：只要在 pom.xml 或 build.gradle 中引入 spring-boot-starter-web、spring-boot-starter-data-jpa 等“启动器”，框架就能根据类路径上的 jar 包、Bean 定义及属性文件，智能地帮你注册 DispatcherServlet、DataSource、JpaVendorAdapter 等组件，并给出合理的默认参数。
为了进一步简化部署，Spring Boot 内嵌了 Tomcat、Jetty 或 Undertow，应用可直接以 java -jar 的方式启动，无需再打 WAR 包放到外部容器。开发阶段，Spring Boot DevTools 提供热部署、LiveReload、自动重启，保存代码即可看到效果；生产阶段，Actuator 模块暴露出 /health、/metrics、/loggers 等端点，结合 Micrometer 可以把 JVM、线程池、数据库连接池指标无缝输出到 Prometheus、Grafana，实现可观测性。
配置外部化是 Spring Boot 的另一大特色。它采用“分层属性源”机制：默认读取 jar 内的 application.properties，再通过命令行参数、环境变量、配置中心（Spring Cloud Config、Consul、Nacos）层层覆盖，真正做到“同一份制品，多环境运行”。结合 Profile，可以在 YAML 中一条线区分 dev、test、prod，避免传统项目“打包三次、改 IP 三次”的痛点。
在微服务时代，Spring Boot 与 Spring Cloud 深度集成，通过 starter 快速引入 Eureka、OpenFeign、Ribbon、Hystrix、Gateway、Sleuth 等组件，实现注册发现、客户端负载均衡、熔断限流、分布式链路追踪。得益于自动配置，这些能力往往只需加依赖、写注解即可拥有，大幅降低微服务门槛。同时，Spring Boot 对 Reactive 编程也提供了一流支持：spring-boot-starter-webflux 基于 Netty，通过少量线程处理海量并发，满足高吞吐、低延迟场景。
从 2.3 开始，Spring Boot 原生支持 GraalVM，借助 spring-native 可以把应用编译成毫秒级启动、几十兆内存的本地镜像，为 Serverless、Kubernetes 弹性伸缩提供极致体验。测试方面，@SpringBootTest、@WebMvcTest、@DataJpaTest 等切片注解，让单元测试、集成测试、Web 层测试都能快速拉起轻量级容器，配合 Testcontainers 还能在 JUnit 5 生命周期里自动启动真实的数据库、Kafka、Redis 容器，保证测试环境与生产一致。
可以说，Spring Boot 不是替代 Spring，而是把 Spring 全家桶的“散装积木”变成“整机出厂”，让开发者聚焦业务逻辑。无论你是传统单体、云原生微服务，还是边缘计算场景，Spring Boot 都能提供一致的编程模型和运维体验，这也是它短短几年就成为 Java 领域事实标准的核心原因', 'Spring Boot快速入门指南', 'Java,Spring Boot', 1, 1),
('Vue 3新特性介绍', 'vue3-new-features', 'Vue 3带来了...', 'Vue 3 Composition API详解', 'Vue,前端', 1, 1);

-- 用户关注关系表
CREATE TABLE IF NOT EXISTS `user_follow` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '关注关系ID',
  `follower_id` INT NOT NULL COMMENT '粉丝ID（关注者）',
  `following_id` INT NOT NULL COMMENT '被关注者ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-关注，0-取消关注',
  UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
  FOREIGN KEY (`follower_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`following_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='用户关注关系表';

-- 插入初始分类数据
INSERT INTO `category` (name, slug, description, icon, color, order_num) VALUES
('技术分享', 'technology', '编程技术相关文章', 'code', '#409eff', 1),
('生活随笔', 'life', '日常生活分享', 'coffee', '#67c23a', 2),
('学习笔记', 'study', '学习心得记录', 'book', '#5cdbd3', 3),
('工作心得', 'work', '工作经验总结', 'briefcase', '#ff9c6e', 4),
('读书笔记', 'reading', '读书感悟分享', 'book-open', '#ff85c0', 5)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 插入初始标签数据
INSERT INTO `tag` (name, slug, description, color) VALUES
('Java', 'java', 'Java编程语言', '#f44336'),
('Spring Boot', 'spring-boot', 'Spring Boot框架', '#4caf50'),
('Vue', 'vue', 'Vue.js前端框架', '#42b983'),
('MySQL', 'mysql', 'MySQL数据库', '#00758f'),
('Docker', 'docker', '容器化技术', '#2496ed')
ON DUPLICATE KEY UPDATE name=VALUES(name);