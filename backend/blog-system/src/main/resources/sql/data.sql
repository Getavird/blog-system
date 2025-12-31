-- data.sql 完整初始化数据文件

-- 1. 插入用户数据（注意：由于 user 表已存在，我们使用 INSERT IGNORE 避免重复）
INSERT IGNORE INTO `user` (username, password, email, role, bio, article_count, like_count, view_count) VALUES
('admin', MD5('admin123'), 'admin@blog.com', 1, '系统管理员', 5, 120, 1500),
('testuser', MD5('test123'), 'test@blog.com', 0, '测试用户', 3, 45, 600),
('zhangsan', MD5('zhangsan123'), 'zhangsan@blog.com', 0, '普通用户张三', 8, 200, 3200),
('lisi', MD5('lisi123'), 'lisi@blog.com', 0, '前端开发工程师', 12, 350, 5200),
('wangwu', MD5('wangwu123'), 'wangwu@blog.com', 0, '后端开发工程师', 15, 420, 6800);

-- 2. 插入分类数据
INSERT IGNORE INTO `category` (name, slug, description, icon, color, order_num) VALUES
('技术分享', 'technology', '编程技术相关文章', 'code', '#409eff', 1),
('生活随笔', 'life', '日常生活分享', 'coffee', '#67c23a', 2),
('学习笔记', 'study', '学习心得记录', 'book', '#5cdbd3', 3),
('工作心得', 'work', '工作经验总结', 'briefcase', '#ff9c6e', 4),
('读书笔记', 'reading', '读书感悟分享', 'book-open', '#ff85c0', 5);

-- 3. 插入标签数据
INSERT IGNORE INTO `tag` (name, slug, description, color) VALUES
('Java', 'java', 'Java编程语言', '#f44336'),
('Spring Boot', 'spring-boot', 'Spring Boot框架', '#4caf50'),
('Vue', 'vue', 'Vue.js前端框架', '#42b983'),
('MySQL', 'mysql', 'MySQL数据库', '#00758f'),
('Docker', 'docker', '容器化技术', '#2496ed'),
('Python', 'python', 'Python编程语言', '#3776ab'),
('JavaScript', 'javascript', 'JavaScript编程语言', '#f7df1e'),
('Redis', 'redis', 'Redis数据库', '#dc382d');

-- 4. 插入文章数据
INSERT IGNORE INTO `article` (title, slug, content, summary, tags, user_id, category_id, view_count, like_count, comment_count, status) VALUES
('Spring Boot入门教程', 'spring-boot-tutorial', 'Spring Boot 是 Pivotal 团队在 2013 年推出的开源框架...', 'Spring Boot快速入门指南', 'Java,Spring Boot', 1, 1, 1200, 85, 23, 1),
('Vue 3新特性介绍', 'vue3-new-features', 'Vue 3带来了Composition API、Teleport、Suspense等新特性...', 'Vue 3 Composition API详解', 'Vue,JavaScript', 1, 1, 850, 45, 12, 1),
('MySQL性能优化实践', 'mysql-performance', '通过索引优化、查询优化和配置调整提升MySQL性能...', 'MySQL性能优化技巧', 'MySQL', 3, 1, 650, 32, 8, 1),
('Docker容器化部署指南', 'docker-deployment', '使用Docker容器化部署Spring Boot应用...', 'Docker容器化实践', 'Docker,Spring Boot', 4, 1, 420, 28, 5, 1),
('Python数据分析入门', 'python-data-analysis', '使用Pandas和NumPy进行数据分析...', 'Python数据分析基础', 'Python', 5, 3, 380, 25, 4, 1);

-- 5. 插入文章标签关联数据
INSERT IGNORE INTO `article_tag` (article_id, tag_id) VALUES
(1, 1), (1, 2), -- Spring Boot入门教程 -> Java, Spring Boot
(2, 3), (2, 7), -- Vue 3新特性介绍 -> Vue, JavaScript
(3, 4),         -- MySQL性能优化实践 -> MySQL
(4, 2), (4, 5), -- Docker容器化部署指南 -> Spring Boot, Docker
(5, 6);         -- Python数据分析入门 -> Python

-- 6. 插入评论数据
INSERT IGNORE INTO `comment` (content, user_id, article_id, parent_id, like_count, status) VALUES
('写的很详细，对我帮助很大！', 2, 1, 0, 5, 1),
('期待后续更新！', 3, 1, 0, 3, 1),
('请问如何配置多数据源？', 4, 1, 0, 0, 1),
('Vue 3的Composition API确实好用', 3, 2, 0, 8, 1),
('MySQL索引优化部分可以再详细些', 5, 3, 0, 2, 1);

-- 7. 插入用户点赞数据（评论点赞）
INSERT IGNORE INTO `user_like` (user_id, comment_id) VALUES
(1, 1), (1, 4), (2, 4), (3, 1), (4, 4);

-- 8. 插入文章点赞数据
INSERT IGNORE INTO `article_like` (user_id, article_id) VALUES
(2, 1), (3, 1), (4, 1), (5, 1),
(2, 2), (3, 2), (4, 2),
(3, 3), (5, 3);

-- 9. 插入用户关注关系数据
INSERT IGNORE INTO `user_follow` (follower_id, following_id, status) VALUES
(2, 1, 1), -- testuser关注admin
(3, 1, 1), -- zhangsan关注admin
(4, 1, 1), -- lisi关注admin
(5, 1, 1), -- wangwu关注admin
(4, 3, 1), -- lisi关注zhangsan
(5, 3, 1); -- wangwu关注zhangsan

-- 10. 插入文件上传记录数据（示例）
INSERT IGNORE INTO `upload_file` (original_name, save_name, file_path, file_size, file_type, file_ext, upload_user_id, used, usage_type, usage_id, status) VALUES
('avatar.jpg', 'avatar_1_20231201.jpg', '/uploads/avatar/', 204800, 'image/jpeg', 'jpg', 1, 1, 'avatar', 1, 1),
('spring-boot-cover.png', 'cover_1_20231202.png', '/uploads/cover/', 512000, 'image/png', 'png', 1, 1, 'cover', 1, 1);

-- 11. 插入搜索记录数据（示例）
INSERT IGNORE INTO `search_record` (keyword, user_id, search_count) VALUES
('Spring Boot', 2, 3),
('Vue 3', 3, 2),
('MySQL', 4, 5),
('Docker', 5, 4);

-- 12. 更新用户统计信息（示例）
UPDATE `user` SET article_count = (
  SELECT COUNT(*) FROM `article` WHERE user_id = `user`.id
);

-- 13. 更新分类文章统计
UPDATE `category` SET article_count = (
  SELECT COUNT(*) FROM `article` WHERE category_id = `category`.id
);

-- 14. 更新标签文章统计
UPDATE `tag` SET article_count = (
  SELECT COUNT(*) FROM `article_tag` WHERE tag_id = `tag`.id
);

-- 15. 更新标签的阅读量和点赞量（示例）
UPDATE `tag` t SET 
  view_count = (
    SELECT IFNULL(SUM(a.view_count), 0) 
    FROM `article` a 
    JOIN `article_tag` at ON a.id = at.article_id 
    WHERE at.tag_id = t.id
  ),
  like_count = (
    SELECT IFNULL(SUM(a.like_count), 0) 
    FROM `article` a 
    JOIN `article_tag` at ON a.id = at.article_id 
    WHERE at.tag_id = t.id
  );