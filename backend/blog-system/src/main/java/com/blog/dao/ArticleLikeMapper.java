package com.blog.dao;

import com.blog.entity.ArticleLike;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleLikeMapper {
    
    /**
     * 添加文章点赞
     */
    @Insert("INSERT INTO article_like(user_id, article_id, create_time) " +
            "VALUES(#{userId}, #{articleId}, NOW())")
    int insert(@Param("userId") Integer userId, @Param("articleId") Integer articleId);
    
    /**
     * 取消文章点赞
     */
    @Delete("DELETE FROM article_like WHERE user_id = #{userId} AND article_id = #{articleId}")
    int delete(@Param("userId") Integer userId, @Param("articleId") Integer articleId);
    
    /**
     * 检查是否已点赞
     */
    @Select("SELECT COUNT(*) FROM article_like WHERE user_id = #{userId} AND article_id = #{articleId}")
    int exists(@Param("userId") Integer userId, @Param("articleId") Integer articleId);
    
    /**
     * 获取文章的点赞用户列表
     */
    @Select("SELECT al.*, u.username, u.avatar " +
            "FROM article_like al " +
            "LEFT JOIN user u ON al.user_id = u.id " +
            "WHERE al.article_id = #{articleId} " +
            "ORDER BY al.create_time DESC " +
            "LIMIT #{limit}")
    List<ArticleLike> getLikesByArticleId(@Param("articleId") Integer articleId, 
                                          @Param("limit") Integer limit);
    
    /**
     * 获取用户点赞的文章列表
     */
    @Select("SELECT al.*, a.title as article_title " +
            "FROM article_like al " +
            "LEFT JOIN article a ON al.article_id = a.id " +
            "WHERE al.user_id = #{userId} AND a.status = 1 " +
            "ORDER BY al.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<ArticleLike> getLikesByUserId(@Param("userId") Integer userId,
                                       @Param("offset") int offset,
                                       @Param("size") int size);
    
    /**
     * 统计文章的点赞数
     */
    @Select("SELECT COUNT(*) FROM article_like WHERE article_id = #{articleId}")
    int countByArticleId(@Param("articleId") Integer articleId);
    
    /**
     * 统计用户的点赞数（点赞了多少篇文章）
     */
    @Select("SELECT COUNT(*) FROM article_like WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Integer userId);
    
    /**
     * 获取用户的最新点赞记录
     */
    @Select("SELECT al.*, a.title as article_title, a.summary as article_summary " +
            "FROM article_like al " +
            "LEFT JOIN article a ON al.article_id = a.id " +
            "WHERE al.user_id = #{userId} AND a.status = 1 " +
            "ORDER BY al.create_time DESC " +
            "LIMIT #{limit}")
    List<ArticleLike> getRecentLikes(@Param("userId") Integer userId, 
                                     @Param("limit") Integer limit);
    
    /**
     * 批量删除文章的点赞记录（删除文章时用）
     */
    @Delete("DELETE FROM article_like WHERE article_id = #{articleId}")
    int deleteByArticleId(@Param("articleId") Integer articleId);
    
    /**
     * 获取文章的点赞统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_likes, " +
            "COUNT(DISTINCT user_id) as unique_users " +
            "FROM article_like " +
            "WHERE article_id = #{articleId}")
    Object getArticleLikeStats(@Param("articleId") Integer articleId);
}