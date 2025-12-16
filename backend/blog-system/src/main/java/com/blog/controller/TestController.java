package com.blog.controller;

import com.blog.common.Result;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器（验证后端是否正常工作）
 */
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    /**
     * 测试后端是否启动成功
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "running");
        data.put("service", "Blog System Backend");
        data.put("version", "1.0.0");
        data.put("timestamp", System.currentTimeMillis());
        return Result.success("后端服务正常运行", data);
    }
    
    /**
     * 测试异常处理
     */
    @GetMapping("/exception")
    public Result<String> testException() {
        // 故意制造异常，测试全局异常处理
        throw new RuntimeException("这是一个测试异常");
    }
    
    /**
     * 测试传统Session
     */
    @GetMapping("/session")
    public Result<Map<String, Object>> testSession(HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();
        
        // 获取Session（传统方式）
        HttpSession session = request.getSession();
        
        data.put("sessionId", session.getId());
        data.put("creationTime", session.getCreationTime());
        data.put("lastAccessedTime", session.getLastAccessedTime());
        data.put("maxInactiveInterval", session.getMaxInactiveInterval());
        
        // 设置一个测试属性
        session.setAttribute("testKey", "testValue");
        data.put("testValue", session.getAttribute("testKey"));
        
        return Result.success("Session测试成功", data);
    }
}