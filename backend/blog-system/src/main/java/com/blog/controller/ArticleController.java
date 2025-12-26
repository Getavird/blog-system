package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.service.ArticleService;
import com.blog.utils.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
     * 获取文章详情
     */
    @GetMapping("/{id}")
    public Result<Article> getArticle(@PathVariable Integer id) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return Result.notFound("文章不存在");
        }
        return Result.success(article);
    }

    /**
     * 创建文章（需要登录）
     */
    @PostMapping
    public Result<Article> createArticle(@RequestBody Article article, HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 设置作者ID
        article.setUserId(currentUser.getId());

        boolean success = articleService.createArticle(article);
        return success ? Result.created(article) : Result.error("创建失败");
    }

    /**
     * 更新文章（需要登录，且是作者或管理员）
     */
    @PutMapping("/{id}")
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
     * 创建草稿（需要登录）
     */
    @PostMapping("/draft")
    public Result<Article> createDraft(@RequestBody Article article, HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 设置作者ID
        article.setUserId(currentUser.getId());
        article.setStatus(0); // 草稿状态
        
        // 如果是草稿，允许标题为空，设置默认标题
        if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
            article.setTitle("无标题草稿");
        }

        // 调用新增的createDraft方法（需要在ArticleService中添加）
        boolean success = articleService.createArticle(article); // 暂时使用createArticle
        return success ? Result.created(article) : Result.error("创建草稿失败");
    }

    /**
     * 获取用户的草稿列表
     */
    @GetMapping("/my-drafts")
    public Result<Map<String, Object>> getMyDrafts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 获取所有文章然后过滤出当前用户的草稿
        List<Article> allArticles = articleService.getArticles(page, size * 5); // 多取一些
        List<Article> drafts = allArticles.stream()
                .filter(article -> article.getUserId().equals(currentUser.getId()) 
                        && article.getStatus() == 0)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("drafts", drafts);
        result.put("page", page);
        result.put("size", size);
        result.put("total", drafts.size());

        return Result.success(result);
    }

    /**
     * 发布草稿
     */
    @PostMapping("/{id}/publish")
    public Result<String> publishDraft(@PathVariable Integer id, HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 获取文章
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return Result.notFound("文章不存在");
        }

        // 检查权限：只有作者可以发布
        if (!article.getUserId().equals(currentUser.getId())) {
            return Result.forbidden("没有权限发布此文章");
        }

        // 检查是否是草稿
        if (article.getStatus() != 0) {
            return Result.error("只有草稿可以发布");
        }

        // 更新文章状态为发布
        article.setStatus(1);
        boolean success = articleService.updateArticle(article);
        
        return success ? Result.success("发布成功") : Result.error("发布失败");
    }

    /**
     * 获取用户的发布文章（不包括草稿）
     */
    @GetMapping("/my-published")
    public Result<Map<String, Object>> getMyPublishedArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 获取所有文章然后过滤出当前用户的发布文章
        List<Article> allArticles = articleService.getArticles(page, size * 5);
        List<Article> published = allArticles.stream()
                .filter(article -> article.getUserId().equals(currentUser.getId()) 
                        && article.getStatus() == 1)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("articles", published);
        result.put("page", page);
        result.put("size", size);
        result.put("total", published.size());

        return Result.success(result);
    }

    /**
     * 更新草稿（只允许作者更新自己的草稿）
     */
    @PutMapping("/draft/{id}")
    public Result<Article> updateDraft(@PathVariable Integer id,
                                       @RequestBody Article article,
                                       HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 获取原文章
        Article existingArticle = articleService.getArticleById(id);
        if (existingArticle == null) {
            return Result.notFound("草稿不存在");
        }

        // 检查权限：只有作者可以更新
        if (!existingArticle.getUserId().equals(currentUser.getId())) {
            return Result.forbidden("没有权限更新此草稿");
        }

        // 确保是草稿
        if (existingArticle.getStatus() != 0) {
            return Result.error("只有草稿可以更新");
        }

        // 更新文章
        article.setId(id);
        article.setStatus(0); // 保持草稿状态
        article.setUserId(currentUser.getId()); // 保持作者不变
        
        boolean success = articleService.updateArticle(article);
        return success ? Result.success(article) : Result.error("更新草稿失败");
    }

    /**
     * 删除草稿（软删除）
     */
    @DeleteMapping("/draft/{id}")
    public Result<String> deleteDraft(@PathVariable Integer id, HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 获取文章
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return Result.notFound("草稿不存在");
        }

        // 检查权限：只有作者可以删除
        if (!article.getUserId().equals(currentUser.getId())) {
            return Result.forbidden("没有权限删除此草稿");
        }

        // 检查是否是草稿
        if (article.getStatus() != 0) {
            return Result.error("只有草稿可以删除");
        }

        // 软删除（设置状态为2）
        article.setStatus(2);
        boolean success = articleService.updateArticle(article);
        
        return success ? Result.success("草稿删除成功") : Result.error("删除失败");
    }
}