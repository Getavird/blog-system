package com.blog.entity;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    /**
     * 原密码
     */
    private String oldPassword;
    
    /**
     * 新密码
     */
    private String newPassword;
    
    /**
     * 确认密码
     */
    private String confirmPassword;
}