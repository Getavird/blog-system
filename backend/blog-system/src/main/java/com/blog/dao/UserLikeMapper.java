package com.blog.dao;

import org.apache.ibatis.annotations.*;

@Mapper
public interface UserLikeMapper {

    @Select("SELECT COUNT(*) FROM user_like WHERE user_id = #{userId} AND comment_id = #{commentId}")
    int exists(@Param("userId") Integer userId, @Param("commentId") Integer commentId);

    @Insert("INSERT INTO user_like(user_id, comment_id) VALUES(#{userId}, #{commentId})")
    int insert(@Param("userId") Integer userId, @Param("commentId") Integer commentId);

    @Delete("DELETE FROM user_like WHERE user_id = #{userId} AND comment_id = #{commentId}")
    int delete(@Param("userId") Integer userId, @Param("commentId") Integer commentId);
}