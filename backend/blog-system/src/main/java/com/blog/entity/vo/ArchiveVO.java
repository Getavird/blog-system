package com.blog.entity.vo;

import lombok.Data;
import java.util.List;

@Data
public class ArchiveVO {
    private String year;
    private String month;
    private List<ArticleArchiveVO> articles;
    private Integer articleCount;
    
    // 新增字段
    private Integer viewCount = 0;    // 该月文章总阅读量
    private Integer likeCount = 0;    // 该月文章总点赞数
    
    // 新增：用于前端显示
    private String monthName;         // 月份名称（如"十月"）
    private String shortMonthName;    // 短月份（如"10月"）
}