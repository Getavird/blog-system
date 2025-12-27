package com.blog.service.impl;

import com.blog.dao.ArticleMapper;
import com.blog.dao.ArticleTagMapper;
import com.blog.dao.TagMapper;
import com.blog.entity.Article;
import com.blog.entity.Tag;
import com.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public List<Tag> getAllTags() {
        return tagMapper.findAll();
    }

    @Override
    public List<Tag> getHotTags(int limit) {
        return tagMapper.findHotTags(limit);
    }

    @Override
    public Tag getTagById(Integer id) {
        return tagMapper.findById(id);
    }

    @Override
    public Tag createTag(Tag tag) {
        try {
            // 1. 验证标签名称
            if (!StringUtils.hasText(tag.getName())) {
                throw new RuntimeException("标签名称不能为空");
            }

            // 2. 检查标签名称是否已存在
            Tag existTag = tagMapper.findByName(tag.getName());
            if (existTag != null) {
                throw new RuntimeException("标签名称已存在");
            }

            // 3. 生成slug（如果没有提供）
            if (!StringUtils.hasText(tag.getSlug())) {
                tag.setSlug(generateSlug(tag.getName()));
            } else {
                // 检查slug是否已存在
                Tag existSlugTag = tagMapper.findBySlug(tag.getSlug());
                if (existSlugTag != null) {
                    throw new RuntimeException("标签别名已存在");
                }
            }

            // 4. 设置默认值
            if (tag.getColor() == null) {
                tag.setColor("#409eff");
            }

            // 5. 保存标签
            int result = tagMapper.insert(tag);
            if (result > 0) {
                System.out.println("✅ 标签创建成功: " + tag.getName() + " (ID: " + tag.getId() + ")");
                return tag;
            } else {
                throw new RuntimeException("创建标签失败");
            }

        } catch (RuntimeException e) {
            System.err.println("❌ 创建标签异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ 创建标签系统异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("创建标签失败");
        }
    }

    @Override
    public boolean updateTag(Tag tag) {
        try {
            // 1. 检查标签是否存在
            Tag existingTag = tagMapper.findById(tag.getId());
            if (existingTag == null) {
                throw new RuntimeException("标签不存在");
            }

            // 2. 如果修改了名称，检查新名称是否已存在
            if (!existingTag.getName().equals(tag.getName())) {
                Tag sameNameTag = tagMapper.findByName(tag.getName());
                if (sameNameTag != null && !sameNameTag.getId().equals(tag.getId())) {
                    throw new RuntimeException("标签名称已存在");
                }
            }

            // 3. 如果修改了slug，检查新slug是否已存在
            if (!existingTag.getSlug().equals(tag.getSlug())) {
                Tag sameSlugTag = tagMapper.findBySlug(tag.getSlug());
                if (sameSlugTag != null && !sameSlugTag.getId().equals(tag.getId())) {
                    throw new RuntimeException("标签别名已存在");
                }
            }

            int result = tagMapper.update(tag);
            return result > 0;

        } catch (RuntimeException e) {
            System.err.println("❌ 更新标签异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ 更新标签系统异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteTag(Integer id) {
        try {
            // 检查标签下是否有文章
            Tag tag = tagMapper.findById(id);
            if (tag == null) {
                throw new RuntimeException("标签不存在");
            }

            if (tag.getArticleCount() > 0) {
                throw new RuntimeException("该标签下有文章，无法删除");
            }

            int result = tagMapper.delete(id);
            return result > 0;

        } catch (RuntimeException e) {
            System.err.println("❌ 删除标签异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ 删除标签系统异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Tag> getTagsByArticleId(Integer articleId) {
        return tagMapper.findByArticleId(articleId);
    }

    @Override
    public boolean processArticleTags(Integer articleId, List<Integer> tagIds) {
        try {
            // 1. 先删除文章的所有标签关联
            articleTagMapper.deleteByArticleId(articleId);

            // 2. 插入新的标签关联
            if (tagIds != null && !tagIds.isEmpty()) {
                for (Integer tagId : tagIds) {
                    articleTagMapper.insert(articleId, tagId);
                }
            }

            // 3. 更新标签的文章数量
            for (Integer tagId : tagIds) {
                tagMapper.updateArticleCount(tagId);
            }

            return true;

        } catch (Exception e) {
            System.err.println("❌ 处理文章标签异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Tag> searchTags(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        // 使用更宽松的搜索条件
        List<Tag> allTags = tagMapper.findAll();
        String lowerKeyword = keyword.toLowerCase();

        return allTags.stream()
                .filter(tag -> tag.getName() != null &&
                        tag.getName().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    // 新增方法实现

    @Override
    public Map<String, Object> getTagOverview() {
        Map<String, Object> overview = new HashMap<>();

        // 标签总数
        List<Tag> allTags = tagMapper.findAll();
        overview.put("tagCount", allTags.size());

        // 文章总数（有标签的文章）
        int totalArticlesWithTags = 0;
        for (Tag tag : allTags) {
            totalArticlesWithTags += tag.getArticleCount();
        }
        overview.put("articleCount", totalArticlesWithTags);

        // 热门标签（前10个）
        List<Map<String, Object>> hotTags = articleTagMapper.findHotTagsWithCount(10);
        overview.put("hotTags", hotTags);

        return overview;
    }

    @Override
    public List<Map<String, Object>> getTagCloud(int limit) {
        return articleTagMapper.findAllTagsWithCount();
    }

    @Override
    public Map<String, Object> getTagDetailByName(String tagName) {
        // 获取标签统计信息
        Map<String, Object> tagStats = articleTagMapper.findTagStatsByName(tagName);

        if (tagStats == null || tagStats.isEmpty()) {
            throw new RuntimeException("标签不存在");
        }

        return tagStats;
    }

    @Override
    public Map<String, Object> getArticlesByTagName(String tagName, int page, int size, String sortType) {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取标签信息
        Map<String, Object> tagInfo = getTagDetailByName(tagName);
        if (tagInfo == null) {
            throw new RuntimeException("标签不存在");
        }

        // 2. 获取文章ID列表
        List<Integer> articleIds = articleTagMapper.findArticleIdsByTagName(tagName);
        int total = articleIds.size();

        if (total == 0) {
            result.put("articles", new ArrayList<>());
            result.put("total", 0);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", 0);
            result.put("tagInfo", tagInfo);
            return result;
        }

        // 3. 分页计算
        int offset = (page - 1) * size;
        int endIndex = Math.min(offset + size, total);

        // 4. 获取文章详情（这里简化处理，实际应该根据ID列表查询）
        // 注意：这里需要根据sortType排序，但需要更复杂的SQL实现
        // 暂时返回所有文章，由前端分页
        List<Article> articles = new ArrayList<>();
        List<Integer> pageArticleIds = articleIds.subList(offset, endIndex);

        for (Integer articleId : pageArticleIds) {
            Article article = articleMapper.findById(articleId);
            if (article != null) {
                articles.add(article);
            }
        }

        // 5. 计算总页数
        int totalPages = (int) Math.ceil((double) total / size);

        // 6. 组装结果
        result.put("articles", articles);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", totalPages);
        result.put("tagInfo", tagInfo);

        return result;
    }

    @Override
    public Map<String, Object> getTagStats() {
        Map<String, Object> stats = new HashMap<>();

        // 标签总数
        List<Tag> allTags = tagMapper.findAll();
        stats.put("tagCount", allTags.size());

        // 文章总数（有标签的文章）
        int totalArticlesWithTags = 0;
        for (Tag tag : allTags) {
            totalArticlesWithTags += tag.getArticleCount();
        }
        stats.put("articleCount", totalArticlesWithTags);

        return stats;
    }

    @Override
    public List<Map<String, Object>> searchTagWithStats(String keyword) {
        List<Tag> tags = searchTags(keyword);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Tag tag : tags) {
            Map<String, Object> tagWithStats = new HashMap<>();
            tagWithStats.put("id", tag.getId());
            tagWithStats.put("name", tag.getName());
            tagWithStats.put("description", tag.getDescription());
            tagWithStats.put("color", tag.getColor());
            tagWithStats.put("articleCount", tag.getArticleCount());
            result.add(tagWithStats);
        }

        return result;
    }

    /**
     * 生成标签slug（将中文转换为拼音或处理特殊字符）
     * 这里简化实现，实际项目可以使用拼音转换库
     */
    private String generateSlug(String name) {
        // 简单实现：移除特殊字符，用-连接
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

}