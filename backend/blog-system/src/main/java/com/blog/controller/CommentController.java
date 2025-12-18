package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Comment;
import com.blog.service.CommentService;
import com.blog.utils.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    /**
     * 获取评论详情
     */
    @GetMapping("/{id}")
    public Result<Comment> getComment(@PathVariable Integer id) {
        Comment comment = commentService.getCommentById(id);
        if (comment == null) {
            return Result.notFound("评论不存在");
        }
        return Result.success(comment);
    }
    
    /**
     * 根据文章ID获取评论列表（分页）
     */
    @GetMapping("/article/{articleId}")
    public Result<List<Comment>> getCommentsByArticleId(
            @PathVariable Integer articleId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        List<Comment> comments = commentService.getCommentsByArticleId(articleId, page, size);
        return Result.success(comments);
    }
    
    /**
     * 获取文章评论树形结构
     */
    @GetMapping("/article/{articleId}/tree")
    public Result<List<Comment>> getCommentTree(@PathVariable Integer articleId) {
        List<Comment> comments = commentService.getCommentTreeByArticleId(articleId);
        return Result.success(comments);
    }
    
    /**
     * 创建评论（需要登录）
     */
    @PostMapping
    public Result<Comment> createComment(@RequestBody Comment comment, HttpServletRequest request) {
        // 检查登录
        if (!SessionUtil.isLogin(request)) {
            return Result.unauthorized("请先登录");
        }
        
        // 设置当前用户ID
        Integer currentUserId = SessionUtil.getCurrentUserId(request);
        comment.setUserId(currentUserId);
        
        // 获取客户端信息
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        try {
            Comment createdComment = commentService.createComment(comment, ipAddress, userAgent);
            return Result.created(createdComment);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新评论（需要登录且是评论作者）
     */
    @PutMapping("/{id}")
    public Result<Comment> updateComment(@PathVariable Integer id, 
                                         @RequestBody Comment comment,
                                         HttpServletRequest request) {
        // 检查登录
        if (!SessionUtil.isLogin(request)) {
            return Result.unauthorized("请先登录");
        }
        
        Integer currentUserId = SessionUtil.getCurrentUserId(request);
        
        // 检查权限
        if (!commentService.canUserModifyComment(currentUserId, id)) {
            return Result.forbidden("没有权限修改此评论");
        }
        
        comment.setId(id);
        try {
            boolean success = commentService.updateComment(comment);
            return success ? Result.success(comment) : Result.error("更新失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除评论（需要登录且是评论作者或管理员）
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteComment(@PathVariable Integer id, HttpServletRequest request) {
        // 检查登录
        if (!SessionUtil.isLogin(request)) {
            return Result.unauthorized("请先登录");
        }
        
        Integer currentUserId = SessionUtil.getCurrentUserId(request);
        boolean isAdmin = SessionUtil.isAdmin(request);
        
        // 检查权限：作者或管理员可以删除
        if (!commentService.canUserModifyComment(currentUserId, id) && !isAdmin) {
            return Result.forbidden("没有权限删除此评论");
        }
        
        try {
            boolean success = commentService.deleteComment(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 点赞评论（需要登录）
     */
    @PostMapping("/{id}/like")
    public Result<String> likeComment(@PathVariable Integer id, HttpServletRequest request) {
        // 检查登录
        if (!SessionUtil.isLogin(request)) {
            return Result.unauthorized("请先登录");
        }
        
        try {
            boolean success = commentService.likeComment(id);
            return success ? Result.success("点赞成功") : Result.error("点赞失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消点赞评论（需要登录）
     */
    @PostMapping("/{id}/unlike")
    public Result<String> unlikeComment(@PathVariable Integer id, HttpServletRequest request) {
        // 检查登录
        if (!SessionUtil.isLogin(request)) {
            return Result.unauthorized("请先登录");
        }
        
        try {
            boolean success = commentService.unlikeComment(id);
            return success ? Result.success("取消点赞成功") : Result.error("取消点赞失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户的最新评论
     */
    @GetMapping("/user/recent")
    public Result<List<Comment>> getUserRecentComments(
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        
        // 检查登录
        if (!SessionUtil.isLogin(request)) {
            return Result.unauthorized("请先登录");
        }
        
        Integer currentUserId = SessionUtil.getCurrentUserId(request);
        List<Comment> comments = commentService.getUserRecentComments(currentUserId, limit);
        return Result.success(comments);
    }
    
    /**
     * 获取评论统计信息
     */
    @GetMapping("/statistics/{articleId}")
    public Result<Map<String, Object>> getCommentStatistics(@PathVariable Integer articleId) {
        // 注意：需要先在CommentServiceImpl中实现getCommentStatistics方法
        // 这里先返回简单统计
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("articleId", articleId);
        stats.put("message", "统计功能待实现");
        return Result.success(stats);
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}