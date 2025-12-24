package com.blog.service;

import com.blog.entity.Category;
import java.util.List;
import java.util.Map;

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

    /**
     * 获取分类统计信息（文章数、总阅读量、总点赞数）
     */
    Map<String, Object> getCategoryStatistics(Integer categoryId);

    /**
     * 获取分类下的文章（支持排序和分页）
     */
    Map<String, Object> getCategoryArticles(Integer categoryId, int page, int size, String sortType);

    /**
     * 获取热门分类（按文章数量排序）
     */
    List<Map<String, Object>> getHotCategories(int limit);
}