package com.blog.service;

import com.blog.entity.Article;
import java.util.List;
import java.util.Map;

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

    // 在 ArticleService.java 中添加以下方法：

    /**
     * 获取热门文章（按浏览量排序）
     */
    List<Article> getHotArticles(int limit);

    /**
     * 获取最新文章（按创建时间排序）
     */
    List<Article> getLatestArticles(int limit);

    /**
     * 根据分类ID获取文章
     */
    List<Article> getArticlesByCategory(Integer categoryId, int page, int size);

    /**
     * 根据标签名称获取文章
     */
    List<Article> getArticlesByTag(String tagName, int page, int size);

    /**
     * 获取文章总数
     */
    int getArticleCount();

    /**
     * 获取总浏览量
     */
    int getTotalViewCount();

    /**
     * 获取分类文章统计
     */
    List<Map<String, Object>> getCategoryStats();

    /**
     * 获取热门标签统计
     */
    List<Map<String, Object>> getHotTags(int limit);
}
