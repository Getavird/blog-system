-- 1. 插入测试数据
-- 用户
INSERT INTO `user` (username, password, email, role) VALUES
('admin', MD5('admin123'), 'admin@blog.com', 1),
('testuser', MD5('test123'), 'test@blog.com', 0),
('zhangsan', MD5('zhangsan123'), 'zhangsan@blog.com', 0);

-- 分类
INSERT INTO `category` (name, slug, description, icon) VALUES
('技术分享', 'technology', '编程技术相关文章', 'code'),
('生活随笔', 'life', '日常生活分享', 'coffee'),
('学习笔记', 'study', '学习心得记录', 'book');

-- 标签
INSERT INTO `tag` (name, slug) VALUES
('Java', 'java'),
('Spring Boot', 'spring-boot'),
('Vue', 'vue'),
('数据库', 'database'),
('前端', 'frontend'),
('后端', 'backend');

-- 文章
INSERT INTO `article` (title, slug, content, summary, user_id, category_id, view_count, like_count) VALUES
('Spring Boot入门教程', 'spring-boot-tutorial', 'Spring Boot 是...', 'Spring Boot快速入门指南', 1, 1, 156, 25),
('Vue 3新特性介绍', 'vue3-new-features', 'Vue 3带来了...', 'Vue 3 Composition API详解', 1, 1, 120, 18),
('MySQL索引优化', 'mysql-index-optimization', '索引是数据库...', 'MySQL索引原理和优化', 2, 1, 89, 12);

-- 文章标签关联
INSERT INTO `article_tag` (article_id, tag_id) VALUES
(1, 1), (1, 2), (1, 6),
(2, 3), (2, 5),
(3, 4), (3, 6);

-- 更新统计信息
UPDATE `user` SET article_count = (SELECT COUNT(*) FROM `article` WHERE user_id = `user`.id);
UPDATE `category` SET article_count = (SELECT COUNT(*) FROM `article` WHERE category_id = `category`.id);
UPDATE `tag` SET article_count = (SELECT COUNT(*) FROM `article_tag` WHERE tag_id = `tag`.id);