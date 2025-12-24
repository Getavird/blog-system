package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文章实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Article extends BaseEntity {
    /**
     * 文章ID
     */
    private Integer id;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 文章内容（HTML格式）
     */
    private String content;
    
    /**
     * 文章摘要
     */
    private String summary;
    
    /**
     * 封面图片URL
     */
    private String coverImage;
    
    /**
     * 文章状态：0-草稿，1-发布，2-删除
     */
    private Integer status = 1;
    
    /**
     * 阅读次数
     */
    private Integer viewCount = 0;
    
    /**
     * 点赞数
     */
    private Integer likeCount = 0;
    
    /**
     * 评论数
     */
    private Integer commentCount = 0;
    
    /**
     * 作者ID（关联user表）
     */
    private Integer userId;
    
    /**
     * 分类ID（关联category表）
     */
    private Integer categoryId;
    
    /**
     * 关联字段：作者用户名
     */
    private String authorName;
    
    /**
     * 关联字段：作者头像
     */
    private String authorAvatar;
    
    /**
     * 关联字段：分类名称
     */
    private String categoryName;
    
    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop = 0;
    
    /**
     * 是否允许评论：0-否，1-是
     */
    private Integer allowComment = 1;
    
    /**
     * 文章标签，多个用逗号分隔
     */
    private String tags;
    
    /**
     * 是否已点赞（查询时使用，非数据库字段）
     */
    private Boolean isLiked = false;
    
    /**
     * 是否已关注作者（查询时使用，非数据库字段）
     */
    private Boolean isFollowing = false;
    
    /**
     * 作者统计信息（查询时使用，非数据库字段）
     */
    private Map<String, Object> authorStats;

    /**
     * 标签列表（查询时使用）
     */
    private List<Tag> tagList;
    
    /**
     * 标签ID列表（用于编辑时传递）
     */
    private List<Integer> tagIds;
    
    // ... 其他字段不变 ...
    
    /**
     * 获取标签名称列表
     */
    public List<String> getTagNames() {
        return tagList.stream()
            .map(Tag::getName)
            .collect(Collectors.toList());
    }
}