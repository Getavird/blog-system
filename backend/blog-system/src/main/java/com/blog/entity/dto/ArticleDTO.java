package com.blog.entity.dto;

import lombok.Data;
import java.util.List;

@Data
public class ArticleDTO {
    private Integer id;
    private String title;
    private String content;
    private String summary;
    private Integer categoryId;
    private List<String> tagNames; // 前端传递的标签名称列表
}