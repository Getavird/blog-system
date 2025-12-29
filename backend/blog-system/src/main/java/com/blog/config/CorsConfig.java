package com.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 简化配置，避免重复
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000") // 使用 allowedOrigins 而不是 allowedOriginPatterns
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Set-Cookie") // 暴露Set-Cookie头部
                .allowCredentials(true)
                .maxAge(3600);
    }
}