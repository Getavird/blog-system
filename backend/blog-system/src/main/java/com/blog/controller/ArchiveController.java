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
     * 获取归档页面概览（包含统计和最近数据）
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getArchiveOverview() {
        Map<String, Object> overview = archiveService.getArchiveOverview();
        return Result.success(overview);
    }
    
    /**
     * 获取可用年份列表
     */
    @GetMapping("/years")
    public Result<List<Integer>> getAvailableYears() {
        List<Integer> years = archiveService.getAvailableYears();
        return Result.success(years);
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
     * 获取某年份的详细统计
     */
    @GetMapping("/year/{year}/stats")
    public Result<Map<String, Object>> getYearStats(@PathVariable Integer year) {
        Map<String, Object> stats = archiveService.getYearStats(year);
        return Result.success(stats);
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
     * 获取归档详情（某年某月）
     */
    @GetMapping("/{year}/{month}/detail")
    public Result<ArchiveVO> getArchiveDetail(
            @PathVariable Integer year, 
            @PathVariable Integer month) {
        
        ArchiveVO archive = archiveService.getArchiveDetail(year, month);
        if (archive == null) {
            return Result.notFound("该月没有文章");
        }
        return Result.success(archive);
    }
    
    /**
     * 获取最活跃年份
     */
    @GetMapping("/most-active-year")
    public Result<Map<String, Object>> getMostActiveYear() {
        Map<String, Object> stats = archiveService.getArchiveStats();
        Integer mostActiveYear = (Integer) stats.get("most_active_year");
        
        if (mostActiveYear != null) {
            Map<String, Object> result = archiveService.getYearStats(mostActiveYear);
            result.put("year", mostActiveYear);
            result.put("isMostActive", true);
            return Result.success(result);
        }
        
        return Result.success();
    }
    
    /**
     * 获取最近几年的统计
     */
    @GetMapping("/recent-stats")
    public Result<List<Map<String, Object>>> getRecentYearStats(
            @RequestParam(defaultValue = "5") Integer limit) {
        
        if (archiveService instanceof com.blog.service.impl.ArchiveServiceImpl) {
            com.blog.service.impl.ArchiveServiceImpl impl = 
                (com.blog.service.impl.ArchiveServiceImpl) archiveService;
            List<Map<String, Object>> stats = impl.getRecentYearStats(limit);
            return Result.success(stats);
        }
        return Result.success(new java.util.ArrayList<>());
    }
    
    /**
     * 获取年份对比统计
     */
    @GetMapping("/year-comparison")
    public Result<Map<String, Object>> getYearComparisonStats() {
        if (archiveService instanceof com.blog.service.impl.ArchiveServiceImpl) {
            com.blog.service.impl.ArchiveServiceImpl impl = 
                (com.blog.service.impl.ArchiveServiceImpl) archiveService;
            Map<String, Object> comparison = impl.getYearComparisonStats();
            return Result.success(comparison);
        }
        return Result.success(new java.util.HashMap<>());
    }
    
    /**
     * 搜索归档（按标题或内容）
     */
    @GetMapping("/search")
    public Result<List<ArticleArchiveVO>> searchArchives(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        // 这里需要实现搜索逻辑
        // 暂时返回空列表
        return Result.success(new java.util.ArrayList<>());
    }
}