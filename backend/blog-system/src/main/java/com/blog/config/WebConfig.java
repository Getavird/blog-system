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
                        // ... 现有的排除路径 ...
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

                        // 新增：排除文件上传和头像相关接口
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
        Path avatarsPath = projectRoot.resolve("uploads").resolve("avatars");

        System.out.println("📁 项目根目录: " + projectRoot.toString());
        System.out.println("📁 上传目录: " + uploadsPath.toString());
        System.out.println("📁 上传目录是否存在: " + Files.exists(uploadsPath));

        // 确保上传目录存在
        try {
            Files.createDirectories(avatarsPath);
            System.out.println("✅ 上传目录已创建或已存在");

            // 列出目录内容（调试用）
            try (var stream = Files.walk(uploadsPath, 2)) {
                System.out.println("📁 上传目录结构:");
                stream.forEach(path -> {
                    try {
                        String relativePath = uploadsPath.relativize(path).toString();
                        if (!relativePath.isEmpty()) {
                            boolean isDir = Files.isDirectory(path);
                            String info = isDir ? "[目录]" : "[" + Files.size(path) + " bytes]";
                            System.out.println("  " + "  ".repeat(relativePath.split("\\\\").length - 1) +
                                    "├─ " + path.getFileName() + " " + info);
                        }
                    } catch (IOException e) {
                        // 忽略
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("⚠️ 无法创建上传目录: " + e.getMessage());
        }

        // 关键修复：使用 file:// 协议和正确的路径格式
        String resourceLocation;

        // Windows系统需要特殊处理
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Windows路径：file:///D:/project/uploads/
            String winPath = uploadsPath.toString().replace("\\", "/");
            if (!winPath.startsWith("/")) {
                winPath = "/" + winPath;
            }
            resourceLocation = "file://" + winPath + "/";
        } else {
            // Unix/Linux路径：file:/home/project/uploads/
            resourceLocation = "file:" + uploadsPath.toString() + "/";
        }

        System.out.println("🔗 资源处理器: /uploads/**");
        System.out.println("🔗 资源位置: " + resourceLocation);

        // 清除缓存配置，避免缓存问题
        registry.addResourceHandler("/uploads/avatars/**")
            .addResourceLocations("file:" + avatarsPath.toString().replace("\\", "/") + "/")
                .setCachePeriod(0) // 开发环境设为0，避免缓存
                .resourceChain(true);

        System.out.println("✅ 静态资源映射配置完成");
    }
}