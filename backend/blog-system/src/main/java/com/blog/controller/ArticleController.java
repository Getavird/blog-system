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
}