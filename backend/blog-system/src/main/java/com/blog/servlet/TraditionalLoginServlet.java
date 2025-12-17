package com.blog.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

// 添加必要的import
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.blog.entity.User;
import com.blog.service.UserService;

/**
 * 传统Servlet示例（用于向老师展示Jakarta EE技术）
 * 技术要点：
 * 1. @WebServlet注解配置
 * 2. HttpServletRequest/Response使用
 * 3. HttpSession会话管理
 * 4. 与Spring Boot整合（获取UserService）
 */
@WebServlet("/traditional/login")
public class TraditionalLoginServlet extends HttpServlet {
    
    private UserService userService;
    
    /**
     * 初始化方法：获取Spring容器中的UserService
     */
    @Override
    public void init() throws ServletException {
        super.init();
        // 通过ServletContext获取Spring ApplicationContext
        ServletContext servletContext = getServletContext();
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        
        if (ctx != null) {
            userService = ctx.getBean(UserService.class);
            System.out.println("✅ TraditionalLoginServlet初始化成功，已获取UserService Bean");
        } else {
            System.err.println("❌ 无法获取Spring ApplicationContext");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter out = resp.getWriter();
        
        // 检查是否有action参数
        String action = req.getParameter("action");
        
        if ("techDemo".equals(action)) {
            // 显示技术演示页面
            showTechDemoPage(out);
        } else {
            // 显示普通登录页面
            showLoginPage(out);
        }
    }
    
    /**
     * 显示技术演示页面
     */
    private void showTechDemoPage(PrintWriter out) {
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("    <title>Jakarta EE Servlet技术演示</title>");
        out.println("    <style>");
        out.println("        body { font-family: 'Segoe UI', Arial, sans-serif; padding: 20px; background: #f5f7fa; }");
        out.println("        .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 5px 15px rgba(0,0,0,0.1); }");
        out.println("        h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }");
        out.println("        h2 { color: #34495e; }");
        out.println("        .tech-point { background: #f8f9fa; padding: 15px; margin: 10px 0; border-left: 4px solid #3498db; }");
        out.println("        .code { background: #2c3e50; color: #ecf0f1; padding: 10px; border-radius: 5px; font-family: 'Courier New', monospace; }");
        out.println("        .login-form { background: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0; }");
        out.println("        .form-group { margin-bottom: 15px; }");
        out.println("        label { display: block; margin-bottom: 5px; color: #2c3e50; }");
        out.println("        input { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; }");
        out.println("        button { padding: 10px 20px; background: #3498db; color: white; border: none; border-radius: 4px; cursor: pointer; }");
        out.println("        button:hover { background: #2980b9; }");
        out.println("        .test-accounts { background: #e8f4fc; padding: 15px; border-radius: 5px; margin: 20px 0; }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <div class='container'>");
        out.println("        <h1>🚀 Jakarta EE Servlet技术演示</h1>");
        out.println("        <p><strong>技术栈：</strong>Spring Boot 3.x + Jakarta EE 10 + MySQL + MyBatis</p>");
        
        out.println("        <div class='tech-point'>");
        out.println("            <h2>🔧 核心技术点展示</h2>");
        out.println("            <ul>");
        out.println("                <li><strong>@WebServlet注解</strong> - 声明Servlet组件，替代web.xml配置</li>");
        out.println("                <li><strong>HttpServletRequest</strong> - 获取请求参数：req.getParameter()</li>");
        out.println("                <li><strong>HttpServletResponse</strong> - 设置响应：resp.setContentType()</li>");
        out.println("                <li><strong>HttpSession</strong> - 会话管理：req.getSession()</li>");
        out.println("                <li><strong>Servlet与Spring整合</strong> - 通过ServletContext获取Spring Bean</li>");
        out.println("                <li><strong>Filter过滤器</strong> - 权限控制（见AuthFilter类）</li>");
        out.println("            </ul>");
        out.println("        </div>");
        
        out.println("        <div class='tech-point'>");
        out.println("            <h2>💻 代码示例</h2>");
        out.println("            <div class='code'>");
        out.println("@WebServlet(\"/traditional/login\")<br>");
        out.println("public class TraditionalLoginServlet extends HttpServlet {<br>");
        out.println("    private UserService userService;<br><br>");
        out.println("    public void init() {<br>");
        out.println("        // 获取Spring Bean<br>");
        out.println("        ApplicationContext ctx = WebApplicationContextUtils<br>");
        out.println("            .getWebApplicationContext(getServletContext());<br>");
        out.println("        userService = ctx.getBean(UserService.class);<br>");
        out.println("    }<br><br>");
        out.println("    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {<br>");
        out.println("        String username = req.getParameter(\"username\");<br>");
        out.println("        String password = req.getParameter(\"password\");<br>");
        out.println("        // 调用Spring Service验证<br>");
        out.println("        User user = userService.login(username, password);<br>");
        out.println("        // Session管理<br>");
        out.println("        HttpSession session = req.getSession();<br>");
        out.println("        session.setAttribute(\"user\", user);<br>");
        out.println("    }<br>");
        out.println("}");
        out.println("            </div>");
        out.println("        </div>");
        
        out.println("        <div class='login-form'>");
        out.println("            <h2>📝 登录测试</h2>");
        out.println("            <form method='post'>");
        out.println("                <div class='form-group'>");
        out.println("                    <label>用户名：</label>");
        out.println("                    <input type='text' name='username' value='admin' required>");
        out.println("                </div>");
        out.println("                <div class='form-group'>");
        out.println("                    <label>密码：</label>");
        out.println("                    <input type='password' name='password' value='123456' required>");
        out.println("                </div>");
        out.println("                <button type='submit'>传统Servlet登录</button>");
        out.println("            </form>");
        out.println("        </div>");
        
        out.println("        <div class='test-accounts'>");
        out.println("            <h3>👥 测试账号</h3>");
        out.println("            <ul>");
        out.println("                <li><strong>管理员：</strong>admin / 123456</li>");
        out.println("                <li><strong>普通用户：</strong>zhangsan / 123456</li>");
        out.println("                <li><strong>普通用户：</strong>lisi / 123456</li>");
        out.println("            </ul>");
        out.println("        </div>");
        
        out.println("        <div>");
        out.println("            <h3>🔗 相关链接</h3>");
        out.println("            <p>");
        out.println("                <a href='/traditional/login'>返回普通登录页</a> | ");
        out.println("                <a href='/api/test/db-test' target='_blank'>测试数据库连接</a> | ");
        out.println("                <a href='/api/articles' target='_blank'>查看文章API</a>");
        out.println("            </p>");
        out.println("        </div>");
        
        out.println("    </div>");
        out.println("</body>");
        out.println("</html>");
    }
    
    /**
     * 显示普通登录页面
     */
    private void showLoginPage(PrintWriter out) {
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
        out.println("        .error { color: red; margin-top: 10px; }");
        out.println("        .success { color: green; margin-top: 10px; }");
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
        out.println("        <p><a href='?action=techDemo'>查看详细技术演示</a></p>");
        out.println("        <p>测试账号：admin / 123456</p>");
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
        
        System.out.println("📝 传统Servlet登录请求 - 用户名: " + username + ", 时间: " + new Date());
        
        try {
            // 验证参数
            if (username == null || username.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                out.write("{\"code\":400, \"message\":\"用户名和密码不能为空\"}");
                return;
            }
            
            // 方式1：如果UserService可用，使用数据库验证
            if (userService != null) {
                System.out.println("🔍 使用UserService进行数据库验证");
                
                try {
                    // 调用Spring的UserService进行验证
                    User user = userService.login(username, password);
                    
                    if (user != null) {
                        // 传统Session管理
                        HttpSession session = req.getSession();
                        session.setAttribute("user", user);
                        session.setAttribute("username", user.getUsername());
                        session.setAttribute("userId", user.getId());
                        session.setAttribute("loginMethod", "traditional-servlet");
                        session.setAttribute("loginTime", new Date());
                        
                        // 设置Session超时时间（30分钟）
                        session.setMaxInactiveInterval(30 * 60);
                        
                        System.out.println("✅ 用户 " + username + " 登录成功 (数据库验证)");
                        
                        String jsonResponse = String.format(
                            "{\"code\":200, \"message\":\"登录成功（数据库验证）\", " +
                            "\"data\":{\"id\":%d, \"username\":\"%s\", \"role\":%d}, " +
                            "\"session\":{\"id\":\"%s\", \"timeout\":%d}}",
                            user.getId(), user.getUsername(), user.getRole(),
                            session.getId(), session.getMaxInactiveInterval()
                        );
                        out.write(jsonResponse);
                    } else {
                        out.write("{\"code\":401, \"message\":\"用户名或密码错误（数据库验证）\"}");
                    }
                } catch (Exception e) {
                    System.err.println("❌ UserService登录异常: " + e.getMessage());
                    out.write("{\"code\":500, \"message\":\"登录服务异常: " + e.getMessage() + "\"}");
                }
            } 
            // 方式2：如果UserService不可用，使用模拟验证（降级处理）
            else {
                System.out.println("⚠️ UserService不可用，使用模拟验证");
                
                // 模拟验证（实际应该查询数据库）
                if ("admin".equals(username) && "123456".equals(password)) {
                    HttpSession session = req.getSession();
                    session.setAttribute("user", username);
                    session.setAttribute("loginTime", System.currentTimeMillis());
                    
                    // 设置Session超时时间（30分钟）
                    session.setMaxInactiveInterval(30 * 60);
                    
                    System.out.println("✅ 用户 " + username + " 登录成功 (模拟验证)");
                    
                    out.write("{\"code\":200, \"message\":\"登录成功（模拟验证）\", \"data\":{\"username\":\"" + username + "\"}}");
                } else {
                    out.write("{\"code\":401, \"message\":\"用户名或密码错误（模拟验证）\"}");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ 传统Servlet登录异常: " + e.getMessage());
            e.printStackTrace();
            
            out.write("{\"code\":500, \"message\":\"系统异常，请稍后重试\"}");
        }
    }
    
    /**
     * 销毁方法
     */
    @Override
    public void destroy() {
        System.out.println("🔚 TraditionalLoginServlet销毁");
        super.destroy();
    }
}