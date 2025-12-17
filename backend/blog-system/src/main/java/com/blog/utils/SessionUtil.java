package com.blog.utils;
import com.blog.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionUtil {
    
    public static final String SESSION_USER = "user";
    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USERNAME = "username";
    public static final String SESSION_ROLE = "role";
    
    /**
     * 获取当前登录用户
     */
    public static User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute(SESSION_USER);
    }
    
    /**
     * 获取当前用户ID
     */
    public static Integer getCurrentUserId(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user != null ? user.getId() : null;
    }
    
    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute(SESSION_USERNAME);
    }
    
    /**
     * 检查是否登录
     */
    public static boolean isLogin(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }
    
    /**
     * 检查是否是管理员
     */
    public static boolean isAdmin(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user != null && user.getRole() != null && user.getRole() == 1;
    }
}