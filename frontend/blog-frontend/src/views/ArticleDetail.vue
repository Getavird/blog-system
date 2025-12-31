<template>
  <div class="article-detail-page">
    <Header />

    <!-- 文章加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-content">
        <el-skeleton :rows="5" animated />
        <el-skeleton style="margin-top: 20px" :rows="10" animated />
      </div>
    </div>

    <!-- 文章内容 -->
    <div v-else-if="article" class="article-container">
      <div class="container">
        <!-- 左中右三栏布局 -->
        <div class="article-body">
          <!-- 左侧：作者模块（固定不滚动） -->
          <aside class="left-sidebar">
            <div class="author-card">
              <div class="author-header" @click="goToAuthorPage">
                <div class="author-avatar-large" @click.stop="goToAuthorPage">
                  <!-- 修复头像路径 -->
                  <img
                    v-if="article.authorAvatar"
                    :src="article.authorAvatar"
                    alt="作者头像"
                  />
                  <div v-else class="avatar-placeholder-large">
                    {{
                      article.authorName ? article.authorName.charAt(0) : "A"
                    }}
                  </div>
                </div>
                <!-- 修复用户名字段 -->
                <h3 class="author-name" @click.stop="goToAuthorPage">
                  {{ article.authorName || "未知作者" }}
                </h3>
                <!-- 可能需要从其他地方获取bio信息 -->
                <p class="author-bio" v-if="article.authorBio">
                  {{ article.authorBio }}
                </p>
              </div>
              <div class="author-stats">
                <div class="stat-item">
                  <!-- 这些字段可能需要从用户公开信息接口获取 -->
                  <div class="stat-number">
                    {{ authorStats.articleCount || 0 }}
                  </div>
                  <div class="stat-label">文章</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">
                    {{ authorStats.likeCount || 0 }}
                  </div>
                  <div class="stat-label">获赞</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">
                    {{ authorStats.followerCount || 0 }}
                  </div>
                  <div class="stat-label">粉丝</div>
                </div>
              </div>
              <el-button
                v-if="!isArticleAuthor && isLoggedIn"
                :type="article.isFollowing ? 'default' : 'primary'"
                size="small"
                @click="toggleFollow"
                :loading="followLoading"
                class="follow-btn"
              >
                {{ article.isFollowing ? "已关注" : "关注作者" }}
              </el-button>
            </div>
          </aside>

          <!-- 中间：文章模块 -->
          <main class="main-content">
            <!-- 面包屑导航 -->
            <div class="breadcrumb">
              <router-link to="/">首页</router-link>
              <el-icon><ArrowRight /></el-icon>

              <!-- 显示分类（如果有） -->
              <span
                v-if="displayCategoryName"
                @click="goToCategory(displayCategoryId)"
                class="category-link"
              >
                {{ displayCategoryName }}
              </span>

              <el-icon v-if="displayCategoryName"><ArrowRight /></el-icon>
              <span class="current">{{ article.title }}</span>
            </div>

            <!-- 文章标题 -->
            <h1 class="article-title">{{ article.title }}</h1>

            <!-- 文章信息（标题下面） -->
            <div class="article-info">
              <div class="info-item">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatTime(article.createTime) }}</span>
              </div>
              <div class="info-item">
                <el-icon><View /></el-icon>
                <span>{{ article.viewCount || 0 }} 阅读</span>
              </div>
              <div class="info-item">
                <el-icon><ChatDotRound /></el-icon>
                <span>{{ article.commentCount || 0 }} 评论</span>
              </div>
              <div class="info-item">
                <el-icon><Star /></el-icon>
                <span>{{ article.likeCount || 0 }} 点赞</span>
              </div>
            </div>

            <!-- 文章分类和标签 -->
            <div class="article-tags">
              <el-tag
                v-if="displayCategoryName"
                type="primary"
                size="large"
                @click="goToCategory(displayCategoryId)"
                class="category-tag"
              >
                <el-icon><Folder /></el-icon>
                {{ displayCategoryName }}
              </el-tag>
              <el-tag
                v-for="tag in processedTags"
                :key="getTagKey(tag, index)"
                size="large"
                @click="goToTag(tag)"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
              <div v-if="processedTags.length === 0" class="no-tags">
                <span class="no-tags-text">暂无标签</span>
              </div>
            </div>

            <!-- 文章内容 -->
            <div class="article-content" v-html="article.content"></div>

            <!-- 文章底部信息 -->
            <div class="article-footer">
              <div class="update-info">
                <el-icon><Clock /></el-icon>
                最后更新于
                {{ formatTime(article.updateTime || article.createTime) }}
              </div>

              <!-- 版权声明 -->
              <div class="copyright">
                <p>© 本文由 {{ article.authorName }} 发布，转载请注明出处</p>
              </div>
            </div>

            <!-- 点赞操作 -->
            <div class="interaction-actions">
              <el-button
                :type="article.isLiked ? 'danger' : 'primary'"
                size="large"
                @click="toggleLike"
                :loading="likeLoading"
                class="action-btn"
                :disabled="!isLoggedIn"
              >
                <el-icon><Star /></el-icon>
                <span :class="{ 'like-count-animation': showLikeAnimation }">
                  {{ article.isLiked ? "已点赞" : "点赞" }} ({{
                    article.likeCount || 0
                  }})
                </span>
              </el-button>

              <!-- 编辑按钮（如果是作者） -->
              <el-button
                v-if="isArticleAuthor && isLoggedIn"
                type="primary"
                size="large"
                @click="editArticle"
                class="edit-btn"
              >
                <el-icon><Edit /></el-icon>
                编辑文章
              </el-button>
            </div>

            <!-- 评论区域 -->
            <div class="comments-section">
              <div class="comments-wrapper">
                <h2 class="comments-title">
                  <el-icon><ChatDotRound /></el-icon>
                  评论 ({{ article.commentCount || 0 }})
                </h2>

                <!-- 登录提示 -->
                <div v-if="!isLoggedIn" class="login-prompt">
                  <p>
                    请先<a href="javascript:;" @click="showLogin = true">登录</a
                    >后发表评论
                  </p>
                </div>

                <!-- 发表评论 -->
                <div v-else class="comment-form-card">
                  <div class="form-header">
                    <div class="user-avatar">
                      <img
                        v-if="currentUserAvatar"
                        :src="currentUserAvatar"
                        alt="用户头像"
                      />
                      <div v-else class="avatar-placeholder-small">
                        {{ currentUserName ? currentUserName.charAt(0) : "U" }}
                      </div>
                    </div>
                    <div class="form-title">发表评论</div>
                  </div>
                  <el-input
                    v-model="commentContent"
                    type="textarea"
                    :rows="4"
                    placeholder="写下你的评论..."
                    resize="none"
                    class="comment-textarea"
                    maxlength="500"
                    show-word-limit
                  />
                  <div class="form-actions">
                    <el-button @click="cancelComment">取消</el-button>
                    <el-button
                      type="primary"
                      @click="submitComment"
                      :loading="commentLoading"
                      :disabled="!commentContent.trim()"
                    >
                      发表评论
                    </el-button>
                  </div>
                </div>

                <!-- 评论列表 -->
                <div v-if="comments.length > 0" class="comments-list">
                  <div
                    v-for="comment in comments"
                    :key="comment.id"
                    class="comment-item"
                  >
                    <div class="comment-header">
                      <div
                        class="comment-author"
                        @click="goToUserPage(comment.userId, comment.username)"
                      >
                        <div class="comment-avatar">
                          <img
                            v-if="comment.userAvatar"
                            :src="getAvatarUrl(comment.userAvatar)"
                            alt="用户头像"
                          />
                          <div v-else class="avatar-placeholder-small">
                            {{
                              comment.username
                                ? comment.username.charAt(0)
                                : "U"
                            }}
                          </div>
                        </div>
                        <div class="comment-author-info">
                          <div class="comment-author-name">
                            {{ comment.username }}
                          </div>
                          <div class="comment-time">
                            {{ formatTime(comment.createTime) }}
                          </div>
                        </div>
                      </div>
                      <div
                        v-if="checkCommentOwnership(comment)"
                        class="comment-actions"
                      >
                        <el-button type="text" @click="editComment(comment)"
                          >编辑</el-button
                        >
                        <el-button
                          type="text"
                          @click="deleteComment(comment.id)"
                          >删除</el-button
                        >
                      </div>
                    </div>
                    <div class="comment-content">{{ comment.content }}</div>
                    <div class="comment-footer">
                      <el-button
                        type="text"
                        size="small"
                        @click="replyToComment(comment)"
                        :disabled="!isLoggedIn"
                      >
                        回复
                      </el-button>
                      <el-button
                        type="text"
                        size="small"
                        :class="{ liked: comment.isLiked }"
                        @click="likeComment(comment)"
                        :disabled="!isLoggedIn"
                      >
                        <el-icon><Star /></el-icon>
                        {{ comment.likeCount || 0 }}
                      </el-button>
                    </div>
                  </div>
                </div>
                <div v-else class="no-comments">
                  <el-icon size="40" color="#c0c4cc"><Comment /></el-icon>
                  <p>还没有评论，快来抢沙发吧～</p>
                </div>
              </div>
            </div>
          </main>

          <!-- 右侧：目录导航 -->
          <aside v-if="showToc" class="right-sidebar">
            <div class="toc-card">
              <h3 class="toc-title">
                <el-icon><Menu /></el-icon>
                文章目录
              </h3>
              <div class="toc-content">
                <div
                  v-for="(item, index) in tocItems"
                  :key="index"
                  :class="['toc-item', `toc-level-${item.level}`]"
                  @click="scrollToHeading(item.id)"
                >
                  {{ item.text }}
                </div>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </div>

    <!-- 文章不存在 -->
    <div v-else class="not-found-container">
      <div class="error-content">
        <el-icon size="80" color="#c0c4cc"><DocumentDelete /></el-icon>
        <h1>文章不存在</h1>
        <p>抱歉，您要访问的文章可能已被删除或不存在</p>
        <el-button type="primary" @click="$router.push('/')">
          返回首页
        </el-button>
      </div>
    </div>

    <!-- 登录弹窗 -->
    <el-dialog
      v-model="showLogin"
      title="用户登录"
      width="400px"
      :close-on-click-modal="false"
      @close="showLogin = false"
    >
      <div style="text-align: center; padding: 20px">
        <p>请先登录才能进行此操作</p>
        <div
          style="
            display: flex;
            gap: 10px;
            justify-content: center;
            margin-top: 20px;
          "
        >
          <el-button @click="showLogin = false">取消</el-button>
          <el-button type="primary" @click="toLoginPage">去登录</el-button>
          <el-button type="success" @click="toRegisterPage">去注册</el-button>
        </div>
      </div>
    </el-dialog>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useArticleStore } from "@/stores/article";
import { useCommentStore } from "@/stores/comment";
import { useUserStore } from "@/stores/user";
import { useCategoryStore } from "@/stores/category";
import { useTagStore } from "@/stores/tag";
import { useFollowStore } from "@/stores/follow";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  ArrowRight,
  Calendar,
  View,
  ChatDotRound,
  Edit,
  Star,
  Folder,
  Menu,
  Clock,
  DocumentDelete,
  Comment,
} from "@element-plus/icons-vue";

// 组件导入
import Header from "@/components/layout/Header.vue";
import Footer from "@/components/layout/Footer.vue";

const route = useRoute();
const router = useRouter();

// Pinia Store
const articleStore = useArticleStore();
const commentStore = useCommentStore();
const userStore = useUserStore();
const categoryStore = useCategoryStore();
const tagStore = useTagStore();
const followStore = useFollowStore();

// 路由参数
const articleId = ref(parseInt(route.params.id) || 0);

// 文章数据
const article = computed(() => articleStore.currentArticle);
const loading = ref(false);
const likeLoading = ref(false);
const followLoading = ref(false);
const showLikeAnimation = ref(false);

// 登录状态
const isLoggedIn = computed(() => userStore.isLoggedIn());
const currentUser = computed(() => userStore.user);
const currentUserName = computed(() => currentUser.value?.username || "");
const currentUserAvatar = computed(() => currentUser.value?.avatar || "");

// 检查是否为文章作者
const isArticleAuthor = computed(() => {
  if (!isLoggedIn.value || !article.value) return false;
  return currentUser.value?.id === article.value.authorId;
});

// 评论相关
const comments = computed(() => commentStore.comments || []);
const commentContent = ref("");
const commentLoading = ref(false);
const showLogin = ref(false);

// 目录相关
const tocItems = ref([]);
const showToc = computed(() => tocItems.value.length > 0);

// 作者统计数据（需要从用户公开信息接口获取）
const authorStats = ref({
  articleCount: 0,
  likeCount: 0,
  viewCount: 0,
  followerCount: 0,
  followingCount: 0,
});

// 处理标签数据
const processedTags = computed(() => {
  if (!article.value || !article.value.tags) return [];

  const tags = article.value.tags;

  // 如果是字符串
  if (typeof tags === "string") {
    return tags
      .split(",")
      .map((tag) => tag.trim())
      .filter((tag) => tag);
  }

  // 如果是数组
  if (Array.isArray(tags)) {
    return tags
      .map((tag) => {
        if (typeof tag === "string") return tag;
        if (tag && typeof tag === "object") return tag.name || tag.label || "";
        return "";
      })
      .filter((tag) => tag);
  }

  return [];
});

const getAvatarUrl = (avatarFileName) => {
  if (!avatarFileName || avatarFileName.trim() === "") {
    return "http://localhost:8080/uploads/avatars/default_avatar.png";
  }

  // 如果已经是完整URL，直接返回
  if (
    avatarFileName.startsWith("http://") ||
    avatarFileName.startsWith("https://") ||
    avatarFileName.startsWith("data:")
  ) {
    return avatarFileName;
  }

  // 清理路径
  let cleanFileName = avatarFileName.trim();

  // 移除可能的路径前缀
  if (cleanFileName.startsWith("/uploads/avatars/")) {
    cleanFileName = cleanFileName.replace("/uploads/avatars/", "");
  } else if (cleanFileName.startsWith("uploads/avatars/")) {
    cleanFileName = cleanFileName.replace("uploads/avatars/", "");
  }

  // 所有头像都从 uploads/avatars/ 目录加载
  return "http://localhost:8080/uploads/avatars/" + cleanFileName;
};

// 获取标签key
const getTagKey = (tag, index) => {
  return `${tag}-${index}`;
};

// 跳转到标签
const goToTag = (tagName) => {
  if (tagName) {
    router.push({
      path: "/tags",
      query: {
        name: encodeURIComponent(tagName),
      },
    });
  }
};

// 显示的分类名称
const displayCategoryName = computed(() => {
  if (!article.value) return "";

  // 方法1: 从category对象获取
  if (article.value.category) {
    // 如果是对象且有name属性
    if (
      typeof article.value.category === "object" &&
      article.value.category.name
    ) {
      return article.value.category.name;
    }
    // 如果是字符串
    if (typeof article.value.category === "string") {
      return article.value.category;
    }
  }

  // 方法2: 从categoryName字段获取
  if (article.value.categoryName) {
    return article.value.categoryName;
  }

  // 方法3: 从分类列表匹配
  if (article.value.categoryId && categories.value.length > 0) {
    const foundCategory = categories.value.find(
      (cat) => cat.id === article.value.categoryId
    );
    if (foundCategory) {
      return foundCategory.name;
    }
  }

  return "";
});
// 添加去注册页面方法
const toRegisterPage = () => {
  showLogin.value = false
  router.push({
    path: '/',
    query: {
      showRegister: true, // 假设首页有注册弹窗
      redirect: route.fullPath,
    },
  })
};

// 显示的分类ID
const displayCategoryId = computed(() => {
  if (!article.value) return "";

  // 方法1: 从category对象获取
  if (article.value.category && article.value.category.id) {
    return article.value.category.id;
  }

  // 方法2: 直接获取categoryId
  if (article.value.categoryId) {
    return article.value.categoryId;
  }

  return "";
});

// 分类数据
const categories = computed(() => categoryStore.categories || []);

// 辅助方法：更新文章列表中的点赞状态
const updateArticlesInList = (articles, articleId, isLiked, likeCount) => {
  if (!Array.isArray(articles)) return;

  const index = articles.findIndex((article) => article.id === articleId);
  if (index !== -1) {
    articles[index].isLiked = isLiked;
    articles[index].likeCount = likeCount;
  }
};

// 辅助方法：重新加载作者统计信息
const loadAuthorStats = async () => {
  if (!article.value || !article.value.authorName) return;

  try {
    console.log("重新加载作者统计信息，用户名:", article.value.authorName);

    const userStore = useUserStore();

    // 并行获取统计信息
    await Promise.allSettled([
      // 获取公开用户统计
      userStore.fetchPublicUserStats(article.value.authorName),

      // 获取关注统计（如果需要）
      (async () => {
        if (article.value.authorId) {
          try {
            const followStore = useFollowStore();
            const followStats = await followStore.fetchUserFollowStats(
              article.value.authorId
            );
            if (followStats) {
              // 更新本地状态
              article.value.fansCount =
                followStats.followerCount || article.value.fansCount;
            }
          } catch (error) {
            console.warn("获取关注统计失败:", error);
          }
        }
      })(),
    ]);

    // 更新文章中的作者统计信息
    const publicUserStats = userStore.publicUserStats;
    if (publicUserStats) {
      article.value.articleCount =
        publicUserStats.articleCount || article.value.articleCount;
      article.value.totalLikeCount =
        publicUserStats.likeCount || article.value.totalLikeCount;
      article.value.fansCount =
        publicUserStats.followerCount || article.value.fansCount;
    }

    console.log("作者统计信息重新加载完成");
  } catch (error) {
    console.error("重新加载作者统计信息失败:", error);
  }
};

// 监听全局点赞事件
const handleArticleLikedEvent = (event) => {
  const {
    articleId: likedArticleId,
    isLiked,
    likeCount,
    authorId,
  } = event.detail;

  // 如果点赞的是当前文章，更新状态
  if (articleId.value === likedArticleId) {
    article.value.isLiked = isLiked;
    article.value.likeCount = likeCount;
  }
};

// 辅助方法：触发点赞动画
const triggerLikeAnimation = () => {
  showLikeAnimation.value = true;

  // 添加动画类
  const likeBtn = document.querySelector(".action-btn");
  if (likeBtn) {
    likeBtn.classList.add("like-animation");

    // 创建粒子效果
    createLikeParticles();

    // 移除动画类（动画结束后）
    setTimeout(() => {
      likeBtn.classList.remove("like-animation");
      showLikeAnimation.value = false;
    }, 500);
  }
};

// 辅助方法：创建点赞粒子效果
const createLikeParticles = () => {
  const container = document.querySelector(".interaction-actions");
  if (!container) return;

  const particleCount = 12;

  for (let i = 0; i < particleCount; i++) {
    const particle = document.createElement("div");
    particle.className = "like-particle";

    // 随机位置和角度
    const angle = (Math.PI * 2 * i) / particleCount;
    const distance = 30 + Math.random() * 20;

    // 随机大小
    const size = 3 + Math.random() * 4;
    particle.style.width = `${size}px`;
    particle.style.height = `${size}px`;

    // 随机颜色
    const hue = 350 + Math.random() * 20; // 红色系
    particle.style.backgroundColor = `hsl(${hue}, 100%, 65%)`;

    // 设置初始位置
    particle.style.position = "absolute";
    particle.style.borderRadius = "50%";
    particle.style.pointerEvents = "none";
    particle.style.zIndex = "1000";

    container.appendChild(particle);

    // 动画
    const animation = particle.animate(
      [
        {
          transform: "translate(0, 0) scale(1)",
          opacity: 1,
        },
        {
          transform: `translate(${Math.cos(angle) * distance}px, ${
            Math.sin(angle) * distance
          }px) scale(0)`,
          opacity: 0,
        },
      ],
      {
        duration: 500 + Math.random() * 200,
        easing: "cubic-bezier(0.1, 0.8, 0.2, 1)",
      }
    );

    // 动画结束后移除粒子
    animation.onfinish = () => {
      particle.remove();
    };
  }
};

// 组件挂载
onMounted(async () => {
  // 初始化用户状态
  userStore.initFromStorage();

  console.log("文章详情页面 - 用户状态:", {
    isLoggedIn: userStore.isLoggedIn(),
    user: userStore.user,
  });

  // 监听全局点赞事件
  window.addEventListener("article-liked", handleArticleLikedEvent);

  // 加载文章详情
  if (articleId.value) {
    await loadArticleDetail();
  }
});

// 组件卸载
onUnmounted(() => {
  // 移除事件监听器
  window.removeEventListener("article-liked", handleArticleLikedEvent);
});

// 监听路由参数变化
watch(
  () => route.params.id,
  async (newId) => {
    if (newId) {
      articleId.value = parseInt(newId);
      await loadArticleDetail();
    }
  }
);

// 加载文章详情
const loadArticleDetail = async () => {
  try {
    loading.value = true;

    console.log("开始加载文章详情，文章ID:", articleId.value);

    // 1. 加载文章详情
    await articleStore.fetchArticleDetail(articleId.value);

    // 2. 如果有作者信息，加载作者公开信息
    if (article.value && article.value.authorName) {
      console.log("立即加载作者信息，作者名:", article.value.authorName);
      await loadAuthorInfo(article.value.authorName);
    }

    // 3. 验证分类数据
    if (article.value) {
      console.log("文章分类信息验证:");
      console.log("- category 对象:", article.value.category);
      console.log("- categoryId:", article.value.categoryId);
      console.log("- categoryName:", article.value.categoryName);

      // 如果有categoryId但没有category对象，尝试从分类列表获取
      if (
        article.value.categoryId &&
        (!article.value.category || !article.value.category.name)
      ) {
        console.log("尝试从分类列表匹配分类信息...");

        // 确保分类数据已加载
        if (categories.value.length === 0) {
          await categoryStore.fetchCategories();
        }

        // 查找匹配的分类
        const foundCategory = categories.value.find(
          (cat) => cat.id === article.value.categoryId
        );
        if (foundCategory) {
          console.log("找到匹配的分类:", foundCategory);
          // 更新文章的分类信息
          article.value.category = foundCategory;
          article.value.categoryName = foundCategory.name;
        } else {
          console.warn("未找到匹配的分类，ID:", article.value.categoryId);
        }
      }
    }

    // 4. 不需要单独调用阅读量接口，因为 GET /api/articles/{id} 已经返回了最新的阅读量

    // 5. 加载文章评论
    await loadArticleComments();

    // 6. 检查当前用户是否关注了作者
    if (
      isLoggedIn.value &&
      article.value &&
      article.value.authorId &&
      !isArticleAuthor.value
    ) {
      try {
        const isFollowing = await followStore.checkFollowStatus(
          article.value.authorId
        );
        article.value.isFollowing = isFollowing;
      } catch (error) {
        console.error("检查关注状态失败:", error);
      }
    }

    // 7. 生成目录
    generateToc();

    console.log("文章详情加载完成:", article.value);
  } catch (error) {
    console.error("加载文章详情失败:", error);
    // 区分不同类型的错误
    if (error.response?.status === 404) {
      // 文章不存在
      ElMessage.error("文章不存在或已被删除");
    } else {
      ElMessage.error("文章加载失败，请稍后重试");
    }
  } finally {
    loading.value = false;
  }
};

const loadAuthorInfo = async (username) => {
  try {
    console.log("加载作者信息，用户名:", username);

    // 清除缓存，确保获取最新数据
    const userStore = useUserStore();

    // 1. 首先清除可能的缓存数据
    userStore.clearPublicUserStats();

    // 2. 并行获取用户公开信息和统计信息
    const [userData, statsData] = await Promise.allSettled([
      userStore.fetchPublicUserInfo(username),
      userStore.fetchPublicUserStats(username),
    ]);

    console.log("作者信息获取结果:", {
      userInfo: userData.status === "fulfilled" ? userData.value : null,
      stats: statsData.status === "fulfilled" ? statsData.value : null,
    });

    // 3. 更新作者统计信息
    if (statsData.status === "fulfilled" && statsData.value) {
      authorStats.value = { ...authorStats.value, ...statsData.value };
      console.log("更新后的作者统计:", authorStats.value);
    } else if (userData.status === "fulfilled" && userData.value?.stats) {
      // 如果获取统计信息失败，使用用户信息中的统计
      authorStats.value = { ...authorStats.value, ...userData.value.stats };
    }

    // 4. 如果有需要，可以更新其他作者信息
    if (userData.status === "fulfilled" && userData.value) {
      if (userData.value.bio && !article.value.authorBio) {
        article.value.authorBio = userData.value.bio;
      }
    }
  } catch (error) {
    console.error("加载作者信息失败:", error);
    // 尝试使用文章中的作者信息作为后备
    if (article.value && article.value.authorId) {
      console.log("尝试使用文章中的作者信息作为后备");
      // 这里可以调用其他方法获取作者信息
    }
  }
};
// 加载文章评论
const loadArticleComments = async () => {
  try {
    console.log("开始加载文章评论");
    await commentStore.fetchArticleComments(articleId.value, {
      page: 1,
      size: 20,
    });
    console.log("评论加载完成，评论数量:", commentStore.comments?.length);
  } catch (error) {
    console.error("加载评论失败:", error);
  }
};

// 生成目录
const generateToc = () => {
  nextTick(() => {
    const contentElement = document.querySelector(".article-content");
    if (!contentElement) return;

    const headings = contentElement.querySelectorAll("h1, h2, h3, h4, h5, h6");
    tocItems.value = Array.from(headings).map((heading, index) => {
      const id = heading.id || `heading-${index}`;
      heading.id = id;
      return {
        id,
        text: heading.textContent || "",
        level: parseInt(heading.tagName.charAt(1)),
      };
    });

    console.log("生成目录，项目数量:", tocItems.value.length);
  });
};

// 滚动到标题
const scrollToHeading = (id) => {
  const element = document.getElementById(id);
  if (element) {
    element.scrollIntoView({ behavior: "smooth" });
  }
};

// 格式化时间
const formatTime = (time) => {
  if (!time) return "";
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / (1000 * 60));
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (minutes < 60) {
    return `${minutes}分钟前`;
  } else if (hours < 24) {
    return `${hours}小时前`;
  } else if (days < 7) {
    return `${days}天前`;
  } else {
    return date.toLocaleDateString("zh-CN");
  }
};

// 点赞/取消点赞文章 - 修复版，解决状态不一致问题
const toggleLike = async () => {
  // 检查登录状态
  if (!isLoggedIn.value) {
    showLogin.value = true;
    ElMessage.warning("请先登录");
    return;
  }

  try {
    likeLoading.value = true;

    console.log("开始点赞操作，文章ID:", articleId.value);

    // 保存当前点赞状态，用于回滚
    const originalIsLiked = article.value.isLiked || false;
    const originalLikeCount = article.value.likeCount || 0;

    console.log("当前点赞状态:", originalIsLiked, "点赞数:", originalLikeCount);

    // 1. 计算新的点赞状态和点赞数
    const newIsLiked = !originalIsLiked;
    const newLikeCount = newIsLiked
      ? originalLikeCount + 1
      : Math.max(0, originalLikeCount - 1);

    // 2. 立即更新本地UI（乐观更新）
    article.value.isLiked = newIsLiked;
    article.value.likeCount = newLikeCount;

    // 3. 调用store的点赞方法
    let result;
    try {
      // 注意：这里调用 articleStore.toggleLike，它应该返回 { success: true/false, data: { isLiked, likeCount } }
      result = await articleStore.toggleLike(articleId.value);
      console.log("store点赞操作结果:", result);
    } catch (storeError) {
      console.error("store点赞操作失败:", storeError);
      // 如果store方法失败，使用原始API调用
      const apiResult = await articleStore.$api.toggleArticleLike(
        articleId.value
      );
      console.log("API点赞操作结果:", apiResult);

      // 解析API响应
      if (apiResult && apiResult.code === 200) {
        result = {
          success: true,
          data: apiResult.data || {
            isLiked: newIsLiked,
            likeCount: newLikeCount,
          },
        };
      } else {
        throw new Error(apiResult?.message || "点赞操作失败");
      }
    }

    // 4. 处理响应结果
    if (result?.success === true) {
      const resultData = result.data || result;
      const finalIsLiked =
        resultData.isLiked !== undefined ? resultData.isLiked : newIsLiked;
      const finalLikeCount =
        resultData.likeCount !== undefined
          ? resultData.likeCount
          : newLikeCount;

      // 更新文章数据
      article.value.isLiked = finalIsLiked;
      article.value.likeCount = finalLikeCount;
      // 5. 重新加载作者统计信息（重要：确保统计信息更新）
      if (article.value && article.value.authorName) {
        console.log("点赞成功，重新加载作者统计信息");
        try {
          // 使用forceRefresh参数确保获取最新数据
          await loadAuthorInfo(article.value.authorName);
          console.log("作者统计信息重新加载完成");
        } catch (refreshError) {
          console.warn("重新加载作者统计失败:", refreshError);
        }
      }
      // 5. 同步作者统计信息
      try {
        if (article.value.authorId) {
          console.log(
            "点赞成功，同步作者统计，作者ID:",
            article.value.authorId
          );

          // 获取用户store
          const userStore = useUserStore();

          // 并行执行多个同步操作
          await Promise.allSettled([
            // 同步用户统计
            userStore.syncUserStats &&
              userStore.syncUserStats(article.value.authorId),

            // 重新加载作者统计信息
            loadAuthorStats(),
          ]);

          console.log("作者统计同步完成");
        }
      } catch (syncError) {
        console.warn("点赞后同步作者统计失败:", syncError);
        // 这里不抛出错误，因为点赞操作本身成功了
      }

      // 6. 根据点赞/取消点赞显示不同的提示
      if (finalIsLiked) {
        ElMessage.success({
          message: "点赞成功",
          duration: 2000,
          showClose: true,
        });

        // 触发点赞成功动画
        triggerLikeAnimation();
      } else {
        ElMessage.info({
          message: "已取消点赞",
          duration: 2000,
          showClose: true,
        });
      }

      // 7. 更新相关文章列表中的点赞状态
      try {
        // 更新主页文章列表中的点赞状态
        const articleStore = useArticleStore();
        updateArticlesInList(
          articleStore.articles,
          articleId.value,
          finalIsLiked,
          finalLikeCount
        );

        // 更新热门文章列表
        updateArticlesInList(
          articleStore.hotArticles,
          articleId.value,
          finalIsLiked,
          finalLikeCount
        );

        // 更新最新文章列表
        updateArticlesInList(
          articleStore.newestArticles,
          articleId.value,
          finalIsLiked,
          finalLikeCount
        );

        // 更新我的文章列表
        if (articleStore.myArticles && articleStore.myArticles.list) {
          updateArticlesInList(
            articleStore.myArticles.list,
            articleId.value,
            finalIsLiked,
            finalLikeCount
          );
        }
      } catch (updateError) {
        console.warn("更新文章列表状态失败:", updateError);
      }

      // 8. 触发全局事件
      window.dispatchEvent(
        new CustomEvent("article-liked", {
          detail: {
            articleId: articleId.value,
            isLiked: finalIsLiked,
            likeCount: finalLikeCount,
            authorId: article.value.authorId,
          },
        })
      );

      return {
        success: true,
        isLiked: finalIsLiked,
        likeCount: finalLikeCount,
      };
    } else {
      // API返回了错误
      throw new Error(result?.message || "操作失败");
    }
  } catch (error) {
    console.error("操作点赞失败:", error);

    // 回滚本地UI状态
    article.value.isLiked = originalIsLiked;
    article.value.likeCount = originalLikeCount;

    // 错误分类处理
    let errorMessage = "操作失败";
    let showLoginPrompt = false;

    if (error.response) {
      // API响应错误
      const { status, data } = error.response;
      console.error("点赞API错误详情:", { status, data });

      switch (status) {
        case 401:
          errorMessage = "请先登录";
          showLoginPrompt = true;
          break;
        case 403:
          errorMessage = "没有权限进行此操作";
          break;
        case 404:
          errorMessage = "文章不存在";
          break;
        case 409:
          errorMessage = "已经点过赞了";
          break;
        case 429:
          errorMessage = "操作过于频繁，请稍后再试";
          break;
        case 500:
          errorMessage = "服务器内部错误";
          break;
        default:
          errorMessage = data?.message || `操作失败 (${status})`;
      }
    } else if (error.request) {
      // 请求发送但无响应
      errorMessage = "网络连接失败，请检查网络";
    } else if (error.message === "点赞成功") {
      // 特殊处理：后端可能返回"点赞成功"作为错误消息（兼容性处理）
      console.warn('收到"点赞成功"的错误消息，按成功处理');
      // 不显示错误，而是更新状态
      article.value.isLiked = !originalIsLiked;
      article.value.likeCount = originalIsLiked
        ? Math.max(0, originalLikeCount - 1)
        : originalLikeCount + 1;

      ElMessage.success("点赞成功");
      return { success: true };
    } else {
      // 其他错误
      errorMessage = error.message || "操作失败";
    }

    // 显示错误提示
    if (errorMessage !== "点赞成功") {
      ElMessage.error({
        message: errorMessage,
        duration: 3000,
        showClose: true,
      });
    }

    // 如果需要登录，显示登录弹窗
    if (showLoginPrompt) {
      showLogin.value = true;
    }

    // 抛出错误供调用者处理
    throw error;
  } finally {
    likeLoading.value = false;
  }
};

// 关注作者
const toggleFollow = async () => {
  if (!isLoggedIn.value) {
    showLogin.value = true;
    ElMessage.warning("请先登录");
    return;
  }

  if (isArticleAuthor.value) {
    ElMessage.warning("不能关注自己");
    return;
  }

  try {
    followLoading.value = true;
    const currentStatus = article.value.isFollowing || false;
    const newStatus = !currentStatus;

    if (newStatus) {
      await followStore.followUser(article.value.authorId);
      ElMessage.success("关注成功");
    } else {
      await followStore.unfollowUser(article.value.authorId);
      ElMessage.info("已取消关注");
    }

    // 更新本地状态
    article.value.isFollowing = newStatus;
    article.value.fansCount = article.value.fansCount || 0;
    article.value.fansCount += newStatus ? 1 : -1;

    // 同步用户统计
    const userStore = useUserStore();
    await userStore.syncUserStats(article.value.authorId);
  } catch (error) {
    console.error("操作关注失败:", error);
    ElMessage.error("操作失败");
  } finally {
    followLoading.value = false;
  }
};

// 编辑文章
const editArticle = () => {
  if (!isLoggedIn.value) {
    showLogin.value = true;
    ElMessage.warning("请先登录");
    return;
  }

  if (!isArticleAuthor.value) {
    ElMessage.error("只有文章作者可以编辑");
    return;
  }

  router.push(`/article/edit/${articleId.value}`);
};

// 跳转到分类
const goToCategory = (categoryId) => {
  if (categoryId) {
    router.push(`/category/${categoryId}`);
  }
};

// 跳转到作者主页 - 修复版
const goToAuthorPage = () => {
  if (!article.value) return;

  console.log("跳转到作者主页，作者信息:", {
    username: article.value.username,
    authorName: article.value.authorName,
    authorId: article.value.authorId,
  });

  // 优先使用 authorName，因为它能显示
  const userName = article.value.authorName || article.value.username;
  console.log("最终使用的用户名:", userName);

  if (userName && userName.trim()) {
    // 移除可能的空白字符
    const cleanUserName = userName.trim();
    // 使用 encodeURIComponent 对中文进行编码
    const encodedUsername = encodeURIComponent(cleanUserName);
    console.log("编码后的用户名:", encodedUsername);

    // 使用路由跳转
    router.push(`/user/${encodedUsername}`);
  } else {
    console.warn("无法获取作者用户名，无法跳转", article.value);
    ElMessage.warning("无法获取作者信息");
  }
};

// 跳转到用户主页 - 评论作者点击
const goToUserPage = (userId, username) => {
  // 优先使用 username
  if (username && username.trim()) {
    const encodedUsername = encodeURIComponent(username.trim());
    router.push(`/user/${encodedUsername}`);
  } else {
    console.warn("无法跳转：缺少用户名");
    ElMessage.warning("无法跳转到用户主页");
  }
};

// 提交评论
const submitComment = async () => {
  if (!isLoggedIn.value) {
    showLogin.value = true;
    ElMessage.warning("请先登录");
    return;
  }

  const content = commentContent.value.trim();
  if (!content) {
    ElMessage.warning("请输入评论内容");
    return;
  }

  try {
    commentLoading.value = true;

    console.log("提交评论:", {
      articleId: articleId.value,
      content: content,
    });

    await commentStore.createComment({
      articleId: articleId.value,
      content: content,
    });

    ElMessage.success("评论成功");
    commentContent.value = "";

    // 更新文章评论数
    if (article.value) {
      article.value.commentCount = (article.value.commentCount || 0) + 1;
    }
  } catch (error) {
    console.error("发表评论失败:", error);
    // 更友好的错误提示
    if (
      error.message.includes("Network Error") ||
      error.code === "ERR_NETWORK"
    ) {
      ElMessage.error("网络错误，请检查连接");
    } else if (error.response?.status === 401) {
      ElMessage.error("请先登录");
      showLogin.value = true;
    } else {
      ElMessage.error(error.message || "评论失败");
    }
  } finally {
    commentLoading.value = false;
  }
};

// 取消评论
const cancelComment = () => {
  commentContent.value = "";
};

// 编辑评论
const editComment = (comment) => {
  if (!isLoggedIn.value) {
    showLogin.value = true;
    ElMessage.warning("请先登录");
    return;
  }

  // 检查权限
  if (comment.userId !== currentUser.value?.id && !isArticleAuthor.value) {
    ElMessage.error("只有评论作者或文章作者可以编辑评论");
    return;
  }

  // 这里可以打开编辑对话框
  ElMessage.info("编辑评论功能待实现");
};

// 删除评论
const deleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm("确定要删除这条评论吗？", "提示", {
      type: "warning",
      confirmButtonText: "确定",
      cancelButtonText: "取消",
    });

    await commentStore.deleteComment(commentId);
    ElMessage.success("删除成功");

    // 更新文章评论数
    if (article.value) {
      article.value.commentCount = Math.max(
        0,
        (article.value.commentCount || 1) - 1
      );
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("删除评论失败:", error);
      ElMessage.error("删除失败");
    }
  }
};

// 回复评论
const replyToComment = (comment) => {
  if (!isLoggedIn.value) {
    showLogin.value = true;
    ElMessage.warning("请先登录");
    return;
  }

  commentContent.value = `@${comment.username} `;
  // 聚焦到评论框
  nextTick(() => {
    const textarea = document.querySelector(".comment-textarea textarea");
    if (textarea) {
      textarea.focus();
    }
  });
};

// 点赞评论
const likeComment = async (comment) => {
  if (!isLoggedIn.value) {
    showLogin.value = true;
    ElMessage.warning("请先登录");
    return;
  }

  try {
    const isLike = !comment.isLiked;
    await commentStore.toggleCommentLike(comment.id, isLike);

    if (isLike) {
      ElMessage.success("点赞成功");
    } else {
      ElMessage.info("已取消点赞");
    }
  } catch (error) {
    console.error("操作评论点赞失败:", error);
    ElMessage.error("操作失败");
  }
};

// 检查评论所有权（作者或管理员）
const checkCommentOwnership = (comment) => {
  if (!isLoggedIn.value) return false;

  // 1. 评论作者
  if (comment.userId === currentUser.value?.id) return true;

  // 2. 文章作者
  if (article.value && article.value.authorId === currentUser.value?.id)
    return true;

  // 3. 管理员（假设角色1为管理员）
  if (currentUser.value?.role === 1) return true;

  return false;
};

// 跳转到登录页
const toLoginPage = () => {
  showLogin.value = false;
  router.push({
    path: "/",
    query: {
      showLogin: true,
      redirect: route.fullPath,
    },
  });
};
</script>

<style scoped>
.article-detail-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
}

/* 加载状态 */
.loading-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.loading-content {
  width: 100%;
  max-width: 800px;
}

/* 文章容器 */
.article-container {
  padding-top: 20px;
  flex: 1;
}

.container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 三栏布局 */
.article-body {
  display: grid;
  grid-template-columns: 280px 1fr 280px;
  gap: 30px;
  margin-bottom: 40px;
  position: relative;
}

/* 左侧：作者模块 */
.left-sidebar {
  height: fit-content;
}

/* 右侧：目录导航 */
.right-sidebar {
  position: sticky;
  top: 20px;
  height: fit-content;
}

/* 中间：文章模块 */
.main-content {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

/* 面包屑导航 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  flex-wrap: wrap;
}

.breadcrumb a {
  color: #666;
  text-decoration: none;
  transition: color 0.3s;
}

.breadcrumb a:hover {
  color: #409eff;
}

.breadcrumb .category-link {
  color: #666;
  cursor: pointer;
  transition: color 0.3s;
}

.breadcrumb .category-link:hover {
  color: #409eff;
  text-decoration: underline;
}

.breadcrumb .current {
  color: #333;
  font-weight: 500;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 文章标题 */
.article-title {
  font-size: 2.2rem;
  font-weight: 700;
  line-height: 1.3;
  color: #1a1a1a;
  margin-bottom: 1.2rem;
  text-align: left;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

/* 文章信息（标题下面） */
.article-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 1.5rem;
  padding-bottom: 1.2rem;
  border-bottom: 1px solid #f0f0f0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 14px;
}

.info-item .el-icon {
  color: #999;
}

/* 文章标签 */
.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  align-items: center;
}

.category-tag,
.tag-item {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 16px;
  padding: 8px 16px;
  font-weight: 500;
}

.category-tag:hover,
.tag-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.no-tags {
  color: #999;
  font-size: 14px;
}

.no-tags-text {
  font-style: italic;
}

/* 文章内容 */
.article-content {
  line-height: 1.8;
  color: #333;
  font-size: 16px;
  font-family: "Georgia", "Times New Roman", serif;
}

.article-content :deep(h1) {
  font-size: 1.8rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 2rem 0 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #409eff;
}

.article-content :deep(h2) {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin: 1.8rem 0 0.8rem;
}

.article-content :deep(h3) {
  font-size: 1.3rem;
  font-weight: 600;
  color: #555;
  margin: 1.5rem 0 0.5rem;
}

.article-content :deep(p) {
  margin: 1.2rem 0;
  text-align: justify;
}

.article-content :deep(ul),
.article-content :deep(ol) {
  margin: 1rem 0;
  padding-left: 2em;
}

.article-content :deep(li) {
  margin: 0.5rem 0;
}

.article-content :deep(pre) {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  overflow: auto;
  margin: 1.5rem 0;
  border-left: 4px solid #409eff;
}

.article-content :deep(code) {
  background: #f6f8fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: "Consolas", "Monaco", "Courier New", monospace;
  font-size: 14px;
  color: #e83e8c;
}

.article-content :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding: 1rem 1.5rem;
  margin: 1.5rem 0;
  color: #666;
  font-style: italic;
  background: #f8f9fa;
  border-radius: 0 8px 8px 0;
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1rem 0;
}

/* 文章底部 */
.article-footer {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  color: #666;
  font-size: 14px;
}

.update-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #999;
}

.copyright {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  text-align: center;
}

.copyright p {
  color: #999;
  font-size: 13px;
  margin: 0;
}

/* 互动操作 */
.interaction-actions {
  margin: 40px 0;
  display: flex;
  gap: 20px;
  justify-content: center;
}

.action-btn,
.edit-btn {
  min-width: 140px;
  height: 48px;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 500;
}

/* 作者卡片 */
.author-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.author-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.author-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.author-header {
  text-align: center;
  margin-bottom: 20px;
  cursor: pointer;
}

.author-avatar-large {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  margin: 0 auto 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: bold;
  overflow: hidden;
  border: 4px solid white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  cursor: pointer;
}

.author-avatar-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-name {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
  transition: color 0.3s;
  cursor: pointer;
}

.author-card:hover .author-name {
  color: #409eff;
}

.author-bio {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 0;
}

.author-stats {
  display: flex;
  justify-content: space-around;
  margin: 20px 0;
  padding: 20px 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}

.stat-item {
  text-align: center;
  cursor: default;
}

.stat-number {
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.follow-btn {
  width: 100%;
  height: 40px;
  border-radius: 20px;
  font-weight: 500;
}

/* 目录卡片 */
.toc-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 20px;
}

/* 目录导航 */
.toc-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  margin-bottom: 15px;
  color: #333;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.toc-content {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 8px;
}

.toc-item {
  padding: 10px 0;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  line-height: 1.5;
  border-left: 2px solid transparent;
  padding-left: 10px;
  font-size: 14px;
}

.toc-item:hover {
  color: #409eff;
  border-left-color: #409eff;
  padding-left: 15px;
}

.toc-level-2 {
  padding-left: 20px;
}
.toc-level-3 {
  padding-left: 30px;
}
.toc-level-4 {
  padding-left: 40px;
}

/* 文章不存在 */
.not-found-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  background: white;
  border-radius: 12px;
  margin: 20px;
}

.error-content {
  text-align: center;
  padding: 40px;
}

.error-content h1 {
  font-size: 2rem;
  color: #333;
  margin: 20px 0 10px;
}

.error-content p {
  font-size: 16px;
  color: #666;
  margin-bottom: 30px;
}

/* 评论区域 */
.comments-section {
  margin-top: 60px;
  padding-top: 40px;
  border-top: 1px solid #e8e8e8;
}

.comments-wrapper {
  width: 100%;
}

.comments-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  margin-bottom: 30px;
  color: #333;
  font-weight: 600;
}

/* 评论表单 */
.comment-form-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 30px;
  border: 1px solid #e8e8e8;
}

.form-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  overflow: hidden;
  flex-shrink: 0;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.form-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.comment-textarea {
  margin-bottom: 20px;
}

.comment-textarea :deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  padding: 12px;
  line-height: 1.6;
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 登录提示样式 */
.login-prompt {
  text-align: center;
  padding: 30px;
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 30px;
  border: 1px solid #e9ecef;
}

.login-prompt p {
  color: #666;
  margin: 0;
}

.login-prompt a {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}

.login-prompt a:hover {
  text-decoration: underline;
}

/* 评论列表 */
.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.comment-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s;
  padding: 4px;
  border-radius: 6px;
}

.comment-author:hover {
  background: rgba(64, 158, 255, 0.1);
}

.comment-author:hover .comment-author-name {
  color: #409eff;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  overflow: hidden;
  flex-shrink: 0;
  border: 2px solid white;
}

.comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-author-info {
  flex: 1;
}

.comment-author-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
  transition: color 0.3s;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.comment-actions .el-button {
  font-size: 12px;
  padding: 0 6px;
  color: #999;
}

.comment-actions .el-button:hover {
  color: #409eff;
}

.comment-content {
  line-height: 1.6;
  color: #333;
  margin-bottom: 15px;
  font-size: 14px;
  white-space: pre-wrap;
}

.comment-footer {
  display: flex;
  gap: 20px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.comment-footer .el-button {
  color: #999;
  font-size: 13px;
}

.comment-footer .liked {
  color: #409eff;
}

/* 未登录时的按钮样式 */
.action-btn:disabled,
.comment-footer .el-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.comment-footer .el-button:disabled {
  color: #c0c4cc;
}

.no-comments {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.no-comments p {
  margin-top: 15px;
  font-size: 14px;
}

/* 头像占位符 */
.avatar-placeholder-large,
.avatar-placeholder-small {
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.avatar-placeholder-large {
  font-size: 36px;
}
.avatar-placeholder-small {
  font-size: 14px;
}

/* 登录弹窗样式 */
:deep(.el-dialog__header) {
  padding: 20px 20px 10px;
}

:deep(.el-dialog__body) {
  padding: 10px 20px 20px;
}

/* 点赞动画样式 */
.like-animation {
  animation: like-pulse 0.5s ease-in-out;
}

@keyframes like-pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

/* 点赞数变化动画 */
.like-count-animation {
  animation: count-bounce 0.3s ease-in-out;
}

@keyframes count-bounce {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .article-body {
    grid-template-columns: 250px 1fr;
    gap: 20px;
  }

  .right-sidebar {
    display: none;
  }
}

@media (max-width: 992px) {
  .article-body {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .left-sidebar,
  .right-sidebar {
    display: block; /* 显示在移动端 */
    position: static;
    margin-bottom: 20px;
  }

  .author-card {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 20px;
  }

  .author-header {
    display: flex;
    align-items: center;
    gap: 15px;
    text-align: left;
    margin-bottom: 0;
    flex: 1;
  }

  .author-avatar-large {
    width: 60px;
    height: 60px;
    font-size: 24px;
    margin: 0;
  }

  .author-info {
    text-align: left;
    flex: 1;
  }

  .author-name {
    font-size: 18px;
    margin-bottom: 4px;
  }

  .author-bio {
    -webkit-line-clamp: 2;
    line-clamp: 2;
    max-height: 40px;
  }

  .author-stats {
    border: none;
    margin: 0;
    padding: 0;
    flex: 1;
    justify-content: space-evenly;
  }

  .follow-btn {
    width: auto;
    min-width: 120px;
  }

  .main-content {
    padding: 25px;
  }

  .article-title {
    font-size: 1.8rem;
  }
}

@media (max-width: 768px) {
  .author-card {
    flex-direction: column;
    text-align: center;
  }

  .author-header {
    flex-direction: column;
    text-align: center;
  }

  .author-info {
    text-align: center;
  }

  .author-stats {
    width: 100%;
    border-top: 1px solid #eee;
    border-bottom: 1px solid #eee;
    padding: 15px 0;
    margin: 15px 0;
  }

  .follow-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .article-title {
    font-size: 1.3rem;
  }

  .main-content {
    padding: 16px;
  }
}
</style>