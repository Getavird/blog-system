package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    /**
     * 用户ID
     */
    private Integer id;
    
    /**
     * 用户名（唯一）
     */
    private String username;
    
    /**
     * 密码（加密存储）
     */
    private String password;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像URL
     */
    private String avatar = "default_avatar.png";
    
    /**
     * 用户角色：0-普通用户，1-管理员
     */
    private Integer role = 0;
    
    /**
     * 用户状态：0-禁用，1-启用
     */
    private Integer status = 1;
    
    /**
     * 个人简介
     */
    private String bio;
    
    /**
     * 默认构造器
     */
    public User() {}
    
    /**
     * 带参数的构造器（用于注册）
     */
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}