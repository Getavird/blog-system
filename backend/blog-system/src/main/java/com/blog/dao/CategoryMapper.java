package com.blog.dao;

import com.blog.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {
    
    /**
     * 查询所有分类
     */
    @Select("SELECT * FROM category ORDER BY order_num ASC")
    List<Category> findAll();
    
    /**
     * 根据ID查询分类
     */
    @Select("SELECT * FROM category WHERE id = #{id}")
    Category findById(Integer id);
    
    /**
     * 插入分类
     */
    @Insert("INSERT INTO category(name, description, order_num, icon, color, create_time) " +
            "VALUES(#{name}, #{description}, #{orderNum}, #{icon}, #{color}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);
    
    /**
     * 更新分类
     */
    @Update("UPDATE category SET name=#{name}, description=#{description}, " +
            "order_num=#{orderNum}, icon=#{icon}, color=#{color} WHERE id=#{id}")
    int update(Category category);
    
    /**
     * 删除分类
     */
    @Delete("DELETE FROM category WHERE id = #{id}")
    int delete(Integer id);
    
    /**
     * 统计分类下的文章数量
     */
    @Select("SELECT COUNT(*) FROM article WHERE category_id = #{categoryId}")
    int countArticlesByCategory(Integer categoryId);
    
    /**
     * 根据名称查询分类（用于检查重复）
     */
    @Select("SELECT * FROM category WHERE name = #{name}")
    Category findByName(String name);
}