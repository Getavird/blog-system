package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 文章点赞实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleLike extends BaseEntity {
    /**
     * 点赞ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 文章ID
     */
    private Integer articleId;

    /**
     * 点赞时间
     */
    private LocalDateTime createTime;

    /**
     * 关联字段：用户名
     */
    private String username;

    /**
     * 关联字段：用户头像
     */
    private String avatar;

    /**
     * 是否已点赞（查询时使用，非数据库字段）
     */
    private Boolean isLiked = false;

    /**
     * 是否已关注作者（查询时使用，非数据库字段）
     */
    private Boolean isFollowing = false;

    /**
     * 作者统计信息（查询时使用，非数据库字段）
     */
    private Map<String, Object> authorStats;
}