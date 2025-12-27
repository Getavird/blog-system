package com.blog.service.impl;

import com.blog.dao.ArticleMapper;
import com.blog.dao.ArticleTagMapper;
import com.blog.dao.TagMapper;
import com.blog.entity.Article;
import com.blog.entity.Tag;
import com.blog.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Override
    public List<Article> getArticles(int page, int size) {
        int offset = (page - 1) * size;
        return articleMapper.findList(offset, size);
    }

    @Override
    public Article getArticleById(Integer id) {
        // 先获取文章
        Article article = articleMapper.findById(id);

        // 增加阅读量
        if (article != null) {
            articleMapper.incrementViewCount(id);
            // 重新获取更新后的数据
            article = articleMapper.findById(id);
        }

        return article;
    }

    @Override
    public boolean createArticle(Article article) {
        // 先保存文章，获取ID
        int result = articleMapper.insert(article);
        if (result > 0 && article.getId() != null) {
            // 然后处理标签
            processArticleTags(article);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateArticle(Article article) {
        // 处理标签逻辑
        processArticleTags(article);

        int result = articleMapper.update(article);
        return result > 0;
    }

    @Override
    public boolean deleteArticle(Integer id) {
        // 这里使用软删除，将状态设为2
        Article article = new Article();
        article.setId(id);
        article.setStatus(2); // 删除状态
        int result = articleMapper.update(article);
        return result > 0;
    }

    @Override
    public void incrementViewCount(Integer id) {
        articleMapper.incrementViewCount(id);
    }

    @Override
    public List<Article> getHotArticles(int limit) {
        return articleMapper.findHotArticles(limit);
    }

    @Override
    public List<Article> getLatestArticles(int limit) {
        return articleMapper.findLatestArticles(limit);
    }

    @Override
    public List<Article> getArticlesByCategory(Integer categoryId, int page, int size) {
        int offset = (page - 1) * size;
        return articleMapper.findByCategoryId(categoryId, offset, size);
    }

    @Override
    public List<Article> getArticlesByTag(String tagName, int page, int size) {
        int offset = (page - 1) * size;
        return articleMapper.findByTagName(tagName, offset, size);
    }

    @Override
    public int getArticleCount() {
        return articleMapper.count();
    }

    @Override
    public int getTotalViewCount() {
        Integer sum = articleMapper.sumViewCount();
        return sum != null ? sum : 0;
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        return articleMapper.countArticlesByCategory();
    }

    @Override
    public List<Map<String, Object>> getHotTags(int limit) {
        return articleMapper.countArticlesByTag(limit);
    }

    /**
     * 处理文章标签逻辑
     * 1. 解析tags字符串（逗号分隔）
     * 2. 创建或获取标签
     * 3. 建立文章-标签关联
     */
    private void processArticleTags(Article article) {
        // 确保文章已保存（有ID）
        if (article.getId() == null) {
            // 对于新文章，先保存再处理标签
            return;
        }

        if (!StringUtils.hasText(article.getTags())) {
            // 清空文章的标签关联
            articleTagMapper.deleteByArticleId(article.getId());
            return;
        }

        // 解析标签字符串
        String[] tagNames = article.getTags().split(",");
        List<Integer> tagIds = new java.util.ArrayList<>();

        for (String tagName : tagNames) {
            String cleanTagName = tagName.trim();
            if (cleanTagName.isEmpty()) {
                continue;
            }

            // 查找标签是否存在
            Tag tag = tagMapper.findByName(cleanTagName);

            if (tag == null) {
                // 创建新标签
                tag = new Tag();
                tag.setName(cleanTagName);
                tag.setSlug(generateSlug(cleanTagName));
                tag.setColor("#409eff"); // 默认颜色
                tagMapper.insert(tag);
                System.out.println("✅ 创建新标签: " + cleanTagName + " (ID: " + tag.getId() + ")");
            }

            if (tag.getId() != null) {
                tagIds.add(tag.getId());
            }
        }

        // 处理文章标签关联
        if (!tagIds.isEmpty()) {
            // 先删除旧的关联
            articleTagMapper.deleteByArticleId(article.getId());

            // 创建新的关联
            for (Integer tagId : tagIds) {
                try {
                    articleTagMapper.insert(article.getId(), tagId);
                } catch (Exception e) {
                    // 忽略重复关联
                    System.out.println("⚠️ 标签关联已存在: articleId=" + article.getId() + ", tagId=" + tagId);
                }
            }

            // 更新标签的文章数量
            for (Integer tagId : tagIds) {
                tagMapper.updateArticleCount(tagId);
            }
        }
    }

    /**
     * 生成标签slug（URL友好格式）
     */
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * 创建草稿（status=0）
     */
    public boolean createDraft(Article article) {
        article.setStatus(0); // 草稿状态
        return createArticle(article);
    }

    /**
     * 发布草稿（status=0 → 1）
     */
    public boolean publishDraft(Integer articleId) {
        Article article = getArticleById(articleId);
        if (article != null && article.getStatus() == 0) {
            article.setStatus(1);
            return updateArticle(article);
        }
        return false;
    }

    @Override
    public List<Article> getUserDrafts(Integer userId, int page, int size) {
        int offset = (page - 1) * size;
        return articleMapper.findByUserIdAndStatus(userId, 0, offset, size);
    }

    @Override
    public List<Article> getUserPublishedArticles(Integer userId, int page, int size) {
        int offset = (page - 1) * size;
        return articleMapper.findByUserIdAndStatus(userId, 1, offset, size);
    }

    @Override
    public Article getArticleByIdWithoutStatus(Integer id) {
        return articleMapper.findByIdWithoutStatus(id);
    }

    @Override
    public Article getArticleByIdAndUserId(Integer id, Integer userId) {
        return articleMapper.findByIdAndUserId(id, userId);
    }
}