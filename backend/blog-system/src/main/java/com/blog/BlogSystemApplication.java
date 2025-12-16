package com.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan  // 扫描Jakarta EE的Servlet、Filter注解
public class BlogSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogSystemApplication.class, args);
        System.out.println("==========================================");
        System.out.println("🚀 博客系统启动成功！（使用Jakarta EE）");
        System.out.println("📊 后端API地址：http://localhost:8080");
        System.out.println("📁 传统Servlet：http://localhost:8080/traditional/login");
        System.out.println("📁 数据库：blog_system");
        System.out.println("==========================================");
    }
}