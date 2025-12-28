package com.blog.config;

import com.blog.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
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
                        "/api/user/login",
                        "/api/user/register",
                        "/api/test/**",
                        "/api/debug/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 配置上传文件的访问路径
        // 获取项目根目录
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path uploadsPath = projectRoot.resolve("uploads");
        
        System.out.println("📁 项目根目录: " + projectRoot.toString());
        System.out.println("📁 上传目录: " + uploadsPath.toString());
        
        // 确保上传目录存在
        try {
            Files.createDirectories(uploadsPath);
            System.out.println("✅ 上传目录已创建或已存在");
        } catch (IOException e) {
            System.err.println("⚠️ 无法创建上传目录: " + e.getMessage());
        }
        
        // 映射 /uploads/** 到文件系统路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsPath.toString() + File.separator);

        // 2. 配置静态资源（可选，保留现有功能）
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "classpath:/public/");
    }
}