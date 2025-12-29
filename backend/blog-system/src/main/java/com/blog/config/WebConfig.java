package com.blog.config;

import com.blog.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // 原有排除路径
                        "/api/user/login",
                        "/api/user/register",
                        "/api/test/**",
                        "/api/debug/**",
                        "/api/articles/**",
                        "/api/categories/**",
                        "/api/tags/**",
                        "/api/archives/**",
                        "/api/comments/article/**",
                        "/api/user/public/**",
                        "/api/search/**",
                        "/api/files/upload",
                        "/api/files/upload/**",
                        "/api/files/editor/upload",
                        "/api/avatar/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("🚀 配置静态资源映射...");
        
        // 获取项目根目录
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path uploadsPath = projectRoot.resolve("uploads");
        Path avatarsPath = uploadsPath.resolve("avatars");
        Path staticPath = projectRoot.resolve("src/main/resources/static");

        System.out.println("📁 项目根目录: " + projectRoot.toString());
        System.out.println("📁 上传目录: " + uploadsPath.toString());
        System.out.println("📁 静态资源目录: " + staticPath.toString());

        // 确保上传目录存在
        try {
            Files.createDirectories(avatarsPath);
            System.out.println("✅ 上传目录已创建或已存在");
        } catch (IOException e) {
            System.err.println("⚠️ 无法创建上传目录: " + e.getMessage());
        }

        // **关键修复：正确配置静态资源映射**
        
        // 1. 映射 /uploads/** 到上传目录
        String uploadsLocation;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Windows路径
            uploadsLocation = "file:///" + uploadsPath.toString().replace("\\", "/") + "/";
        } else {
            // Unix/Linux路径
            uploadsLocation = "file:" + uploadsPath.toString() + "/";
        }
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsLocation)
                .setCachePeriod(0);
        
        System.out.println("✅ 映射: /uploads/** -> " + uploadsLocation);

        // 2. 映射 /static/** 到 classpath:/static/
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);
        
        System.out.println("✅ 映射: /static/** -> classpath:/static/");

        // 3. 单独映射 /uploads/avatars/** 确保头像能访问
        String avatarsLocation;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            avatarsLocation = "file:///" + avatarsPath.toString().replace("\\", "/") + "/";
        } else {
            avatarsLocation = "file:" + avatarsPath.toString() + "/";
        }
        
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(avatarsLocation)
                .setCachePeriod(0);
        
        System.out.println("✅ 映射: /uploads/avatars/** -> " + avatarsLocation);

        System.out.println("✅ 静态资源映射配置完成");
    }
}