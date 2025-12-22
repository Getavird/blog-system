package com.blog.dao;

import com.blog.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

        /**
         * 根据ID查询评论
         */
        @Select("SELECT c.*, u.username, u.avatar as user_avatar, " +
                        "ru.username as reply_username " +
                        "FROM comment c " +
                        "LEFT JOIN user u ON c.user_id = u.id " +
                        "LEFT JOIN user ru ON c.reply_to = ru.id " +
                        "WHERE c.id = #{id} AND c.status = 1")
        Comment findById(Integer id);

        /**
         * 根据文章ID查询评论（分页）
         */
        @Select("SELECT c.*, u.username, u.avatar as user_avatar, " +
                        "ru.username as reply_username " +
                        "FROM comment c " +
                        "LEFT JOIN user u ON c.user_id = u.id " +
                        "LEFT JOIN user ru ON c.reply_to = ru.id " +
                        "WHERE c.article_id = #{articleId} AND c.status = 1 " +
                        "ORDER BY c.create_time ASC " +
                        "LIMIT #{offset}, #{size}")
        List<Comment> findByArticleId(@Param("articleId") Integer articleId,
                        @Param("offset") int offset,
                        @Param("size") int size);

        /**
         * 查询顶级评论（parent_id=0）
         */
        @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
                        "FROM comment c " +
                        "LEFT JOIN user u ON c.user_id = u.id " +
                        "WHERE c.article_id = #{articleId} AND c.parent_id = 0 AND c.status = 1 " +
                        "ORDER BY c.create_time ASC")
        List<Comment> findTopLevelComments(Integer articleId);

        /**
         * 查询子评论
         */
        @Select("SELECT c.*, u.username, u.avatar as user_avatar, " +
                        "ru.username as reply_username " +
                        "FROM comment c " +
                        "LEFT JOIN user u ON c.user_id = u.id " +
                        "LEFT JOIN user ru ON c.reply_to = ru.id " +
                        "WHERE c.parent_id = #{parentId} AND c.status = 1 " +
                        "ORDER BY c.create_time ASC")
        List<Comment> findChildComments(Integer parentId);

        /**
         * 插入评论
         */
        @Insert("INSERT INTO comment(content, article_id, user_id, parent_id, " +
                        "reply_to, like_count, status, ip_address, user_agent, create_time) " +
                        "VALUES(#{content}, #{articleId}, #{userId}, #{parentId}, " +
                        "#{replyUserId}, #{likeCount}, #{status}, #{ipAddress}, #{userAgent}, NOW())")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(Comment comment);

        /**
         * 更新评论
         */
        @Update("UPDATE comment SET content=#{content}, status=#{status}, " +
                        "update_time=NOW() WHERE id=#{id}")
        int update(Comment comment);

        /**
         * 软删除评论（修改状态）
         */
        @Update("UPDATE comment SET status=0 WHERE id=#{id}")
        int softDelete(Integer id);

        /**
         * 增加点赞数
         */
        @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{id}")
        int incrementLikeCount(Integer id);

        /**
         * 减少点赞数
         */
        @Update("UPDATE comment SET like_count = like_count - 1 WHERE id = #{id} AND like_count > 0")
        int decrementLikeCount(Integer id);

        /**
         * 统计文章评论数量
         */
        @Select("SELECT COUNT(*) FROM comment WHERE article_id = #{articleId} AND status = 1")
        int countByArticleId(Integer articleId);

        /**
         * 统计用户评论数量
         */
        @Select("SELECT COUNT(*) FROM comment WHERE user_id = #{userId} AND status = 1")
        int countByUserId(Integer userId);

        /**
         * 获取用户的最新评论
         */
        @Select("SELECT c.*, a.title as article_title " +
                        "FROM comment c " +
                        "LEFT JOIN article a ON c.article_id = a.id " +
                        "WHERE c.user_id = #{userId} AND c.status = 1 " +
                        "ORDER BY c.create_time DESC " +
                        "LIMIT #{limit}")
        List<Comment> findRecentByUserId(@Param("userId") Integer userId,
                        @Param("limit") int limit);
}