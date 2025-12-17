package com.blog.service.impl;

import com.blog.dao.UserMapper;
import com.blog.entity.User;
import com.blog.service.UserService;
import com.blog.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public User register(User user) {
        // 检查用户名是否存在
        User existUser = userMapper.findByUsername(user.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 密码加密
        String encryptedPassword = PasswordUtil.encrypt(user.getPassword());
        user.setPassword(encryptedPassword);
        
        // 保存用户（需要先完善UserMapper的insert方法）
        // int result = userMapper.insert(user);
        // return result > 0 ? user : null;
        
        // 暂时返回模拟数据
        user.setId(1001);
        return user;
    }
    
    @Override
public User login(String username, String password) {
    try {
        System.out.println("🔍 开始用户登录验证: " + username);
        
        // 1. 根据用户名查询用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            System.out.println("❌ 用户不存在: " + username);
            throw new RuntimeException("用户不存在");
        }
        
        System.out.println("✅ 找到用户: ID=" + user.getId() + ", 用户名=" + user.getUsername());
        
        // 2. 验证密码（使用PasswordUtil）
        String encryptedPassword = PasswordUtil.encrypt(password);
        if (!user.getPassword().equals(encryptedPassword)) {
            System.out.println("❌ 密码不匹配");
            throw new RuntimeException("密码错误");
        }
        
        System.out.println("✅ 密码验证通过");
        
        // 3. 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            System.out.println("❌ 用户已被禁用");
            throw new RuntimeException("用户已被禁用");
        }
        
        // 4. 不返回密码（安全）
        user.setPassword(null);
        
        System.out.println("🎉 用户登录成功: " + username);
        return user;
        
    } catch (Exception e) {
        System.err.println("💥 登录过程异常: " + e.getMessage());
        throw new RuntimeException("登录失败: " + e.getMessage());
    }
}
    
    @Override
    public User getUserById(Integer id) {
        // 调用Mapper查询用户
        return userMapper.findById(id);
    }
    
    @Override
    public boolean updateUser(User user) {
        // 这里需要实现更新逻辑
        // 暂时返回true，表示更新成功
        // 实际应该：int result = userMapper.update(user);
        //           return result > 0;
        return true;
    }
}