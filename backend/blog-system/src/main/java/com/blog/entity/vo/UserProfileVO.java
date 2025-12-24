package com.blog.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserProfileVO {
    // 基本信息
    private Integer id;
    private String username;
    private String email;
    private String avatar;
    private String bio;
    
    // 统计信息
    private UserStatsVO stats;
    
    // 账户状态
    private LocalDateTime createTime;      // 注册时间
    private LocalDateTime lastLoginTime;   // 最后登录时间
    private String lastLoginIp;   // 最后登录IP
    private Boolean isOnline;     // 是否在线
}