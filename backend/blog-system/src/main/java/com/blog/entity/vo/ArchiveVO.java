package com.blog.entity.vo;

import lombok.Data;
import java.util.List;

/**
 * 归档分组数据
 */
@Data
public class ArchiveVO {
    /**
     * 归档年份
     */
    private String year;
    
    /**
     * 归档月份
     */
    private String month;
    
    /**
     * 文章列表
     */
    private List<ArticleArchiveVO> articles;
    
    /**
     * 文章数量
     */
    private Integer articleCount;
}
