package com.blog.dao;

import com.blog.entity.SearchRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SearchRecordMapper {
    
    /**
     * 插入或更新搜索记录
     */
    @Insert("INSERT INTO search_record (keyword, user_id, search_count, last_search_time, create_time) " +
            "VALUES (#{keyword}, #{userId}, 1, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "search_count = search_count + 1, last_search_time = NOW()")
    int upsert(SearchRecord record);
    
    /**
     * 获取热门搜索关键词
     */
    @Select("SELECT keyword, SUM(search_count) as total_count " +
            "FROM search_record " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "GROUP BY keyword " +
            "ORDER BY total_count DESC " +
            "LIMIT #{limit}")
    List<String> getHotKeywords(@Param("limit") int limit);
    
    /**
     * 获取用户的搜索历史
     */
    @Select("SELECT DISTINCT keyword FROM search_record " +
            "WHERE user_id = #{userId} " +
            "ORDER BY last_search_time DESC " +
            "LIMIT #{limit}")
    List<String> getSearchHistory(@Param("userId") Integer userId, 
                                  @Param("limit") int limit);
    
    /**
     * 清空搜索记录
     */
    @Delete("DELETE FROM search_record")
    int clearAll();
}