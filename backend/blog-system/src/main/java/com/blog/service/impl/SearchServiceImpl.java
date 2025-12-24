package com.blog.service.impl;

import com.blog.dao.ArticleMapper;
import com.blog.dao.UserMapper;
import com.blog.dao.SearchRecordMapper;  // 新增
import com.blog.dao.TagMapper;  // 新增
import com.blog.entity.Article;
import com.blog.entity.SearchRecord;
import com.blog.entity.SearchResult;
import com.blog.entity.User;
import com.blog.entity.Tag;  // 新增
import com.blog.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TagMapper tagMapper;  // 新增
    
    @Autowired
    private SearchRecordMapper searchRecordMapper;  // 新增
    
    @Override
    public SearchResult<Object> fullSearch(String keyword, Integer page, Integer size) {
        if (!StringUtils.hasText(keyword)) {
            return new SearchResult<>(keyword, 0, page, size, new ArrayList<>());
        }
        
        // 分别搜索文章和用户
        SearchResult<Article> articleResult = searchArticles(keyword, 1, 5);
        SearchResult<User> userResult = searchUsers(keyword, 1, 3);
        SearchResult<Tag> tagResult = searchTags(keyword, 1, 2);  // 新增标签搜索
        
        // 合并结果
        List<Object> combinedItems = new ArrayList<>();
        combinedItems.addAll(articleResult.getItems());
        combinedItems.addAll(userResult.getItems());
        combinedItems.addAll(tagResult.getItems());
        
        int total = articleResult.getTotal() + userResult.getTotal() + tagResult.getTotal();
        
        return new SearchResult<>(keyword, total, page, size, combinedItems);
    }
    
    @Override
    public SearchResult<Article> searchArticles(String keyword, Integer page, Integer size) {
        if (!StringUtils.hasText(keyword)) {
            return new SearchResult<>(keyword, 0, page, size, new ArrayList<>());
        }
        
        int offset = (page - 1) * size;
        
        List<Article> articles = articleMapper.searchArticles(
            keyword, offset, size
        );
        
        int total = articleMapper.countSearchArticles(keyword);
        
        return new SearchResult<>(keyword, total, page, size, articles);
    }
    
    @Override
    public SearchResult<User> searchUsers(String keyword, Integer page, Integer size) {
        if (!StringUtils.hasText(keyword)) {
            return new SearchResult<>(keyword, 0, page, size, new ArrayList<>());
        }
        
        int offset = (page - 1) * size;
        
        List<User> users = userMapper.searchUsers(keyword, offset, size);
        
        int total = userMapper.countSearchUsers(keyword);
        
        return new SearchResult<>(keyword, total, page, size, users);
    }
    
    // 新增方法：搜索标签
    public SearchResult<Tag> searchTags(String keyword, Integer page, Integer size) {
        if (!StringUtils.hasText(keyword)) {
            return new SearchResult<>(keyword, 0, page, size, new ArrayList<>());
        }
        
        // 获取所有标签
        List<Tag> allTags = tagMapper.findAll();
        
        // 过滤包含关键词的标签（名称或描述）
        List<Tag> filteredTags = allTags.stream()
            .filter(tag -> 
                (tag.getName() != null && tag.getName().toLowerCase().contains(keyword.toLowerCase())) ||
                (tag.getDescription() != null && tag.getDescription().toLowerCase().contains(keyword.toLowerCase()))
            )
            .collect(Collectors.toList());
        
        // 分页
        int total = filteredTags.size();
        int offset = (page - 1) * size;
        int end = Math.min(offset + size, total);
        
        List<Tag> pagedTags = filteredTags.subList(offset, end);
        
        return new SearchResult<>(keyword, total, page, size, pagedTags);
    }
    
    @Override
    public SearchResult<Article> advancedSearchArticles(String title, String content, 
                                                       String tag, String category,
                                                       Integer minView, Integer maxView,
                                                       Integer page, Integer size) {
        
        int offset = (page - 1) * size;
        
        List<Article> articles = articleMapper.advancedSearch(
            title, content, tag, category, minView, maxView, offset, size
        );
        
        int total = articleMapper.countAdvancedSearch(
            title, content, tag, category, minView, maxView
        );
        
        return new SearchResult<>("", total, page, size, articles);
    }
    
    @Override
    public List<String> getHotKeywords(Integer limit) {
        // 从数据库获取热门关键词
        try {
            return searchRecordMapper.getHotKeywords(limit == null ? 10 : limit);
        } catch (Exception e) {
            // 如果表不存在或查询失败，返回示例数据
            List<String> hotKeywords = new ArrayList<>();
            hotKeywords.add("Spring Boot");
            hotKeywords.add("Vue");
            hotKeywords.add("Java");
            hotKeywords.add("MySQL");
            hotKeywords.add("前端");
            
            if (limit != null && hotKeywords.size() > limit) {
                return hotKeywords.subList(0, limit);
            }
            
            return hotKeywords;
        }
    }
    
    @Override
    public void saveSearchRecord(String keyword, Integer userId) {
        try {
            SearchRecord record = new SearchRecord();
            record.setKeyword(keyword);
            record.setUserId(userId);
            searchRecordMapper.upsert(record);
        } catch (Exception e) {
            // 记录日志但不要影响主流程
            System.err.println("保存搜索记录失败: " + e.getMessage());
        }
    }
    
    // 新增方法：获取搜索建议
    public List<String> getSearchSuggestions(String prefix, int limit) {
        if (!StringUtils.hasText(prefix)) {
            return new ArrayList<>();
        }
        
        List<String> suggestions = new ArrayList<>();
        
        // 从文章标题获取建议
        List<Article> articles = articleMapper.searchArticles(prefix, 0, 5);
        articles.stream()
            .map(Article::getTitle)
            .filter(title -> title.toLowerCase().contains(prefix.toLowerCase()))
            .limit(limit)
            .forEach(suggestions::add);
        
        // 从标签获取建议
        List<Tag> tags = tagMapper.findAll();
        tags.stream()
            .map(Tag::getName)
            .filter(name -> name != null && name.toLowerCase().contains(prefix.toLowerCase()))
            .limit(limit - suggestions.size())
            .forEach(suggestions::add);
        
        // 如果还不够，添加通用建议
        if (suggestions.size() < limit) {
            suggestions.add(prefix + " 教程");
            suggestions.add(prefix + " 入门");
            suggestions.add(prefix + " 基础");
        }
        
        return suggestions;
    }
}