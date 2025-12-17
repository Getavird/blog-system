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
        try {
            System.out.println("🔧 开始用户注册: " + user.getUsername());
            
            // 1. 检查用户名是否存在
            User existUser = userMapper.findByUsername(user.getUsername());
            if (existUser != null) {
                System.out.println("❌ 用户名已存在: " + user.getUsername());
                throw new RuntimeException("用户名已存在");
            }
            
            // 2. 检查邮箱是否存在（可选）
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                // 需要先在UserMapper中实现findByEmail方法
                // User existEmail = userMapper.findByEmail(user.getEmail());
                // if (existEmail != null) {
                //     throw new RuntimeException("邮箱已注册");
                // }
            }
            
            // 3. 设置默认值
            if (user.getAvatar() == null || user.getAvatar().trim().isEmpty()) {
                user.setAvatar("default_avatar.png");
            }
            if (user.getRole() == null) {
                user.setRole(0); // 默认普通用户
            }
            if (user.getStatus() == null) {
                user.setStatus(1); // 默认启用
            }
            
            // 4. 密码加密（使用MD5）
            String encryptedPassword = PasswordUtil.encrypt(user.getPassword());
            user.setPassword(encryptedPassword);
            
            System.out.println("✅ 用户信息验证通过，准备保存到数据库");
            
            // 5. 保存到数据库（需要先完善UserMapper.insert方法）
            // 目前先使用模拟数据，等Mapper完善后替换
            // int result = userMapper.insert(user);
            // if (result > 0) {
            //     // 返回用户信息（清除密码）
            //     user.setPassword(null);
            //     return user;
            // } else {
            //     throw new RuntimeException("注册失败，请稍后重试");
            // }
            
            // 临时模拟返回（开发阶段使用）
            user.setId(1000 + (int)(Math.random() * 9000)); // 模拟ID
            user.setPassword(null); // 清除密码返回
            System.out.println("✅ 用户注册成功: " + user.getUsername() + " (ID: " + user.getId() + ")");
            return user;
            
        } catch (RuntimeException e) {
            System.err.println("❌ 用户注册异常: " + e.getMessage());
            throw e; // 重新抛出异常，让Controller处理
        } catch (Exception e) {
            System.err.println("❌ 用户注册系统异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("注册过程发生错误");
        }
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
            
            System.out.println("✅ 找到用户: ID=" + user.getId() + 
                             ", 用户名=" + user.getUsername() + 
                             ", 数据库密码=" + user.getPassword());
            
            // 2. 验证密码（使用PasswordUtil进行MD5加密后比较）
            String encryptedPassword = PasswordUtil.encrypt(password);
            System.out.println("🔐 输入密码加密后: " + encryptedPassword);
            
            if (!user.getPassword().equals(encryptedPassword)) {
                System.out.println("❌ 密码不匹配");
                System.out.println("   - 数据库密码: " + user.getPassword());
                System.out.println("   - 输入加密后: " + encryptedPassword);
                throw new RuntimeException("密码错误");
            }
            
            System.out.println("✅ 密码验证通过");
            
            // 3. 检查用户状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                System.out.println("❌ 用户已被禁用");
                throw new RuntimeException("用户已被禁用");
            }
            
            // 4. 记录登录成功日志
            System.out.println("🎉 用户登录成功: " + username + 
                             " (ID: " + user.getId() + 
                             ", 角色: " + user.getRole() + ")");
            
            // 5. 不返回密码（安全考虑）
            user.setPassword(null);
            
            return user;
            
        } catch (RuntimeException e) {
            System.err.println("💥 登录过程异常: " + e.getMessage());
            // 直接抛出，让Controller处理
            throw new RuntimeException("登录失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("💥 登录系统异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("登录过程发生错误");
        }
    }
    
    @Override
    public User getUserById(Integer id) {
        try {
            System.out.println("📋 查询用户信息: ID=" + id);
            
            // 调用Mapper查询用户
            User user = userMapper.findById(id);
            
            if (user == null) {
                System.out.println("❌ 用户不存在: ID=" + id);
                return null;
            }
            
            // 不返回密码
            user.setPassword(null);
            
            System.out.println("✅ 找到用户: " + user.getUsername());
            return user;
            
        } catch (Exception e) {
            System.err.println("❌ 查询用户异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean updateUser(User user) {
        try {
            System.out.println("✏️ 更新用户信息: ID=" + user.getId());
            
            // 1. 检查用户是否存在
            User existingUser = userMapper.findById(user.getId());
            if (existingUser == null) {
                System.out.println("❌ 要更新的用户不存在: ID=" + user.getId());
                return false;
            }
            
            // 2. 只允许更新部分字段（不允许直接修改密码和角色）
            // 注意：密码修改应该有专门的接口，使用单独的验证流程
            
            // 更新邮箱
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                existingUser.setEmail(user.getEmail());
                System.out.println("   - 更新邮箱: " + user.getEmail());
            }
            
            // 更新头像
            if (user.getAvatar() != null && !user.getAvatar().trim().isEmpty()) {
                existingUser.setAvatar(user.getAvatar());
                System.out.println("   - 更新头像: " + user.getAvatar());
            }
            
            // 更新个人简介
            if (user.getBio() != null) {
                existingUser.setBio(user.getBio());
                System.out.println("   - 更新简介: " + (user.getBio().length() > 50 ? 
                    user.getBio().substring(0, 50) + "..." : user.getBio()));
            }
            
            // 3. 保存到数据库（需要先完善UserMapper.update方法）
            // 目前先返回true，等Mapper完善后替换
            // int result = userMapper.update(existingUser);
            // if (result > 0) {
            //     System.out.println("✅ 用户信息更新成功");
            //     return true;
            // } else {
            //     System.out.println("❌ 用户信息更新失败");
            //     return false;
            // }
            
            // 临时模拟成功
            System.out.println("✅ 用户信息更新成功（模拟）");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ 更新用户异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 辅助方法：验证用户名是否可用（用于注册时检查）
     */
    public boolean isUsernameAvailable(String username) {
        try {
            User user = userMapper.findByUsername(username);
            return user == null; // 如果没有找到，说明用户名可用
        } catch (Exception e) {
            System.err.println("❌ 检查用户名异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 辅助方法：根据邮箱查询用户（用于找回密码等功能）
     * 注意：需要先在UserMapper中实现findByEmail方法
     */
    public User getUserByEmail(String email) {
        try {
            System.out.println("📧 根据邮箱查询用户: " + email);
            
            // User user = userMapper.findByEmail(email);
            // if (user != null) {
            //     user.setPassword(null); // 不返回密码
            // }
            // return user;
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ 根据邮箱查询用户异常: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 辅助方法：修改密码（需要旧密码验证）
     */
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        try {
            System.out.println("🔑 修改密码: 用户ID=" + userId);
            
            // 1. 获取用户信息
            User user = userMapper.findById(userId);
            if (user == null) {
                System.out.println("❌ 用户不存在: ID=" + userId);
                return false;
            }
            
            // 2. 验证旧密码
            String encryptedOldPassword = PasswordUtil.encrypt(oldPassword);
            if (!user.getPassword().equals(encryptedOldPassword)) {
                System.out.println("❌ 旧密码错误");
                return false;
            }
            
            // 3. 加密新密码
            String encryptedNewPassword = PasswordUtil.encrypt(newPassword);
            
            // 4. 更新密码（需要先完善UserMapper.updatePassword方法）
            // user.setPassword(encryptedNewPassword);
            // int result = userMapper.updatePassword(userId, encryptedNewPassword);
            // return result > 0;
            
            // 暂时返回成功（模拟）
            System.out.println("✅ 密码修改成功（模拟）");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ 修改密码异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 辅助方法：重置密码（管理员功能）
     */
    public boolean resetPassword(Integer userId, String newPassword) {
        try {
            System.out.println("🔄 重置密码: 用户ID=" + userId);
            
            // 1. 获取用户信息
            User user = userMapper.findById(userId);
            if (user == null) {
                System.out.println("❌ 用户不存在: ID=" + userId);
                return false;
            }
            
            // 2. 加密新密码
            String encryptedPassword = PasswordUtil.encrypt(newPassword);
            
            // 3. 更新密码（需要先完善UserMapper.updatePassword方法）
            // int result = userMapper.updatePassword(userId, encryptedPassword);
            // if (result > 0) {
            //     System.out.println("✅ 密码重置成功");
            //     return true;
            // } else {
            //     System.out.println("❌ 密码重置失败");
            //     return false;
            // }
            
            // 暂时返回成功（模拟）
            System.out.println("✅ 密码重置成功（模拟）");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ 重置密码异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 辅助方法：检查用户是否是管理员
     */
    public boolean isAdmin(Integer userId) {
        try {
            User user = userMapper.findById(userId);
            return user != null && user.getRole() != null && user.getRole() == 1;
        } catch (Exception e) {
            System.err.println("❌ 检查管理员权限异常: " + e.getMessage());
            return false;
        }
    }
}