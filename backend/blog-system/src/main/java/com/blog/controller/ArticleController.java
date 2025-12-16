package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * 创建文章
     */
    @PostMapping
    public Result<Article> createArticle(@RequestBody Article article) {
        boolean success = articleService.createArticle(article);
        return success ? Result.created(article) : Result.error("创建失败");
    }
    
    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    public Result<Article> updateArticle(@PathVariable Integer id, 
                                         @RequestBody Article article) {
        article.setId(id);
        boolean success = articleService.updateArticle(article);
        return success ? Result.success(article) : Result.error("更新失败");
    }
    
    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteArticle(@PathVariable Integer id) {
        boolean success = articleService.deleteArticle(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
