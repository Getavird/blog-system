package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Article extends BaseEntity {
    /**
     * 文章ID
     */
    private Integer id;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 文章内容（HTML格式）
     */
    private String content;
    
    /**
     * 文章摘要
     */
    private String summary;
    
    /**
     * 封面图片URL
     */
    private String coverImage;
    
    /**
     * 文章状态：0-草稿，1-发布，2-删除
     */
    private Integer status = 1;
    
    /**
     * 阅读次数
     */
    private Integer viewCount = 0;
    
    /**
     * 点赞数
     */
    private Integer likeCount = 0;
    
    /**
     * 评论数
     */
    private Integer commentCount = 0;
    
    /**
     * 作者ID（关联user表）
     */
    private Integer userId;
    
    /**
     * 分类ID（关联category表）
     */
    private Integer categoryId;
    
    /**
     * 关联字段：作者用户名（查询时使用）
     */
    private String authorName;
    
    /**
     * 关联字段：作者头像
     */
    private String authorAvatar;
    
    /**
     * 关联字段：分类名称
     */
    private String categoryName;
    
    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop = 0;
    
    /**
     * 是否允许评论：0-否，1-是
     */
    private Integer allowComment = 1;
    
    /**
     * 文章标签，多个用逗号分隔
     */
    private String tags;

    public void setCreateTime(String format) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCreateTime'");
    }
}