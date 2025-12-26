package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Tag;
import com.blog.service.TagService;
import com.blog.utils.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
     * 创建标签（普通用户也可以创建，用于文章编写）
     */
    @PostMapping
    public Result<Tag> createTag(@RequestBody Tag tag, HttpServletRequest request) {
        // 改为：检查登录即可，不需要管理员权限
        if (!SessionUtil.isLogin(request)) {
            return Result.unauthorized("请先登录");
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

    /**
     * 标签页面概览
     * GET /api/tags/overview
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getTagOverview() {
        try {
            Map<String, Object> overview = tagService.getTagOverview();
            return Result.success(overview);
        } catch (Exception e) {
            return Result.error("获取标签概览失败: " + e.getMessage());
        }
    }

    /**
     * 标签云
     * GET /api/tags/cloud
     */
    @GetMapping("/cloud")
    public Result<List<Map<String, Object>>> getTagCloud() {
        try {
            List<Map<String, Object>> tagCloud = tagService.getTagCloud(100); // 限制100个标签
            return Result.success(tagCloud);
        } catch (Exception e) {
            return Result.error("获取标签云失败: " + e.getMessage());
        }
    }

    /**
     * 标签详情
     * GET /api/tags/name/{tagName}/detail
     */
    @GetMapping("/name/{tagName}/detail")
    public Result<Map<String, Object>> getTagDetailByName(@PathVariable String tagName) {
        try {
            Map<String, Object> tagDetail = tagService.getTagDetailByName(tagName);
            return Result.success(tagDetail);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取标签详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据标签获取文章列表（支持分页和排序）
     * GET /api/tags/name/{tagName}/articles?page=1&size=15&sort=latest
     * 
     * 排序参数：
     * - latest: 最新（按创建时间）
     * - hot: 最热（按浏览量）
     * - likes: 点赞（按点赞数）
     */
    @GetMapping("/name/{tagName}/articles")
    public Result<Map<String, Object>> getArticlesByTagName(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "15") Integer size,
            @RequestParam(defaultValue = "latest") String sort) {

        // 验证排序参数
        String[] validSorts = { "latest", "hot", "likes" };
        if (!Arrays.asList(validSorts).contains(sort.toLowerCase())) {
            return Result.badRequest("排序参数错误，可选值：latest, hot, likes");
        }

        // 验证分页参数
        if (page < 1)
            page = 1;
        if (size < 1 || size > 50)
            size = 15;

        try {
            Map<String, Object> articlesData = tagService.getArticlesByTagName(tagName, page, size, sort.toLowerCase());
            return Result.success(articlesData);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取标签文章失败: " + e.getMessage());
        }
    }

    /**
     * 获取标签统计
     * GET /api/tags/stats
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getTagStats() {
        try {
            Map<String, Object> stats = tagService.getTagStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取标签统计失败: " + e.getMessage());
        }
    }

    /**
     * 搜索标签（带统计信息）
     * GET /api/tags/search-with-stats?keyword=xxx
     */
    @GetMapping("/search-with-stats")
    public Result<List<Map<String, Object>>> searchTagWithStats(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.badRequest("搜索关键词不能为空");
        }

        try {
            List<Map<String, Object>> tags = tagService.searchTagWithStats(keyword);
            return Result.success(tags);
        } catch (Exception e) {
            return Result.error("搜索标签失败: " + e.getMessage());
        }
    }
}