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