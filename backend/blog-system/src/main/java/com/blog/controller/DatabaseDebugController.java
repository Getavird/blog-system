package com.blog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DatabaseDebugController {
    
    @Autowired
    private DataSource dataSource;
    
    @GetMapping("/db-status")
    public Map<String, Object> checkDatabase() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            result.put("status", "success");
            result.put("message", "数据库连接正常");
            
            Map<String, String> info = new HashMap<>();
            DatabaseMetaData metaData = conn.getMetaData();
            
            info.put("url", metaData.getURL());
            info.put("product", metaData.getDatabaseProductName());
            info.put("version", metaData.getDatabaseProductVersion());
            info.put("driver", metaData.getDriverName() + " " + metaData.getDriverVersion());
            info.put("username", metaData.getUserName());
            info.put("isValid", String.valueOf(conn.isValid(2)));
            
            result.put("databaseInfo", info);
            
            List<String> tables = getTableNames(conn);
            result.put("tableCount", tables.size());
            result.put("tables", tables);
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "数据库连接失败: " + e.getMessage());
            result.put("error", e.toString());
        }
        
        return result;
    }
    
    @GetMapping("/db-test")
    public String testConnection() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>数据库连接测试</title>");
        sb.append("<style>");
        sb.append("body { font-family: Arial, sans-serif; padding: 20px; }");
        sb.append(".success { background-color: #d4edda; padding: 15px; border-radius: 5px; }");
        sb.append(".error { background-color: #f8d7da; padding: 15px; border-radius: 5px; }");
        sb.append("h1 { color: #333; }");
        sb.append("h3 { color: #155724; }");
        sb.append("</style></head><body>");
        sb.append("<h1>数据库连接测试</h1>");
        
        try (Connection conn = dataSource.getConnection()) {
            sb.append("<div class='success'>");
            sb.append("<h3>✅ 数据库连接成功！</h3>");
            
            DatabaseMetaData metaData = conn.getMetaData();
            sb.append("<p><strong>URL:</strong> ").append(metaData.getURL()).append("</p>");
            sb.append("<p><strong>数据库:</strong> ").append(metaData.getDatabaseProductName()).append("</p>");
            sb.append("<p><strong>版本:</strong> ").append(metaData.getDatabaseProductVersion()).append("</p>");
            sb.append("<p><strong>驱动:</strong> ").append(metaData.getDriverName()).append(" ").append(metaData.getDriverVersion()).append("</p>");
            sb.append("<p><strong>用户:</strong> ").append(metaData.getUserName()).append("</p>");
            sb.append("<p><strong>连接状态:</strong> 有效 (").append(conn.isValid(2)).append(")</p>");
            sb.append("</div>");
            
            sb.append("<h3>数据库表列表:</h3>");
            List<String> tables = getTableNames(conn);
            sb.append("<ul>");
            for (String table : tables) {
                sb.append("<li>").append(table).append("</li>");
            }
            sb.append("</ul>");
            sb.append("<p>总计: ").append(tables.size()).append(" 个表</p>");
            
        } catch (Exception e) {
            sb.append("<div class='error'>");
            sb.append("<h3>❌ 数据库连接失败</h3>");
            sb.append("<p>").append(e.getMessage()).append("</p>");
            sb.append("</div>");
        }
        
        sb.append("<br><a href='/api/debug/db-status'>查看JSON格式</a>");
        sb.append("<br><a href='/api/test/status'>返回测试主页</a>");
        sb.append("</body></html>");
        
        return sb.toString();
    }
    
    private List<String> getTableNames(Connection conn) throws Exception {
        List<String> tables = new ArrayList<>();
        
        try (ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        
        return tables;
    }
}
