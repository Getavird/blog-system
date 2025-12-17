package com.blog.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/traditional/logout")
public class TraditionalLogoutServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();
        
        System.out.println("=== 传统Servlet退出登录 ===");
        
        HttpSession session = req.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("username");
            String sessionId = session.getId();
            
            // 手动移除所有属性（传统方式）
            session.removeAttribute("user");
            session.removeAttribute("username");
            session.removeAttribute("userId");
            session.removeAttribute("loginMethod");
            session.removeAttribute("loginTime");
            
            // 使Session失效（传统方式）
            session.invalidate();
            
            System.out.println("✅ 用户 " + username + " 已退出登录");
            System.out.println("✅ Session " + sessionId + " 已失效");
            
            out.write("{\"code\":200, \"message\":\"退出登录成功\"}");
        } else {
            out.write("{\"code\":400, \"message\":\"未登录\"}");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPost(req, resp);
    }
}