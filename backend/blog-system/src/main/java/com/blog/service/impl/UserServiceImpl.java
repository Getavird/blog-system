package com.blog.service.impl;

import com.blog.dao.UserMapper;
import com.blog.entity.ChangePasswordRequest;
import com.blog.entity.User;
import com.blog.service.UserService;
import com.blog.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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
            
            // 2. 检查邮箱是否存在
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                User existEmail = userMapper.findByEmail(user.getEmail());
                if (existEmail != null) {
                    throw new RuntimeException("邮箱已注册");
                }
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
            if (user.getBio() == null) {
                user.setBio("");
            }
            
            // 4. 密码加密
            String encryptedPassword = PasswordUtil.encrypt(user.getPassword());
            user.setPassword(encryptedPassword);
            
            System.out.println("✅ 用户信息验证通过，准备保存到数据库");
            
            // 5. 保存到数据库
            int result = userMapper.insert(user);
            if (result > 0) {
                System.out.println("✅ 用户注册成功: " + user.getUsername() + " (ID: " + user.getId() + ")");
                // 返回用户信息（清除密码）
                user.setPassword(null);
                return user;
            } else {
                throw new RuntimeException("注册失败，请稍后重试");
            }
            
        } catch (RuntimeException e) {
            System.err.println("❌ 用户注册异常: " + e.getMessage());
            throw e;
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
            
            // 2. 验证密码
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
            
            // 5. 更新最后登录时间（需要先在User实体和表中添加lastLoginTime字段）
            // 暂时跳过，保持简单
            
            // 6. 不返回密码（安全考虑）
            user.setPassword(null);
            
            return user;
            
        } catch (RuntimeException e) {
            System.err.println("💥 登录过程异常: " + e.getMessage());
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
            // 更新邮箱
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                // 检查邮箱是否被其他用户使用
                User emailUser = userMapper.findByEmail(user.getEmail());
                if (emailUser != null && !emailUser.getId().equals(user.getId())) {
                    throw new RuntimeException("邮箱已被其他用户使用");
                }
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
            
            // 3. 保存到数据库
            int result = userMapper.update(existingUser);
            if (result > 0) {
                System.out.println("✅ 用户信息更新成功");
                return true;
            } else {
                System.out.println("❌ 用户信息更新失败");
                return false;
            }
            
        } catch (RuntimeException e) {
            System.err.println("❌ 更新用户异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ 更新用户系统异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        
    }
    /**
     * 更新用户头像
     */
    @Override
    public boolean updateUserAvatar(User user) {
        try {
            System.out.println("🖼️ 更新用户头像: ID=" + user.getId());
            
            // 1. 检查用户是否存在
            User existingUser = userMapper.findById(user.getId());
            if (existingUser == null) {
                System.out.println("❌ 要更新头像的用户不存在: ID=" + user.getId());
                return false;
            }
            
            // 2. 更新头像字段
            existingUser.setAvatar(user.getAvatar());
            
            // 3. 保存到数据库
            int result = userMapper.update(existingUser);
            if (result > 0) {
                System.out.println("✅ 用户头像更新成功: ID=" + user.getId() + 
                                 ", 新头像: " + user.getAvatar());
                return true;
            } else {
                System.out.println("❌ 用户头像更新失败");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ 更新用户头像异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 修改密码（需要旧密码验证）
     */
@Override
public boolean changePassword(Integer userId, ChangePasswordRequest request) {
    try {
        System.out.println("🔐 修改密码 - 用户ID: " + userId);
        
        // 1. 验证参数
        if (request == null) {
            throw new RuntimeException("请求参数不能为空");
        }
        
        if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
            throw new RuntimeException("原密码不能为空");
        }
        
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }
        
        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
            throw new RuntimeException("确认密码不能为空");
        }
        
        // 2. 验证新密码长度
        if (request.getNewPassword().length() < 6) {
            throw new RuntimeException("新密码长度至少6位");
        }
        
        // 3. 验证新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("新密码和确认密码不一致");
        }
        
        // 4. 获取用户信息
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        System.out.println("👤 用户信息 - 用户名: " + user.getUsername() + 
                         ", 数据库密码: " + user.getPassword());
        
        // 5. 验证原密码
        String encryptedOldPassword = PasswordUtil.encrypt(request.getOldPassword());
        System.out.println("🔍 原密码验证 - 输入加密: " + encryptedOldPassword + 
                         ", 数据库存储: " + user.getPassword());
        
        if (!user.getPassword().equals(encryptedOldPassword)) {
            System.out.println("❌ 原密码错误");
            throw new RuntimeException("原密码错误");
        }
        
        // 6. 验证新密码是否与原密码相同
        String encryptedNewPassword = PasswordUtil.encrypt(request.getNewPassword());
        if (user.getPassword().equals(encryptedNewPassword)) {
            throw new RuntimeException("新密码不能与原密码相同");
        }
        
        // 7. 检查新密码强度（可选）
        int strength = PasswordUtil.checkPasswordStrength(request.getNewPassword());
        System.out.println("📊 新密码强度: " + strength + "级");
        
        // 8. 更新密码
        int result = userMapper.updatePassword(userId, encryptedNewPassword);
        
        if (result > 0) {
            System.out.println("✅ 密码修改成功 - 用户ID: " + userId);
            
            // 9. 记录密码修改日志（可选）
            System.out.println("📝 密码修改记录: " + user.getUsername() + 
                             " 于 " + new java.util.Date() + " 修改密码");
            return true;
        } else {
            System.out.println("❌ 密码更新失败");
            return false;
        }
        
    } catch (RuntimeException e) {
        System.err.println("❌ 修改密码业务异常: " + e.getMessage());
        throw e;
    } catch (Exception e) {
        System.err.println("❌ 修改密码系统异常: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("修改密码失败，请稍后重试");
    }
}
    
    /**
     * 检查用户名是否可用
     */
    public boolean isUsernameAvailable(String username) {
        try {
            User user = userMapper.findByUsername(username);
            return user == null;
        } catch (Exception e) {
            System.err.println("❌ 检查用户名异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查是否是管理员
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
    
    /**
     * 获取用户统计数据
     */
    public User getUserWithStats(Integer userId) {
        User user = userMapper.findById(userId);
        if (user != null) {
            user.setPassword(null);
            // 这里可以添加统计信息查询，如文章数、获赞数等
        }
        return user;
    }
}