package com.blog.service.impl;

import com.blog.dao.ArticleMapper;
import com.blog.dao.UserMapper;
import com.blog.dao.SearchRecordMapper;  // 新增
import com.blog.dao.TagMapper;  // 新增
import com.blog.entity.Article;
import com.blog.entity.FullSearchResponse;
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
public FullSearchResponse fullSearch(String keyword, Integer page, Integer size) {
    if (!StringUtils.hasText(keyword)) {
        return new FullSearchResponse(keyword, page, size, 0, 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }
    
    // 使用传入的分页参数，但调整各模块的默认大小
    int articleSize = Math.min(size, 10);  // 文章最多10条
    int userSize = Math.min(size, 5);      // 用户最多5条
    int tagSize = Math.min(size, 5);       // 标签最多5条
    
    // 对于full搜索，我们总是返回第一页的各模块数据
    // 因为这是综合搜索，不是分页搜索
    SearchResult<Article> articleResult = searchArticles(keyword, 1, articleSize);
    SearchResult<User> userResult = searchUsers(keyword, 1, userSize);
    SearchResult<Tag> tagResult = searchTags(keyword, 1, tagSize);
    
    int total = articleResult.getTotal() + userResult.getTotal() + tagResult.getTotal();
    
    return new FullSearchResponse(keyword, page, size, total,
            articleResult.getItems(), userResult.getItems(), tagResult.getItems());
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
/**
 * 搜索标签 - 使用数据库搜索优化性能
 */
public SearchResult<Tag> searchTags(String keyword, Integer page, Integer size) {
    // 1. 检查关键词是否为空
    if (!StringUtils.hasText(keyword)) {
        return new SearchResult<>(keyword, 0, page, size, new ArrayList<>());
    }
    
    // 2. 计算分页偏移量
    int offset = (page - 1) * size;
    
    // 3. 优先使用数据库搜索（更高效）
    try {
        // 3.1 调用数据库搜索方法
        List<Tag> tags = tagMapper.searchTags(keyword, offset, size);
        
        // 3.2 统计搜索总数
        int total = tagMapper.countSearchTags(keyword);
        
        // 3.3 返回搜索结果
        return new SearchResult<>(keyword, total, page, size, tags);
        
    } catch (Exception e) {
        // 4. 如果数据库方法不存在或出错，使用内存过滤（兼容性处理）
        System.err.println("⚠️ 数据库标签搜索方法不存在或出错，使用内存过滤: " + e.getMessage());
        e.printStackTrace();
        
        // 4.1 获取所有标签
        List<Tag> allTags = tagMapper.findAll();
        
        // 4.2 在内存中过滤包含关键词的标签
        List<Tag> filteredTags = allTags.stream()
            .filter(tag -> {
                if (tag == null) return false;
                
                String lowerKeyword = keyword.toLowerCase();
                String tagName = tag.getName() != null ? tag.getName().toLowerCase() : "";
                String tagDesc = tag.getDescription() != null ? tag.getDescription().toLowerCase() : "";
                
                return tagName.contains(lowerKeyword) || tagDesc.contains(lowerKeyword);
            })
            .collect(Collectors.toList());
        
        // 4.3 计算总数
        int total = filteredTags.size();
        
        // 4.4 内存分页处理
        List<Tag> pagedTags;
        if (offset >= total) {
            pagedTags = new ArrayList<>(); // 偏移量超出范围，返回空列表
        } else {
            int end = Math.min(offset + size, total);
            pagedTags = filteredTags.subList(offset, end);
        }
        
        // 4.5 返回内存过滤的结果
        return new SearchResult<>(keyword, total, page, size, pagedTags);
    }
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