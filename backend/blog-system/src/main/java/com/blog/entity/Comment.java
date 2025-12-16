package com.blog.entity;

import lombok.Data;

/**
 * 评论实体类
 */
@Data
public class Comment {
    /**
     * 评论ID
     */
    private Integer id;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 文章ID（关联article表）
     */
    private Integer articleId;
    
    /**
     * 用户ID（关联user表）
     */
    private Integer userId;
    
    /**
     * 父评论ID（0表示顶级评论）
     */
    private Integer parentId = 0;
    
    /**
     * 回复的用户ID
     */
    private Integer replyUserId;
    
    /**
     * 点赞数
     */
    private Integer likeCount = 0;
    
    /**
     * 评论状态：0-删除，1-正常
     */
    private Integer status = 1;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 关联字段：用户名
     */
    private String username;
    
    /**
     * 关联字段：用户头像
     */
    private String userAvatar;
    
    /**
     * 关联字段：回复的用户名
     */
    private String replyUsername;
}