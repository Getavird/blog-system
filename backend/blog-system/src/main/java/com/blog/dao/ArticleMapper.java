package com.blog.dao;

import com.blog.entity.Article;
import com.blog.entity.vo.ArticleArchiveVO;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

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

        // 新增：获取所有归档文章（按年月分组）
        @Select("SELECT YEAR(create_time) as year, MONTH(create_time) as month, " +
                        "COUNT(*) as article_count " +
                        "FROM article " +
                        "WHERE status = 1 " +
                        "GROUP BY YEAR(create_time), MONTH(create_time) " +
                        "ORDER BY year DESC, month DESC")
        List<Map<String, Object>> getArchiveGroups();

        // 新增：根据年月获取文章列表
        @Select("SELECT a.id, a.title, a.create_time, a.tags, " +
                        "c.name as category_name " +
                        "FROM article a " +
                        "LEFT JOIN category c ON a.category_id = c.id " +
                        "WHERE YEAR(a.create_time) = #{year} " +
                        "AND MONTH(a.create_time) = #{month} " +
                        "AND a.status = 1 " +
                        "ORDER BY a.create_time DESC")
        List<ArticleArchiveVO> getArticlesByYearMonth(@Param("year") Integer year,
                        @Param("month") Integer month);

        // 新增：获取归档统计信息
        @Select("SELECT " +
                        "(SELECT COUNT(*) FROM article WHERE status = 1) as total_articles, " +
                        "(SELECT COUNT(DISTINCT YEAR(create_time)) FROM article WHERE status = 1) as total_years, " +
                        "(SELECT COUNT(DISTINCT CONCAT(YEAR(create_time), '-', MONTH(create_time))) " +
                        "FROM article WHERE status = 1) as total_months")
        Map<String, Object> getArchiveStats();

        /**
         * 获取热门文章（按浏览量排序）
         */
        @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar, " +
                        "c.name as category_name " +
                        "FROM article a " +
                        "LEFT JOIN user u ON a.user_id = u.id " +
                        "LEFT JOIN category c ON a.category_id = c.id " +
                        "WHERE a.status = 1 " +
                        "ORDER BY a.view_count DESC " +
                        "LIMIT #{limit}")
        List<Article> findHotArticles(@Param("limit") int limit);

        /**
         * 获取最新文章（按创建时间排序）
         */
        @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar, " +
                        "c.name as category_name " +
                        "FROM article a " +
                        "LEFT JOIN user u ON a.user_id = u.id " +
                        "LEFT JOIN category c ON a.category_id = c.id " +
                        "WHERE a.status = 1 " +
                        "ORDER BY a.create_time DESC " +
                        "LIMIT #{limit}")
        List<Article> findLatestArticles(@Param("limit") int limit);

        /**
         * 根据分类ID获取文章
         */
        @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar, " +
                        "c.name as category_name " +
                        "FROM article a " +
                        "LEFT JOIN user u ON a.user_id = u.id " +
                        "LEFT JOIN category c ON a.category_id = c.id " +
                        "WHERE a.status = 1 AND a.category_id = #{categoryId} " +
                        "ORDER BY a.create_time DESC " +
                        "LIMIT #{offset}, #{size}")
        List<Article> findByCategoryId(@Param("categoryId") Integer categoryId,
                        @Param("offset") int offset,
                        @Param("size") int size);

        /**
         * 根据标签名称获取文章
         */
        @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar, " +
                        "c.name as category_name " +
                        "FROM article a " +
                        "LEFT JOIN user u ON a.user_id = u.id " +
                        "LEFT JOIN category c ON a.category_id = c.id " +
                        "WHERE a.status = 1 AND a.tags LIKE CONCAT('%', #{tagName}, '%') " +
                        "ORDER BY a.create_time DESC " +
                        "LIMIT #{offset}, #{size}")
        List<Article> findByTagName(@Param("tagName") String tagName,
                        @Param("offset") int offset,
                        @Param("size") int size);

        /**
         * 统计文章总数
         */
        @Select("SELECT COUNT(*) FROM article WHERE status = 1")
        int count();

        /**
         * 统计总浏览量
         */
        @Select("SELECT SUM(view_count) FROM article WHERE status = 1")
        int sumViewCount();

        /**
         * 统计分类文章数量
         */
        @Select("SELECT c.id, c.name, COUNT(a.id) as article_count " +
                        "FROM category c " +
                        "LEFT JOIN article a ON c.id = a.category_id AND a.status = 1 " +
                        "GROUP BY c.id, c.name " +
                        "ORDER BY article_count DESC")
        List<Map<String, Object>> countArticlesByCategory();

        /**
         * 统计热门标签
         */
        @Select("SELECT tag_name, COUNT(*) as article_count FROM ( " +
                        "  SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(tags, ',', n.digit+1), ',', -1) as tag_name " +
                        "  FROM article " +
                        "  CROSS JOIN (SELECT 0 digit UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3) n " +
                        "  WHERE LENGTH(tags) - LENGTH(REPLACE(tags, ',', '')) >= n.digit " +
                        "    AND status = 1 " +
                        "    AND tags IS NOT NULL " +
                        "    AND tags != '' " +
                        ") t " +
                        "WHERE tag_name != '' " +
                        "GROUP BY tag_name " +
                        "ORDER BY article_count DESC " +
                        "LIMIT #{limit}")
        List<Map<String, Object>> countArticlesByTag(@Param("limit") int limit);

        // 根据分类ID获取最新文章
        @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar, " +
                        "c.name as category_name " +
                        "FROM article a " +
                        "LEFT JOIN user u ON a.user_id = u.id " +
                        "LEFT JOIN category c ON a.category_id = c.id " +
                        "WHERE a.status = 1 AND a.category_id = #{categoryId} " +
                        "ORDER BY a.create_time DESC " +
                        "LIMIT #{offset}, #{size}")
        List<Article> findLatestByCategory(@Param("categoryId") Integer categoryId,
                        @Param("offset") int offset,
                        @Param("size") int size);

        // 根据分类ID获取热门文章（按浏览量）
        @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar, " +
                        "c.name as category_name " +
                        "FROM article a " +
                        "LEFT JOIN user u ON a.user_id = u.id " +
                        "LEFT JOIN category c ON a.category_id = c.id " +
                        "WHERE a.status = 1 AND a.category_id = #{categoryId} " +
                        "ORDER BY a.view_count DESC " +
                        "LIMIT #{offset}, #{size}")
        List<Article> findHotByCategory(@Param("categoryId") Integer categoryId,
                        @Param("offset") int offset,
                        @Param("size") int size);

        // 根据分类ID获取按点赞排序的文章
        @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar, " +
                        "c.name as category_name " +
                        "FROM article a " +
                        "LEFT JOIN user u ON a.user_id = u.id " +
                        "LEFT JOIN category c ON a.category_id = c.id " +
                        "WHERE a.status = 1 AND a.category_id = #{categoryId} " +
                        "ORDER BY a.like_count DESC " +
                        "LIMIT #{offset}, #{size}")
        List<Article> findLikesByCategory(@Param("categoryId") Integer categoryId,
                        @Param("offset") int offset,
                        @Param("size") int size);

        // 统计分类下的总阅读量
        @Select("SELECT SUM(view_count) FROM article WHERE category_id = #{categoryId} AND status = 1")
        Integer sumViewCountByCategory(@Param("categoryId") Integer categoryId);

        // 统计分类下的总点赞数
        @Select("SELECT SUM(like_count) FROM article WHERE category_id = #{categoryId} AND status = 1")
        Integer sumLikeCountByCategory(@Param("categoryId") Integer categoryId);

        // 根据标签名称搜索文章（用于标签页面，带排序）
        @Select({
                "<script>",
                "SELECT DISTINCT a.*, u.username as author_name, u.avatar as author_avatar, ",
                "c.name as category_name ",
                "FROM article a ",
                "LEFT JOIN user u ON a.user_id = u.id ",
                "LEFT JOIN category c ON a.category_id = c.id ",
                "WHERE a.status = 1 ",
                "AND (",
                "  a.tags LIKE CONCAT('%', #{tagName}, '%') ",
                "  OR EXISTS (",
                "    SELECT 1 FROM article_tag at ",
                "    INNER JOIN tag t ON at.tag_id = t.id ",
                "    WHERE at.article_id = a.id AND t.name = #{tagName}",
                "  )",
                ") ",
                "<if test='sortType != null and sortType == \"hot\"'>",
                "  ORDER BY a.view_count DESC ",
                "</if>",
                "<if test='sortType != null and sortType == \"likes\"'>",
                "  ORDER BY a.like_count DESC ",
                "</if>",
                "<if test='sortType == null or sortType == \"latest\"'>",
                "  ORDER BY a.create_time DESC ",
                "</if>",
                "LIMIT #{offset}, #{size}",
                "</script>"
        })
        List<Article> findByTagNameWithSort(@Param("tagName") String tagName,
                                           @Param("sortType") String sortType,
                                           @Param("offset") int offset,
                                           @Param("size") int size);

        // 统计标签下的文章数量
        @Select({
                "SELECT COUNT(DISTINCT a.id) FROM article a ",
                "WHERE a.status = 1 ",
                "AND (",
                "  a.tags LIKE CONCAT('%', #{tagName}, '%') ",
                "  OR EXISTS (",
                "    SELECT 1 FROM article_tag at ",
                "    INNER JOIN tag t ON at.tag_id = t.id ",
                "    WHERE at.article_id = a.id AND t.name = #{tagName}",
                "  )",
                ")"
        })
        int countByTagName(@Param("tagName") String tagName);

        // 获取年份统计（每年文章数量）
        @Select("SELECT YEAR(create_time) as year, COUNT(*) as article_count, " +
                        "SUM(view_count) as view_count, SUM(like_count) as like_count " +
                        "FROM article WHERE status = 1 " +
                        "GROUP BY YEAR(create_time) " +
                        "ORDER BY year DESC")
        List<Map<String, Object>> getYearStats();

        // 获取某年所有文章（用于年份筛选）
        @Select("SELECT a.*, u.username as author_name, u.avatar as author_avatar, " +
                        "c.name as category_name " +
                        "FROM article a " +
                        "LEFT JOIN user u ON a.user_id = u.id " +
                        "LEFT JOIN category c ON a.category_id = c.id " +
                        "WHERE YEAR(a.create_time) = #{year} AND a.status = 1 " +
                        "ORDER BY a.create_time DESC")
        List<Article> findByYear(@Param("year") Integer year);

        // 获取某年每月的统计
        @Select("SELECT MONTH(create_time) as month, COUNT(*) as article_count, " +
                        "SUM(view_count) as view_count, SUM(like_count) as like_count " +
                        "FROM article " +
                        "WHERE YEAR(create_time) = #{year} AND status = 1 " +
                        "GROUP BY MONTH(create_time) " +
                        "ORDER BY month DESC")
        List<Map<String, Object>> getMonthStatsByYear(@Param("year") Integer year);
}