package com.blog.entity;

import lombok.Data;

/**
 * 分类实体类
 */
@Data
public class Category {
    /**
     * 分类ID
     */
    private Integer id;
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 排序号（小的在前）
     */
    private Integer orderNum = 0;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 关联字段：文章数量
     */
    private Integer articleCount;
    
    /**
     * 分类图标
     */
    private String icon;
    
    /**
     * 分类颜色
     */
    private String color = "#409eff";
}