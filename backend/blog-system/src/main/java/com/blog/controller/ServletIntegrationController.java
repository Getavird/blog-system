// ServletIntegrationController.java
package com.blog.controller;

import com.blog.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/integration")
public class ServletIntegrationController {
    
    /**
     * 测试Servlet与Spring的Session共享
     */
    @GetMapping("/session-share")
    public Result<Map<String, Object>> testSessionShare(HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();
        
        // 获取当前Session
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            data.put("hasSession", true);
            data.put("sessionId", session.getId());
            
            // 检查是否包含Servlet设置的属性
            Object userFromServlet = session.getAttribute("user");
            data.put("hasServletUser", userFromServlet != null);
            
            if (userFromServlet != null) {
                data.put("servletUsername", session.getAttribute("username"));
                data.put("servletUserId", session.getAttribute("userId"));
                data.put("servletLoginMethod", session.getAttribute("loginMethod"));
            }
            
            // Spring端也设置一个属性
            session.setAttribute("springAttribute", "set_by_spring_" + System.currentTimeMillis());
            data.put("springAttribute", session.getAttribute("springAttribute"));
            
            data.put("message", "Servlet和Spring共享Session成功");
        } else {
            data.put("hasSession", false);
            data.put("message", "无活跃Session，请先登录");
        }
        
        return Result.success(data);
    }
    
    /**
     * 显示所有Session属性
     */
    @GetMapping("/session-dump")
    public Result<Map<String, Object>> dumpSession(HttpServletRequest request) {
        Map<String, Object> sessionData = new HashMap<>();
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            sessionData.put("sessionId", session.getId());
            sessionData.put("creationTime", session.getCreationTime());
            sessionData.put("lastAccessedTime", session.getLastAccessedTime());
            sessionData.put("maxInactiveInterval", session.getMaxInactiveInterval());
            
            // 获取所有属性名（传统方式无法直接获取，需要记录）
            Map<String, Object> attributes = new HashMap<>();
            
            // 尝试获取已知的属性
            String[] attributeNames = {"user", "username", "userId", "loginMethod", 
                                      "loginTime", "springAttribute"};
            
            for (String name : attributeNames) {
                Object value = session.getAttribute(name);
                if (value != null) {
                    if (value instanceof com.blog.entity.User) {
                        com.blog.entity.User user = (com.blog.entity.User) value;
                        attributes.put(name, user.getUsername() + " (ID: " + user.getId() + ")");
                    } else {
                        attributes.put(name, value.toString());
                    }
                }
            }
            
            sessionData.put("attributes", attributes);
            sessionData.put("attributeCount", attributes.size());
        }
        
        return Result.success(sessionData);
    }
}