package com.blog.service.impl;

import com.blog.dao.CommentMapper;
import com.blog.dao.UserLikeMapper;
import com.blog.entity.Comment;
import com.blog.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private UserLikeMapper userLikeMapper;
    
    @Override
    public Comment getCommentById(Integer id) {
        return commentMapper.findById(id);
    }
    
    @Override
    public List<Comment> getCommentsByArticleId(Integer articleId, int page, int size) {
        int offset = (page - 1) * size;
        return commentMapper.findByArticleId(articleId, offset, size);
    }
    
    @Override
    public List<Comment> getCommentTreeByArticleId(Integer articleId) {
        // 获取所有顶级评论
        List<Comment> topLevelComments = commentMapper.findTopLevelComments(articleId);
        
        // 为每个顶级评论获取子评论
        for (Comment comment : topLevelComments) {
            List<Comment> childComments = commentMapper.findChildComments(comment.getId());
            // 设置子评论列表
            comment.setChildComments(childComments);
        }
        
        return topLevelComments;
    }
    
    @Override
    public Comment createComment(Comment comment, String ipAddress, String userAgent) {
        try {
            // 1. 验证评论内容
            if (!StringUtils.hasText(comment.getContent())) {
                throw new RuntimeException("评论内容不能为空");
            }
            
            if (comment.getContent().length() > 1000) {
                throw new RuntimeException("评论内容过长");
            }
            
            // 2. 验证文章ID
            if (comment.getArticleId() == null) {
                throw new RuntimeException("文章ID不能为空");
            }
            
            // 3. 设置默认值
            if (comment.getParentId() == null) {
                comment.setParentId(0);
            }
            
            if (comment.getLikeCount() == null) {
                comment.setLikeCount(0);
            }
            
            if (comment.getStatus() == null) {
                comment.setStatus(1); // 正常状态
            }
            
            // 4. 设置IP和User-Agent
            comment.setIpAddress(ipAddress);
            comment.setUserAgent(userAgent);
            
            // 5. 保存评论
            int result = commentMapper.insert(comment);
            if (result > 0) {
                System.out.println("✅ 评论创建成功: ID=" + comment.getId() + 
                                 ", 文章ID=" + comment.getArticleId());
                
                // 重新获取完整的评论信息（包含用户信息）
                Comment savedComment = commentMapper.findById(comment.getId());
                return savedComment;
            } else {
                throw new RuntimeException("创建评论失败");
            }
            
        } catch (RuntimeException e) {
            System.err.println("❌ 创建评论异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ 创建评论系统异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("创建评论失败");
        }
    }
    
    @Override
    public boolean updateComment(Comment comment) {
        try {
            // 1. 检查评论是否存在
            Comment existingComment = commentMapper.findById(comment.getId());
            if (existingComment == null) {
                throw new RuntimeException("评论不存在");
            }
            
            // 2. 验证评论内容
            if (!StringUtils.hasText(comment.getContent())) {
                throw new RuntimeException("评论内容不能为空");
            }
            
            if (comment.getContent().length() > 1000) {
                throw new RuntimeException("评论内容过长");
            }
            
            // 3. 只能更新内容，不能修改其他字段
            existingComment.setContent(comment.getContent());
            
            int result = commentMapper.update(existingComment);
            return result > 0;
            
        } catch (RuntimeException e) {
            System.err.println("❌ 更新评论异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ 更新评论系统异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteComment(Integer id) {
        try {
            Comment comment = commentMapper.findById(id);
            if (comment == null) {
                throw new RuntimeException("评论不存在");
            }
            
            // 使用软删除
            int result = commentMapper.softDelete(id);
            return result > 0;
            
        } catch (RuntimeException e) {
            System.err.println("❌ 删除评论异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ 删除评论系统异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean likeComment(Integer commentId, Integer userId) {
        if (userLikeMapper.exists(userId, commentId) > 0) {
            throw new RuntimeException("您已点赞过该评论");
        }
        int inserted = userLikeMapper.insert(userId, commentId);
        if (inserted > 0) {
            commentMapper.incrementLikeCount(commentId);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean unlikeComment(Integer commentId, Integer userId) {
        if (userLikeMapper.exists(userId, commentId) == 0) {
            throw new RuntimeException("您还未点赞该评论");
        }
        int deleted = userLikeMapper.delete(userId, commentId);
        if (deleted > 0) {
            commentMapper.decrementLikeCount(commentId);
            return true;
        }
        return false;
    }
    
    @Override
    public List<Comment> getUserRecentComments(Integer userId, int limit) {
        return commentMapper.findRecentByUserId(userId, limit);
    }
    
    @Override
    public boolean canUserModifyComment(Integer userId, Integer commentId) {
        Comment comment = commentMapper.findById(commentId);
        if (comment == null) {
            return false;
        }
        
        // 评论的作者可以修改自己的评论
        return comment.getUserId().equals(userId);
    }
    
    /**
     * 获取评论统计信息
     */
    public Map<String, Object> getCommentStatistics(Integer articleId) {
        Map<String, Object> stats = new HashMap<>();
        
        int totalComments = commentMapper.countByArticleId(articleId);
        stats.put("total", totalComments);
        
        // 可以添加更多统计信息，如：
        // - 最近24小时评论数
        // - 评论点赞总数
        // - 平均评论长度等
        
        return stats;
    }
    
    /**
     * 构建完整评论树（支持多层嵌套）
     */
    public List<Comment> buildFullCommentTree(List<Comment> comments) {
        Map<Integer, Comment> commentMap = new HashMap<>();
        List<Comment> rootComments = new ArrayList<>();
        
        // 第一次遍历：建立ID到Comment的映射
        for (Comment comment : comments) {
            // 确保每个评论都有空的子评论列表
            comment.setChildComments(new ArrayList<>());
            commentMap.put(comment.getId(), comment);
        }
        
        // 第二次遍历：建立父子关系
        for (Comment comment : comments) {
            if (comment.getParentId() == 0) {
                rootComments.add(comment);
            } else {
                Comment parent = commentMap.get(comment.getParentId());
                if (parent != null) {
                    // 使用addChildComment方法
                    parent.addChildComment(comment);
                }
            }
        }
        
        return rootComments;
    }
    
    /**
     * 获取文章的所有评论（包括子评论）
     */
    public List<Comment> getAllCommentsByArticleId(Integer articleId) {
        // 先获取平铺的所有评论
        List<Comment> allComments = commentMapper.findByArticleId(articleId, 0, 1000); // 假设文章评论不超过1000
        
        // 构建评论树
        return buildFullCommentTree(allComments);
    }
    
    /**
     * 获取评论的完整路径（从根评论到当前评论）
     */
    public List<Comment> getCommentPath(Integer commentId) {
        List<Comment> path = new ArrayList<>();
        Comment current = commentMapper.findById(commentId);
        
        while (current != null) {
            path.add(0, current); // 添加到开头
            if (current.getParentId() == 0) {
                break;
            }
            current = commentMapper.findById(current.getParentId());
        }
        
        return path;
    }
    
    /**
     * 检查评论是否属于指定文章
     */
    public boolean isCommentBelongsToArticle(Integer commentId, Integer articleId) {
        Comment comment = commentMapper.findById(commentId);
        return comment != null && comment.getArticleId().equals(articleId);
    }
    
    /**
     * 获取评论及其所有子评论的ID
     */
    public List<Integer> getCommentAndChildrenIds(Integer commentId) {
        List<Integer> ids = new ArrayList<>();
        ids.add(commentId);
        
        // 递归获取子评论ID
        getChildrenCommentIds(commentId, ids);
        
        return ids;
    }
    
    /**
     * 递归获取子评论ID
     */
    private void getChildrenCommentIds(Integer parentId, List<Integer> ids) {
        List<Comment> children = commentMapper.findChildComments(parentId);
        for (Comment child : children) {
            ids.add(child.getId());
            getChildrenCommentIds(child.getId(), ids);
        }
    }
}