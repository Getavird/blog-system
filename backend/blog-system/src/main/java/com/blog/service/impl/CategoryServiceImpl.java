package com.blog.service.impl;

import com.blog.dao.ArticleMapper;
import com.blog.dao.CategoryMapper;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private ArticleMapper articleMapper;
    
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
    
    @Override
    public Map<String, Object> getCategoryStatistics(Integer categoryId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取分类信息
        Category category = categoryMapper.findById(categoryId);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        
        // 文章数量
        int articleCount = categoryMapper.countArticlesByCategory(categoryId);
        
        // 总阅读量
        Integer totalViews = articleMapper.sumViewCountByCategory(categoryId);
        if (totalViews == null) totalViews = 0;
        
        // 总点赞数
        Integer totalLikes = articleMapper.sumLikeCountByCategory(categoryId);
        if (totalLikes == null) totalLikes = 0;
        
        // 组装统计信息
        stats.put("category", category);
        stats.put("articleCount", articleCount);
        stats.put("totalViews", totalViews);
        stats.put("totalLikes", totalLikes);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getCategoryArticles(Integer categoryId, int page, int size, String sortType) {
        Map<String, Object> result = new HashMap<>();
        
        int offset = (page - 1) * size;
        List<Article> articles;
        int total = categoryMapper.countArticlesByCategory(categoryId);
        
        // 根据排序类型获取文章
        switch (sortType.toLowerCase()) {
            case "hot":
                articles = articleMapper.findHotByCategory(categoryId, offset, size);
                break;
            case "likes":
                articles = articleMapper.findLikesByCategory(categoryId, offset, size);
                break;
            case "latest":
            default:
                articles = articleMapper.findLatestByCategory(categoryId, offset, size);
        }
        
        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / size);
        
        result.put("articles", articles);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", totalPages);
        result.put("sortType", sortType);
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getHotCategories(int limit) {
        List<Category> categories = getAllCategories();
        List<Map<String, Object>> hotCategories = new ArrayList<>();
        
        for (Category category : categories) {
            Map<String, Object> categoryInfo = new HashMap<>();
            categoryInfo.put("id", category.getId());
            categoryInfo.put("name", category.getName());
            categoryInfo.put("description", category.getDescription());
            categoryInfo.put("color", category.getColor());
            categoryInfo.put("icon", category.getIcon());
            categoryInfo.put("orderNum", category.getOrderNum());
            
            // 统计文章数量
            int articleCount = categoryMapper.countArticlesByCategory(category.getId());
            categoryInfo.put("articleCount", articleCount);
            
            // 只添加有文章的分类
            if (articleCount > 0) {
                hotCategories.add(categoryInfo);
            }
        }
        
        // 按文章数量排序（从多到少）
        hotCategories.sort((a, b) -> {
            int countA = (int) a.get("articleCount");
            int countB = (int) b.get("articleCount");
            return Integer.compare(countB, countA);
        });
        
        // 限制数量
        if (hotCategories.size() > limit) {
            hotCategories = hotCategories.subList(0, limit);
        }
        
        return hotCategories;
    }
}