package com.blog.dao;

import com.blog.entity.Article;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper {

        /**
         * 根据ID查询文章
         */
        @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar, " +
                        "c.name as category_name " +
                        "FROM article a " +
                        "LEFT JOIN user u ON a.user_id = u.id " +
                        "LEFT JOIN category c ON a.category_id = c.id " +
                        "WHERE a.id = #{id} AND a.status = 1")
        Article findById(Integer id);

        /**
         * 查询文章列表（分页）
         */
        @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar, " +
                        "c.name as category_name " +
                        "FROM article a " +
                        "LEFT JOIN user u ON a.user_id = u.id " +
                        "LEFT JOIN category c ON a.category_id = c.id " +
                        "WHERE a.status = 1 " +
                        "ORDER BY a.is_top DESC, a.create_time DESC " +
                        "LIMIT #{offset}, #{size}")
        List<Article> findList(@Param("offset") int offset, @Param("size") int size);

        /**
         * 插入文章
         */
        @Insert("INSERT INTO article (title, content, summary, cover_image, status, " +
                        "user_id, category_id, is_top, allow_comment, tags, create_time, update_time) " +
                        "VALUES (#{title}, #{content}, #{summary}, #{coverImage}, #{status}, " +
                        "#{userId}, #{categoryId}, #{isTop}, #{allowComment}, #{tags}, NOW(), NOW())")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(Article article);

        /**
         * 更新文章
         */
        @Update("UPDATE article SET " +
                        "title = #{title}, content = #{content}, summary = #{summary}, " +
                        "cover_image = #{coverImage}, status = #{status}, category_id = #{categoryId}, " +
                        "is_top = #{isTop}, allow_comment = #{allowComment}, tags = #{tags}, " +
                        "update_time = NOW() " +
                        "WHERE id = #{id}")
        int update(Article article);

        /**
         * 增加阅读量
         */
        @Update("UPDATE article SET view_count = view_count + 1 WHERE id = #{id}")
        int incrementViewCount(Integer id);

        /**
         * 搜索文章
         */
        @Select({
                        "<script>",
                        "SELECT a.*, u.username as author_name, c.name as category_name ",
                        "FROM article a ",
                        "LEFT JOIN user u ON a.user_id = u.id ",
                        "LEFT JOIN category c ON a.category_id = c.id ",
                        "WHERE a.status = 1 ",
                        "  AND (a.title LIKE CONCAT('%', #{keyword}, '%') ",
                        "    OR a.content LIKE CONCAT('%', #{keyword}, '%') ",
                        "    OR a.tags LIKE CONCAT('%', #{keyword}, '%') ",
                        "    OR a.summary LIKE CONCAT('%', #{keyword}, '%')) ",
                        "ORDER BY a.create_time DESC ",
                        "LIMIT #{offset}, #{size}",
                        "</script>"
        })
        List<Article> searchArticles(@Param("keyword") String keyword,
                        @Param("offset") int offset,
                        @Param("size") int size);

        /**
         * 统计搜索文章数量
         */
        @Select({
                        "<script>",
                        "SELECT COUNT(*) FROM article a ",
                        "WHERE a.status = 1 ",
                        "  AND (a.title LIKE CONCAT('%', #{keyword}, '%') ",
                        "    OR a.content LIKE CONCAT('%', #{keyword}, '%') ",
                        "    OR a.tags LIKE CONCAT('%', #{keyword}, '%') ",
                        "    OR a.summary LIKE CONCAT('%', #{keyword}, '%'))",
                        "</script>"
        })
        int countSearchArticles(@Param("keyword") String keyword);

        /**
         * 高级搜索
         */
        @Select({
                        "<script>",
                        "SELECT a.*, u.username as author_name, c.name as category_name ",
                        "FROM article a ",
                        "LEFT JOIN user u ON a.user_id = u.id ",
                        "LEFT JOIN category c ON a.category_id = c.id ",
                        "WHERE a.status = 1 ",
                        "<if test='title != null and title != \"\"'>",
                        "  AND a.title LIKE CONCAT('%', #{title}, '%') ",
                        "</if>",
                        "<if test='content != null and content != \"\"'>",
                        "  AND a.content LIKE CONCAT('%', #{content}, '%') ",
                        "</if>",
                        "<if test='tag != null and tag != \"\"'>",
                        "  AND a.tags LIKE CONCAT('%', #{tag}, '%') ",
                        "</if>",
                        "<if test='category != null and category != \"\"'>",
                        "  AND c.name LIKE CONCAT('%', #{category}, '%') ",
                        "</if>",
                        "<if test='minView != null'>",
                        "  AND a.view_count >= #{minView} ",
                        "</if>",
                        "<if test='maxView != null'>",
                        "  AND a.view_count &lt;= #{maxView} ",
                        "</if>",
                        "ORDER BY a.create_time DESC ",
                        "LIMIT #{offset}, #{size}",
                        "</script>"
        })
        List<Article> advancedSearch(@Param("title") String title,
                        @Param("content") String content,
                        @Param("tag") String tag,
                        @Param("category") String category,
                        @Param("minView") Integer minView,
                        @Param("maxView") Integer maxView,
                        @Param("offset") int offset,
                        @Param("size") int size);

        /**
         * 统计高级搜索数量
         */
        @Select({
                        "<script>",
                        "SELECT COUNT(*) FROM article a ",
                        "LEFT JOIN category c ON a.category_id = c.id ",
                        "WHERE a.status = 1 ",
                        "<if test='title != null and title != \"\"'>",
                        "  AND a.title LIKE CONCAT('%', #{title}, '%') ",
                        "</if>",
                        "<if test='content != null and content != \"\"'>",
                        "  AND a.content LIKE CONCAT('%', #{content}, '%') ",
                        "</if>",
                        "<if test='tag != null and tag != \"\"'>",
                        "  AND a.tags LIKE CONCAT('%', #{tag}, '%') ",
                        "</if>",
                        "<if test='category != null and category != \"\"'>",
                        "  AND c.name LIKE CONCAT('%', #{category}, '%') ",
                        "</if>",
                        "<if test='minView != null'>",
                        "  AND a.view_count >= #{minView} ",
                        "</if>",
                        "<if test='maxView != null'>",
                        "  AND a.view_count &lt;= #{maxView} ",
                        "</if>",
                        "</script>"
        })
        int countAdvancedSearch(@Param("title") String title,
                        @Param("content") String content,
                        @Param("tag") String tag,
                        @Param("category") String category,
                        @Param("minView") Integer minView,
                        @Param("maxView") Integer maxView);
}
