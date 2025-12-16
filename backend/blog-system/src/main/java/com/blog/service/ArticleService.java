package com.blog.service;

import com.blog.entity.Article;
import java.util.List;

public interface ArticleService {
    
    /**
     * 获取文章列表
     */
    List<Article> getArticles(int page, int size);
    
    /**
     * 获取文章详情
     */
    Article getArticleById(Integer id);
    
    /**
     * 创建文章
     */
    boolean createArticle(Article article);
    
    /**
     * 更新文章
     */
    boolean updateArticle(Article article);
    
    /**
     * 删除文章
     */
    boolean deleteArticle(Integer id);
    
    /**
     * 增加阅读量
     */
    void incrementViewCount(Integer id);
}
