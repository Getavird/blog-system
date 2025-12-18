package com.blog.entity;

import lombok.Data;

import java.util.List;

/**
 * 评论实体类
 */
@Data
public class Comment {
    /**
     * 评论ID
     */
    private Integer id;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 文章ID（关联article表）
     */
    private Integer articleId;
    
    /**
     * 用户ID（关联user表）
     */
    private Integer userId;
    
    /**
     * 父评论ID（0表示顶级评论）
     */
    private Integer parentId = 0;
    
    /**
     * 回复的用户ID
     */
    private Integer replyUserId;
    
    /**
     * 点赞数
     */
    private Integer likeCount = 0;
    
    /**
     * 评论状态：0-删除，1-正常
     */
    private Integer status = 1;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 更新时间
     */
    private String updateTime;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 用户代理
     */
    private String userAgent;
    
    /**
     * 关联字段：用户名
     */
    private String username;
    
    /**
     * 关联字段：用户头像
     */
    private String userAvatar;
    
    /**
     * 关联字段：回复的用户名
     */
    private String replyUsername;
    
    /**
     * 文章标题（用于显示在用户评论列表中）
     */
    private String articleTitle;
    
    /**
     * 子评论列表（非数据库字段，用于构建评论树）
     */
    private List<Comment> childComments;
    
    // 由于Lombok @Data注解会自动生成getter/setter，
    // 但需要显式添加这些方法以支持树形结构
    
    /**
     * 添加子评论
     */
    public void addChildComment(Comment childComment) {
        if (this.childComments == null) {
            this.childComments = new java.util.ArrayList<>();
        }
        this.childComments.add(childComment);
    }
    
    /**
     * 获取子评论数量
     */
    public int getChildCount() {
        return this.childComments != null ? this.childComments.size() : 0;
    }
    
    /**
     * 判断是否有子评论
     */
    public boolean hasChildren() {
        return this.childComments != null && !this.childComments.isEmpty();
    }
    
    /**
     * 获取所有子评论（如果为null则返回空列表）
     */
    public List<Comment> getChildComments() {
        if (this.childComments == null) {
            return java.util.Collections.emptyList();
        }
        return this.childComments;
    }
    
    /**
     * 设置子评论列表
     */
    public void setChildComments(List<Comment> childComments) {
        this.childComments = childComments;
    }
    
    /**
     * 获取完整评论路径（用于面包屑导航）
     */
    public String getCommentPath() {
        if (parentId == null || parentId == 0) {
            return "#" + id;
        }
        return "回复#" + parentId;
    }
    
    /**
     * 判断是否是回复评论
     */
    public boolean isReply() {
        return parentId != null && parentId > 0;
    }
    
    /**
     * 判断是否是顶级评论
     */
    public boolean isTopLevel() {
        return parentId == null || parentId == 0;
    }
}