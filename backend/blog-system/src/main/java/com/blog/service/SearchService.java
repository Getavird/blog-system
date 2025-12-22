package com.blog.service;

import com.blog.entity.Article;
import com.blog.entity.SearchResult;
import com.blog.entity.User;
import java.util.List;

public interface SearchService {
    
    /**
     * 全文搜索（搜索文章和用户）
     */
    SearchResult<Object> fullSearch(String keyword, Integer page, Integer size);
    
    /**
     * 搜索文章（标题、内容、标签）
     */
    SearchResult<Article> searchArticles(String keyword, Integer page, Integer size);
    
    /**
     * 搜索用户（用户名、邮箱、简介）
     */
    SearchResult<User> searchUsers(String keyword, Integer page, Integer size);
    
    /**
     * 高级搜索文章（多条件）
     */
    SearchResult<Article> advancedSearchArticles(String title, String content, 
                                               String tag, String category, 
                                               Integer minView, Integer maxView,
                                               Integer page, Integer size);
    
    /**
     * 获取热门搜索关键词
     */
    List<String> getHotKeywords(Integer limit);
    
    /**
     * 保存搜索记录（用于统计）
     */
    void saveSearchRecord(String keyword, Integer userId);
}