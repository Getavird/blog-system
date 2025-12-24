package com.blog.utils;

import com.blog.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionUtil {
    
    public static final String SESSION_USER = "currentUser";
    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USERNAME = "username";
    public static final String SESSION_ROLE = "role";
    
    /**
     * 检查用户是否登录
     */
    public static boolean isLogin(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }
    
    /**
     * 获取当前登录用户
     */
    public static User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute(SESSION_USER);
        }
        return null;
    }
    
    /**
     * 获取当前用户ID
     */
    public static Integer getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userIdObj = session.getAttribute(SESSION_USER_ID);
            if (userIdObj instanceof Integer) {
                return (Integer) userIdObj;
            }
        }
        return null;
    }
    
    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object usernameObj = session.getAttribute(SESSION_USERNAME);
            if (usernameObj instanceof String) {
                return (String) usernameObj;
            }
        }
        return null;
    }
    
    /**
     * 获取当前用户角色
     */
    public static Integer getCurrentUserRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object roleObj = session.getAttribute(SESSION_ROLE);
            if (roleObj instanceof Integer) {
                return (Integer) roleObj;
            }
        }
        return null;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user != null && user.getRole() != null && user.getRole() == 1;
    }
    
    /**
     * 设置用户登录信息到Session
     */
    public static void setLoginUser(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER, user);
        session.setAttribute(SESSION_USER_ID, user.getId());
        session.setAttribute(SESSION_USERNAME, user.getUsername());
        session.setAttribute(SESSION_ROLE, user.getRole());
    }
    
    /**
     * 清除用户登录信息
     */
    public static void clearLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(SESSION_USER);
            session.removeAttribute(SESSION_USER_ID);
            session.removeAttribute(SESSION_USERNAME);
            session.removeAttribute(SESSION_ROLE);
            session.invalidate();
        }
    }
}