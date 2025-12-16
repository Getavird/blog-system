-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
  `avatar` VARCHAR(200) DEFAULT 'default_avatar.png' COMMENT '头像',
  `role` TINYINT DEFAULT 0 COMMENT '角色：0普通用户，1管理员',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
  `description` VARCHAR(200) COMMENT '个人简介',
  `article_count` INT DEFAULT 0 COMMENT '文章数',
  `like_count` INT DEFAULT 0 COMMENT '获赞数',
  `view_count` INT DEFAULT 0 COMMENT '浏览数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_username (username),
  INDEX idx_email (email),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='用户表';

-- 分类表
CREATE TABLE IF NOT EXISTS `category` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `slug` VARCHAR(50) UNIQUE COMMENT '分类别名（URL友好）',
  `description` VARCHAR(200) COMMENT '分类描述',
  `icon` VARCHAR(50) COMMENT '图标',
  `order_num` INT DEFAULT 0 COMMENT '排序号',
  `article_count` INT DEFAULT 0 COMMENT '文章数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_name (name),
  INDEX idx_order_num (order_num)
) ENGINE=InnoDB COMMENT='分类表';

-- 标签表
CREATE TABLE IF NOT EXISTS `tag` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
  `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `slug` VARCHAR(50) UNIQUE COMMENT '标签别名',
  `description` VARCHAR(200) COMMENT '标签描述',
  `article_count` INT DEFAULT 0 COMMENT '文章数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_name (name)
) ENGINE=InnoDB COMMENT='标签表';

-- 文章表
CREATE TABLE IF NOT EXISTS `article` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '文章ID',
  `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
  `slug` VARCHAR(200) UNIQUE COMMENT '文章别名',
  `content` LONGTEXT NOT NULL COMMENT '文章内容',
  `content_type` TINYINT DEFAULT 0 COMMENT '内容类型：0Markdown，1富文本',
  `summary` VARCHAR(500) COMMENT '文章摘要',
  `cover_image` VARCHAR(200) COMMENT '封面图片',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0草稿，1发布，2私密',
  `view_count` INT DEFAULT 0 COMMENT '阅读数',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT DEFAULT 0 COMMENT '评论数',
  `allow_comment` TINYINT DEFAULT 1 COMMENT '允许评论：0否，1是',
  `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶：0否，1是',
  `user_id` INT NOT NULL COMMENT '作者ID',
  `category_id` INT COMMENT '分类ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `publish_time` DATETIME COMMENT '发布时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`category_id`) REFERENCES `category`(`id`) ON DELETE SET NULL,
  INDEX idx_user_id (user_id),
  INDEX idx_category_id (category_id),
  INDEX idx_status (status),
  INDEX idx_is_top (is_top),
  INDEX idx_publish_time (publish_time),
  FULLTEXT INDEX idx_fulltext_title_content (title, content)
) ENGINE=InnoDB COMMENT='文章表';

-- 文章标签关联表
CREATE TABLE IF NOT EXISTS `article_tag` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
  `article_id` INT NOT NULL COMMENT '文章ID',
  `tag_id` INT NOT NULL COMMENT '标签ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`article_id`) REFERENCES `article`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`tag_id`) REFERENCES `tag`(`id`) ON DELETE CASCADE,
  UNIQUE KEY uk_article_tag (article_id, tag_id)
) ENGINE=InnoDB COMMENT='文章标签关联表';

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `article_id` INT NOT NULL COMMENT '文章ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `parent_id` INT DEFAULT 0 COMMENT '父评论ID',
  `reply_to` INT COMMENT '回复的用户ID',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0待审核，1正常，2删除',
  `ip_address` VARCHAR(45) COMMENT 'IP地址',
  `user_agent` VARCHAR(500) COMMENT '用户代理',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`article_id`) REFERENCES `article`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`reply_to`) REFERENCES `user`(`id`) ON DELETE SET NULL,
  INDEX idx_article_id (article_id),
  INDEX idx_user_id (user_id),
  INDEX idx_parent_id (parent_id),
  INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='评论表';

-- 点赞表
CREATE TABLE IF NOT EXISTS `like_record` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
  `type` TINYINT NOT NULL COMMENT '类型：1文章，2评论',
  `target_id` INT NOT NULL COMMENT '目标ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  UNIQUE KEY uk_type_target_user (type, target_id, user_id),
  INDEX idx_type_target (type, target_id)
) ENGINE=InnoDB COMMENT='点赞记录表';