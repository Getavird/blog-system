package com.blog.service;

import com.blog.entity.ChangePasswordRequest;
import com.blog.entity.User;
import com.blog.entity.vo.UserProfileVO;

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
     * 修改用户密码
     */
    boolean changePassword(Integer userId, ChangePasswordRequest request);

    /**
     * 更新用户头像
     */
    boolean updateUserAvatar(User user);

    /**
     * 获取用户个人中心信息
     */
    UserProfileVO getUserProfile(Integer userId);

    /**
     * 更新个人简介
     */
    boolean updateBio(Integer userId, String bio);

    /**
     * 更新最后登录信息
     */
    void updateLastLogin(Integer userId, String ip);

    /**
     * 更新最后活动时间（用于判断在线状态）
     */
    void updateLastActive(Integer userId);
}