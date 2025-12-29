package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.vo.FollowVO;
import com.blog.entity.vo.FollowCountVO;
import com.blog.service.FollowService;
import com.blog.service.UserService;
import com.blog.utils.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
public class FollowController {
    
    @Autowired
    private FollowService followService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 关注用户 - 支持用户名或用户ID
     */
    @PostMapping("/{identifier}")
    public Result<String> follow(@PathVariable String identifier, HttpServletRequest request) {
        try {
            // 1. 检查登录
            Integer currentUserId = SessionUtil.getCurrentUserId(request);
            if (currentUserId == null) {
                return Result.unauthorized("请先登录");
            }
            
            // 2. 解析identifier：可能是用户ID或用户名
            Integer targetUserId = parseUserId(identifier);
            if (targetUserId == null) {
                return Result.error("用户不存在");
            }
            
            // 3. 检查不能关注自己
            if (currentUserId.equals(targetUserId)) {
                return Result.error("不能关注自己");
            }
            
            // 4. 关注用户
            boolean success = followService.follow(currentUserId, targetUserId);
            if (success) {
                return Result.success("关注成功");
            } else {
                return Result.error("关注失败");
            }
            
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ 关注接口异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("关注失败，请稍后重试");
        }
    }
    
    /**
     * 取消关注 - 支持用户名或用户ID
     */
    @DeleteMapping("/{identifier}")
    public Result<String> unfollow(@PathVariable String identifier, HttpServletRequest request) {
        try {
            // 1. 检查登录
            Integer currentUserId = SessionUtil.getCurrentUserId(request);
            if (currentUserId == null) {
                return Result.unauthorized("请先登录");
            }
            
            // 2. 解析identifier：可能是用户ID或用户名
            Integer targetUserId = parseUserId(identifier);
            if (targetUserId == null) {
                return Result.error("用户不存在");
            }
            
            // 3. 取消关注
            boolean success = followService.unfollow(currentUserId, targetUserId);
            if (success) {
                return Result.success("取消关注成功");
            } else {
                return Result.error("取消关注失败");
            }
            
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ 取消关注接口异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("取消关注失败，请稍后重试");
        }
    }
    
    /**
     * 检查是否关注 - 支持用户名或用户ID
     */
    @GetMapping("/check/{identifier}")
    public Result<Map<String, Object>> checkFollowing(@PathVariable String identifier, 
                                                     HttpServletRequest request) {
        try {
            // 1. 检查登录
            Integer currentUserId = SessionUtil.getCurrentUserId(request);
            if (currentUserId == null) {
                return Result.unauthorized("请先登录");
            }
            
            // 2. 解析identifier：可能是用户ID或用户名
            Integer targetUserId = parseUserId(identifier);
            if (targetUserId == null) {
                return Result.error("用户不存在");
            }
            
            // 3. 检查是否关注
            boolean isFollowing = followService.isFollowing(currentUserId, targetUserId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("isFollowing", isFollowing);
            result.put("userId", targetUserId);
            
            return Result.success(result);
            
        } catch (Exception e) {
            System.err.println("❌ 检查关注状态接口异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("检查关注状态失败");
        }
    }
    
    /**
     * 获取关注列表（我关注的人）
     */
    @GetMapping("/following")
    public Result<List<FollowVO>> getFollowingList(HttpServletRequest request,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 1. 检查登录
            Integer currentUserId = SessionUtil.getCurrentUserId(request);
            if (currentUserId == null) {
                return Result.unauthorized("请先登录");
            }
            
            // 2. 获取关注列表
            List<FollowVO> followingList = followService.getFollowingList(currentUserId, page, size);
            
            return Result.success("获取关注列表成功", followingList);
            
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ 获取关注列表接口异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取关注列表失败");
        }
    }
    
    /**
     * 获取粉丝列表（关注我的人）
     */
    @GetMapping("/followers")
    public Result<List<FollowVO>> getFollowerList(HttpServletRequest request,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 1. 检查登录
            Integer currentUserId = SessionUtil.getCurrentUserId(request);
            if (currentUserId == null) {
                return Result.unauthorized("请先登录");
            }
            
            // 2. 获取粉丝列表
            List<FollowVO> followerList = followService.getFollowerList(currentUserId, page, size);
            
            return Result.success("获取粉丝列表成功", followerList);
            
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ 获取粉丝列表接口异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取粉丝列表失败");
        }
    }
    
    /**
     * 获取当前用户的关注数量统计
     */
    @GetMapping("/counts")
    public Result<FollowCountVO> getFollowCounts(HttpServletRequest request) {
        try {
            // 1. 检查登录
            Integer currentUserId = SessionUtil.getCurrentUserId(request);
            if (currentUserId == null) {
                return Result.unauthorized("请先登录");
            }
            
            // 2. 获取关注数量统计
            FollowCountVO countVO = followService.getFollowCount(currentUserId);
            
            return Result.success("获取关注数量成功", countVO);
            
        } catch (Exception e) {
            System.err.println("❌ 获取关注数量接口异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取关注数量失败");
        }
    }
    
    /**
     * 获取用户的关注数量（公开接口，无需登录） - 支持用户名或用户ID
     */
    @GetMapping("/counts/{identifier}")
    public Result<FollowCountVO> getUserFollowCounts(@PathVariable String identifier) {
        try {
            // 1. 解析identifier：可能是用户ID或用户名
            Integer userId = parseUserId(identifier);
            if (userId == null) {
                return Result.error("用户不存在");
            }
            
            // 2. 获取用户的关注数量统计
            FollowCountVO countVO = followService.getFollowCount(userId);
            
            return Result.success("获取用户关注数量成功", countVO);
            
        } catch (Exception e) {
            System.err.println("❌ 获取用户关注数量接口异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取用户关注数量失败");
        }
    }
    
    /**
     * 解析用户标识符为用户ID
     * @param identifier 用户ID（数字）或用户名
     * @return 用户ID，如果不存在则返回null
     */
    private Integer parseUserId(String identifier) {
        try {
            // 尝试作为数字解析（用户ID）
            if (identifier.matches("\\d+")) {
                Integer userId = Integer.parseInt(identifier);
                // 验证用户ID是否存在
                if (userService.getUserById(userId) != null) {
                    return userId;
                }
                return null;
            }
            
            // 如果不是数字，尝试作为用户名查询
            com.blog.entity.User user = userService.getUserByUsername(identifier);
            if (user != null) {
                return user.getId();
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("解析用户标识符失败: " + identifier + ", 错误: " + e.getMessage());
            return null;
        }
    }
}