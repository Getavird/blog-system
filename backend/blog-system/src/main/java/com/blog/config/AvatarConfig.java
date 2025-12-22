package com.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 头像上传配置
 */
@Configuration
public class AvatarConfig implements WebMvcConfigurer {
    
    @Value("${blog.upload.avatar-path:./uploads/avatars/}")
    private String avatarUploadPath;
    
    @Value("${blog.upload.avatar-url:/uploads/avatars/}")
    private String avatarUrlPath;
    
    /**
     * 获取头像上传目录的绝对路径
     */
    public String getAvatarUploadDir() {
        return avatarUploadPath;
    }
    
    /**
     * 获取头像访问URL前缀
     */
    public String getAvatarUrlPrefix() {
        return avatarUrlPath;
    }
    
    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(avatarUrlPath + "**")
                .addResourceLocations("file:" + avatarUploadPath);
    }
}