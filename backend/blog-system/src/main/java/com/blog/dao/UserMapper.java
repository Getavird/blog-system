package com.blog.dao;

import com.blog.entity.User;
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
    // @Insert("INSERT INTO user(username, password, email) VALUES(#{username}, #{password}, #{email})")
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
}