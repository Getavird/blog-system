package com.blog.entity.dto;

import lombok.Data;

/**
 * 文章查询DTO
 */
@Data
public class ArticleQueryDTO {
    /**
     * 标题关键词
     */
    private String title;
    
    /**
     * 文章状态：0-草稿，1-已发布
     */
    private Integer status;
    
    /**
     * 分类ID
     */
    private Integer categoryId;
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
    
    /**
     * 用户ID（当前登录用户）
     */
    private Integer userId;
}