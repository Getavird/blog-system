package com.blog.dao;

import com.blog.entity.Tag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TagMapper {

        /**
         * 查询所有标签
         */
        @Select("SELECT * FROM tag ORDER BY article_count DESC, name ASC")
        List<Tag> findAll();

        /**
         * 根据ID查询标签
         */
        @Select("SELECT * FROM tag WHERE id = #{id}")
        Tag findById(Integer id);

        /**
         * 根据名称查询标签
         */
        @Select("SELECT * FROM tag WHERE name = #{name}")
        Tag findByName(String name);

        /**
         * 根据别名查询标签
         */
        @Select("SELECT * FROM tag WHERE slug = #{slug}")
        Tag findBySlug(String slug);

        /**
         * 插入标签
         */
        @Insert("INSERT INTO tag(name, slug, description, color, icon, create_time) " +
                        "VALUES(#{name}, #{slug}, #{description}, #{color}, #{icon}, NOW())")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(Tag tag);

        /**
         * 更新标签
         */
        @Update("UPDATE tag SET name=#{name}, slug=#{slug}, description=#{description}, " +
                        "color=#{color}, icon=#{icon} WHERE id=#{id}")
        int update(Tag tag);

        /**
         * 删除标签
         */
        @Delete("DELETE FROM tag WHERE id = #{id}")
        int delete(Integer id);

        /**
         * 根据文章ID查询标签
         */
        @Select("SELECT t.* FROM tag t " +
                        "INNER JOIN article_tag at ON t.id = at.tag_id " +
                        "WHERE at.article_id = #{articleId}")
        List<Tag> findByArticleId(Integer articleId);

        /**
         * 获取热门标签（按文章数量排序）
         */
        @Select("SELECT * FROM tag ORDER BY article_count DESC LIMIT #{limit}")
        List<Tag> findHotTags(@Param("limit") int limit);

        /**
         * 更新标签的文章数量
         */
        @Update("UPDATE tag SET article_count = " +
                        "(SELECT COUNT(*) FROM article_tag WHERE tag_id = #{tagId}) " +
                        "WHERE id = #{tagId}")
        int updateArticleCount(@Param("tagId") Integer tagId);

        /**
         * 批量查询标签（用于文章编辑时显示）
         */
        @Select({
                        "<script>",
                        "SELECT * FROM tag WHERE id IN",
                        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
                        "#{id}",
                        "</foreach>",
                        "</script>"
        })
        List<Tag> findByIds(@Param("ids") List<Integer> ids);
}