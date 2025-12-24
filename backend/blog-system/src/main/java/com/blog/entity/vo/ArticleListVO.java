package com.blog.entity.vo;

import lombok.Data;
import com.blog.entity.Article;

import java.util.List;

/**
 * 文章列表返回VO
 */
@Data
public class ArticleListVO {
    /**
     * 文章列表
     */
    private List<Article> articles;
    
    /**
     * 当前页码
     */
    private Integer page;
    
    /**
     * 每页大小
     */
    private Integer size;
    
    /**
     * 总条数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Integer pages;
}
