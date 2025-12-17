package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Category;
import com.blog.service.CategoryService;
import com.blog.utils.SessionUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
