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

    /**
     * 创建草稿
     */
    boolean createDraft(Article article);

    /**
     * 发布草稿
     */
    boolean publishDraft(Integer articleId);

    /**
     * 获取用户的草稿列表
     */
    List<Article> getUserDrafts(Integer userId, int page, int size);

    /**
     * 获取用户的发布文章列表
     */
    List<Article> getUserPublishedArticles(Integer userId, int page, int size);

    /**
     * 根据ID获取文章（不限制状态）
     */
    Article getArticleByIdWithoutStatus(Integer id);

    /**
     * 根据ID和用户ID获取文章（用于权限验证）
     */
    Article getArticleByIdAndUserId(Integer id, Integer userId);


    /**
     * 根据用户ID和状态查询文章（分页）
     * 
     * @param userId 用户ID
     * @param status 文章状态（0=草稿，1=发布，2=删除）
     * @param page   页码
     * @param size   每页数量
     * @return 文章列表
     */
    List<Article> getArticlesByUserIdAndStatus(Integer userId, Integer status, Integer page, Integer size);

    /**
     * 统计用户特定状态的文章数量
     * 
     * @param userId 用户ID
     * @param status 文章状态
     * @return 文章数量
     */
    Long countArticlesByUserIdAndStatus(Integer userId, Integer status);

    /**
     * 根据用户ID查询所有文章（包括各种状态，用于管理员）
     * 
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 文章列表
     */
    List<Article> getArticlesByUserId(Integer userId, Integer page, Integer size);

    /**
     * 统计用户所有文章数量
     * 
     * @param userId 用户ID
     * @return 文章数量
     */
    Long countArticlesByUserId(Integer userId);

    /**
     * 点赞文章
     */
    boolean likeArticle(Integer articleId, Integer userId);
    
    /**
     * 取消点赞文章
     */
    boolean unlikeArticle(Integer articleId, Integer userId);
    
    /**
     * 切换点赞状态（点赞/取消点赞）
     */
    Map<String, Object> toggleLike(Integer articleId, Integer userId);
    
    /**
     * 检查用户是否已点赞文章
     */
    boolean isArticleLikedByUser(Integer articleId, Integer userId);
    
    /**
     * 获取文章点赞数
     */
    int getArticleLikeCount(Integer articleId);
    
    /**
     * 获取用户点赞的文章列表
     */
    List<Article> getLikedArticles(Integer userId, Integer page, Integer size);
}
