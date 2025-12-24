package com.blog.entity.vo;

import lombok.Data;

@Data
public class FollowCountVO {
    private Integer followingCount;  // 关注数
    private Integer followerCount;   // 粉丝数
}