package com.blog.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 头像处理工具类
 */
public class AvatarUtil {
    
    // 允许的头像文件类型
    private static final String[] ALLOWED_EXTENSIONS = {
        "jpg", "jpeg", "png", "gif", "webp", "bmp"
    };
    
    // 最大头像文件大小：2MB
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;
    
    /**
     * 验证头像文件
     */
    public static boolean validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return false;
        }
        
        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(extension)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 生成新的头像文件名
     */
    public static String generateAvatarFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "avatar_" + uuid + "." + extension;
    }
    
    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
    
    /**
     * 保存头像文件
     */
    public static String saveAvatarFile(MultipartFile file, String uploadDir) throws IOException {
        // 确保上传目录存在
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 生成新文件名
        String newFilename = generateAvatarFileName(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(newFilename);
        
        // 保存文件
        file.transferTo(filePath.toFile());
        
        return newFilename;
    }
    
    /**
     * 删除旧头像文件
     */
    public static boolean deleteOldAvatar(String oldAvatarFilename, String uploadDir) {
        if (oldAvatarFilename == null || oldAvatarFilename.trim().isEmpty()) {
            return false;
        }
        
        // 如果是默认头像，不删除
        if (oldAvatarFilename.startsWith("default_") || oldAvatarFilename.equals("default_avatar.png")) {
            return true;
        }
        
        try {
            Path oldFilePath = Paths.get(uploadDir, oldAvatarFilename);
            if (Files.exists(oldFilePath)) {
                Files.delete(oldFilePath);
                return true;
            }
        } catch (Exception e) {
            System.err.println("删除旧头像文件失败: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * 获取头像URL
     */
    public static String getAvatarUrl(String filename, String urlPrefix) {
        if (filename == null || filename.trim().isEmpty()) {
            return urlPrefix + "default_avatar.png";
        }
        return urlPrefix + filename;
    }
    
    /**
     * 获取默认头像URL
     */
    public static String getDefaultAvatarUrl() {
    return "/images/default-avatars/default_avatar.png";
}
}