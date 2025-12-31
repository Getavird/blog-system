package com.blog.service.impl;

import com.blog.dao.ArticleLikeMapper;
import com.blog.dao.ArticleMapper;
import com.blog.dao.ArticleTagMapper;
import com.blog.dao.TagMapper;
import com.blog.dao.UploadFileMapper;
import com.blog.dao.UserLikeMapper;

import com.blog.entity.Article;
import com.blog.entity.Tag;
import com.blog.entity.UploadFile;
import com.blog.service.ArticleService;
import com.blog.service.FileService;
import com.blog.utils.EditorImageUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    @Autowired
    private EditorImageUtils editorImageUtils;

    @Autowired
    private FileService fileService;

    @Autowired
    private UploadFileMapper uploadFileMapper;

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

    @Override
    public List<Article> getArticles(int page, int size) {
        int offset = (page - 1) * size;
        return articleMapper.findList(offset, size);
    }

    @Override
    public Article getArticleById(Integer id) {
        // 先获取文章（现在可以获取所有状态的文章）
        Article article = articleMapper.findById(id);

        // 如果是已发布的文章，增加阅读量
        if (article != null && article.getStatus() == 1) { // 只对已发布文章增加阅读量
            articleMapper.incrementViewCount(id);
            // 重新获取更新后的数据
            article = articleMapper.findById(id);
        }
        // 如果是草稿（status=0）或已删除（status=2），不增加阅读量

        // ✅ 注意：这里不设置 isLiked 字段，因为需要当前用户ID
        // isLiked 字段将在 Controller 层设置

        return article;
    }

    @Override
    public boolean createArticle(Article article) {
        article = processArticleImages(article);
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
        // 处理文章内容中的图片
        article = processArticleImages(article);
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

    private Article processArticleImages(Article article) {
        if (article.getContent() == null) {
            return article;
        }

        String content = article.getContent();

        // 简单的base64图片检测和处理
        Pattern pattern = Pattern.compile("data:image/([^;]+);base64,([^\"]+)");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String base64Image = matcher.group(0);
            String mimeType = matcher.group(1);
            String base64Data = matcher.group(2);

            try {
                // 生成文件名
                String ext = mimeType.equals("jpeg") ? "jpg" : mimeType;
                String fileName = "img_" + System.currentTimeMillis() + "_" +
                        new Random().nextInt(1000) + "." + ext;

                // 保存到本地
                byte[] bytes = Base64.getDecoder().decode(base64Data);
                Path path = Paths.get("uploads", "article_images", fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, bytes);

                // 替换URL
                String imageUrl = "/uploads/article_images/" + fileName;
                content = content.replace(base64Image, imageUrl);

            } catch (Exception e) {
                // 失败就跳过
                System.out.println("跳过base64图片处理: " + e.getMessage());
            }
        }

        article.setContent(content);
        return article;
    }

    // 在 FileServiceImpl.java 中，确保使用正确的上传路径
    @Value("${blog.upload.path:./uploads/}")
    private String uploadBasePath; // 应该指向 ./uploads/

    public UploadFile uploadFile(MultipartFile file, Integer userId, String usageType) throws IOException {
        // 确保上传目录存在
        String uploadDir = uploadBasePath;
        if (!uploadDir.endsWith("/")) {
            uploadDir += "/";
        }

        // 根据使用类型创建子目录
        String subDir = "";
        switch (usageType) {
            case "avatar":
                subDir = "avatars/";
                break;
            case "article":
            case "article_content":
                subDir = "article_images/";
                break;
            case "cover":
                subDir = "covers/";
                break;
            default:
                subDir = "general/";
        }

        String fullUploadDir = uploadDir + subDir;

        // 创建目录
        File dir = new File(fullUploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String fileExt = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String saveName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExt;

        // 保存文件
        Path filePath = Paths.get(fullUploadDir, saveName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 保存到数据库...
        UploadFile uploadFile = new UploadFile();
        uploadFile.setOriginalName(originalFilename);
        uploadFile.setSaveName(saveName);
        uploadFile.setFilePath(subDir + saveName); // 相对路径
        // ... 其他字段设置

        return uploadFile;
    }

    @Override
    public List<Article> getArticlesByUserIdAndStatus(Integer userId, Integer status, Integer page, Integer size) {
        try {
            if (page == null || page < 1)
                page = 1;
            if (size == null || size < 1)
                size = 10;

            int offset = (page - 1) * size;

            // 调用已有的Mapper方法
            return articleMapper.findByUserIdAndStatus(userId, status, offset, size);

        } catch (Exception e) {
            System.err.println("❌ 查询用户文章列表异常: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Long countArticlesByUserIdAndStatus(Integer userId, Integer status) {
        try {
            Long count = articleMapper.countByUserIdAndStatus(userId, status);
            return count != null ? count : 0L;
        } catch (Exception e) {
            System.err.println("❌ 统计用户文章数量异常: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }

    @Override
    public List<Article> getArticlesByUserId(Integer userId, Integer page, Integer size) {
        try {
            if (page == null || page < 1)
                page = 1;
            if (size == null || size < 1)
                size = 10;

            int offset = (page - 1) * size;

            // 使用新添加的findByUserId方法
            return articleMapper.findByUserId(userId, offset, size);

        } catch (Exception e) {
            System.err.println("❌ 查询用户所有文章异常: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Long countArticlesByUserId(Integer userId) {
        try {
            Long count = articleMapper.countByUserId(userId);
            return count != null ? count : 0L;
        } catch (Exception e) {
            System.err.println("❌ 统计用户所有文章异常: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 点赞文章
     */
    @Override
    public boolean likeArticle(Integer articleId, Integer userId) {
        try {
            System.out.println("👍 用户 " + userId + " 点赞文章 " + articleId);

            // 1. 检查是否已经点赞
            if (isArticleLikedByUser(articleId, userId)) {
                System.out.println("⚠️ 用户已点赞过此文章");
                return false; // 不再抛出异常，而是返回 false
            }

            // 2. 添加点赞记录
            int result = articleLikeMapper.insert(userId, articleId);

            // 3. 更新文章点赞数
            if (result > 0) {
                Article article = articleMapper.findById(articleId);
                if (article != null) {
                    int currentLikes = article.getLikeCount() != null ? article.getLikeCount() : 0;
                    article.setLikeCount(currentLikes + 1);
                    articleMapper.update(article);
                    System.out.println("✅ 点赞成功，文章 " + articleId + " 点赞数: " + (currentLikes + 1));
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            System.err.println("❌ 点赞文章异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 取消点赞文章
     */
    @Override
    public boolean unlikeArticle(Integer articleId, Integer userId) {
        try {
            System.out.println("👎 用户 " + userId + " 取消点赞文章 " + articleId);

            // 1. 检查是否已经点赞
            if (!isArticleLikedByUser(articleId, userId)) {
                System.out.println("⚠️ 用户未点赞过此文章");
                return false; // 不再抛出异常，而是返回 false
            }

            // 2. 删除点赞记录
            int result = articleLikeMapper.delete(userId, articleId);

            // 3. 更新文章点赞数
            if (result > 0) {
                Article article = articleMapper.findById(articleId);
                if (article != null) {
                    int currentLikes = article.getLikeCount() != null ? article.getLikeCount() : 0;
                    article.setLikeCount(Math.max(0, currentLikes - 1));
                    articleMapper.update(article);
                    System.out.println("✅ 取消点赞成功，文章 " + articleId + " 点赞数: " + Math.max(0, currentLikes - 1));
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            System.err.println("❌ 取消点赞异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否已点赞文章
     */
    @Override
    public boolean isArticleLikedByUser(Integer articleId, Integer userId) {
        try {
            int count = articleLikeMapper.exists(userId, articleId);
            return count > 0;
        } catch (Exception e) {
            System.err.println("❌ 检查文章点赞状态异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取文章点赞数
     */
    @Override
    public int getArticleLikeCount(Integer articleId) {
        try {
            return articleLikeMapper.countByArticleId(articleId);
        } catch (Exception e) {
            System.err.println("❌ 获取文章点赞数异常: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 切换点赞状态（点赞/取消点赞）
     */
    @Override
    public Map<String, Object> toggleLike(Integer articleId, Integer userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean isLiked = isArticleLikedByUser(articleId, userId);

            if (isLiked) {
                // 已点赞，执行取消点赞
                boolean success = unlikeArticle(articleId, userId);
                result.put("success", success);
                result.put("action", "unlike");
                result.put("message", success ? "取消点赞成功" : "取消点赞失败");

                if (success) {
                    result.put("isLiked", false);
                }
            } else {
                // 未点赞，执行点赞
                boolean success = likeArticle(articleId, userId);
                result.put("success", success);
                result.put("action", "like");
                result.put("message", success ? "点赞成功" : "点赞失败");

                if (success) {
                    result.put("isLiked", true);
                }
            }

            // 获取更新后的点赞数
            Article article = articleMapper.findById(articleId);
            if (article != null) {
                result.put("likeCount", article.getLikeCount() != null ? article.getLikeCount() : 0);
            }

            // 确保 isLiked 字段存在
            if (!result.containsKey("isLiked")) {
                result.put("isLiked", !isLiked);
            }

            return result;

        } catch (RuntimeException e) {
            // 这里不应该抛出异常，而是返回错误信息
            System.err.println("❌ 切换点赞状态异常: " + e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
            result.put("isLiked", isArticleLikedByUser(articleId, userId)); // 返回当前状态
            return result;
        } catch (Exception e) {
            System.err.println("❌ 切换点赞状态未知异常: " + e.getMessage());
            result.put("success", false);
            result.put("message", "操作失败");
            result.put("isLiked", isArticleLikedByUser(articleId, userId)); // 返回当前状态
            return result;
        }
    }

    /**
     * 获取用户点赞的文章列表
     */
    @Override
    public List<Article> getLikedArticles(Integer userId, Integer page, Integer size) {
        try {
            System.out.println("📋 获取用户点赞文章列表: userId=" + userId);

            // 获取用户点赞的文章ID列表
            List<Integer> likedArticleIds = articleLikeMapper.findLikedArticleIds(userId);

            if (likedArticleIds == null || likedArticleIds.isEmpty()) {
                return new ArrayList<>();
            }

            // 分页参数
            if (page == null || page < 1)
                page = 1;
            if (size == null || size < 1)
                size = 10;

            // 这里简化处理：返回前N个点赞的文章
            // 实际项目中应该实现分页查询
            List<Article> likedArticles = new ArrayList<>();
            int start = Math.min((page - 1) * size, likedArticleIds.size());
            int end = Math.min(start + size, likedArticleIds.size());

            for (int i = start; i < end; i++) {
                Integer articleId = likedArticleIds.get(i);
                Article article = articleMapper.findById(articleId);
                if (article != null && article.getStatus() == 1) {
                    likedArticles.add(article);
                }
            }

            return likedArticles;

        } catch (Exception e) {
            System.err.println("❌ 获取点赞文章列表异常: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public int getArticleCountByUserId(Integer userId) {
        try {
            return articleMapper.countArticlesByUserId(userId);
        } catch (Exception e) {
            System.err.println("获取用户文章数量失败: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int getDraftCountByUserId(Integer userId) {
        try {
            return articleMapper.countDraftsByUserId(userId);
        } catch (Exception e) {
            System.err.println("获取用户草稿数量失败: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int getPublishedArticleCountByUserId(Integer userId) {
        try {
            return articleMapper.countPublishedArticlesByUserId(userId);
        } catch (Exception e) {
            System.err.println("获取用户已发布文章数量失败: " + e.getMessage());
            return 0;
        }
    }
}