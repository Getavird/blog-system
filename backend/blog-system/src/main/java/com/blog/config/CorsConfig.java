package com.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:3000")
                .allowedMethods("*")
                .allowCredentials(true)
                .maxAge(3600);

        registry.addMapping("/traditional/**")
                .allowedOriginPatterns("http://localhost:3000")
                .allowedMethods("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}