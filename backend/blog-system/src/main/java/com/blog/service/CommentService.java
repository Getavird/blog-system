package com.blog.service;

import com.blog.entity.Comment;

import java.util.List;

public interface CommentService {
    
    /**
     * 获取评论详情
     */
    Comment getCommentById(Integer id);
    
    /**
     * 根据文章ID获取评论列表（分页）
     */
    List<Comment> getCommentsByArticleId(Integer articleId, int page, int size);
    
    /**
     * 获取文章评论的树形结构
     */
    List<Comment> getCommentTreeByArticleId(Integer articleId);
    
    /**
     * 创建评论
     */
    Comment createComment(Comment comment, String ipAddress, String userAgent);
    
    /**
     * 更新评论
     */
    boolean updateComment(Comment comment);
    
    /**
     * 删除评论（软删除）
     */
    boolean deleteComment(Integer id);
    
    /**
     * 点赞评论
     */
    boolean likeComment(Integer commentId);
    
    /**
     * 取消点赞评论
     */
    boolean unlikeComment(Integer commentId);
    
    /**
     * 获取用户的最新评论
     */
    List<Comment> getUserRecentComments(Integer userId, int limit);
    
    /**
     * 检查用户是否有权限修改评论
     */
    boolean canUserModifyComment(Integer userId, Integer commentId);
}