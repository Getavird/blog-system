package com.blog.dao;

import com.blog.entity.User;

import java.util.List;

import org.apache.ibatis.annotations.*;

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
     * 插入用户
     */
    // @Insert("INSERT INTO user(username, password, email) VALUES(#{username},
    // #{password}, #{email})")
    // int insert(User user);

    // 确保以下测试方法都存在：

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
     * 检查user表是否存在
     */
    @Select("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'user'")
    int checkUserTableExists();

    /**
     * 获取数据库名称
     */
    @Select("SELECT DATABASE()")
    String getDatabaseName();

    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);

    @Insert("INSERT INTO user(username, password, email, avatar, role, status, bio, create_time, update_time) " +
            "VALUES(#{username}, #{password}, #{email}, #{avatar}, #{role}, #{status}, #{bio}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE user SET email=#{email}, avatar=#{avatar}, bio=#{bio}, update_time=NOW() WHERE id=#{id}")
    int update(User user);

    @Update("UPDATE user SET password=#{password}, update_time=NOW() WHERE id=#{id}")
    int updatePassword(@Param("id") Integer id, @Param("password") String password);

    /**
     * 搜索用户
     */
    @Select({
            "<script>",
            "SELECT id, username, email, avatar, role, status, bio, ",
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
}