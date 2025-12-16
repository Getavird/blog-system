package com.blog.service;

import com.blog.entity.Category;
import java.util.List;

public interface CategoryService {
    
    /**
     * 获取所有分类
     */
    List<Category> getAllCategories();
    
    /**
     * 创建分类
     */
    Category createCategory(Category category);
    
    /**
     * 根据ID获取分类
     */
    Category getCategoryById(Integer id);
    
    /**
     * 更新分类
     */
    boolean updateCategory(Category category);
    
    /**
     * 删除分类
     */
    boolean deleteCategory(Integer id);
}