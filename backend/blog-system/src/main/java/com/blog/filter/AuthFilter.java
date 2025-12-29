package com.blog.filter;

import com.blog.service.UserService;
import com.blog.utils.SessionUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthFilter implements Filter {

    @Autowired
    private UserService userService;

    // 不需要拦截的路径
    private static final String[] EXCLUDE_PATHS = {
            "/api/user/login",
            "/api/user/register",
            "/api/test/**",
            "/api/debug/**",
            "/traditional/**",
            "/static/**",
            "/error",
            // 添加文章相关的不需要登录的路径
            "/api/articles",
            "/api/articles/**",
            "/api/article/**",
            // 添加分类相关的不需要登录的路径
            "/api/categories",
            "/api/categories/**",
            // 添加标签相关的不需要登录的路径
            "/api/tags",
            "/api/tags/**",
            // 添加归档相关的不需要登录的路径
            "/api/archives",
            "/api/archives/**",
            // 添加评论相关的不需要登录的路径（查看评论不需要登录）
            "/api/comments/**",
            "/api/comment/**",
            // 添加用户公开信息路径
            "/api/user/public/**",
            "/api/user/profile/**",
            // 添加搜索路径
            "/api/search/**",
            // 添加上传文件访问路径
            "/uploads/**",
            "/api/user/avatar", // 如果前端还在用这个路径
            "/api/avatar/**", // 确保所有头像相关API都能正常访问
            "/api/files/**", // 确保文件上传API正常
            "/api/public/images/**" // 如果使用了备用方案
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化代码（如果有）
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 调试日志：显示所有请求
        System.out.println("🔍 AuthFilter - 请求: " + method + " " + uri);

        // 检查是否在排除列表中
        for (String path : EXCLUDE_PATHS) {
            // 处理通配符路径
            if (path.endsWith("/**")) {
                String basePath = path.replace("/**", "");
                if (uri.startsWith(basePath)) {
                    System.out.println("✅ AuthFilter - 跳过（匹配 " + path + "）");
                    chain.doFilter(request, response);
                    return;
                }
            } else if (uri.equals(path) || uri.startsWith(path + "/")) {
                System.out.println("✅ AuthFilter - 跳过（匹配 " + path + "）");
                chain.doFilter(request, response);
                return;
            }
        }

        // 检查登录状态
        if (!SessionUtil.isLogin(request)) {
            System.out.println("❌ AuthFilter - 需要登录: " + uri);
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
            return;
        }

        // 更新用户最后活动时间
        try {
            Integer userId = SessionUtil.getCurrentUserId(request);
            if (userId != null && userService != null) {
                userService.updateLastActive(userId);
            }
        } catch (Exception e) {
            System.err.println("❌ 更新用户活动时间失败: " + e.getMessage());
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 清理资源
    }
}