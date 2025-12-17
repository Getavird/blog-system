package com.blog.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletResponse;

import javax.sql.DataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class DatabaseTest implements CommandLineRunner {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 开始测试数据库连接 ===");
        
        try (Connection conn = dataSource.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ 数据库连接成功！");
                System.out.println("🔗 连接URL: " + conn.getMetaData().getURL());
                System.out.println("🛠️  数据库产品: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("📊 数据库版本: " + conn.getMetaData().getDatabaseProductVersion());
                System.out.println("👤 用户名: " + conn.getMetaData().getUserName());
                
                // 测试查询
                testQuery(conn);
            }
        } catch (Exception e) {
            System.err.println("❌ 数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 数据库连接测试完成 ===\n");
    }
    
    private void testQuery(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
            
            System.out.println("📋 数据库中的表:");
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("   - " + rs.getString(1));
            }
            System.out.println("📈 总计: " + count + " 个表");
            
            // 查询数据库版本
            rs.close();
            try (ResultSet rs2 = stmt.executeQuery("SELECT VERSION()")) {
                if (rs2.next()) {
                    System.out.println("🔧 MySQL版本: " + rs2.getString(1));
                }
            }
            
        } catch (Exception e) {
            System.out.println("⚠️  查询测试失败: " + e.getMessage());
        }
    }
    @GetMapping("/db-test")
    public void dbTest(HttpServletResponse response) throws IOException {
        response.sendRedirect("/api/debug/db-test");
    }
}