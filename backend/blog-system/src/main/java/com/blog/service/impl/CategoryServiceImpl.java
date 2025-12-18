package com.blog.service.impl;

import com.blog.dao.CategoryMapper;
import com.blog.entity.Category;
import com.blog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.findAll();
    }
    
    @Override
    public Category createCategory(Category category) {
        try {
            // 检查分类名称是否已存在
            Category existCategory = categoryMapper.findByName(category.getName());
            if (existCategory != null) {
                throw new RuntimeException("分类名称已存在");
            }
            
            // 设置默认值
            if (category.getOrderNum() == null) {
                category.setOrderNum(0);
            }
            if (category.getColor() == null) {
                category.setColor("#409eff");
            }
            
            // 插入数据库
            int result = categoryMapper.insert(category);
            if (result > 0) {
                return category;
            } else {
                throw new RuntimeException("创建分类失败");
            }
            
        } catch (Exception e) {
            System.err.println("创建分类异常: " + e.getMessage());
            throw new RuntimeException("创建分类失败: " + e.getMessage());
        }
    }
    
    @Override
    public Category getCategoryById(Integer id) {
        Category category = categoryMapper.findById(id);
        if (category != null) {
            // 统计文章数量
            int articleCount = categoryMapper.countArticlesByCategory(id);
            category.setArticleCount(articleCount);
        }
        return category;
    }
    
    @Override
    public boolean updateCategory(Category category) {
        try {
            // 检查分类是否存在
            Category existingCategory = categoryMapper.findById(category.getId());
            if (existingCategory == null) {
                throw new RuntimeException("分类不存在");
            }
            
            // 如果修改了名称，检查新名称是否已存在（排除自身）
            if (!existingCategory.getName().equals(category.getName())) {
                Category sameNameCategory = categoryMapper.findByName(category.getName());
                if (sameNameCategory != null && !sameNameCategory.getId().equals(category.getId())) {
                    throw new RuntimeException("分类名称已存在");
                }
            }
            
            int result = categoryMapper.update(category);
            return result > 0;
            
        } catch (Exception e) {
            System.err.println("更新分类异常: " + e.getMessage());
            throw new RuntimeException("更新分类失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean deleteCategory(Integer id) {
        try {
            // 检查分类下是否有文章
            int articleCount = categoryMapper.countArticlesByCategory(id);
            if (articleCount > 0) {
                throw new RuntimeException("该分类下有文章，无法删除");
            }
            
            int result = categoryMapper.delete(id);
            return result > 0;
            
        } catch (Exception e) {
            System.err.println("删除分类异常: " + e.getMessage());
            throw new RuntimeException("删除分类失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取热门分类（按文章数量排序）
     */
    public List<Category> getHotCategories(int limit) {
        List<Category> categories = categoryMapper.findAll();
        // 为每个分类设置文章数量
        for (Category category : categories) {
            int articleCount = categoryMapper.countArticlesByCategory(category.getId());
            category.setArticleCount(articleCount);
        }
        // 按文章数量排序并限制数量
        categories.sort((c1, c2) -> c2.getArticleCount() - c1.getArticleCount());
        if (categories.size() > limit) {
            categories = categories.subList(0, limit);
        }
        return categories;
    }
}