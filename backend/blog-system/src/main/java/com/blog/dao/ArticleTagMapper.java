package com.blog.dao;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ArticleTagMapper {
    
    @Insert("INSERT INTO article_tag(article_id, tag_id) VALUES(#{articleId}, #{tagId})")
    int insert(@Param("articleId") Integer articleId, @Param("tagId") Integer tagId);
    
    @Delete("DELETE FROM article_tag WHERE article_id = #{articleId}")
    int deleteByArticleId(@Param("articleId") Integer articleId);
    
    @Delete("DELETE FROM article_tag WHERE article_id = #{articleId} AND tag_id = #{tagId}")
    int deleteByArticleAndTag(@Param("articleId") Integer articleId, @Param("tagId") Integer tagId);
    
    @Select("SELECT tag_id FROM article_tag WHERE article_id = #{articleId}")
    List<Integer> findTagIdsByArticleId(@Param("articleId") Integer articleId);
    
    @Select("SELECT article_id FROM article_tag WHERE tag_id = #{tagId}")
    List<Integer> findArticleIdsByTagId(@Param("tagId") Integer tagId);
    
    @Select("SELECT COUNT(*) FROM article_tag WHERE tag_id = #{tagId}")
    int countArticlesByTagId(@Param("tagId") Integer tagId);
    
    @Select("SELECT COUNT(*) FROM article_tag WHERE article_id = #{articleId} AND tag_id = #{tagId}")
    int exists(@Param("articleId") Integer articleId, @Param("tagId") Integer tagId);
    
    // 用于标签页面的统计查询
    @Select("SELECT t.id, t.name, t.color, COUNT(at.article_id) as article_count " +
            "FROM tag t " +
            "LEFT JOIN article_tag at ON t.id = at.tag_id " +
            "GROUP BY t.id, t.name, t.color " +
            "ORDER BY article_count DESC")
    List<Map<String, Object>> findAllTagsWithCount();
    
    @Select("SELECT t.id, t.name, t.color, COUNT(at.article_id) as article_count " +
            "FROM tag t " +
            "LEFT JOIN article_tag at ON t.id = at.tag_id " +
            "GROUP BY t.id, t.name, t.color " +
            "ORDER BY article_count DESC LIMIT #{limit}")
    List<Map<String, Object>> findHotTagsWithCount(@Param("limit") int limit);
    
    @Select("SELECT t.*, " +
            "(SELECT COUNT(*) FROM article_tag WHERE tag_id = t.id) as article_count, " +
            "(SELECT SUM(a.view_count) FROM article a " +
            " INNER JOIN article_tag at ON a.id = at.article_id " +
            " WHERE at.tag_id = t.id AND a.status = 1) as total_views, " +
            "(SELECT SUM(a.like_count) FROM article a " +
            " INNER JOIN article_tag at ON a.id = at.article_id " +
            " WHERE at.tag_id = t.id AND a.status = 1) as total_likes " +
            "FROM tag t WHERE t.name = #{tagName}")
    Map<String, Object> findTagStatsByName(@Param("tagName") String tagName);
    
    @Select("SELECT a.id FROM article a " +
            "INNER JOIN article_tag at ON a.id = at.article_id " +
            "INNER JOIN tag t ON at.tag_id = t.id " +
            "WHERE t.name = #{tagName} AND a.status = 1")
    List<Integer> findArticleIdsByTagName(@Param("tagName") String tagName);
}