package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.ChangePasswordRequest;
import com.blog.entity.User;
import com.blog.entity.vo.ArticlePublicVO;
import com.blog.entity.vo.UserProfileVO;
import com.blog.entity.vo.UserPublicVO;
import com.blog.entity.vo.UserStatsVO;
import com.blog.service.UserService;
import com.blog.utils.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器（现在实现真实逻辑）
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        try {
            User registeredUser = userService.register(user);
            return Result.success("注册成功", registeredUser);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody User user, HttpServletRequest request) {
        try {
            User loggedInUser = userService.login(user.getUsername(), user.getPassword());

            // 设置Session
            jakarta.servlet.http.HttpSession session = request.getSession();
            session.setAttribute(SessionUtil.SESSION_USER, loggedInUser);
            session.setAttribute(SessionUtil.SESSION_USERNAME, loggedInUser.getUsername());
            session.setAttribute(SessionUtil.SESSION_USER_ID, loggedInUser.getId());
            session.setAttribute(SessionUtil.SESSION_ROLE, loggedInUser.getRole());

            // 更新最后登录信息
            String ip = request.getRemoteAddr();
            userService.updateLastLogin(loggedInUser.getId(), ip);
            userService.updateLastActive(loggedInUser.getId());

            return Result.success("登录成功", loggedInUser);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    /**
 * 获取当前用户信息
 */
@GetMapping("/info")
public Result<User> getUserInfo(HttpServletRequest request) {
    User currentUser = SessionUtil.getCurrentUser(request);
    if (currentUser == null) {
        return Result.unauthorized("请先登录");
    }
    
    // 处理头像路径
    String avatar = currentUser.getAvatar();
    if (StringUtils.hasText(avatar)) {
        if (avatar.equals("default_avatar.png")) {
            currentUser.setAvatar("/static/images/default-avatars/default_avatar.png");
        } else if (!avatar.startsWith("/uploads/avatars/")) {
            currentUser.setAvatar("/uploads/avatars/" + avatar);
        }
    } else {
        currentUser.setAvatar("/static/images/default-avatars/default_avatar.png");
    }
    
    return Result.success(currentUser);
}

    /**
     * 获取个人中心完整信息
     */
    @GetMapping("/profile")
    public Result<UserProfileVO> getUserProfile(HttpServletRequest request) {
        try {
            User currentUser = SessionUtil.getCurrentUser(request);
            if (currentUser == null) {
                return Result.unauthorized("请先登录");
            }

            // 更新最后活动时间
            userService.updateLastActive(currentUser.getId());

            UserProfileVO profile = userService.getUserProfile(currentUser.getId());
            return Result.success("获取个人信息成功", profile);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ 获取个人中心信息异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取信息失败");
        }
    }

    /**
     * 更新个人简介
     */
    @PutMapping("/bio")
    public Result<String> updateBio(@RequestParam String bio,
            HttpServletRequest request) {
        try {
            User currentUser = SessionUtil.getCurrentUser(request);
            if (currentUser == null) {
                return Result.unauthorized("请先登录");
            }

            boolean success = userService.updateBio(currentUser.getId(), bio);
            if (success) {
                // 更新最后活动时间
                userService.updateLastActive(currentUser.getId());
                return Result.success("个人简介更新成功");
            } else {
                return Result.error("个人简介更新失败");
            }
        } catch (Exception e) {
            System.err.println("❌ 更新个人简介异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("更新失败");
        }
    }

    /**
     * 获取账户统计信息
     */
    @GetMapping("/stats")
    public Result<UserStatsVO> getUserStats(HttpServletRequest request) {
        try {
            User currentUser = SessionUtil.getCurrentUser(request);
            if (currentUser == null) {
                return Result.unauthorized("请先登录");
            }

            // 更新最后活动时间
            userService.updateLastActive(currentUser.getId());

            UserProfileVO profile = userService.getUserProfile(currentUser.getId());
            return Result.success("获取统计信息成功", profile.getStats());
        } catch (Exception e) {
            System.err.println("❌ 获取统计信息异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取统计信息失败");
        }
    }

    /**
     * 获取账户状态信息
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getUserStatus(HttpServletRequest request) {
        try {
            User currentUser = SessionUtil.getCurrentUser(request);
            if (currentUser == null) {
                return Result.unauthorized("请先登录");
            }

            // 更新最后活动时间
            userService.updateLastActive(currentUser.getId());

            UserProfileVO profile = userService.getUserProfile(currentUser.getId());

            Map<String, Object> status = new HashMap<>();
            status.put("createTime", profile.getCreateTime());
            status.put("lastLoginTime", profile.getLastLoginTime());
            status.put("lastLoginIp", profile.getLastLoginIp());
            status.put("isOnline", profile.getIsOnline());

            return Result.success("获取账户状态成功", status);
        } catch (Exception e) {
            System.err.println("❌ 获取账户状态异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取账户状态失败");
        }
    }

    /**
     * 用户退出登录（REST API方式）
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Result.success("退出成功");
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody User user, HttpServletRequest request) {
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 设置当前用户ID，防止修改他人信息
        user.setId(currentUser.getId());

        boolean success = userService.updateUser(user);
        if (success) {
            // 更新最后活动时间
            userService.updateLastActive(currentUser.getId());
        }
        return success ? Result.success("更新成功", user) : Result.error("更新失败");
    }

    /**
     * 修改用户密码
     */
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        try {
            // 1. 检查登录
            User currentUser = SessionUtil.getCurrentUser(httpRequest);
            if (currentUser == null) {
                return Result.unauthorized("请先登录");
            }

            // 2. 调用服务修改密码
            boolean success = userService.changePassword(currentUser.getId(), request);

            if (success) {
                // 3. 可选：修改密码后强制退出登录（安全考虑）
                jakarta.servlet.http.HttpSession session = httpRequest.getSession(false);
                if (session != null) {
                    session.invalidate();
                }

                return Result.success("密码修改成功，请重新登录");
            } else {
                return Result.error("密码修改失败");
            }

        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ 修改密码接口异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("系统异常，请稍后重试");
        }
    }

    /**
     * 获取公开用户信息
     * 路径: GET /api/user/public/{username}
     */
    @GetMapping("/public/{username}")
    public Result<UserPublicVO> getPublicUserInfo(@PathVariable String username) {
        try {
            UserPublicVO user = userService.getPublicUserInfo(username);
            if (user == null) {
                return Result.notFound("用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            System.err.println("❌ 获取公开用户信息失败: " + e.getMessage());
            return Result.error("获取用户信息失败");
        }
    }

    /**
     * 获取用户公开文章列表
     * 路径: GET /api/user/public/{username}/articles
     */
    @GetMapping("/public/{username}/articles")
    public Result<PageResult<ArticlePublicVO>> getPublicUserArticles(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 只获取已发布的文章 (status = 1)
            PageResult<ArticlePublicVO> articles = userService.getPublicUserArticles(username, page, size, 1);
            return Result.success("获取文章列表成功", articles);
        } catch (Exception e) {
            System.err.println("❌ 获取用户公开文章失败: " + e.getMessage());
            return Result.error("获取文章列表失败");
        }
    }

    /**
     * 获取用户公开统计
     * 路径: GET /api/user/public/{username}/stats
     */
    @GetMapping("/public/{username}/stats")
    public Result<UserStatsVO> getPublicUserStats(@PathVariable String username) {
        try {
            UserStatsVO stats = userService.getPublicUserStats(username);
            if (stats == null) {
                return Result.notFound("用户不存在");
            }
            return Result.success("获取用户统计成功", stats);
        } catch (Exception e) {
            System.err.println("❌ 获取用户统计失败: " + e.getMessage());
            return Result.error("获取统计信息失败");
        }
    }
    
}