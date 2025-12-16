package com.blog.dao;

import com.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
     * 注意：这里先用注解，明天创建XML文件
     */
    // @Insert("INSERT INTO user(username, password, email) VALUES(#{username}, #{password}, #{email})")
    // int insert(User user);
}