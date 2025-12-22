package com.blog.service.impl;

import com.blog.dao.ArticleMapper;
import com.blog.dao.UserMapper;
import com.blog.entity.Article;
import com.blog.entity.SearchResult;
import com.blog.entity.User;
import com.blog.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public SearchResult<Object> fullSearch(String keyword, Integer page, Integer size) {
        if (!StringUtils.hasText(keyword)) {
            return new SearchResult<>(keyword, 0, page, size, new ArrayList<>());
        }
        
        // 分别搜索文章和用户
        SearchResult<Article> articleResult = searchArticles(keyword, page, size / 2);
        SearchResult<User> userResult = searchUsers(keyword, page, size / 2);
        
        // 合并结果
        List<Object> combinedItems = new ArrayList<>();
        combinedItems.addAll(articleResult.getItems());
        combinedItems.addAll(userResult.getItems());
        
        int total = articleResult.getTotal() + userResult.getTotal();
        
        return new SearchResult<>(keyword, total, page, size, combinedItems);
    }
    
    @Override
    public SearchResult<Article> searchArticles(String keyword, Integer page, Integer size) {
        if (!StringUtils.hasText(keyword)) {
            return new SearchResult<>(keyword, 0, page, size, new ArrayList<>());
        }
        
        int offset = (page - 1) * size;
        
        // 使用LIKE进行模糊搜索（支持标题、内容、标签）
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
        
        // 搜索用户（用户名、邮箱、简介）
        List<User> users = userMapper.searchUsers(keyword, offset, size);
        
        int total = userMapper.countSearchUsers(keyword);
        
        return new SearchResult<>(keyword, total, page, size, users);
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
        // 这里可以查询搜索记录表，返回热门关键词
        // 暂时返回模拟数据
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
    
    @Override
    public void saveSearchRecord(String keyword, Integer userId) {
        // 这里可以保存搜索记录到数据库，用于统计热门搜索
        System.out.println("保存搜索记录: keyword=" + keyword + ", userId=" + userId);
        
        // 实际实现需要创建搜索记录表
        // searchRecordMapper.insert(new SearchRecord(keyword, userId));
    }
}