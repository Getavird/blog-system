package com.blog.service.impl;

import com.blog.dao.ArticleMapper;
import com.blog.entity.Article;
import com.blog.entity.vo.ArchiveVO;
import com.blog.entity.vo.ArticleArchiveVO;
import com.blog.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArchiveServiceImpl implements ArchiveService {
    
    @Autowired
    private ArticleMapper articleMapper;
    
    // 月份名称映射
    private static final Map<Integer, String> MONTH_NAMES = new HashMap<>();
    static {
        MONTH_NAMES.put(1, "一月"); MONTH_NAMES.put(2, "二月"); 
        MONTH_NAMES.put(3, "三月"); MONTH_NAMES.put(4, "四月");
        MONTH_NAMES.put(5, "五月"); MONTH_NAMES.put(6, "六月");
        MONTH_NAMES.put(7, "七月"); MONTH_NAMES.put(8, "八月");
        MONTH_NAMES.put(9, "九月"); MONTH_NAMES.put(10, "十月");
        MONTH_NAMES.put(11, "十一月"); MONTH_NAMES.put(12, "十二月");
    }
    
    @Override
    public List<ArchiveVO> getAllArchives() {
        List<Map<String, Object>> groups = articleMapper.getArchiveGroups();
        List<ArchiveVO> archives = new ArrayList<>();
        
        for (Map<String, Object> group : groups) {
            Integer year = (Integer) group.get("year");
            Integer month = (Integer) group.get("month");
            
            ArchiveVO archive = createArchiveVO(year, month);
            if (archive != null) {
                archives.add(archive);
            }
        }
        
        return archives;
    }
    
    @Override
    public Map<String, Object> getArchiveStats() {
        Map<String, Object> stats = articleMapper.getArchiveStats();
        
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("total_articles", 0);
            stats.put("total_years", 0);
            stats.put("total_months", 0);
        }
        
        // 添加最活跃年份
        Integer mostActiveYear = getMostActiveYear();
        stats.put("most_active_year", mostActiveYear);
        
        // 格式化数据
        stats.put("formatted_total_articles", 
                 String.format("%d 篇文章", stats.get("total_articles")));
        stats.put("formatted_total_years", 
                 String.format("%d 个年份", stats.get("total_years")));
        stats.put("formatted_total_months", 
                 String.format("%d 个月份", stats.get("total_months")));
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getArchiveOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 1. 基本统计
        Map<String, Object> stats = getArchiveStats();
        overview.put("stats", stats);
        
        // 2. 年份列表
        List<Integer> years = getAvailableYears();
        overview.put("years", years);
        
        // 3. 最近一年的归档数据（用于首页展示）
        if (!years.isEmpty()) {
            int latestYear = years.get(0); // 降序排列，第一个是最新年份
            List<ArchiveVO> yearArchives = getArchivesByYear(latestYear);
            overview.put("latestYearArchives", yearArchives);
        }
        
        return overview;
    }
    
    @Override
    public List<ArchiveVO> getArchivesByYear(Integer year) {
        // 获取指定年份的所有归档数据
        List<ArchiveVO> allArchives = getAllArchives();
        
        return allArchives.stream()
                .filter(archive -> String.valueOf(year).equals(archive.getYear()))
                .sorted((a1, a2) -> {
                    // 按月份倒序排列（最新的在前）
                    Integer month1 = Integer.parseInt(a1.getMonth().replace("月", ""));
                    Integer month2 = Integer.parseInt(a2.getMonth().replace("月", ""));
                    return month2.compareTo(month1);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ArticleArchiveVO> getArticlesByYearMonth(Integer year, Integer month) {
        List<ArticleArchiveVO> articles = articleMapper.getArticlesByYearMonth(year, month);
        
        // 为每篇文章补充完整信息
        for (ArticleArchiveVO article : articles) {
            Article fullArticle = articleMapper.findById(article.getId());
            if (fullArticle != null) {
                article.setViewCount(fullArticle.getViewCount());
                article.setLikeCount(fullArticle.getLikeCount());
                article.setCommentCount(fullArticle.getCommentCount());
                article.setAuthorName(fullArticle.getAuthorName());
                
                // 格式化时间
                if (article.getCreateTime() != null) {
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd");
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    article.setFormatDate(article.getCreateTime().format(dateFormatter));
                    article.setFormatTime(article.getCreateTime().format(timeFormatter));
                }
            }
        }
        
        return articles;
    }
    
    @Override
    public List<Integer> getAvailableYears() {
        List<ArchiveVO> archives = getAllArchives();
        return archives.stream()
                .map(archive -> Integer.parseInt(archive.getYear()))
                .distinct()
                .sorted((y1, y2) -> y2.compareTo(y1)) // 降序排列
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getYearStats(Integer year) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取该年份的所有归档
        List<ArchiveVO> yearArchives = getArchivesByYear(year);
        
        // 计算统计
        int totalArticles = yearArchives.stream()
                .mapToInt(ArchiveVO::getArticleCount)
                .sum();
        
        int totalViews = yearArchives.stream()
                .mapToInt(ArchiveVO::getViewCount)
                .sum();
        
        int totalLikes = yearArchives.stream()
                .mapToInt(ArchiveVO::getLikeCount)
                .sum();
        
        int monthCount = yearArchives.size();
        
        stats.put("year", year);
        stats.put("totalArticles", totalArticles);
        stats.put("totalViews", totalViews);
        stats.put("totalLikes", totalLikes);
        stats.put("monthCount", monthCount);
        
        return stats;
    }
    
    @Override
    public ArchiveVO getArchiveDetail(Integer year, Integer month) {
        return createArchiveVO(year, month);
    }
    
    /**
     * 创建ArchiveVO对象
     */
    private ArchiveVO createArchiveVO(Integer year, Integer month) {
        if (year == null || month == null) {
            return null;
        }
        
        // 获取该月的文章
        List<ArticleArchiveVO> articles = getArticlesByYearMonth(year, month);
        if (articles.isEmpty()) {
            return null;
        }
        
        // 计算该月的总阅读量和总点赞数
        int totalViews = articles.stream()
                .mapToInt(ArticleArchiveVO::getViewCount)
                .sum();
        
        int totalLikes = articles.stream()
                .mapToInt(ArticleArchiveVO::getLikeCount)
                .sum();
        
        // 创建ArchiveVO
        ArchiveVO archive = new ArchiveVO();
        archive.setYear(String.valueOf(year));
        archive.setMonth(String.format("%02d月", month));
        archive.setMonthName(MONTH_NAMES.getOrDefault(month, "未知月"));
        archive.setShortMonthName(month + "月");
        archive.setArticles(articles);
        archive.setArticleCount(articles.size());
        archive.setViewCount(totalViews);
        archive.setLikeCount(totalLikes);
        
        return archive;
    }
    
    /**
     * 获取最活跃年份（文章最多的年份）
     */
    private Integer getMostActiveYear() {
        List<ArchiveVO> archives = getAllArchives();
        
        // 按年份分组统计文章数
        Map<Integer, Integer> yearArticleCount = new HashMap<>();
        for (ArchiveVO archive : archives) {
            Integer year = Integer.parseInt(archive.getYear());
            yearArticleCount.put(year, 
                yearArticleCount.getOrDefault(year, 0) + archive.getArticleCount());
        }
        
        // 找出文章数最多的年份
        return yearArticleCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    /**
     * 获取最近N年的归档统计
     */
    public List<Map<String, Object>> getRecentYearStats(int limit) {
        List<Integer> years = getAvailableYears();
        List<Map<String, Object>> recentStats = new ArrayList<>();
        
        for (int i = 0; i < Math.min(limit, years.size()); i++) {
            Integer year = years.get(i);
            Map<String, Object> stats = getYearStats(year);
            recentStats.add(stats);
        }
        
        return recentStats;
    }
    
    /**
     * 获取年份对比统计
     */
    public Map<String, Object> getYearComparisonStats() {
        List<Integer> years = getAvailableYears();
        Map<String, Object> comparison = new HashMap<>();
        
        List<Map<String, Object>> yearStats = new ArrayList<>();
        for (Integer year : years) {
            Map<String, Object> stats = getYearStats(year);
            yearStats.add(stats);
        }
        
        comparison.put("years", years);
        comparison.put("yearStats", yearStats);
        
        // 计算趋势
        if (years.size() >= 2) {
            int latestArticles = (int) ((Map<String, Object>) yearStats.get(0)).get("totalArticles");
            int previousArticles = (int) ((Map<String, Object>) yearStats.get(1)).get("totalArticles");
            int trend = latestArticles - previousArticles;
            comparison.put("articleTrend", trend);
        }
        
        return comparison;
    }
}