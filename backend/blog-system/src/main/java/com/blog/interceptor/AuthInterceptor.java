package com.blog.interceptor;

import com.blog.utils.SessionUtil;
import com.blog.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    
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
        "/error"
    };
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        
        // 检查是否在排除列表中
        for (String path : EXCLUDE_PATHS) {
            if (path.endsWith("/**") && uri.startsWith(path.replace("/**", ""))) {
                return true;
            }
            if (uri.equals(path)) {
                return true;
            }
        }
        
        // 检查登录状态
        if (!SessionUtil.isLogin(request)) {
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
            return false;
        }
        
        // 更新用户最后活动时间
        try {
            Integer userId = SessionUtil.getCurrentUserId(request);
            if (userId != null) {
                userService.updateLastActive(userId);
            }
        } catch (Exception e) {
            // 记录错误但不中断请求
            System.err.println("❌ 更新用户活动时间失败: " + e.getMessage());
        }
        
        return true;
    }
}