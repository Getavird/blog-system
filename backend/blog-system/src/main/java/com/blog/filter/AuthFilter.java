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
        "/api/search/**"
    };
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化代码（如果有）
    }
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        
        // 转换为 HttpServletRequest/HttpServletResponse
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 检查是否在排除列表中
        for (String path : EXCLUDE_PATHS) {
            if (path.endsWith("/**") && uri.startsWith(path.replace("/**", ""))) {
                chain.doFilter(request, response);
                return;
            }
            if (uri.equals(path)) {
                chain.doFilter(request, response);
                return;
            }
        }
        
        // 特殊处理：GET请求可能不需要登录
        if ("GET".equalsIgnoreCase(method)) {
            // 检查是否是公开资源的GET请求（可以进一步细化）
            if (uri.startsWith("/api/user/public/") || 
                uri.startsWith("/api/user/profile/") ||
                uri.startsWith("/api/comments/article/") ||
                uri.startsWith("/api/comment/article/")) {
                chain.doFilter(request, response);
                return;
            }
        }
        
        // 检查登录状态
        if (!SessionUtil.isLogin(request)) {
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
            // 记录错误但不中断请求
            System.err.println("❌ 更新用户活动时间失败: " + e.getMessage());
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        // 清理资源
    }
}