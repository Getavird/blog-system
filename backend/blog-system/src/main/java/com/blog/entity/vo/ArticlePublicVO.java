package com.blog.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import com.blog.entity.Category;
import com.blog.entity.Tag;

@Data
public class ArticlePublicVO {
    private Integer id;
    private String title;
    private String summary;
    private String coverImage;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Tag> tags;
    private Category category;
    private String authorName;
    private String authorAvatar;
    
}