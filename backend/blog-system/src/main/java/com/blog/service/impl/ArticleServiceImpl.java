package com.blog.service.impl;

import com.blog.dao.ArticleLikeMapper;
import com.blog.dao.ArticleMapper;
import com.blog.dao.CommentMapper;
import com.blog.dao.TagMapper;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import com.blog.service.FollowService;
import com.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
<<<<<<< Updated upstream
=======
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
>>>>>>> Stashed changes

@Service
@Transactional
public class ArticleServiceImpl implements ArticleService {
    
    @Autowired
    private ArticleMapper articleMapper;
<<<<<<< Updated upstream
    
=======

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentMapper commentMapper;

>>>>>>> Stashed changes
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
        int result = articleMapper.insert(article);
        return result > 0;
    }
    
    @Override
    public boolean updateArticle(Article article) {
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
<<<<<<< Updated upstream
=======

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

    @Override
    public List<Article> getMyArticles(Integer userId, String title, Integer status,
            Integer categoryId, int page, int size) {
        int offset = (page - 1) * size;
        return articleMapper.findMyArticles(userId, title, status, categoryId, offset, size);
    }

    @Override
    public int countMyArticles(Integer userId, String title, Integer status, Integer categoryId) {
        return articleMapper.countMyArticles(userId, title, status, categoryId);
    }

    @Override
    public boolean batchDeleteMyArticles(List<Integer> ids, Integer userId) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        int result = articleMapper.batchDeleteArticles(ids, userId);
        return result > 0;
    }

    @Override
    public boolean batchUpdateArticleStatus(List<Integer> ids, Integer status, Integer userId) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        int result = articleMapper.batchUpdateArticleStatus(ids, status, userId);
        return result > 0;
    }

    @Override
    public Map<String, Object> getUserArticleStats(Integer userId) {
        return articleMapper.getUserArticleStats(userId);
    }

    @Override
    public boolean toggleArticleLike(Integer articleId, Integer userId) {
        try {
            // 检查文章是否存在
            Article article = articleMapper.findById(articleId);
            if (article == null) {
                throw new RuntimeException("文章不存在");
            }

            // 检查是否已点赞
            boolean alreadyLiked = articleLikeMapper.exists(userId, articleId) > 0;

            if (alreadyLiked) {
                // 如果已经点赞，则取消点赞
                int deleteResult = articleLikeMapper.delete(userId, articleId);
                if (deleteResult > 0) {
                    // 更新文章的点赞数
                    article.setLikeCount(Math.max(0, article.getLikeCount() - 1));
                    articleMapper.update(article);
                    System.out.println("✅ 取消点赞成功 - 文章ID: " + articleId + ", 用户ID: " + userId);
                    return false; // 返回false表示现在是未点赞状态
                }
            } else {
                // 如果未点赞，则点赞
                int insertResult = articleLikeMapper.insert(userId, articleId);
                if (insertResult > 0) {
                    // 更新文章的点赞数
                    article.setLikeCount(article.getLikeCount() + 1);
                    articleMapper.update(article);
                    System.out.println("✅ 点赞成功 - 文章ID: " + articleId + ", 用户ID: " + userId);
                    return true; // 返回true表示现在是已点赞状态
                }
            }

            throw new RuntimeException("操作失败");
        } catch (RuntimeException e) {
            System.err.println("切换点赞状态异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("切换点赞状态系统异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("操作失败，请稍后重试");
        }
    }

    @Override
    public boolean isArticleLiked(Integer articleId, Integer userId) {
        try {
            return articleLikeMapper.exists(userId, articleId) > 0;
        } catch (Exception e) {
            System.err.println("检查点赞状态异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getArticleLikeStats(Integer articleId) {
        try {
            Object stats = articleLikeMapper.getArticleLikeStats(articleId);
            if (stats instanceof Map) {
                return (Map<String, Object>) stats;
            }
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("total_likes", 0);
            defaultStats.put("unique_users", 0);
            return defaultStats;
        } catch (Exception e) {
            System.err.println("获取点赞统计异常: " + e.getMessage());
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("total_likes", 0);
            defaultStats.put("unique_users", 0);
            return defaultStats;
        }
    }

    @Override
    public Article getArticleDetail(Integer id, Integer currentUserId) {
        try {
            // 获取文章详情
            Article article = articleMapper.findById(id);
            if (article == null) {
                return null;
            }

            // 增加阅读量
            articleMapper.incrementViewCount(id);
            article = articleMapper.findById(id); // 重新获取更新后的数据

            // 检查当前用户是否已点赞文章
            if (currentUserId != null) {
                boolean isLiked = isArticleLiked(id, currentUserId);
                article.setIsLiked(isLiked);
            } else {
                article.setIsLiked(false);
            }

            return article;
        } catch (Exception e) {
            System.err.println("获取文章详情异常: " + e.getMessage());
            throw e;
        }
    }

    @Service
    public class ArticleServiceImpl implements ArticleService {

        @Autowired
        private ArticleMapper articleMapper;

        @Autowired
        private TagMapper tagMapper;

        @Override
        public boolean createArticleWithTags(Article article, List<String> tagNames) {
            // 1. 创建文章
            int result = articleMapper.insert(article);
            if (result <= 0)
                return false;

            // 2. 处理标签
            if (tagNames != null && !tagNames.isEmpty()) {
                saveArticleTags(article.getId(), tagNames);
            }

            return true;
        }

        @Override
        public boolean updateArticleWithTags(Article article, List<String> tagNames) {
            // 1. 更新文章
            int result = articleMapper.update(article);
            if (result <= 0)
                return false;

            // 2. 删除旧的标签关联
            articleMapper.deleteArticleTags(article.getId());

            // 3. 保存新的标签关联
            if (tagNames != null && !tagNames.isEmpty()) {
                saveArticleTags(article.getId(), tagNames);
            }

            return true;
        }

        /**
         * 保存文章标签关联
         */
        private void saveArticleTags(Integer articleId, List<String> tagNames) {
            // 1. 去重
            Set<String> uniqueTags = new HashSet<>(tagNames);

            // 2. 查询已存在的标签
            List<Tag> existingTags = tagMapper.findByNames(new ArrayList<>(uniqueTags));
            Set<String> existingTagNames = existingTags.stream()
                    .map(Tag::getName)
                    .collect(Collectors.toSet());

            // 3. 创建新标签
            List<Tag> newTags = uniqueTags.stream()
                    .filter(tag -> !existingTagNames.contains(tag))
                    .map(tagName -> {
                        Tag tag = new Tag();
                        tag.setName(tagName);
                        tag.setSlug(tagName.toLowerCase().replace(" ", "-"));
                        return tag;
                    })
                    .collect(Collectors.toList());

            if (!newTags.isEmpty()) {
                tagMapper.batchInsertTags(newTags);
                // 重新查询所有标签（包括新创建的）
                existingTags = tagMapper.findByNames(new ArrayList<>(uniqueTags));
            }

            // 4. 创建关联
            List<Integer> tagIds = existingTags.stream()
                    .map(Tag::getId)
                    .collect(Collectors.toList());

            if (!tagIds.isEmpty()) {
                articleMapper.insertArticleTags(articleId, tagIds);
            }
        }

        @Override
        public Article getArticleWithTags(Integer id, Integer currentUserId) {
            // 1. 查询文章基本信息
            Article article = articleMapper.findByIdWithTags(id);
            if (article == null)
                return null;

            // 2. 查询标签列表
            List<Tag> tagList = tagMapper.findByArticleId(id);
            article.setTagList(tagList);

            // 3. 设置标签字符串（用于编辑时回显）
            String tags = tagList.stream()
                    .map(Tag::getName)
                    .collect(Collectors.joining(","));
            article.setTags(tags);

            // 4. 其他逻辑（点赞状态、关注状态等）
            // ... 你的现有逻辑

            return article;
        }
    }
>>>>>>> Stashed changes
}