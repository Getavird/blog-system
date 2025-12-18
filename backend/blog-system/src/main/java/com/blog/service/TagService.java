package com.blog.service;

import com.blog.entity.Tag;

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
}