package com.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签实体类
 */
@Data
public class Tag {
    /**
     * 标签ID
     */
    private Integer id;
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 标签别名（URL友好）
     */
    private String slug;
    
    /**
     * 标签描述
     */
    private String description;
    
    /**
     * 文章数量
     */
    private Integer articleCount = 0;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 标签颜色
     */
    private String color = "#409eff";
    
    /**
     * 标签图标
     */
    private String icon;
}