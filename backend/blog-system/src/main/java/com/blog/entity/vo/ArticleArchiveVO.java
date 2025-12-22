package com.blog.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 归档文章简略信息
 */
@Data
public class ArticleArchiveVO {
    /**
     * 文章ID
     */
    private Integer id;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 文章创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 文章分类名称
     */
    private String categoryName;
    
    /**
     * 文章标签（逗号分隔）
     */
    private String tags;
}