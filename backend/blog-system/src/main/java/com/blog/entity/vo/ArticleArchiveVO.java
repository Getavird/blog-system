package com.blog.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleArchiveVO {
    private Integer id;
    private String title;
    private LocalDateTime createTime;
    private String categoryName;
    private String tags;
    
    // 新增字段
    private Integer viewCount = 0;    // 阅读量
    private Integer likeCount = 0;    // 点赞数
    private Integer commentCount = 0; // 评论数
    private String authorName;        // 作者名
    
    // 新增：用于前端显示的格式化字段
    private String formatDate;        // 格式化日期（如"10-28"）
    private String formatTime;        // 格式化时间（如"14:30"）
}