package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.vo.FollowVO;
import com.blog.entity.vo.FollowCountVO;
import com.blog.service.FollowService;
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
    
    /**
     * 关注用户
     */
    @PostMapping("/{userId}")
    public Result<String> follow(@PathVariable Integer userId, HttpServletRequest request) {
        try {
            // 1. 检查登录
            Integer currentUserId = SessionUtil.getCurrentUserId(request);
            if (currentUserId == null) {
                return Result.unauthorized("请先登录");
            }
            
            // 2. 关注用户
            boolean success = followService.follow(currentUserId, userId);
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
     * 取消关注
     */
    @DeleteMapping("/{userId}")
    public Result<String> unfollow(@PathVariable Integer userId, HttpServletRequest request) {
        try {
            // 1. 检查登录
            Integer currentUserId = SessionUtil.getCurrentUserId(request);
            if (currentUserId == null) {
                return Result.unauthorized("请先登录");
            }
            
            // 2. 取消关注
            boolean success = followService.unfollow(currentUserId, userId);
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
     * 检查是否关注
     */
    @GetMapping("/check/{userId}")
    public Result<Map<String, Object>> checkFollowing(@PathVariable Integer userId, 
                                                     HttpServletRequest request) {
        try {
            // 1. 检查登录
            Integer currentUserId = SessionUtil.getCurrentUserId(request);
            if (currentUserId == null) {
                return Result.unauthorized("请先登录");
            }
            
            // 2. 检查是否关注
            boolean isFollowing = followService.isFollowing(currentUserId, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("isFollowing", isFollowing);
            
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
     * 获取关注数量统计
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
     * 获取用户的关注数量（公开接口，无需登录）
     */
    @GetMapping("/counts/{userId}")
    public Result<FollowCountVO> getUserFollowCounts(@PathVariable Integer userId) {
        try {
            // 获取用户的关注数量统计
            FollowCountVO countVO = followService.getFollowCount(userId);
            
            return Result.success("获取用户关注数量成功", countVO);
            
        } catch (Exception e) {
            System.err.println("❌ 获取用户关注数量接口异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取用户关注数量失败");
        }
    }
}