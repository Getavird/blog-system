package com.blog.entity.vo;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FollowVO {
    private Integer id;             // 关注关系ID
    private Integer userId;         // 用户ID
    private String username;        // 用户名
    private String avatar;          // 头像
    private String bio;             // 个人简介
    private LocalDateTime followTime; // 关注时间
    private Boolean isFollowing;    // 我是否关注他（用于粉丝列表）
    private Boolean isFollowed;     // 他是否关注我（互相关注）
}