package com.blog.service.impl;

import com.blog.dao.TagMapper;
import com.blog.entity.Tag;
import com.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class TagServiceImpl implements TagService {
    
    @Autowired
    private TagMapper tagMapper;
    
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
        // 这个方法需要先创建 article_tag 表的Mapper
        // 这里先返回true，等Mapper完善后再实现
        System.out.println("🔖 处理文章标签 - 文章ID: " + articleId + ", 标签IDs: " + tagIds);
        return true;
    }
    
    @Override
    public List<Tag> searchTags(String keyword) {
        // 简单的模糊搜索实现
        // 实际项目中可以使用全文搜索或更复杂的查询
        List<Tag> allTags = tagMapper.findAll();
        return allTags.stream()
                .filter(tag -> tag.getName().contains(keyword) || 
                               tag.getDescription().contains(keyword))
                .toList();
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