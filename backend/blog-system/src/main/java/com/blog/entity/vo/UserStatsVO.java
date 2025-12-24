package com.blog.entity.vo;

import lombok.Data;

@Data
public class UserStatsVO {
    private Integer articleCount = 0;  // 文章数
    private Integer likeCount = 0;     // 获赞数
    private Integer viewCount = 0;     // 阅读数
    private Integer fanCount = 0;      // 粉丝数（暂时为0，因为没实现关注功能）
}