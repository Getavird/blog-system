package com.blog.utils;

import com.blog.dao.*;
import com.blog.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 测试数据生成器（开发环境使用）
 * 当数据库中没有数据时自动生成测试数据
 */
@Component
public class TestDataGenerator implements CommandLineRunner {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private TagMapper tagMapper;
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private CommentMapper commentMapper;
    
    private final Random random = new Random();
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 检查测试数据 ===");
        
        // 检查用户数量
        int userCount = userMapper.countUsers();
        System.out.println("📊 当前用户数量: " + userCount);
        
        if (userCount == 0) {
            System.out.println("🔄 开始生成测试数据...");
            generateTestData();
            System.out.println("✅ 测试数据生成完成");
        } else {
            System.out.println("✅ 数据库已有数据，跳过生成测试数据");
        }
    }
    
    private void generateTestData() {
        try {
            // 1. 生成测试用户
            System.out.println("👤 生成测试用户...");
            User admin = createUser("admin", "admin123", "admin@test.com", 1);
            User user1 = createUser("user1", "user123", "user1@test.com", 0);
            User user2 = createUser("user2", "user123", "user2@test.com", 0);
            
            // 2. 生成测试分类
            System.out.println("📂 生成测试分类...");
            Category techCategory = createCategory("技术分享", "分享编程技术经验");
            Category lifeCategory = createCategory("生活随笔", "记录生活点滴");
            Category studyCategory = createCategory("学习笔记", "学习心得记录");
            
            // 3. 生成测试标签
            System.out.println("🏷️  生成测试标签...");
            Tag javaTag = createTag("Java", "Java编程语言");
            Tag springTag = createTag("Spring Boot", "Spring Boot框架");
            Tag vueTag = createTag("Vue", "Vue前端框架");
            Tag mysqlTag = createTag("MySQL", "MySQL数据库");
            Tag frontendTag = createTag("前端", "前端开发技术");
            Tag backendTag = createTag("后端", "后端开发技术");
            
            // 4. 生成测试文章
            System.out.println("📝 生成测试文章...");
            Article article1 = createArticle("Spring Boot入门指南", 
                "Spring Boot 是构建 Java 应用的最佳实践...", 
                admin.getId(), techCategory.getId());
            
            Article article2 = createArticle("Vue 3新特性解析", 
                "Vue 3带来了许多令人兴奋的新特性...", 
                user1.getId(), techCategory.getId());
            
            Article article3 = createArticle("我的编程学习之路", 
                "记录我学习编程的心路历程...", 
                user2.getId(), studyCategory.getId());
            
            // 5. 生成测试评论
            System.out.println("💬 生成测试评论...");
            createComment("写得真好，学习了！", article1.getId(), user1.getId());
            createComment("感谢分享，解决了我的问题", article1.getId(), user2.getId());
            createComment("期待更多技术分享", article2.getId(), admin.getId());
            
            System.out.println("🎉 测试数据生成完成！");
            
        } catch (Exception e) {
            System.err.println("❌ 生成测试数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private User createUser(String username, String password, String email, Integer role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encrypt(password));
        user.setEmail(email);
        user.setRole(role);
        user.setAvatar("avatar" + (random.nextInt(5) + 1) + ".jpg");
        user.setBio("这是" + username + "的个人简介");
        
        userMapper.insert(user);
        System.out.println("   ✅ 创建用户: " + username + " (ID: " + user.getId() + ")");
        return user;
    }
    
    private Category createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setIcon("icon-" + name.toLowerCase().replace(" ", "-"));
        category.setOrderNum(random.nextInt(10));
        category.setColor(getRandomColor());
        
        categoryMapper.insert(category);
        System.out.println("   ✅ 创建分类: " + name + " (ID: " + category.getId() + ")");
        return category;
    }
    
    private Tag createTag(String name, String description) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setDescription(description);
        tag.setSlug(name.toLowerCase().replace(" ", "-"));
        tag.setColor(getRandomColor());
        
        tagMapper.insert(tag);
        System.out.println("   ✅ 创建标签: " + name + " (ID: " + tag.getId() + ")");
        return tag;
    }
    
    private Article createArticle(String title, String content, Integer userId, Integer categoryId) {
        Article article = new Article();
        article.setTitle(title);
        article.setContent("<h1>" + title + "</h1><p>" + content + "</p>");
        article.setSummary(content.substring(0, Math.min(100, content.length())) + "...");
        article.setUserId(userId);
        article.setCategoryId(categoryId);
        article.setViewCount(random.nextInt(1000));
        article.setLikeCount(random.nextInt(100));
        article.setCommentCount(random.nextInt(50));
        article.setCoverImage("cover" + (random.nextInt(5) + 1) + ".jpg");
        article.setStatus(1);
        article.setAllowComment(1);
        article.setIsTop(random.nextBoolean() ? 1 : 0);
        article.setTags("Java,Spring Boot");
        
        articleMapper.insert(article);
        System.out.println("   ✅ 创建文章: " + title + " (ID: " + article.getId() + ")");
        return article;
    }
    
    private void createComment(String content, Integer articleId, Integer userId) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setParentId(0);
        comment.setLikeCount(random.nextInt(10));
        comment.setStatus(1);
        
        commentMapper.insert(comment);
        System.out.println("   ✅ 创建评论: " + content.substring(0, Math.min(20, content.length())) + "...");
    }
    
    private String getRandomColor() {
        String[] colors = {
            "#409eff", "#67c23a", "#e6a23c", "#f56c6c", 
            "#909399", "#ff85c0", "#5cdbd3", "#b37feb"
        };
        return colors[random.nextInt(colors.length)];
    }
}