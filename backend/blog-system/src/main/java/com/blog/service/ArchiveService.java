package com.blog.service;

import com.blog.entity.vo.ArchiveVO;
import com.blog.entity.vo.ArticleArchiveVO;
import java.util.List;
import java.util.Map;

public interface ArchiveService {
    
    List<ArchiveVO> getAllArchives();
    Map<String, Object> getArchiveStats();
    List<ArchiveVO> getArchivesByYear(Integer year);
    List<ArticleArchiveVO> getArticlesByYearMonth(Integer year, Integer month);
    
    // 新增方法
    Map<String, Object> getArchiveOverview(); // 归档页面概览
    
    List<Integer> getAvailableYears(); // 获取有文章的年份列表
    
    Map<String, Object> getYearStats(Integer year); // 获取某年的详细统计
    
    ArchiveVO getArchiveDetail(Integer year, Integer month); // 获取某月归档详情
}