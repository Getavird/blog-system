package com.blog.controller;

import com.blog.common.Result;
import com.blog.dao.UserMapper;
import com.blog.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
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
        @Autowired
    private UserMapper userMapper;
    

        @GetMapping("/hello")
    public String hello() {
        return "{\"message\": \"后端服务正常，当前时间: " + new java.util.Date() + "\"}";
    }
    
    @GetMapping("/db")
    public String checkDB() {
        // 简单的数据库连接测试
        return "{\"status\": \"数据库连接正常\"}";
    }


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
     * 测试数据库连接（基础测试）
     */
    @GetMapping("/db/connection")
    public Result<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试1：执行简单的SELECT 1
            int testResult = userMapper.testConnection();
            result.put("select1", testResult);
            result.put("select1_status", testResult == 1 ? "✅ 成功" : "❌ 失败");
            
            // 测试2：获取数据库名称
            String dbName = userMapper.getDatabaseName();
            result.put("database_name", dbName);
            result.put("database_status", dbName != null ? "✅ 成功" : "❌ 失败");
            
            // 测试3：检查user表是否存在
            int tableExists = userMapper.checkUserTableExists();
            result.put("user_table_exists", tableExists);
            result.put("table_status", tableExists > 0 ? "✅ 存在" : "❌ 不存在");
            
            // 测试4：统计用户数量
            if (tableExists > 0) {
                int userCount = userMapper.countUsers();
                result.put("user_count", userCount);
                result.put("count_status", "✅ 成功");
            } else {
                result.put("user_count", 0);
                result.put("count_status", "⚠️ 跳过（表不存在）");
            }
            
            result.put("overall_status", "✅ 数据库连接测试成功");
            return Result.success("数据库连接测试完成", result);
            
        } catch (Exception e) {
            result.put("overall_status", "❌ 数据库连接失败");
            result.put("error", e.getMessage());
            result.put("error_class", e.getClass().getName());
            
            // 修复这里：创建一个Result对象，然后设置data
            Result<Map<String, Object>> errorResult = Result.error("数据库连接失败：" + e.getMessage());
            errorResult.setData(result);
            return errorResult;
        }
    }
    
    /**
     * 测试查询具体数据
     */
    @GetMapping("/db/data")
    public Result<Map<String, Object>> testDatabaseData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试查询admin用户
            User adminUser = userMapper.findByUsername("admin");
            
            if (adminUser != null) {
                result.put("admin_user_found", true);
                result.put("admin_id", adminUser.getId());
                result.put("admin_username", adminUser.getUsername());
                result.put("admin_email", adminUser.getEmail());
                result.put("admin_role", adminUser.getRole());
                result.put("status", "✅ 成功找到admin用户");
            } else {
                result.put("admin_user_found", false);
                result.put("status", "⚠️ admin用户不存在");
            }
            
            // 测试查询testuser用户
            User testUser = userMapper.findByUsername("testuser");
            if (testUser != null) {
                result.put("testuser_found", true);
                result.put("testuser_id", testUser.getId());
            } else {
                result.put("testuser_found", false);
            }
            
            return Result.success("数据查询测试完成", result);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            
            // 修复这里：创建一个Result对象，然后设置data
            Result<Map<String, Object>> errorResult = Result.error("数据查询失败：" + e.getMessage());
            errorResult.setData(result);
            return errorResult;
        }
    }
    
    /**
     * 详细的数据库诊断
     */
    @GetMapping("/db/diagnose")
    public Result<Map<String, Object>> diagnoseDatabase() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查数据库基本信息
            result.put("timestamp", System.currentTimeMillis());
            result.put("test_url", "/api/test/db/diagnose");
            
            // 1. 检查连接
            try {
                int connectionTest = userMapper.testConnection();
                result.put("connection_test", connectionTest == 1 ? "✅ 通过" : "❌ 失败");
            } catch (Exception e) {
                result.put("connection_test", "❌ 失败: " + e.getMessage());
            }
            
            // 2. 检查表
            try {
                int tableCount = userMapper.checkUserTableExists();
                result.put("user_table", tableCount > 0 ? "✅ 存在" : "❌ 不存在");
            } catch (Exception e) {
                result.put("user_table", "❌ 检查失败: " + e.getMessage());
            }
            
            // 3. 检查数据
            try {
                int userCount = userMapper.countUsers();
                result.put("user_count", userCount);
                result.put("data_status", userCount > 0 ? "✅ 有数据" : "⚠️ 无数据");
            } catch (Exception e) {
                result.put("data_status", "❌ 查询失败: " + e.getMessage());
            }
            
            // 4. 尝试查询具体用户
            try {
                User user = userMapper.findByUsername("admin");
                result.put("admin_exists", user != null);
                if (user != null) {
                    result.put("admin_info", user.getUsername() + " (ID: " + user.getId() + ")");
                }
            } catch (Exception e) {
                result.put("admin_query", "❌ 失败: " + e.getMessage());
            }
            
            result.put("diagnosis_complete", true);
            return Result.success("数据库诊断完成", result);
            
        } catch (Exception e) {
            result.put("diagnosis_complete", false);
            result.put("error", e.getMessage());
            
            // 修复这里：创建一个Result对象，然后设置data
            Result<Map<String, Object>> errorResult = Result.error("诊断过程出错");
            errorResult.setData(result);
            return errorResult;
        }
    }
    
    /**
     * 测试异常处理
     */
    @GetMapping("/exception")
    public Result<String> testException() {
        throw new RuntimeException("这是一个测试异常");
    }
    
    /**
     * 测试Session功能
     */
    @GetMapping("/session")
    public Result<Map<String, Object>> testSession(HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();
        
        // 获取Session
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
    @GetMapping("/ping")
    public String ping() {
    System.out.println("Ping接口被调用，时间：" + new java.util.Date());
    return "PONG - " + new java.util.Date();
}

}