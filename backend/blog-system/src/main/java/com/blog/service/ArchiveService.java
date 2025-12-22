package com.blog.service;

import com.blog.entity.vo.ArchiveVO;
import com.blog.entity.vo.ArticleArchiveVO;
import java.util.List;
import java.util.Map;

public interface ArchiveService {
    
    /**
     * 获取所有归档数据（按年月分组）
     */
    List<ArchiveVO> getAllArchives();
    
    /**
     * 获取归档统计信息
     */
    Map<String, Object> getArchiveStats();
    
    /**
     * 根据年份获取归档数据
     */
    List<ArchiveVO> getArchivesByYear(Integer year);
    
    /**
     * 根据年月获取文章列表
     */
    List<ArticleArchiveVO> getArticlesByYearMonth(Integer year, Integer month);
}