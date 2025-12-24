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
<<<<<<< Updated upstream
}
=======

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

    /**
     * 获取我的文章列表
     */
    List<Article> getMyArticles(Integer userId, String title, Integer status,
                                Integer categoryId, int page, int size);

    /**
     * 统计我的文章数量
     */
    int countMyArticles(Integer userId, String title, Integer status, Integer categoryId);

    /**
     * 批量删除我的文章
     */
    boolean batchDeleteMyArticles(List<Integer> ids, Integer userId);

    /**
     * 批量更新文章状态
     */
    boolean batchUpdateArticleStatus(List<Integer> ids, Integer status, Integer userId);

    /**
     * 获取用户的文章统计数据
     */
    Map<String, Object> getUserArticleStats(Integer userId);

    /**
     * 切换文章点赞状态（点赞/取消点赞）
     */
    boolean toggleArticleLike(Integer articleId, Integer userId);

    /**
     * 检查用户是否点赞了文章
     */
    boolean isArticleLiked(Integer articleId, Integer userId);

    /**
     * 获取文章的点赞统计
     */
    Map<String, Object> getArticleLikeStats(Integer articleId);

    /**
     * 获取文章详情（包含用户统计信息）
     */
    Article getArticleDetail(Integer id, Integer currentUserId);

      /**
     * 创建文章（带标签）
     */
    boolean createArticleWithTags(Article article, List<String> tagNames);
    
    /**
     * 更新文章（带标签）
     */
    boolean updateArticleWithTags(Article article, List<String> tagNames);
    
    /**
     * 获取文章详情（带标签列表）
     */
    Article getArticleWithTags(Integer id, Integer currentUserId);
}
>>>>>>> Stashed changes
