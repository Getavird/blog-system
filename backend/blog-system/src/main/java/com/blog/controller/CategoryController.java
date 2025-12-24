package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Category;
import com.blog.service.CategoryService;
import com.blog.utils.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有分类
     */
    @GetMapping
    public Result<List<Category>> getCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @PostMapping
    public Result<Category> createCategory(@RequestBody Category category, HttpServletRequest request) {
        // 只有管理员可以创建分类
        if (!SessionUtil.isAdmin(request)) {
            return Result.forbidden("需要管理员权限");
        }

        Category savedCategory = categoryService.createCategory(category);
        return Result.created(savedCategory);
    }

    /**
     * 获取所有分类及其文章数量
     * GET /api/categories/with-stats
     */
    @GetMapping("/with-stats")
    public Result<List<Map<String, Object>>> getCategoriesWithStats() {
        List<Map<String, Object>> categories = categoryService.getHotCategories(100); // 获取所有
        return Result.success(categories);
    }

    /**
     * 分类详情页面 - 获取分类信息和统计
     * GET /api/categories/{id}/detail
     */
    @GetMapping("/{id}/detail")
    public Result<Map<String, Object>> getCategoryDetail(@PathVariable Integer id) {
        try {
            Map<String, Object> categoryStats = categoryService.getCategoryStatistics(id);
            return Result.success(categoryStats);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分类文章列表（支持排序和分页）
     * GET /api/categories/{id}/articles?page=1&size=15&sort=latest
     * 
     * 排序参数：
     * - latest: 最新（按创建时间）
     * - hot: 最热（按浏览量）
     * - likes: 点赞（按点赞数）
     */
    @GetMapping("/{id}/articles")
    public Result<Map<String, Object>> getCategoryArticles(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "15") Integer size,
            @RequestParam(defaultValue = "latest") String sort) {

        // 验证排序参数
        String[] validSorts = { "latest", "hot", "likes" };
        if (!Arrays.asList(validSorts).contains(sort.toLowerCase())) {
            return Result.badRequest("排序参数错误，可选值：latest, hot, likes");
        }

        // 验证分页参数
        if (page < 1)
            page = 1;
        if (size < 1 || size > 50)
            size = 15;

        try {
            Map<String, Object> articlesData = categoryService.getCategoryArticles(id, page, size, sort.toLowerCase());
            return Result.success(articlesData);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取热门分类（按文章数量排序，用于首页侧边栏）
     * GET /api/categories/hot?limit=6
     */
    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> getHotCategories(
            @RequestParam(defaultValue = "6") Integer limit) {
        List<Map<String, Object>> hotCategories = categoryService.getHotCategories(limit);
        return Result.success(hotCategories);
    }

    /**
     * 更新分类（管理员权限）
     */
    @PutMapping("/{id}")
    public Result<Category> updateCategory(@PathVariable Integer id,
            @RequestBody Category category,
            HttpServletRequest request) {
        // 检查管理员权限
        if (!SessionUtil.isAdmin(request)) {
            return Result.forbidden("需要管理员权限");
        }

        category.setId(id);
        boolean success = categoryService.updateCategory(category);
        return success ? Result.success("更新成功", category) : Result.error("更新失败");
    }

    /**
     * 删除分类（管理员权限）
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable Integer id, HttpServletRequest request) {
        // 检查管理员权限
        if (!SessionUtil.isAdmin(request)) {
            return Result.forbidden("需要管理员权限");
        }

        try {
            boolean success = categoryService.deleteCategory(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}