package com.blog.entity.vo;

import lombok.Data;

@Data
public class UserStatsVO {
    private Integer articleCount = 0;  // 文章数
    private Integer likeCount = 0;     // 获赞数
    private Integer viewCount = 0;     // 阅读数
    private Integer followingCount = 0; // 关注数
    private Integer followerCount = 0; // 粉丝数
}