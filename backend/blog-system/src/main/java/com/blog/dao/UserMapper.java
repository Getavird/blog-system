package com.blog.dao;

import com.blog.entity.User;
import com.blog.entity.vo.UserStatsVO;

import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserMapper {

        /**
         * 根据用户名查询用户
         */
        @Select("SELECT * FROM user WHERE username = #{username}")
        User findByUsername(String username);

        /**
         * 根据ID查询用户
         */
        @Select("SELECT * FROM user WHERE id = #{id}")
        User findById(Integer id);

        /**
         * 根据邮箱查询用户
         */
        @Select("SELECT * FROM user WHERE email = #{email}")
        User findByEmail(String email);

        /**
         * 插入用户
         */
        @Insert("INSERT INTO user(username, password, email, avatar, role, status, bio, " +
                        "article_count, like_count, view_count, create_time, update_time) " +
                        "VALUES(#{username}, #{password}, #{email}, #{avatar}, #{role}, #{status}, #{bio}, " +
                        "#{articleCount}, #{likeCount}, #{viewCount}, NOW(), NOW())")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(User user);

        /**
         * 更新用户基本信息
         */
        @Update("UPDATE user SET " +
                        "email = #{email}, " +
                        "avatar = #{avatar}, " +
                        "bio = #{bio}, " +
                        "update_time = NOW() " +
                        "WHERE id = #{id}")
        int update(User user);

        /**
         * 更新用户密码
         */
        @Update("UPDATE user SET password = #{password}, update_time = NOW() WHERE id = #{id}")
        int updatePassword(@Param("id") Integer id, @Param("password") String password);

        /**
         * 更新最后登录信息
         */
        @Update("UPDATE user SET " +
                        "last_login_time = #{lastLoginTime}, " +
                        "last_login_ip = #{lastLoginIp}, " +
                        "update_time = NOW() " +
                        "WHERE id = #{userId}")
        int updateLastLogin(@Param("userId") Integer userId,
                        @Param("lastLoginTime") LocalDateTime lastLoginTime,
                        @Param("lastLoginIp") String lastLoginIp);

        /**
         * 更新最后活动时间
         */
        @Update("UPDATE user SET " +
                        "last_active_time = #{lastActiveTime}, " +
                        "update_time = NOW() " +
                        "WHERE id = #{userId}")
        int updateLastActive(@Param("userId") Integer userId,
                        @Param("lastActiveTime") LocalDateTime lastActiveTime);

        /**
         * 更新用户统计信息
         */
        @Update("UPDATE user SET " +
                        "article_count = #{articleCount}, " +
                        "like_count = #{likeCount}, " +
                        "view_count = #{viewCount}, " +
                        "update_time = NOW() " +
                        "WHERE id = #{id}")
        int updateStats(@Param("id") Integer id,
                        @Param("articleCount") Integer articleCount,
                        @Param("likeCount") Integer likeCount,
                        @Param("viewCount") Integer viewCount);

        /**
         * 搜索用户
         */
        @Select({
                        "<script>",
                        "SELECT id, username, email, avatar, role, status, bio, ",
                        "       article_count, like_count, view_count, ",
                        "       last_login_time, last_login_ip, last_active_time, ",
                        "       create_time, update_time ",
                        "FROM user ",
                        "WHERE status = 1 ",
                        "  AND (username LIKE CONCAT('%', #{keyword}, '%') ",
                        "    OR email LIKE CONCAT('%', #{keyword}, '%') ",
                        "    OR bio LIKE CONCAT('%', #{keyword}, '%')) ",
                        "ORDER BY create_time DESC ",
                        "LIMIT #{offset}, #{size}",
                        "</script>"
        })
        List<User> searchUsers(@Param("keyword") String keyword,
                        @Param("offset") int offset,
                        @Param("size") int size);

        /**
         * 统计搜索用户数量
         */
        @Select({
                        "<script>",
                        "SELECT COUNT(*) FROM user ",
                        "WHERE status = 1 ",
                        "  AND (username LIKE CONCAT('%', #{keyword}, '%') ",
                        "    OR email LIKE CONCAT('%', #{keyword}, '%') ",
                        "    OR bio LIKE CONCAT('%', #{keyword}, '%'))",
                        "</script>"
        })
        int countSearchUsers(@Param("keyword") String keyword);

        /**
         * 获取用户实时统计信息
         * 文章数：只统计status=1（已发布）的文章
         * 阅读数：统计用户所有文章（status != 0，即包括已发布和已删除）的阅读数总和
         * 获赞数：通过article_like表关联，统计用户所有文章（status != 0）的点赞总数
         * 粉丝数：统计关注该用户的用户数
         * 关注数：统计该用户关注的用户数
         */
        @Select("SELECT " +
                        "(SELECT COUNT(*) FROM article WHERE user_id = #{userId} AND status = 1) as articleCount, " +
                        "(SELECT COALESCE(SUM(view_count), 0) FROM article WHERE user_id = #{userId} AND status != 0) as viewCount, "
                        +
                        "(SELECT COALESCE(COUNT(*), 0) FROM article_like al " +
                        " WHERE EXISTS (SELECT 1 FROM article a WHERE a.id = al.article_id AND a.user_id = #{userId} AND a.status != 0)) as likeCount, "
                        +
                        "(SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId} AND status = 1) as followingCount, "
                        +
                        "(SELECT COUNT(*) FROM user_follow WHERE following_id = #{userId} AND status = 1) as followerCount")
        UserStatsVO getUserStats(Integer userId);

        /**
         * 获取在线用户列表
         */
        @Select("SELECT id, username, email, avatar, bio, last_active_time " +
                        "FROM user " +
                        "WHERE last_active_time IS NOT NULL " +
                        "  AND last_active_time > DATE_SUB(NOW(), INTERVAL 5 MINUTE) " +
                        "  AND status = 1 " +
                        "ORDER BY last_active_time DESC " +
                        "LIMIT #{limit}")
        List<User> getOnlineUsers(@Param("limit") int limit);

        /**
         * 检查用户名是否已存在
         */
        @Select("SELECT COUNT(*) FROM user WHERE username = #{username} AND id != #{excludeId}")
        int countByUsernameExcludeId(@Param("username") String username,
                        @Param("excludeId") Integer excludeId);

        /**
         * 检查邮箱是否已存在
         */
        @Select("SELECT COUNT(*) FROM user WHERE email = #{email} AND id != #{excludeId}")
        int countByEmailExcludeId(@Param("email") String email,
                        @Param("excludeId") Integer excludeId);

        /**
         * 测试数据库连接（简单的SELECT 1）
         */
        @Select("SELECT 1")
        int testConnection();

        /**
         * 统计用户数量
         */
        @Select("SELECT COUNT(*) FROM user")
        int countUsers();

        /**
         * 获取活跃用户（按文章数排序）
         */
        @Select("SELECT id, username, email, avatar, bio, article_count " +
                        "FROM user " +
                        "WHERE status = 1 " +
                        "ORDER BY article_count DESC " +
                        "LIMIT #{limit}")
        List<User> getActiveUsers(@Param("limit") int limit);

        /**
         * 批量更新用户状态
         */
        @Update("UPDATE user SET status = #{status}, update_time = NOW() WHERE id IN " +
                        "<foreach item='id' collection='ids' open='(' separator=',' close=')'>#{id}</foreach>")
        int batchUpdateStatus(@Param("ids") List<Integer> ids, @Param("status") Integer status);

        /**
         * 获取用户创建时间分布（用于统计图表）
         */
        @Select("SELECT DATE(create_time) as date, COUNT(*) as count " +
                        "FROM user " +
                        "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                        "GROUP BY DATE(create_time) " +
                        "ORDER BY date ASC")
        List<UserRegistrationStats> getUserRegistrationStats();

        /**
         * 获取用户列表（分页，管理员用）
         */
        @Select({
                        "<script>",
                        "SELECT id, username, email, avatar, role, status, bio, ",
                        "       article_count, like_count, view_count, ",
                        "       last_login_time, last_login_ip, last_active_time, ",
                        "       create_time, update_time ",
                        "FROM user ",
                        "WHERE 1=1 ",
                        "<if test='username != null and username != \"\"'>",
                        "  AND username LIKE CONCAT('%', #{username}, '%') ",
                        "</if>",
                        "<if test='email != null and email != \"\"'>",
                        "  AND email LIKE CONCAT('%', #{email}, '%') ",
                        "</if>",
                        "<if test='role != null'>",
                        "  AND role = #{role} ",
                        "</if>",
                        "<if test='status != null'>",
                        "  AND status = #{status} ",
                        "</if>",
                        "ORDER BY create_time DESC ",
                        "LIMIT #{offset}, #{size}",
                        "</script>"
        })
        List<User> getUsers(@Param("username") String username,
                        @Param("email") String email,
                        @Param("role") Integer role,
                        @Param("status") Integer status,
                        @Param("offset") int offset,
                        @Param("size") int size);

        /**
         * 统计用户列表数量
         */
        @Select({
                        "<script>",
                        "SELECT COUNT(*) FROM user WHERE 1=1 ",
                        "<if test='username != null and username != \"\"'>",
                        "  AND username LIKE CONCAT('%', #{username}, '%') ",
                        "</if>",
                        "<if test='email != null and email != \"\"'>",
                        "  AND email LIKE CONCAT('%', #{email}, '%') ",
                        "</if>",
                        "<if test='role != null'>",
                        "  AND role = #{role} ",
                        "</if>",
                        "<if test='status != null'>",
                        "  AND status = #{status} ",
                        "</if>",
                        "</script>"
        })
        int countUsersByCondition(@Param("username") String username,
                        @Param("email") String email,
                        @Param("role") Integer role,
                        @Param("status") Integer status);

        /**
         * 检查user表是否存在
         */
        @Select("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'user'")
        int checkUserTableExists();

        /**
         * 获取数据库名称
         */
        @Select("SELECT DATABASE()")
        String getDatabaseName();

        /**
         * 更新用户头像
         */
        int updateAvatar(@Param("id") Integer id, @Param("avatar") String avatar);

        /**
         * 实时计算用户统计信息
         */
        @Select("SELECT " +
                        "(SELECT COUNT(*) FROM article WHERE user_id = #{userId} AND status = 1) as articleCount, " +
                        "(SELECT COALESCE(SUM(view_count), 0) FROM article WHERE user_id = #{userId} AND status != 0) as viewCount, "
                        +
                        "(SELECT COALESCE(COUNT(*), 0) FROM article_like al " +
                        " WHERE EXISTS (SELECT 1 FROM article a WHERE a.id = al.article_id AND a.user_id = #{userId} AND a.status != 0)) as likeCount")
        UserStatsVO calculateUserStats(Integer userId);

        /**
         * 获取粉丝数
         */
        @Select("SELECT COUNT(*) FROM user_follow WHERE following_id = #{userId} AND status = 1")
        int countFollowers(Integer userId);

        /**
         * 获取关注数
         */
        @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId} AND status = 1")
        int countFollowing(Integer userId);

        /**
         * 更新用户统计信息（多个字段）
         */
        @Update("UPDATE user SET " +
                        "article_count = #{articleCount}, " +
                        "like_count = #{likeCount}, " +
                        "view_count = #{viewCount}, " +
                        "update_time = NOW() " +
                        "WHERE id = #{userId}")
        int updateAllStats(@Param("userId") Integer userId,
                        @Param("articleCount") Integer articleCount,
                        @Param("likeCount") Integer likeCount,
                        @Param("viewCount") Integer viewCount);
}

/**
 * 用户注册统计类（内部类）
 */
class UserRegistrationStats {
        private LocalDateTime date;
        private Integer count;

        public LocalDateTime getDate() {
                return date;
        }

        public void setDate(LocalDateTime date) {
                this.date = date;
        }

        public Integer getCount() {
                return count;
        }

        public void setCount(Integer count) {
                this.count = count;
        }
}