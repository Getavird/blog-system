

-- 用户表
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
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='用户表';

-- 分类表
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
) ENGINE=InnoDB COMMENT='分类表';

-- 文章表 - 添加tags字段
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
  `tags` VARCHAR(255) COMMENT '文章标签，多个用逗号分隔',  -- 添加这行
  `user_id` INT NOT NULL COMMENT '作者ID',
  `category_id` INT COMMENT '分类ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `publish_time` DATETIME COMMENT '发布时间'
) ENGINE=InnoDB COMMENT='文章表';