package com.blog.dao;

import com.blog.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
    // @Insert("INSERT INTO category(name, description, icon, order_num) VALUES(#{name}, #{description}, #{icon}, #{orderNum})")
    // int insert(Category category);
    
    /**
     * 更新分类
     */
    // @Update("UPDATE category SET name=#{name}, description=#{description}, icon=#{icon}, order_num=#{orderNum} WHERE id=#{id}")
    // int update(Category category);
    
    /**
     * 删除分类
     */
    // @Delete("DELETE FROM category WHERE id = #{id}")
    // int delete(Integer id);
}