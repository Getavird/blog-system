package com.blog.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
/**
 * 文章详情页面响应DTO
 */
@Data
public class ArticleDetailDTO {
    /**
     * 文章信息
     */
    private ArticleInfo article;
    
    /**
     * 作者信息
     */
    private AuthorInfo author;
    
    /**
     * 当前用户状态
     */
    private UserStatus userStatus;
    
    /**
     * 评论统计
     */
    private CommentStats commentStats;
    
    @Data
    public static class ArticleInfo {
        private Integer id;
        private String title;
        private String content;
        private String summary;
        private String coverImage;
        private Integer viewCount;
        private Integer likeCount;
        private Integer commentCount;
        private String categoryName;
        private List<String> tags;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }
    
    @Data
    public static class AuthorInfo {
        private Integer id;
        private String username;
        private String avatar;
        private String bio;
        private Integer articleCount;     // 文章数
        private Integer totalLikeCount;   // 总获赞数（所有文章）
        private Integer followerCount;    // 粉丝数
        private Boolean isOnline;         // 是否在线
    }
    
    @Data
    public static class UserStatus {
        private Boolean isLiked;          // 是否已点赞
        private Boolean isFollowing;      // 是否已关注作者
        private Boolean isAuthor;         // 是否是作者本人
    }
    
    @Data
    public static class CommentStats {
        private Integer total;            // 评论总数
        private Integer topLevelCount;    // 顶级评论数
    }
}