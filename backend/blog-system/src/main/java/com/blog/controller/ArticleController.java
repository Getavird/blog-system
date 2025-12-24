package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.entity.dto.ArticleDTO;
import com.blog.service.ArticleService;
import com.blog.utils.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    
    @Autowired
    private ArticleService articleService;
    
    /**
     * 获取文章列表
     */
    @GetMapping
    public Result<List<Article>> getArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        List<Article> articles = articleService.getArticles(page, size);
        return Result.success(articles);
    }
    
    /**
     * 获取文章详情（带标签）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getArticle(@PathVariable Integer id,
            HttpServletRequest request) {
        try {
            // 获取当前用户ID（可能为空）
            Integer currentUserId = null;
            User currentUser = SessionUtil.getCurrentUser(request);
            if (currentUser != null) {
                currentUserId = currentUser.getId();
            }

            // 获取文章详情（包含标签）
            Article article = articleService.getArticleDetail(id, currentUserId);
            if (article == null) {
                return Result.notFound("文章不存在");
            }

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("article", article);
            data.put("tags", article.getTagList()); // 标签列表
            data.put("isLiked", article.getIsLiked()); // 是否已点赞
            data.put("isFollowing", article.getIsFollowing()); // 是否已关注

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取文章详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建文章（带标签）
     */
    @PostMapping
    public Result<Article> createArticle(@RequestBody ArticleDTO articleDTO,
            HttpServletRequest request) {
        try {
            // 检查登录
            User currentUser = SessionUtil.getCurrentUser(request);
            if (currentUser == null) {
                return Result.unauthorized("请先登录");
            }

            // 转换 DTO 为 Entity
            Article article = convertToArticle(articleDTO);
            article.setUserId(currentUser.getId());

            // 创建文章并处理标签
            boolean success = articleService.createArticleWithTags(
                    article, articleDTO.getTagNames());

            return success ? Result.created(article) : Result.error("创建失败");
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
<<<<<<< Updated upstream
        
        // 设置作者ID
        article.setUserId(currentUser.getId());
        
        boolean success = articleService.createArticle(article);
        return success ? Result.created(article) : Result.error("创建失败");
=======
>>>>>>> Stashed changes
    }
    
    /**
     * 更新文章（带标签）
     */
    @PutMapping("/{id}")
<<<<<<< Updated upstream
    public Result<Article> updateArticle(@PathVariable Integer id, 
                                         @RequestBody Article article,
                                         HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }
        
        // 检查权限：只有作者或管理员可以修改
        Article existingArticle = articleService.getArticleById(id);
        if (existingArticle == null) {
            return Result.notFound("文章不存在");
        }
        
        // 如果不是作者且不是管理员
        if (!existingArticle.getUserId().equals(currentUser.getId()) && currentUser.getRole() != 1) {
            return Result.forbidden("没有权限修改此文章");
        }
        
        article.setId(id);
        boolean success = articleService.updateArticle(article);
        return success ? Result.success(article) : Result.error("更新失败");
=======
    public Result<Article> updateArticle(@PathVariable Integer id,
            @RequestBody ArticleDTO articleDTO,
            HttpServletRequest request) {
        try {
            // 检查登录
            User currentUser = SessionUtil.getCurrentUser(request);
            if (currentUser == null) {
                return Result.unauthorized("请先登录");
            }

            // 检查权限：只有作者或管理员可以修改
            Article existingArticle = articleService.getArticleById(id);
            if (existingArticle == null) {
                return Result.notFound("文章不存在");
            }

            // 如果不是作者且不是管理员
            if (!existingArticle.getUserId().equals(currentUser.getId()) && currentUser.getRole() != 1) {
                return Result.forbidden("没有权限修改此文章");
            }

            // 转换 DTO 为 Entity
            Article article = convertToArticle(articleDTO);
            article.setId(id);
            article.setUserId(existingArticle.getUserId()); // 保持原作者ID

            // 更新文章并处理标签
            boolean success = articleService.updateArticleWithTags(
                    article, articleDTO.getTagNames());

            return success ? Result.success(article) : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
>>>>>>> Stashed changes
    }
    
    /**
     * 删除文章（需要登录，且是作者或管理员）
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteArticle(@PathVariable Integer id, HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }
        
        // 检查权限：只有作者或管理员可以删除
        Article existingArticle = articleService.getArticleById(id);
        if (existingArticle == null) {
            return Result.notFound("文章不存在");
        }
        
        // 如果不是作者且不是管理员
        if (!existingArticle.getUserId().equals(currentUser.getId()) && currentUser.getRole() != 1) {
            return Result.forbidden("没有权限删除此文章");
        }
        
        boolean success = articleService.deleteArticle(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
<<<<<<< Updated upstream
=======

    /**
     * 获取热门文章（按浏览量排序）
     * GET /api/articles/hot?limit=10
     */
    @GetMapping("/hot")
    public Result<List<Article>> getHotArticles(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<Article> articles = articleService.getHotArticles(limit);
        return Result.success(articles);
    }

    /**
     * 获取最新文章（按创建时间排序）
     * GET /api/articles/latest?limit=10
     */
    @GetMapping("/latest")
    public Result<List<Article>> getLatestArticles(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<Article> articles = articleService.getLatestArticles(limit);
        return Result.success(articles);
    }

    /**
     * 获取分类统计（每个分类有多少文章）
     * GET /api/articles/category-stats
     */
    @GetMapping("/category-stats")
    public Result<List<Map<String, Object>>> getCategoryStats() {
        List<Map<String, Object>> stats = articleService.getCategoryStats();
        return Result.success(stats);
    }

    /**
     * 获取热门标签统计
     * GET /api/articles/hot-tags?limit=20
     */
    @GetMapping("/hot-tags")
    public Result<List<Map<String, Object>>> getHotTags(
            @RequestParam(defaultValue = "20") Integer limit) {
        List<Map<String, Object>> hotTags = articleService.getHotTags(limit);
        return Result.success(hotTags);
    }

    /**
     * 根据分类获取文章
     * GET /api/articles/category/{categoryId}?page=1&size=10
     */
    @GetMapping("/category/{categoryId}")
    public Result<List<Article>> getArticlesByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        List<Article> articles = articleService.getArticlesByCategory(categoryId, page, size);
        return Result.success(articles);
    }

    /**
     * 根据标签获取文章
     * GET /api/articles/tag/{tagName}?page=1&size=10
     */
    @GetMapping("/tag/{tagName}")
    public Result<List<Article>> getArticlesByTag(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        List<Article> articles = articleService.getArticlesByTag(tagName, page, size);
        return Result.success(articles);
    }

    /**
     * 获取我的文章列表（我的文章页面）
     * GET /api/articles/my?page=1&size=10&title=搜索词&status=0&categoryId=1
     */
    @GetMapping("/my")
    public Result<Map<String, Object>> getMyArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer categoryId,
            HttpServletRequest request) {

        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 获取我的文章列表
        List<Article> articles = articleService.getMyArticles(
                currentUser.getId(), title, status, categoryId, page, size);

        // 获取总条数
        int total = articleService.countMyArticles(
                currentUser.getId(), title, status, categoryId);

        // 组装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("articles", articles);
        result.put("page", page);
        result.put("size", size);
        result.put("total", total);
        result.put("pages", (int) Math.ceil((double) total / size));

        return Result.success(result);
    }

    /**
     * 获取我的文章统计数据
     * GET /api/articles/my/stats
     */
    @GetMapping("/my/stats")
    public Result<Map<String, Object>> getMyArticleStats(HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        Map<String, Object> stats = articleService.getUserArticleStats(currentUser.getId());
        return Result.success(stats);
    }

    /**
     * 批量删除我的文章
     * DELETE /api/articles/my/batch
     * Body: [1, 2, 3]
     */
    @DeleteMapping("/my/batch")
    public Result<String> batchDeleteMyArticles(@RequestBody List<Integer> ids,
            HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的文章");
        }

        boolean success = articleService.batchDeleteMyArticles(ids, currentUser.getId());
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }

    /**
     * 批量更新文章状态（草稿/发布）
     * PUT /api/articles/my/batch/status?status=1
     * Body: [1, 2, 3]
     */
    @PutMapping("/my/batch/status")
    public Result<String> batchUpdateArticleStatus(
            @RequestParam Integer status,
            @RequestBody List<Integer> ids,
            HttpServletRequest request) {

        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要更新的文章");
        }

        if (status != 0 && status != 1) {
            return Result.error("状态值不合法，0=草稿，1=发布");
        }

        boolean success = articleService.batchUpdateArticleStatus(ids, status, currentUser.getId());
        String message = status == 1 ? "批量发布成功" : "批量转为草稿成功";
        return success ? Result.success(message) : Result.error("批量更新失败");
    }

    /**
     * 切换文章点赞状态（点赞/取消点赞）
     * POST /api/articles/{id}/toggle-like
     */
    @PostMapping("/{id}/toggle-like")
    public Result<Map<String, Object>> toggleArticleLike(@PathVariable Integer id,
            HttpServletRequest request) {
        try {
            // 检查登录
            User currentUser = SessionUtil.getCurrentUser(request);
            if (currentUser == null) {
                return Result.unauthorized("请先登录");
            }

            // 切换点赞状态
            boolean isLiked = articleService.toggleArticleLike(id, currentUser.getId());

            // 获取更新后的文章信息
            Article article = articleService.getArticleById(id);

            Map<String, Object> result = new HashMap<>();
            result.put("isLiked", isLiked);
            result.put("likeCount", article != null ? article.getLikeCount() : 0);
            result.put("message", isLiked ? "点赞成功" : "取消点赞成功");

            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("操作失败");
        }
    }

    /**
     * 检查是否点赞文章
     * GET /api/articles/{id}/like-status
     */
    @GetMapping("/{id}/like-status")
    public Result<Map<String, Object>> checkLikeStatus(@PathVariable Integer id,
            HttpServletRequest request) {
        try {
            // 检查登录
            User currentUser = SessionUtil.getCurrentUser(request);
            if (currentUser == null) {
                return Result.unauthorized("请先登录");
            }

            boolean isLiked = articleService.isArticleLiked(id, currentUser.getId());
            Map<String, Object> result = new HashMap<>();
            result.put("isLiked", isLiked);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("检查点赞状态失败");
        }
    }

    /**
     * 获取文章的点赞统计信息
     * GET /api/articles/{id}/like-stats
     */
    @GetMapping("/{id}/like-stats")
    public Result<Map<String, Object>> getArticleLikeStats(@PathVariable Integer id) {
        try {
            Map<String, Object> stats = articleService.getArticleLikeStats(id);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取点赞统计失败");
        }
    }

    /**
     * 将 ArticleDTO 转换为 Article 实体
     */
    private Article convertToArticle(ArticleDTO articleDTO) {
        if (articleDTO == null) {
            return null;
        }

        Article article = new Article();
        article.setId(articleDTO.getId());
        article.setTitle(articleDTO.getTitle());
        article.setContent(articleDTO.getContent());
        article.setSummary(articleDTO.getSummary());
        article.setCoverImage(articleDTO.getCoverImage());
        article.setStatus(articleDTO.getStatus());
        article.setCategoryId(articleDTO.getCategoryId());
        article.setIsTop(articleDTO.getIsTop());
        article.setAllowComment(articleDTO.getAllowComment());

        return article;
    }
>>>>>>> Stashed changes
}