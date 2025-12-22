package com.blog.service.impl;

import com.blog.dao.ArticleMapper;
import com.blog.entity.vo.ArchiveVO;
import com.blog.entity.vo.ArticleArchiveVO;
import com.blog.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArchiveServiceImpl implements ArchiveService {
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Override
    public List<ArchiveVO> getAllArchives() {
        // 1. 获取所有归档分组
        List<Map<String, Object>> groups = articleMapper.getArchiveGroups();
        
        // 2. 构建归档列表
        List<ArchiveVO> archives = new ArrayList<>();
        
        for (Map<String, Object> group : groups) {
            Integer year = (Integer) group.get("year");
            Integer month = (Integer) group.get("month");
            
            // 获取该年月下的文章
            List<ArticleArchiveVO> articles = articleMapper.getArticlesByYearMonth(year, month);
            
            ArchiveVO archive = new ArchiveVO();
            archive.setYear(String.valueOf(year));
            archive.setMonth(formatMonth(month));
            archive.setArticles(articles);
            archive.setArticleCount(articles.size());
            
            archives.add(archive);
        }
        
        return archives;
    }
    
    @Override
    public Map<String, Object> getArchiveStats() {
        // 直接从数据库获取统计信息
        Map<String, Object> stats = articleMapper.getArchiveStats();
        
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("total_articles", 0);
            stats.put("total_years", 0);
            stats.put("total_months", 0);
        }
        
        // 添加格式化后的数据
        stats.put("formatted_total_articles", 
                 String.format("共 %d 篇文章", stats.get("total_articles")));
        stats.put("formatted_total_years", 
                 String.format("跨越 %d 年", stats.get("total_years")));
        stats.put("formatted_total_months", 
                 String.format("%d 个月份", stats.get("total_months")));
        
        return stats;
    }
    
    @Override
    public List<ArchiveVO> getArchivesByYear(Integer year) {
        // 获取指定年份的所有归档数据
        List<ArchiveVO> allArchives = getAllArchives();
        
        return allArchives.stream()
                .filter(archive -> String.valueOf(year).equals(archive.getYear()))
                .sorted((a1, a2) -> {
                    // 按月份倒序排列
                    Integer month1 = Integer.parseInt(a1.getMonth().replace("月", ""));
                    Integer month2 = Integer.parseInt(a2.getMonth().replace("月", ""));
                    return month2.compareTo(month1);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ArticleArchiveVO> getArticlesByYearMonth(Integer year, Integer month) {
        return articleMapper.getArticlesByYearMonth(year, month);
    }
    
    /**
     * 格式化月份（1 -> "01月"）
     */
    private String formatMonth(Integer month) {
        if (month == null) return "00月";
        return String.format("%02d月", month);
    }
    
    /**
     * 获取年份列表（去重）
     */
    public List<String> getYearList() {
        List<ArchiveVO> archives = getAllArchives();
        return archives.stream()
                .map(ArchiveVO::getYear)
                .distinct()
                .sorted((y1, y2) -> y2.compareTo(y1)) // 降序排列
                .collect(Collectors.toList());
    }
    
    /**
     * 获取归档时间轴数据（按年份分组）
     */
    public Map<String, List<ArchiveVO>> getArchiveTimeline() {
        List<ArchiveVO> archives = getAllArchives();
        
        // 按年份分组
        Map<String, List<ArchiveVO>> timeline = new TreeMap<>(Collections.reverseOrder());
        
        for (ArchiveVO archive : archives) {
            String year = archive.getYear();
            timeline.computeIfAbsent(year, k -> new ArrayList<>()).add(archive);
        }
        
        return timeline;
    }
    
    /**
     * 获取最近N个月的归档数据
     */
    public List<ArchiveVO> getRecentArchives(int limit) {
        List<ArchiveVO> archives = getAllArchives();
        
        // 限制数量
        if (archives.size() > limit) {
            archives = archives.subList(0, limit);
        }
        
        return archives;
    }
}