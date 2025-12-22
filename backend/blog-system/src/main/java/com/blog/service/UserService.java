package com.blog.service;

import com.blog.entity.ChangePasswordRequest;
import com.blog.entity.User;

public interface UserService {
    
    /**
     * 用户注册
     */
    User register(User user);
    
    /**
     * 用户登录
     */
    User login(String username, String password);
    
    /**
     * 根据ID获取用户
     */
    User getUserById(Integer id);
    
    /**
     * 更新用户信息
     */
    boolean updateUser(User user);

    /**
        修改用户密码
    */
    boolean changePassword(Integer userId, ChangePasswordRequest request);

    /**
     * 更新用户头像
     */
    boolean updateUserAvatar(User user);
}