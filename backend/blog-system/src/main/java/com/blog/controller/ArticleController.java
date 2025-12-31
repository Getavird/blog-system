package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.service.ArticleService;
import com.blog.utils.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 获取文章列表
     */
    @GetMapping
    public Result<List<Article>> getArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        List<Article> articles = articleService.getArticles(page, size);
        return Result.success(articles);
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/{id}")
    public Result<Article> getArticle(@PathVariable Integer id, HttpServletRequest request) {
        try {
            System.out.println("🔍 获取文章详情，ID: " + id);

            // 获取当前用户（如果有）
            User currentUser = SessionUtil.getCurrentUser(request);
            Integer currentUserId = currentUser != null ? currentUser.getId() : null;

            // 获取文章（包括草稿）
            Article article = articleService.getArticleById(id);

            if (article == null) {
                System.out.println("❌ 文章不存在，ID: " + id);
                return Result.notFound("文章不存在");
            }

            // ✅ 关键修改：获取当前用户的点赞状态并设置到文章对象中
            if (currentUserId != null) {
                boolean isLiked = articleService.isArticleLikedByUser(id, currentUserId);
                article.setIsLiked(isLiked);
                System.out.println("❤️ 当前用户点赞状态: " + isLiked);
            } else {
                article.setIsLiked(false); // 未登录用户默认为未点赞
                System.out.println("👤 未登录用户，点赞状态: false");
            }

            System.out.println("✅ 找到文章: " + article.getTitle());
            System.out.println("📊 文章状态: " + article.getStatus());
            System.out.println("👤 文章作者ID: " + article.getUserId());

            // 检查文章状态和处理权限
            if (article.getStatus() == 0) { // 草稿
                System.out.println("📝 这是草稿文章，检查权限...");

                // 如果没有登录，返回未授权
                if (currentUser == null) {
                    System.out.println("❌ 未登录用户尝试访问草稿");
                    return Result.unauthorized("请先登录");
                }

                // 检查是否是作者或管理员
                boolean isAuthor = article.getUserId().equals(currentUser.getId());
                boolean isAdmin = currentUser.getRole() == 1;

                System.out.println("🔐 权限检查 - 是作者: " + isAuthor + ", 是管理员: " + isAdmin);

                if (!isAuthor && !isAdmin) {
                    System.out.println("❌ 权限不足，当前用户ID: " + currentUser.getId() +
                            ", 文章作者ID: " + article.getUserId());
                    return Result.forbidden("没有权限访问此文章");
                }

                System.out.println("✅ 权限检查通过，返回草稿文章");

            } else if (article.getStatus() == 1) { // 已发布
                System.out.println("📰 这是已发布文章");

                // 智能判断是否是编辑模式，决定是否增加阅读量
                boolean isEditMode = isEditRequest(request);

                if (isEditMode) {
                    System.out.println("✏️ 编辑模式，不增加阅读量");
                } else {
                    // 只有非编辑模式才增加阅读量
                    System.out.println("📈 非编辑模式，增加阅读量");

                    // 检查是否是作者查看自己的文章（作者查看自己文章时不增加阅读量）
                    boolean isAuthorViewingOwnArticle = false;
                    if (currentUser != null) {
                        isAuthorViewingOwnArticle = article.getUserId().equals(currentUser.getId());
                    }

                    if (isAuthorViewingOwnArticle) {
                        System.out.println("👤 作者查看自己的文章，不增加阅读量");
                    } else {
                        // 增加阅读量
                        articleService.incrementViewCount(id);
                        System.out.println("📊 阅读量已增加，当前阅读量: " + (article.getViewCount() + 1));
                        // 更新本地对象
                        article.setViewCount(article.getViewCount() + 1);
                    }
                }

            } else if (article.getStatus() == 2) { // 已删除
                System.out.println("🗑️ 文章已删除，ID: " + id);
                return Result.notFound("文章不存在或已被删除");
            }

            System.out.println("🎉 返回文章数据");
            return Result.success(article);

        } catch (Exception e) {
            System.err.println("💥 获取文章详情异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取文章失败: " + e.getMessage());
        }
    }

    /**
     * 判断请求是否是编辑模式
     */
    private boolean isEditRequest(HttpServletRequest request) {
        // 方法1：检查URL参数
        String editParam = request.getParameter("edit");
        if (editParam != null && (editParam.equals("true") || editParam.equals("1"))) {
            return true;
        }

        // 方法2：检查Referer是否包含编辑页面
        String referer = request.getHeader("Referer");
        if (referer != null) {
            // 如果Referer包含编辑页面的路径
            if (referer.contains("/article/edit/") ||
                    referer.contains("/edit?") ||
                    referer.contains("edit=true")) {
                return true;
            }
        }

        // 方法3：检查请求头中是否有特定标记（前端可以设置）
        String editHeader = request.getHeader("X-Edit-Mode");
        if (editHeader != null && (editHeader.equals("true") || editHeader.equals("1"))) {
            return true;
        }

        // 方法4：检查请求的URI是否来自编辑页面
        String requestUri = request.getRequestURI();
        if (requestUri.contains("/edit/")) {
            return true;
        }

        return false;
    }

    /**
     * 创建文章（需要登录）
     */
    @PostMapping
    public Result<Article> createArticle(@RequestBody Article article, HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 设置作者ID
        article.setUserId(currentUser.getId());

        boolean success = articleService.createArticle(article);
        return success ? Result.created(article) : Result.error("创建失败");
    }

    /**
     * 更新文章（需要登录，且是作者或管理员）
     */
    @PutMapping("/{id}")
    public Result<Article> updateArticle(@PathVariable Integer id,
            @RequestBody Article article,
            HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 检查权限：只有作者或管理员可以修改
        Article existingArticle = articleService.getArticleById(id);
        if (existingArticle == null) {
            return Result.notFound("文章不存在");
        }

        // 如果不是作者且不是管理员
        if (!existingArticle.getUserId().equals(currentUser.getId()) && currentUser.getRole() != 1) {
            return Result.forbidden("没有权限修改此文章");
        }

        article.setId(id);
        boolean success = articleService.updateArticle(article);
        return success ? Result.success(article) : Result.error("更新失败");
    }

    /**
     * 删除文章（需要登录，且是作者或管理员）
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteArticle(@PathVariable Integer id, HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 检查权限：只有作者或管理员可以删除
        Article existingArticle = articleService.getArticleById(id);
        if (existingArticle == null) {
            return Result.notFound("文章不存在");
        }

        // 如果不是作者且不是管理员
        if (!existingArticle.getUserId().equals(currentUser.getId()) && currentUser.getRole() != 1) {
            return Result.forbidden("没有权限删除此文章");
        }

        boolean success = articleService.deleteArticle(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 获取热门文章（按浏览量排序）
     * GET /api/articles/hot?limit=10
     */
    @GetMapping("/hot")
    public Result<List<Article>> getHotArticles(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<Article> articles = articleService.getHotArticles(limit);
        return Result.success(articles);
    }

    /**
     * 获取最新文章（按创建时间排序）
     * GET /api/articles/latest?limit=10
     * 同时支持 /api/articles/newest 路径以兼容前端
     */
    @GetMapping({ "/latest", "/newest" })
    public Result<List<Article>> getLatestArticles(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<Article> articles = articleService.getLatestArticles(limit);
        return Result.success(articles);
    }

    /**
     * 获取分类统计（每个分类有多少文章）
     * GET /api/articles/category-stats
     */
    @GetMapping("/category-stats")
    public Result<List<Map<String, Object>>> getCategoryStats() {
        List<Map<String, Object>> stats = articleService.getCategoryStats();
        return Result.success(stats);
    }

    /**
     * 获取热门标签统计
     * GET /api/articles/hot-tags?limit=20
     */
    @GetMapping("/hot-tags")
    public Result<List<Map<String, Object>>> getHotTags(
            @RequestParam(defaultValue = "20") Integer limit) {
        List<Map<String, Object>> hotTags = articleService.getHotTags(limit);
        return Result.success(hotTags);
    }

    /**
     * 根据分类获取文章
     * GET /api/articles/category/{categoryId}?page=1&size=10
     */
    @GetMapping("/category/{categoryId}")
    public Result<List<Article>> getArticlesByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        List<Article> articles = articleService.getArticlesByCategory(categoryId, page, size);
        return Result.success(articles);
    }

    /**
     * 根据标签获取文章
     * GET /api/articles/tag/{tagName}?page=1&size=10
     */
    @GetMapping("/tag/{tagName}")
    public Result<List<Article>> getArticlesByTag(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        List<Article> articles = articleService.getArticlesByTag(tagName, page, size);
        return Result.success(articles);
    }

    /**
     * 创建草稿（需要登录）
     */
    @PostMapping("/draft")
    public Result<Article> createDraft(@RequestBody Article article, HttpServletRequest request) {
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        article.setUserId(currentUser.getId());
        article.setAuthorName(currentUser.getUsername());
        article.setAuthorAvatar(currentUser.getAvatar());
        article.setStatus(0);

        boolean success = articleService.createDraft(article);
        return success ? Result.created(article) : Result.error("创建草稿失败");
    }

    /**
     * 获取用户的草稿列表
     */
    @GetMapping("/my-drafts")
    public Result<Map<String, Object>> getMyDrafts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 使用专门的草稿查询方法
        List<Article> drafts = articleService.getUserDrafts(currentUser.getId(), page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("drafts", drafts);
        result.put("page", page);
        result.put("size", size);
        result.put("total", drafts.size());

        return Result.success(result);
    }

    /**
     * 发布草稿
     */
    @PostMapping("/{id}/publish")
    public Result<String> publishDraft(@PathVariable Integer id, HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 使用可以查询草稿的方法
        Article article = articleService.getArticleByIdAndUserId(id, currentUser.getId());
        if (article == null) {
            return Result.notFound("草稿不存在或没有权限");
        }

        // 检查是否是草稿
        if (article.getStatus() != 0) {
            return Result.error("只有草稿可以发布");
        }

        // 更新文章状态为发布
        article.setStatus(1);
        boolean success = articleService.updateArticle(article);

        return success ? Result.success("发布成功") : Result.error("发布失败");
    }

    /**
     * 获取用户的发布文章（不包括草稿）
     */
    @GetMapping("/my-published")
    public Result<Map<String, Object>> getMyPublishedArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 获取所有文章然后过滤出当前用户的发布文章
        List<Article> allArticles = articleService.getArticles(page, size * 5);
        List<Article> published = allArticles.stream()
                .filter(article -> article.getUserId().equals(currentUser.getId())
                        && article.getStatus() == 1)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("articles", published);
        result.put("page", page);
        result.put("size", size);
        result.put("total", published.size());

        return Result.success(result);
    }

    /**
     * 更新草稿（只允许作者更新自己的草稿）
     */
    @PutMapping("/draft/{id}")
    public Result<Article> updateDraft(@PathVariable Integer id,
            @RequestBody Article article,
            HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 使用可以查询草稿的方法
        Article existingArticle = articleService.getArticleByIdAndUserId(id, currentUser.getId());
        if (existingArticle == null) {
            return Result.notFound("草稿不存在或没有权限");
        }

        // 确保是草稿
        if (existingArticle.getStatus() != 0) {
            return Result.error("只有草稿可以更新");
        }

        // 更新文章
        article.setId(id);
        article.setStatus(0); // 保持草稿状态
        article.setUserId(currentUser.getId()); // 保持作者不变

        boolean success = articleService.updateArticle(article);
        if (success) {
            // 重新获取更新后的文章
            Article updatedArticle = articleService.getArticleByIdAndUserId(id, currentUser.getId());
            return Result.success(updatedArticle);
        }
        return Result.error("更新草稿失败");
    }

    /**
     * 删除草稿（软删除）
     */
    @DeleteMapping("/draft/{id}")
    public Result<String> deleteDraft(@PathVariable Integer id, HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        // 获取文章
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return Result.notFound("草稿不存在");
        }

        // 检查权限：只有作者可以删除
        if (!article.getUserId().equals(currentUser.getId())) {
            return Result.forbidden("没有权限删除此草稿");
        }

        // 检查是否是草稿
        if (article.getStatus() != 0) {
            return Result.error("只有草稿可以删除");
        }

        // 软删除（设置状态为2）
        article.setStatus(2);
        boolean success = articleService.updateArticle(article);

        return success ? Result.success("草稿删除成功") : Result.error("删除失败");
    }

    /**
     * 点赞文章
     * POST /api/articles/{id}/like
     */
    @PostMapping("/{id}/like")
    public Result<String> likeArticle(@PathVariable Integer id, HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            boolean success = articleService.likeArticle(id, currentUser.getId());
            return success ? Result.success("点赞成功") : Result.error("点赞失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消点赞文章
     * DELETE /api/articles/{id}/like
     */
    @DeleteMapping("/{id}/like")
    public Result<String> unlikeArticle(@PathVariable Integer id, HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            boolean success = articleService.unlikeArticle(id, currentUser.getId());
            return success ? Result.success("取消点赞成功") : Result.error("取消点赞失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取文章点赞状态（当前用户是否已点赞）
     * GET /api/articles/{id}/like/status
     */
    @GetMapping("/{id}/like/status")
    public Result<Boolean> getLikeStatus(@PathVariable Integer id, HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            boolean isLiked = articleService.isArticleLikedByUser(id, currentUser.getId());
            return Result.success(isLiked);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取文章点赞数
     * GET /api/articles/{id}/likes/count
     */
    @GetMapping("/{id}/likes/count")
    public Result<Integer> getLikeCount(@PathVariable Integer id) {
        try {
            int count = articleService.getArticleLikeCount(id);
            return Result.success(count);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户点赞的文章列表
     * GET /api/articles/liked
     */
    @GetMapping("/liked")
    public Result<List<Article>> getLikedArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        List<Article> likedArticles = articleService.getLikedArticles(currentUser.getId(), page, size);
        return Result.success(likedArticles);
    }

    /**
     * 切换点赞状态（点赞/取消点赞）
     * POST /api/articles/{id}/toggle-like
     */
    @PostMapping("/{id}/toggle-like")
    public Result<Map<String, Object>> toggleLike(@PathVariable Integer id, HttpServletRequest request) {
        // 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            Map<String, Object> result = articleService.toggleLike(id, currentUser.getId());
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户的所有文章（包括已发布和草稿，需要登录）
     * GET /api/articles/my?page=1&size=10
     * 修复前端的错误调用
     */
    @GetMapping("/my")
    public Result<Map<String, Object>> getMyArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        try {
            // 检查登录
            User currentUser = SessionUtil.getCurrentUser(request);
            if (currentUser == null) {
                return Result.unauthorized("请先登录");
            }

            // 获取所有文章然后过滤出当前用户的文章
            List<Article> allArticles = articleService.getArticles(page, size * 5);
            List<Article> myArticles = allArticles.stream()
                    .filter(article -> article.getUserId().equals(currentUser.getId()))
                    .collect(Collectors.toList());

            // 分页处理
            int start = (page - 1) * size;
            int end = Math.min(start + size, myArticles.size());
            List<Article> pagedArticles = myArticles.subList(start, end);

            Map<String, Object> result = new HashMap<>();
            result.put("articles", pagedArticles);
            result.put("page", page);
            result.put("size", size);
            result.put("total", myArticles.size());

            return Result.success(result);

        } catch (Exception e) {
            System.err.println("❌ 获取我的文章接口异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取文章失败");
        }
    }

    /**
     * 获取当前用户文章数量（用于统计，需要登录）
     * GET /api/articles/my/count
     * 用于UserProfile.vue中获取文章数量统计
     */
    @GetMapping("/my/count")
    public Result<Map<String, Object>> getMyArticleCount(HttpServletRequest request) {
        try {
            // 检查登录
            User currentUser = SessionUtil.getCurrentUser(request);
            if (currentUser == null) {
                return Result.unauthorized("请先登录");
            }

            // 这里需要实现获取用户文章数量的逻辑
            int articleCount = articleService.getArticleCountByUserId(currentUser.getId());
            int draftCount = articleService.getDraftCountByUserId(currentUser.getId());
            int publishedCount = articleService.getPublishedArticleCountByUserId(currentUser.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", articleCount);
            result.put("draftCount", draftCount);
            result.put("publishedCount", publishedCount);
            result.put("userId", currentUser.getId());

            return Result.success(result);

        } catch (Exception e) {
            System.err.println("❌ 获取我的文章数量接口异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取文章数量失败");
        }
    }
}