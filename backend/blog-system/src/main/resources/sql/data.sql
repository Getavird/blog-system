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
('Spring Boot入门教程', 'spring-boot-tutorial', 'Spring Boot 是...', 'Spring Boot快速入门指南', 'Java,Spring Boot', 1, 1),
('Vue 3新特性介绍', 'vue3-new-features', 'Vue 3带来了...', 'Vue 3 Composition API详解', 'Vue,前端', 1, 1);