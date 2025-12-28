package com.blog.service.impl;

import com.blog.common.PageResult;
import com.blog.dao.UserMapper;
import com.blog.entity.Article;
import com.blog.entity.ChangePasswordRequest;
import com.blog.entity.User;
import com.blog.entity.vo.ArticlePublicVO;
import com.blog.entity.vo.UserProfileVO;
import com.blog.entity.vo.UserPublicVO;
import com.blog.entity.vo.UserStatsVO;
import com.blog.service.ArticleService;
import com.blog.service.FollowService;
import com.blog.service.UserService;
import com.blog.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FollowService followService;

    @Autowired
    private ArticleService articleService;

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

            // 5. 不返回密码（安全考虑）
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
                System.out.println("   - 更新简介: "
                        + (user.getBio().length() > 50 ? user.getBio().substring(0, 50) + "..." : user.getBio()));
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

    /**
     * 获取用户个人中心信息
     */
    @Override
    public UserProfileVO getUserProfile(Integer userId) {
        try {
            System.out.println("📋 获取用户个人中心信息: ID=" + userId);

            // 1. 获取用户基本信息
            User user = userMapper.findById(userId);
            if (user == null) {
                System.out.println("❌ 用户不存在: ID=" + userId);
                throw new RuntimeException("用户不存在");
            }

            System.out.println("✅ 找到用户: " + user.getUsername());

            // 2. 创建UserProfileVO
            UserProfileVO profileVO = new UserProfileVO();

            // 3. 设置基本信息
            profileVO.setId(user.getId());
            profileVO.setUsername(user.getUsername());
            profileVO.setEmail(user.getEmail());
            profileVO.setAvatar(user.getAvatar());
            profileVO.setBio(user.getBio());
            profileVO.setCreateTime(user.getCreateTime());
            profileVO.setLastLoginTime(user.getLastLoginTime());
            profileVO.setLastLoginIp(user.getLastLoginIp());

            // 4. 判断在线状态（最后活动时间在5分钟内为在线）
            boolean isOnline = false;
            if (user.getLastActiveTime() != null) {
                java.time.Duration duration = java.time.Duration.between(
                        user.getLastActiveTime(),
                        java.time.LocalDateTime.now());
                long diffInMinutes = duration.toMinutes();
                isOnline = diffInMinutes < 5; // 5分钟内活动算在线
            }
            profileVO.setIsOnline(isOnline);

            // 5. 设置统计信息
            UserStatsVO statsVO = new UserStatsVO();
            statsVO.setArticleCount(user.getArticleCount() != null ? user.getArticleCount() : 0);
            statsVO.setLikeCount(user.getLikeCount() != null ? user.getLikeCount() : 0);
            statsVO.setViewCount(user.getViewCount() != null ? user.getViewCount() : 0);

            // 6. 获取关注和粉丝数量
            try {
                statsVO.setFollowingCount(followService.getFollowingCount(userId));
                statsVO.setFollowerCount(followService.getFollowerCount(userId));
            } catch (Exception e) {
                System.err.println("❌ 获取关注数量异常: " + e.getMessage());
                // 如果获取失败，设置为0
                statsVO.setFollowingCount(0);
                statsVO.setFollowerCount(0);
            }

            profileVO.setStats(statsVO);

            System.out.println("📊 用户统计信息: 文章=" + statsVO.getArticleCount() +
                    ", 获赞=" + statsVO.getLikeCount() +
                    ", 阅读=" + statsVO.getViewCount() +
                    ", 关注=" + statsVO.getFollowingCount() +
                    ", 粉丝=" + statsVO.getFollowerCount());

            return profileVO;

        } catch (RuntimeException e) {
            System.err.println("❌ 获取用户个人中心信息异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ 获取用户个人中心信息系统异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取用户信息失败");
        }
    }

    /**
     * 更新个人简介
     */
    @Override
    public boolean updateBio(Integer userId, String bio) {
        try {
            System.out.println("✏️ 更新用户个人简介: ID=" + userId);

            // 检查用户是否存在
            User user = userMapper.findById(userId);
            if (user == null) {
                System.out.println("❌ 用户不存在: ID=" + userId);
                return false;
            }

            // 更新用户对象
            user.setBio(bio);

            // 保存到数据库
            int result = userMapper.update(user);
            if (result > 0) {
                System.out.println("✅ 个人简介更新成功: ID=" + userId +
                        ", 新简介: " + (bio.length() > 50 ? bio.substring(0, 50) + "..." : bio));
                return true;
            } else {
                System.out.println("❌ 个人简介更新失败");
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ 更新个人简介异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新最后登录信息
     */
    @Override
    public void updateLastLogin(Integer userId, String ip) {
        try {
            System.out.println("🔐 更新用户最后登录信息: ID=" + userId + ", IP=" + ip);

            // 检查用户是否存在
            User user = userMapper.findById(userId);
            if (user == null) {
                System.out.println("⚠️ 用户不存在，跳过更新最后登录信息: ID=" + userId);
                return;
            }

            // 更新最后登录信息
            int result = userMapper.updateLastLogin(userId, java.time.LocalDateTime.now(), ip);
            if (result > 0) {
                System.out.println("✅ 最后登录信息更新成功");
            } else {
                System.out.println("⚠️ 最后登录信息更新失败（可能是字段不存在）");
            }

        } catch (Exception e) {
            System.err.println("❌ 更新最后登录信息异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 更新最后活动时间
     */
    @Override
    public void updateLastActive(Integer userId) {
        try {
            // 只在调试时打印，避免日志过多
            // System.out.println("🔄 更新用户最后活动时间: ID=" + userId);

            // 检查用户是否存在
            User user = userMapper.findById(userId);
            if (user == null) {
                // 调试时打印
                // System.out.println("⚠️ 用户不存在，跳过更新最后活动时间: ID=" + userId);
                return;
            }

            // 更新最后活动时间
            int result = userMapper.updateLastActive(userId, java.time.LocalDateTime.now());
            if (result <= 0) {
                System.out.println("⚠️ 最后活动时间更新失败（可能是字段不存在）");
            }

        } catch (Exception e) {
            System.err.println("❌ 更新最后活动时间异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public UserPublicVO getPublicUserInfo(String username) {
        try {
            User user = userMapper.findByUsername(username);
            if (user == null) {
                return null;
            }

            UserPublicVO publicVO = new UserPublicVO();
            publicVO.setId(user.getId());
            publicVO.setUsername(user.getUsername());
            publicVO.setAvatar(user.getAvatar());
            publicVO.setBio(user.getBio());
            publicVO.setCreateTime(user.getCreateTime());
            publicVO.setLastActiveTime(user.getLastActiveTime());

            // 获取统计信息
            UserStatsVO stats = new UserStatsVO();
            stats.setArticleCount(user.getArticleCount() != null ? user.getArticleCount() : 0);
            stats.setLikeCount(user.getLikeCount() != null ? user.getLikeCount() : 0);
            stats.setViewCount(user.getViewCount() != null ? user.getViewCount() : 0);
            stats.setFollowingCount(followService.getFollowingCount(user.getId()));
            stats.setFollowerCount(followService.getFollowerCount(user.getId()));

            publicVO.setStats(stats);

            return publicVO;

        } catch (Exception e) {
            System.err.println("❌ 获取公开用户信息异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PageResult<ArticlePublicVO> getPublicUserArticles(String username, Integer page, Integer size,
            Integer status) {
        try {
            // 1. 根据用户名获取用户
            User user = userMapper.findByUsername(username);
            if (user == null) {
                return new PageResult<>(0L, page, size, new ArrayList<>());
            }

            // 2. 获取文章列表
            List<Article> articles = articleService.getArticlesByUserIdAndStatus(user.getId(), status, page, size);

            // 3. 获取文章总数
            Long total = articleService.countArticlesByUserIdAndStatus(user.getId(), status);

            // 4. 转换为ArticlePublicVO
            List<ArticlePublicVO> articleVOs = articles.stream()
                    .map(this::convertToArticlePublicVO)
                    .collect(java.util.stream.Collectors.toList());

            return new PageResult<>(total, page, size, articleVOs);

        } catch (Exception e) {
            System.err.println("❌ 获取用户公开文章异常: " + e.getMessage());
            e.printStackTrace();
            return new PageResult<>(0L, page, size, new ArrayList<>());
        }
    }

    /**
     * 将Article转换为ArticlePublicVO（添加到UserServiceImpl类中）
     */
    private ArticlePublicVO convertToArticlePublicVO(Article article) {
        ArticlePublicVO vo = new ArticlePublicVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setCoverImage(article.getCoverImage());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setCommentCount(article.getCommentCount());
        vo.setCreateTime(article.getCreateTime());
        vo.setUpdateTime(article.getUpdateTime());
        vo.setAuthorName(article.getAuthorName());
        vo.setAuthorAvatar(article.getAuthorAvatar());

        // 获取标签
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            String[] tagNames = article.getTags().split(",");
            List<com.blog.entity.Tag> tags = java.util.Arrays.stream(tagNames)
                    .map(tagName -> {
                        com.blog.entity.Tag tag = new com.blog.entity.Tag();
                        tag.setName(tagName.trim());
                        return tag;
                    })
                    .collect(java.util.stream.Collectors.toList());
            vo.setTags(tags);
        }

        // 获取分类信息
        if (article.getCategoryName() != null) {
            com.blog.entity.Category category = new com.blog.entity.Category();
            category.setId(article.getCategoryId());
            category.setName(article.getCategoryName());
            vo.setCategory(category);
        }

        return vo;
    }

    @Override
    public UserStatsVO getPublicUserStats(String username) {
        try {
            User user = userMapper.findByUsername(username);
            if (user == null) {
                return null;
            }

            UserStatsVO stats = new UserStatsVO();
            stats.setArticleCount(user.getArticleCount() != null ? user.getArticleCount() : 0);
            stats.setLikeCount(user.getLikeCount() != null ? user.getLikeCount() : 0);
            stats.setViewCount(user.getViewCount() != null ? user.getViewCount() : 0);
            stats.setFollowingCount(followService.getFollowingCount(user.getId()));
            stats.setFollowerCount(followService.getFollowerCount(user.getId()));

            return stats;

        } catch (Exception e) {
            System.err.println("❌ 获取用户公开统计异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }
}