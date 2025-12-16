package com.blog.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 传统Servlet示例（用于向老师展示）
 */
@WebServlet("/traditional/login")
public class TraditionalLoginServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter out = resp.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("    <title>传统Servlet登录</title>");
        out.println("    <style>");
        out.println("        body { font-family: Arial; padding: 20px; }");
        out.println("        .container { max-width: 400px; margin: 0 auto; }");
        out.println("        .form-group { margin-bottom: 15px; }");
        out.println("        label { display: block; margin-bottom: 5px; }");
        out.println("        input { width: 100%; padding: 8px; }");
        out.println("        button { padding: 10px 20px; background: #007bff; color: white; border: none; }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <div class='container'>");
        out.println("        <h2>传统Servlet登录页面</h2>");
        out.println("        <form method='post'>");
        out.println("            <div class='form-group'>");
        out.println("                <label>用户名：</label>");
        out.println("                <input type='text' name='username' required>");
        out.println("            </div>");
        out.println("            <div class='form-group'>");
        out.println("                <label>密码：</label>");
        out.println("                <input type='password' name='password' required>");
        out.println("            </div>");
        out.println("            <button type='submit'>登录</button>");
        out.println("        </form>");
        out.println("        <p style='margin-top: 20px;'>");
        out.println("            <strong>说明：</strong>这是一个传统的Servlet示例，" +
                    "展示了JavaWeb课程中学到的技术。");
        out.println("        </p>");
        out.println("    </div>");
        out.println("</body>");
        out.println("</html>");
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();
        
        // 获取参数（传统方式）
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        
        // 简单验证（实际应该查询数据库）
        if ("admin".equals(username) && "123456".equals(password)) {
            // 传统Session管理
            HttpSession session = req.getSession();
            session.setAttribute("user", username);
            session.setAttribute("loginTime", System.currentTimeMillis());
            
            // 设置Session超时时间（30分钟）
            session.setMaxInactiveInterval(30 * 60);
            
            out.write("{\"code\":200, \"message\":\"登录成功\", \"data\":{\"username\":\"" + username + "\"}}");
        } else {
            out.write("{\"code\":400, \"message\":\"用户名或密码错误\"}");
        }
    }
}