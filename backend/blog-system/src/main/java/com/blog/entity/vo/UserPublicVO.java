package com.blog.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserPublicVO {
    private Integer id;
    private String username;
    private String avatar;
    private String bio;
    private LocalDateTime createTime;
    private LocalDateTime lastActiveTime;
    private UserStatsVO stats;
}