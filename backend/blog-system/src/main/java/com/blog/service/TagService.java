package com.blog.service;

import com.blog.entity.Tag;
import java.util.Map;
import java.util.List;

public interface TagService {

    /**
     * 获取所有标签
     */
    List<Tag> getAllTags();

    /**
     * 获取热门标签
     */
    List<Tag> getHotTags(int limit);

    /**
     * 根据ID获取标签
     */
    Tag getTagById(Integer id);

    /**
     * 创建标签
     */
    Tag createTag(Tag tag);

    /**
     * 更新标签
     */
    boolean updateTag(Tag tag);

    /**
     * 删除标签
     */
    boolean deleteTag(Integer id);

    /**
     * 根据文章ID获取标签
     */
    List<Tag> getTagsByArticleId(Integer articleId);

    /**
     * 批量处理文章标签
     */
    boolean processArticleTags(Integer articleId, List<Integer> tagIds);

    /**
     * 根据名称搜索标签
     */
    List<Tag> searchTags(String keyword);

    /**
     * 获取标签页面概览统计
     */
    Map<String, Object> getTagOverview();

    /**
     * 获取标签云数据（带文章数量）
     */
    List<Map<String, Object>> getTagCloud(int limit);

    /**
     * 根据标签名称获取标签详情（带统计信息）
     */
    Map<String, Object> getTagDetailByName(String tagName);

    /**
     * 根据标签名称获取相关文章（支持分页和排序）
     */
    Map<String, Object> getArticlesByTagName(String tagName, int page, int size, String sortType);

    /**
     * 获取标签总数和文章总数
     */
    Map<String, Object> getTagStats();

    /**
     * 搜索标签（支持模糊搜索）
     */
    List<Map<String, Object>> searchTagWithStats(String keyword);
}