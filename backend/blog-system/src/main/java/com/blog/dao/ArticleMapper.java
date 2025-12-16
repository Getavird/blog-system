package com.blog.dao;

import com.blog.entity.Article;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper {
    
    /**
     * 根据ID查询文章
     */
    @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar, " +
            "c.name as category_name " +
            "FROM article a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "LEFT JOIN category c ON a.category_id = c.id " +
            "WHERE a.id = #{id} AND a.status = 1")
    Article findById(Integer id);
    
    /**
     * 查询文章列表（分页）
     */
    @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar, " +
            "c.name as category_name " +
            "FROM article a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "LEFT JOIN category c ON a.category_id = c.id " +
            "WHERE a.status = 1 " +
            "ORDER BY a.is_top DESC, a.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<Article> findList(@Param("offset") int offset, @Param("size") int size);
    
    /**
     * 插入文章
     */
    @Insert("INSERT INTO article (title, content, summary, cover_image, status, " +
            "user_id, category_id, is_top, allow_comment, tags, create_time, update_time) " +
            "VALUES (#{title}, #{content}, #{summary}, #{coverImage}, #{status}, " +
            "#{userId}, #{categoryId}, #{isTop}, #{allowComment}, #{tags}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Article article);
    
    /**
     * 更新文章
     */
    @Update("UPDATE article SET " +
            "title = #{title}, content = #{content}, summary = #{summary}, " +
            "cover_image = #{coverImage}, status = #{status}, category_id = #{categoryId}, " +
            "is_top = #{isTop}, allow_comment = #{allowComment}, tags = #{tags}, " +
            "update_time = NOW() " +
            "WHERE id = #{id}")
    int update(Article article);
    
    /**
     * 增加阅读量
     */
    @Update("UPDATE article SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Integer id);
}
