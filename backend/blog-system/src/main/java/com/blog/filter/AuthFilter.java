package com.blog.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 传统Filter示例（用于向老师展示）
 */
@WebFilter("/*")
public class AuthFilter implements Filter {
    
    // 不需要过滤的路径
    private static final String[] EXCLUDE_URLS = {
        "/traditional/login", 
        "/login", 
        "/register", 
        "/api/user/login",
        "/api/user/register",
        "/static/",
        "/css/", 
        "/js/", 
        "/images/"
    };
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 添加CORS响应头
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");

        // 处理预检请求
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        
        // 检查是否在排除列表中
        for (String excludeUrl : EXCLUDE_URLS) {
            if (uri.startsWith(contextPath + excludeUrl)) {
                chain.doFilter(request, response);
                return;
            }
        }
        
        // 传统Session检查
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            // 如果是API请求，返回JSON错误
            if (uri.startsWith(contextPath + "/api/")) {
                resp.setContentType("application/json;charset=utf-8");
                resp.setStatus(401);
                resp.getWriter().write("{\"code\":401, \"message\":\"未登录，请先登录\"}");
                return;
            }
            // 否则重定向到登录页
            resp.sendRedirect(contextPath + "/traditional/login");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("权限验证过滤器初始化...");
    }
    
    @Override
    public void destroy() {
        System.out.println("权限验证过滤器销毁...");
    }
}