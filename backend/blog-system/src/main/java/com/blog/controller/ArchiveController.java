package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.vo.ArchiveVO;
import com.blog.entity.vo.ArticleArchiveVO;
import com.blog.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/archives")
public class ArchiveController {
    
    @Autowired
    private ArchiveService archiveService;
    
    /**
     * 获取所有归档数据
     */
    @GetMapping
    public Result<List<ArchiveVO>> getAllArchives() {
        List<ArchiveVO> archives = archiveService.getAllArchives();
        return Result.success(archives);
    }
    
    /**
     * 获取归档统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getArchiveStats() {
        Map<String, Object> stats = archiveService.getArchiveStats();
        return Result.success(stats);
    }
    
    /**
     * 获取归档时间轴（按年份分组）
     */
    @GetMapping("/timeline")
    public Result<Map<String, List<ArchiveVO>>> getArchiveTimeline() {
        if (archiveService instanceof com.blog.service.impl.ArchiveServiceImpl) {
            com.blog.service.impl.ArchiveServiceImpl impl = 
                (com.blog.service.impl.ArchiveServiceImpl) archiveService;
            Map<String, List<ArchiveVO>> timeline = impl.getArchiveTimeline();
            return Result.success(timeline);
        }
        return Result.success(new java.util.HashMap<>());
    }
    
    /**
     * 获取年份列表
     */
    @GetMapping("/years")
    public Result<List<String>> getYearList() {
        if (archiveService instanceof com.blog.service.impl.ArchiveServiceImpl) {
            com.blog.service.impl.ArchiveServiceImpl impl = 
                (com.blog.service.impl.ArchiveServiceImpl) archiveService;
            List<String> years = impl.getYearList();
            return Result.success(years);
        }
        return Result.success(new java.util.ArrayList<>());
    }
    
    /**
     * 根据年份获取归档数据
     */
    @GetMapping("/year/{year}")
    public Result<List<ArchiveVO>> getArchivesByYear(@PathVariable Integer year) {
        List<ArchiveVO> archives = archiveService.getArchivesByYear(year);
        return Result.success(archives);
    }
    
    /**
     * 根据年月获取文章列表
     */
    @GetMapping("/{year}/{month}")
    public Result<List<ArticleArchiveVO>> getArticlesByYearMonth(
            @PathVariable Integer year, 
            @PathVariable Integer month) {
        
        List<ArticleArchiveVO> articles = archiveService.getArticlesByYearMonth(year, month);
        return Result.success(articles);
    }
    
    /**
     * 获取最近N个月的归档数据
     */
    @GetMapping("/recent")
    public Result<List<ArchiveVO>> getRecentArchives(
            @RequestParam(defaultValue = "6") Integer limit) {
        
        if (archiveService instanceof com.blog.service.impl.ArchiveServiceImpl) {
            com.blog.service.impl.ArchiveServiceImpl impl = 
                (com.blog.service.impl.ArchiveServiceImpl) archiveService;
            List<ArchiveVO> recentArchives = impl.getRecentArchives(limit);
            return Result.success(recentArchives);
        }
        return Result.success(new java.util.ArrayList<>());
    }
    
    /**
     * 获取归档数量
     */
    @GetMapping("/count")
    public Result<Map<String, Long>> getArchiveCount() {
        List<ArchiveVO> archives = archiveService.getAllArchives();
        
        // 计算总文章数
        long totalArticles = archives.stream()
                .mapToLong(archive -> archive.getArticleCount())
                .sum();
        
        // 计算年份和月份数量
        long totalYears = archives.stream()
                .map(ArchiveVO::getYear)
                .distinct()
                .count();
        
        Map<String, Long> counts = new java.util.HashMap<>();
        counts.put("totalArchives", (long) archives.size());
        counts.put("totalArticles", totalArticles);
        counts.put("totalYears", totalYears);
        
        return Result.success();
    }
}