package com.blog.dao;

import com.blog.entity.Follow;
import com.blog.entity.vo.FollowVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FollowMapper {

    /**
     * 插入关注记录
     */
    @Insert("INSERT INTO user_follow(follower_id, following_id, create_time, update_time) " +
            "VALUES(#{followerId}, #{followingId}, NOW(), NOW())")
    int insert(Follow follow);

    /**
     * 取消关注（软删除）
     */
    @Update("UPDATE user_follow SET status = 0, update_time = NOW() " +
            "WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    int cancelFollow(@Param("followerId") Integer followerId, 
                     @Param("followingId") Integer followingId);

    /**
     * 重新关注（恢复）
     */
    @Update("UPDATE user_follow SET status = 1, update_time = NOW() " +
            "WHERE follower_id = #{followerId} AND followingId = #{followingId}")
    int reFollow(@Param("followerId") Integer followerId, 
                 @Param("followingId") Integer followingId);

    /**
     * 删除关注记录（硬删除）
     */
    @Delete("DELETE FROM user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    int delete(@Param("followerId") Integer followerId, 
               @Param("followingId") Integer followingId);

    /**
     * 查询关注关系
     */
    @Select("SELECT * FROM user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    Follow selectFollow(@Param("followerId") Integer followerId, 
                        @Param("followingId") Integer followingId);

    /**
     * 查询是否已关注
     */
    @Select("SELECT COUNT(*) FROM user_follow " +
            "WHERE follower_id = #{followerId} AND following_id = #{followingId} AND status = 1")
    int checkFollow(@Param("followerId") Integer followerId, 
                    @Param("followingId") Integer followingId);

    /**
     * 查询用户关注列表（我关注的人）
     */
    @Select("SELECT f.id, u.id as userId, u.username, u.avatar, u.bio, f.create_time as followTime, " +
            "       1 as isFollowing, " +
            "       (SELECT COUNT(*) FROM user_follow f2 WHERE f2.follower_id = u.id AND f2.following_id = #{currentUserId} AND f2.status = 1) as isFollowed " +
            "FROM user_follow f " +
            "JOIN user u ON f.following_id = u.id " +
            "WHERE f.follower_id = #{followerId} AND f.status = 1 " +
            "ORDER BY f.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<FollowVO> selectFollowingList(@Param("followerId") Integer followerId,
                                       @Param("currentUserId") Integer currentUserId,
                                       @Param("offset") int offset,
                                       @Param("size") int size);

    /**
     * 查询用户粉丝列表（关注我的人）
     */
    @Select("SELECT f.id, u.id as userId, u.username, u.avatar, u.bio, f.create_time as followTime, " +
            "       (SELECT COUNT(*) FROM user_follow f2 WHERE f2.follower_id = #{currentUserId} AND f2.following_id = u.id AND f2.status = 1) as isFollowing, " +
            "       1 as isFollowed " +
            "FROM user_follow f " +
            "JOIN user u ON f.follower_id = u.id " +
            "WHERE f.following_id = #{followingId} AND f.status = 1 " +
            "ORDER BY f.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<FollowVO> selectFollowerList(@Param("followingId") Integer followingId,
                                      @Param("currentUserId") Integer currentUserId,
                                      @Param("offset") int offset,
                                      @Param("size") int size);

    /**
     * 统计关注数量
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId} AND status = 1")
    int countFollowing(@Param("userId") Integer userId);

    /**
     * 统计粉丝数量
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE following_id = #{userId} AND status = 1")
    int countFollowers(@Param("userId") Integer userId);

    /**
     * 统计互相关注的数量
     */
    @Select("SELECT COUNT(*) FROM user_follow f1 " +
            "JOIN user_follow f2 ON f1.follower_id = f2.following_id AND f1.following_id = f2.follower_id " +
            "WHERE f1.follower_id = #{userId} AND f1.status = 1 AND f2.status = 1")
    int countMutualFollowing(@Param("userId") Integer userId);

    /**
     * 获取互相关注的用户ID列表
     */
    @Select("SELECT f1.following_id FROM user_follow f1 " +
            "JOIN user_follow f2 ON f1.follower_id = f2.following_id AND f1.following_id = f2.follower_id " +
            "WHERE f1.follower_id = #{userId} AND f1.status = 1 AND f2.status = 1")
    List<Integer> selectMutualFollowingIds(@Param("userId") Integer userId);
}