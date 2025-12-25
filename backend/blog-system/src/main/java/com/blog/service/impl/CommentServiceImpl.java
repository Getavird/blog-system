package com.blog.service.impl;

import com.blog.dao.CommentMapper;
import com.blog.dao.UserLikeMapper;
import com.blog.entity.Comment;
import com.blog.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public Comment getCommentById(Integer id) {
        try {
            return commentMapper.findById(id);
        } catch (Exception e) {
            System.err.println("❌ 获取评论异常, ID=" + id + ": " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<Comment> getCommentsByArticleId(Integer articleId, int page, int size) {
        try {
            int offset = (page - 1) * size;
            return commentMapper.findByArticleId(articleId, offset, size);
        } catch (Exception e) {
            System.err.println("❌ 获取文章评论异常, articleId=" + articleId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Comment> getCommentTreeByArticleId(Integer articleId) {
        try {
            // 获取所有顶级评论
            List<Comment> topLevelComments = commentMapper.findTopLevelComments(articleId);
            
            // 为每个顶级评论获取子评论
            for (Comment comment : topLevelComments) {
                List<Comment> childComments = commentMapper.findChildComments(comment.getId());
                comment.setChildComments(childComments);
            }
            
            return topLevelComments;
        } catch (Exception e) {
            System.err.println("❌ 获取评论树异常, articleId=" + articleId + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public Comment createComment(Comment comment, String ipAddress, String userAgent) {
        System.out.println("🚀 开始创建评论...");
        System.out.println("📝 评论内容: " + comment.getContent());
        System.out.println("📝 文章ID: " + comment.getArticleId());
        System.out.println("📝 用户ID: " + comment.getUserId());
        System.out.println("📝 父评论ID: " + comment.getParentId());
        System.out.println("📝 回复用户ID: " + comment.getReplyUserId());
        
        try {
            // 1. 基本验证
            validateComment(comment);
            
            // 2. 设置默认值
            setDefaultValues(comment);
            
            // 3. 验证父评论（如果有）
            if (comment.getParentId() != null && comment.getParentId() > 0) {
                validateParentComment(comment.getParentId());
            }
            
            // 4. 设置IP和User-Agent
            comment.setIpAddress(ipAddress);
            comment.setUserAgent(userAgent);
            
            // 5. 设置时间
            String now = dateFormat.format(new Date());
            comment.setCreateTime(now);
            comment.setUpdateTime(now);
            
            // 6. 保存评论
            System.out.println("💾 执行数据库插入...");
            int result = commentMapper.insert(comment);
            System.out.println("✅ 插入结果: " + result + " 行受影响");
            
            if (result <= 0) {
                throw new RuntimeException("数据库插入失败，影响行数为0");
            }
            
            // 7. 获取插入后的ID
            System.out.println("🆔 尝试获取插入后的评论ID...");
            Integer insertedId = comment.getId();
            
            if (insertedId == null || insertedId == 0) {
                System.err.println("⚠️  MyBatis未返回自增ID，尝试其他方式获取...");
                
                // 尝试从数据库获取最后插入的ID
                try {
                    insertedId = commentMapper.getLastInsertId();
                    if (insertedId != null && insertedId > 0) {
                        comment.setId(insertedId);
                        System.out.println("✅ 通过getLastInsertId获取到ID: " + insertedId);
                    } else {
                        // 尝试查询最新的一条评论
                        Comment latest = findLatestCommentByUserAndArticle(comment.getUserId(), comment.getArticleId());
                        if (latest != null) {
                            comment.setId(latest.getId());
                            System.out.println("✅ 通过查询获取到最新评论ID: " + latest.getId());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ 获取最后插入ID失败: " + e.getMessage());
                }
            } else {
                System.out.println("✅ MyBatis返回的自增ID: " + insertedId);
            }
            
            // 8. 重新查询完整的评论信息
            Comment savedComment = null;
            if (comment.getId() != null && comment.getId() > 0) {
                try {
                    savedComment = commentMapper.findById(comment.getId());
                    if (savedComment != null) {
                        System.out.println("✅ 成功查询到保存的评论");
                        return savedComment;
                    }
                } catch (Exception e) {
                    System.err.println("❌ 查询保存的评论失败: " + e.getMessage());
                }
            }
            
            // 9. 如果无法查询，至少返回插入的对象
            System.out.println("⚠️  无法查询到保存的评论，返回原始对象");
            System.out.println("📊 返回的评论对象: ID=" + comment.getId() + 
                             ", content=" + comment.getContent());
            return comment;
            
        } catch (RuntimeException e) {
            System.err.println("❌ 创建评论业务异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ 创建评论系统异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("创建评论失败: " + e.getMessage());
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
            existingComment.setUpdateTime(dateFormat.format(new Date()));
            
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
        try {
            // 检查是否已经点赞
            if (userLikeMapper.exists(userId, commentId) > 0) {
                throw new RuntimeException("您已点赞过该评论");
            }
            
            // 插入点赞记录
            int inserted = userLikeMapper.insert(userId, commentId);
            if (inserted > 0) {
                // 更新评论点赞数
                commentMapper.incrementLikeCount(commentId);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ 点赞评论异常: " + e.getMessage());
            throw new RuntimeException("点赞失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean unlikeComment(Integer commentId, Integer userId) {
        try {
            // 检查是否已经点赞
            if (userLikeMapper.exists(userId, commentId) == 0) {
                throw new RuntimeException("您还未点赞该评论");
            }
            
            // 删除点赞记录
            int deleted = userLikeMapper.delete(userId, commentId);
            if (deleted > 0) {
                // 更新评论点赞数
                commentMapper.decrementLikeCount(commentId);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ 取消点赞异常: " + e.getMessage());
            throw new RuntimeException("取消点赞失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<Comment> getUserRecentComments(Integer userId, int limit) {
        try {
            return commentMapper.findRecentByUserId(userId, limit);
        } catch (Exception e) {
            System.err.println("❌ 获取用户最近评论异常: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean canUserModifyComment(Integer userId, Integer commentId) {
        try {
            Comment comment = commentMapper.findById(commentId);
            if (comment == null) {
                return false;
            }
            
            // 评论的作者可以修改自己的评论
            return comment.getUserId().equals(userId);
        } catch (Exception e) {
            System.err.println("❌ 检查评论权限异常: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 验证评论数据
     */
    private void validateComment(Comment comment) {
        if (comment == null) {
            throw new RuntimeException("评论不能为空");
        }
        
        if (!StringUtils.hasText(comment.getContent())) {
            throw new RuntimeException("评论内容不能为空");
        }
        
        if (comment.getContent().length() > 1000) {
            throw new RuntimeException("评论内容不能超过1000字");
        }
        
        if (comment.getArticleId() == null) {
            throw new RuntimeException("文章ID不能为空");
        }
        
        if (comment.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
    }
    
    /**
     * 设置默认值
     */
    private void setDefaultValues(Comment comment) {
        if (comment.getParentId() == null) {
            comment.setParentId(0); // 根评论
        }
        
        if (comment.getReplyUserId() == null && comment.getParentId() > 0) {
            // 如果是子评论，尝试从父评论获取回复用户ID
            try {
                Comment parent = commentMapper.findById(comment.getParentId());
                if (parent != null) {
                    comment.setReplyUserId(parent.getUserId());
                }
            } catch (Exception e) {
                System.err.println("⚠️  无法获取父评论信息: " + e.getMessage());
            }
        }
        
        if (comment.getLikeCount() == null) {
            comment.setLikeCount(0);
        }
        
        if (comment.getStatus() == null) {
            comment.setStatus(1); // 正常状态
        }
    }
    
    /**
     * 验证父评论
     */
    private void validateParentComment(Integer parentId) {
        try {
            if (parentId != null && parentId > 0) {
                Comment parentComment = commentMapper.findById(parentId);
                if (parentComment == null) {
                    throw new RuntimeException("父评论不存在");
                }
                if (parentComment.getStatus() != 1) {
                    throw new RuntimeException("父评论已被删除");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("验证父评论失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询用户对某篇文章的最新评论
     */
    private Comment findLatestCommentByUserAndArticle(Integer userId, Integer articleId) {
        try {
            // 查询用户的最新评论（限制1条）
            List<Comment> recentComments = commentMapper.findRecentByUserId(userId, 1);
            if (recentComments != null && !recentComments.isEmpty()) {
                Comment latest = recentComments.get(0);
                if (latest.getArticleId().equals(articleId)) {
                    return latest;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("❌ 查询最新评论失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取评论统计信息
     */
    public Map<String, Object> getCommentStatistics(Integer articleId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            int totalComments = commentMapper.countByArticleId(articleId);
            stats.put("total", totalComments);
            stats.put("articleId", articleId);
        } catch (Exception e) {
            System.err.println("❌ 获取评论统计异常: " + e.getMessage());
            stats.put("total", 0);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * 构建完整评论树（支持多层嵌套）
     */
    public List<Comment> buildFullCommentTree(List<Comment> comments) {
        Map<Integer, Comment> commentMap = new HashMap<>();
        List<Comment> rootComments = new ArrayList<>();
        
        try {
            // 第一次遍历：建立ID到Comment的映射
            for (Comment comment : comments) {
                comment.setChildComments(new ArrayList<>());
                commentMap.put(comment.getId(), comment);
            }
            
            // 第二次遍历：建立父子关系
            for (Comment comment : comments) {
                if (comment.getParentId() == 0 || comment.getParentId() == null) {
                    rootComments.add(comment);
                } else {
                    Comment parent = commentMap.get(comment.getParentId());
                    if (parent != null) {
                        parent.addChildComment(comment);
                    }
                }
            }
            
            return rootComments;
        } catch (Exception e) {
            System.err.println("❌ 构建评论树异常: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取文章的所有评论（包括子评论）
     */
    public List<Comment> getAllCommentsByArticleId(Integer articleId) {
        try {
            // 先获取平铺的所有评论
            List<Comment> allComments = commentMapper.findByArticleId(articleId, 0, 1000);
            // 构建评论树
            return buildFullCommentTree(allComments);
        } catch (Exception e) {
            System.err.println("❌ 获取文章所有评论异常: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取评论的完整路径（从根评论到当前评论）
     */
    public List<Comment> getCommentPath(Integer commentId) {
        List<Comment> path = new ArrayList<>();
        
        try {
            Comment current = commentMapper.findById(commentId);
            while (current != null) {
                path.add(0, current);
                if (current.getParentId() == 0 || current.getParentId() == null) {
                    break;
                }
                current = commentMapper.findById(current.getParentId());
            }
        } catch (Exception e) {
            System.err.println("❌ 获取评论路径异常: " + e.getMessage());
        }
        
        return path;
    }
    
    /**
     * 检查评论是否属于指定文章
     */
    public boolean isCommentBelongsToArticle(Integer commentId, Integer articleId) {
        try {
            Comment comment = commentMapper.findById(commentId);
            return comment != null && comment.getArticleId().equals(articleId);
        } catch (Exception e) {
            System.err.println("❌ 检查评论归属异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取评论及其所有子评论的ID
     */
    public List<Integer> getCommentAndChildrenIds(Integer commentId) {
        List<Integer> ids = new ArrayList<>();
        
        try {
            ids.add(commentId);
            // 递归获取子评论ID
            getChildrenCommentIds(commentId, ids);
        } catch (Exception e) {
            System.err.println("❌ 获取评论子ID异常: " + e.getMessage());
        }
        
        return ids;
    }
    
    /**
     * 递归获取子评论ID
     */
    private void getChildrenCommentIds(Integer parentId, List<Integer> ids) {
        try {
            List<Comment> children = commentMapper.findChildComments(parentId);
            for (Comment child : children) {
                ids.add(child.getId());
                getChildrenCommentIds(child.getId(), ids);
            }
        } catch (Exception e) {
            System.err.println("❌ 递归获取子评论ID异常: " + e.getMessage());
        }
    }
    
    /**
     * 获取评论的分页信息
     */
    public Map<String, Object> getCommentsWithPagination(Integer articleId, Integer page, Integer size) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取分页数据
            List<Comment> comments = getCommentsByArticleId(articleId, page, size);
            result.put("comments", comments);
            
            // 获取总数
            int total = commentMapper.countByArticleId(articleId);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (int) Math.ceil((double) total / size));
        } catch (Exception e) {
            System.err.println("❌ 获取分页评论异常: " + e.getMessage());
            result.put("comments", new ArrayList<>());
            result.put("total", 0);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 批量删除评论
     */
    public boolean batchDeleteComments(List<Integer> commentIds) {
        try {
            int successCount = 0;
            for (Integer commentId : commentIds) {
                if (deleteComment(commentId)) {
                    successCount++;
                }
            }
            return successCount == commentIds.size();
        } catch (Exception e) {
            System.err.println("❌ 批量删除评论异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 更新评论点赞数（直接设置）
     */
    public boolean updateCommentLikeCount(Integer commentId, Integer likeCount) {
        try {
            Comment comment = commentMapper.findById(commentId);
            if (comment == null) {
                throw new RuntimeException("评论不存在");
            }
            
            comment.setLikeCount(likeCount);
            comment.setUpdateTime(dateFormat.format(new Date()));
            int result = commentMapper.update(comment);
            return result > 0;
        } catch (Exception e) {
            System.err.println("❌ 更新评论点赞数异常: " + e.getMessage());
            return false;
        }
    }
}