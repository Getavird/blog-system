package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Tag;
import com.blog.service.TagService;
import com.blog.utils.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    
    @Autowired
    private TagService tagService;
    
    /**
     * 获取所有标签
     */
    @GetMapping
    public Result<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return Result.success(tags);
    }
    
    /**
     * 获取热门标签
     */
    @GetMapping("/hot")
    public Result<List<Tag>> getHotTags(@RequestParam(defaultValue = "10") Integer limit) {
        List<Tag> tags = tagService.getHotTags(limit);
        return Result.success(tags);
    }
    
    /**
     * 根据ID获取标签
     */
    @GetMapping("/{id}")
    public Result<Tag> getTagById(@PathVariable Integer id) {
        Tag tag = tagService.getTagById(id);
        if (tag == null) {
            return Result.notFound("标签不存在");
        }
        return Result.success(tag);
    }
    
    /**
     * 创建标签（需要管理员权限）
     */
    @PostMapping
    public Result<Tag> createTag(@RequestBody Tag tag, HttpServletRequest request) {
        // 检查管理员权限
        if (!SessionUtil.isAdmin(request)) {
            return Result.forbidden("需要管理员权限");
        }
        
        try {
            Tag createdTag = tagService.createTag(tag);
            return Result.created(createdTag);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新标签（需要管理员权限）
     */
    @PutMapping("/{id}")
    public Result<Tag> updateTag(@PathVariable Integer id, @RequestBody Tag tag, HttpServletRequest request) {
        // 检查管理员权限
        if (!SessionUtil.isAdmin(request)) {
            return Result.forbidden("需要管理员权限");
        }
        
        tag.setId(id);
        try {
            boolean success = tagService.updateTag(tag);
            return success ? Result.success(tag) : Result.error("更新失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除标签（需要管理员权限）
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteTag(@PathVariable Integer id, HttpServletRequest request) {
        // 检查管理员权限
        if (!SessionUtil.isAdmin(request)) {
            return Result.forbidden("需要管理员权限");
        }
        
        try {
            boolean success = tagService.deleteTag(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据文章ID获取标签
     */
    @GetMapping("/article/{articleId}")
    public Result<List<Tag>> getTagsByArticleId(@PathVariable Integer articleId) {
        List<Tag> tags = tagService.getTagsByArticleId(articleId);
        return Result.success(tags);
    }
    
    /**
     * 搜索标签
     */
    @GetMapping("/search")
    public Result<List<Tag>> searchTags(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.badRequest("搜索关键词不能为空");
        }
        
        List<Tag> tags = tagService.searchTags(keyword);
        return Result.success(tags);
    }
}