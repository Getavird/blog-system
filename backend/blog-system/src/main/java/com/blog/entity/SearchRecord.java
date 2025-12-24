package com.blog.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SearchRecord {
    private Integer id;
    private String keyword;
    private Integer userId;
    private Integer searchCount;
    private LocalDateTime lastSearchTime;
    private LocalDateTime createTime;
}