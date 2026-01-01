-- 1. 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `blog_system` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 2. 使用这个数据库
USE `blog_system`;

-- 3. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
  `avatar` VARCHAR(200) DEFAULT 'default_avatar.png' COMMENT '头像',
  `role` TINYINT DEFAULT 0 COMMENT '角色：0普通用户，1管理员',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
  `bio` VARCHAR(200) COMMENT '个人简介',
  `article_count` INT DEFAULT 0 COMMENT '文章数',
  `like_count` INT DEFAULT 0 COMMENT '获赞数',
  `view_count` INT DEFAULT 0 COMMENT '浏览数',
  `last_login_time` DATETIME COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(45) COMMENT '最后登录IP',
  `last_active_time` DATETIME COMMENT '最后活动时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 4. 分类表
CREATE TABLE IF NOT EXISTS `category` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `slug` VARCHAR(50) UNIQUE COMMENT '分类别名（URL友好）',
  `description` VARCHAR(200) COMMENT '分类描述',
  `icon` VARCHAR(50) COMMENT '图标',
  `color` VARCHAR(20) DEFAULT '#409eff' COMMENT '分类颜色',
  `order_num` INT DEFAULT 0 COMMENT '排序号',
  `article_count` INT DEFAULT 0 COMMENT '文章数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 5. 标签表
CREATE TABLE IF NOT EXISTS `tag` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
  `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
  `slug` VARCHAR(50) UNIQUE COMMENT '标签别名',
  `description` VARCHAR(200) COMMENT '标签描述',
  `color` VARCHAR(20) DEFAULT '#409eff' COMMENT '标签颜色',
  `icon` VARCHAR(50) COMMENT '图标',
  `article_count` INT DEFAULT 0 COMMENT '文章数量',
  `view_count` INT DEFAULT 0 COMMENT '总阅读量',
  `like_count` INT DEFAULT 0 COMMENT '总点赞数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 6. 文章表 - 添加tags字段
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
  `tags` VARCHAR(255) COMMENT '文章标签，多个用逗号分隔',
  `user_id` INT NOT NULL COMMENT '作者ID',
  `category_id` INT COMMENT '分类ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `publish_time` DATETIME COMMENT '发布时间',
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`category_id`) REFERENCES `category`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 7. 文章标签关联表
CREATE TABLE IF NOT EXISTS `article_tag` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `article_id` INT NOT NULL COMMENT '文章ID',
  `tag_id` INT NOT NULL COMMENT '标签ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_tag_id` (`tag_id`),
  FOREIGN KEY (`article_id`) REFERENCES `article`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`tag_id`) REFERENCES `tag`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

-- 8. 评论表
CREATE TABLE IF NOT EXISTS `comment` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `user_id` INT NOT NULL COMMENT '评论用户ID',
  `article_id` INT NOT NULL COMMENT '文章ID',
  `parent_id` INT DEFAULT 0 COMMENT '父评论ID（0表示顶级评论）',
  `reply_user_id` INT NULL COMMENT '回复的用户ID',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0删除，1正常',
  `ip_address` VARCHAR(100) NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) NULL COMMENT 'User-Agent',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_article_id` (`article_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_reply_user_id` (`reply_user_id`),
  KEY `idx_create_time` (`create_time`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`article_id`) REFERENCES `article`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`reply_user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 9. 用户点赞评论表
CREATE TABLE IF NOT EXISTS `user_like` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `comment_id` INT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_comment` (`user_id`, `comment_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_comment_id` (`comment_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`comment_id`) REFERENCES `comment`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户点赞评论表';

-- 10. 文件上传记录表
CREATE TABLE IF NOT EXISTS `upload_file` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID',
  `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `save_name` VARCHAR(255) NOT NULL COMMENT '保存文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
  `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
  `file_type` VARCHAR(100) COMMENT '文件类型',
  `file_ext` VARCHAR(50) COMMENT '文件扩展名',
  `upload_user_id` INT COMMENT '上传用户ID',
  `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `used` TINYINT DEFAULT 0 COMMENT '0未使用，1已使用',
  `usage_type` VARCHAR(50) COMMENT '使用类型：avatar头像, cover封面, article文章',
  `usage_id` INT COMMENT '关联ID（如文章ID）',
  `status` TINYINT DEFAULT 1 COMMENT '1正常，0删除',
  KEY `idx_upload_user_id` (`upload_user_id`),
  KEY `idx_usage_type_id` (`usage_type`, `usage_id`),
  KEY `idx_status` (`status`),
  KEY `idx_upload_time` (`upload_time`),
  FOREIGN KEY (`upload_user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件上传记录表';

-- 11. 搜索记录表（可选，用于统计热门搜索）
CREATE TABLE IF NOT EXISTS `search_record` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `keyword` VARCHAR(100) NOT NULL,
  `user_id` INT,
  `search_count` INT DEFAULT 1,
  `last_search_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_keyword` (`keyword`),
  KEY `idx_user_time` (`user_id`, `last_search_time`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索记录表';

-- 12. 文章点赞表
CREATE TABLE IF NOT EXISTS `article_like` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `user_id` INT NOT NULL COMMENT '用户ID',
  `article_id` INT NOT NULL COMMENT '文章ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_article_id` (`article_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`article_id`) REFERENCES `article`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章点赞表';

-- 13. 用户关注关系表
CREATE TABLE IF NOT EXISTS `user_follow` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `follower_id` INT NOT NULL COMMENT '关注者ID',
  `following_id` INT NOT NULL COMMENT '被关注者ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-取消关注，1-已关注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
  KEY `idx_follower_id` (`follower_id`),
  KEY `idx_following_id` (`following_id`),
  KEY `idx_status` (`status`),
  FOREIGN KEY (`follower_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`following_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';