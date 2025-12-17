package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.User;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器（基础框架）
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        // 明天实现具体逻辑
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername(user.getUsername());
        mockUser.setEmail(user.getEmail());
        return Result.success("注册功能已预留", mockUser);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
  public Result<User> login(@RequestBody User user) {
        // 业务逻辑
        return Result.success("登录成功", user);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo() {
        // 明天实现具体逻辑
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("admin");
        mockUser.setRole(1);
        return Result.success("用户信息功能已预留", mockUser);
    }
}