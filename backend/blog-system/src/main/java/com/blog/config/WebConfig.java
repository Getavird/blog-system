package com.blog.config;

import com.blog.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
        // 注意：我们使用 "./uploads/" 配置，这里映射到 "/uploads/**"
        String normalizedPath = "./uploads/";
        if (normalizedPath.startsWith("./")) {
            normalizedPath = normalizedPath.substring(2);
        }
        if (!normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }

        // 映射 /uploads/** 到文件系统路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + normalizedPath);

        // 2. 配置静态资源（可选，保留现有功能）
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "classpath:/public/");
    }
}