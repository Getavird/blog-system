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
        // 这里需要实现插入逻辑
        // int result = categoryMapper.insert(category);
        // return category;
        
        // 暂时返回模拟数据
        category.setId(100);
        return category;
    }
    
    @Override
    public Category getCategoryById(Integer id) {
        return categoryMapper.findById(id);
    }
    
    @Override
    public boolean updateCategory(Category category) {
        // int result = categoryMapper.update(category);
        // return result > 0;
        return true;
    }
    
    @Override
    public boolean deleteCategory(Integer id) {
        // int result = categoryMapper.delete(id);
        // return result > 0;
        return true;
    }
}