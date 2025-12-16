package com.blog.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TestMapper {
    
    @Select("SELECT 1")
    int testConnection();
    
    @Select("SELECT DATABASE()")
    String getDatabaseName();
    
    @Select("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = #{tableName}")
    int checkTableExists(String tableName);
    
    @Select("SELECT COUNT(*) FROM user")
    int countUsers();
}