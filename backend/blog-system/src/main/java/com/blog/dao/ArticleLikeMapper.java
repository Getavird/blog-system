package com.blog.dao;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleLikeMapper {

    /**
     * 检查用户是否点赞了文章
     */
    @Select("SELECT COUNT(*) FROM article_like WHERE user_id = #{userId} AND article_id = #{articleId}")
    int exists(@Param("userId") Integer userId, @Param("articleId") Integer articleId);

    /**
     * 点赞文章
     */
    @Insert("INSERT INTO article_like(user_id, article_id) VALUES(#{userId}, #{articleId})")
    int insert(@Param("userId") Integer userId, @Param("articleId") Integer articleId);

    /**
     * 取消点赞文章
     */
    @Delete("DELETE FROM article_like WHERE user_id = #{userId} AND article_id = #{articleId}")
    int delete(@Param("userId") Integer userId, @Param("articleId") Integer articleId);

    /**
     * 获取文章点赞数
     */
    @Select("SELECT COUNT(*) FROM article_like WHERE article_id = #{articleId}")
    int countByArticleId(@Param("articleId") Integer articleId);

    /**
     * 获取用户点赞的文章ID列表
     */
    @Select("SELECT article_id FROM article_like WHERE user_id = #{userId}")
    List<Integer> findLikedArticleIds(@Param("userId") Integer userId);

    /**
     * 删除文章的所有点赞记录（当文章被删除时）
     */
    @Delete("DELETE FROM article_like WHERE article_id = #{articleId}")
    int deleteByArticleId(@Param("articleId") Integer articleId);

    /**
     * 删除用户的所有文章点赞记录（当用户被删除时）
     */
    @Delete("DELETE FROM article_like WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Integer userId);
}