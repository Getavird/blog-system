package com.blog.utils;

import org.springframework.web.multipart.MultipartFile;

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
     * 保存头像文件 - 修复版
     */
    public static String saveAvatarFile(MultipartFile file, String uploadDir) throws IOException {
        // 处理相对路径
        String processedUploadDir = processUploadDir(uploadDir);
        System.out.println("📁 原始上传目录: " + uploadDir);
        System.out.println("📁 处理后上传目录: " + processedUploadDir);

        Path uploadPath = Paths.get(processedUploadDir);
        System.out.println("📁 绝对上传路径: " + uploadPath.toAbsolutePath());

        // 确保上传目录存在
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("✅ 创建上传目录");
        }

        // 生成新文件名
        String newFilename = generateAvatarFileName(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(newFilename);
        System.out.println("📁 保存文件到: " + filePath.toAbsolutePath());

        // 保存文件
        file.transferTo(filePath.toFile());

        // 验证文件是否保存成功
        if (Files.exists(filePath)) {
            long fileSize = Files.size(filePath);
            System.out.println("✅ 文件保存成功，大小: " + fileSize + " bytes");
        } else {
            System.err.println("❌ 文件保存失败");
            throw new IOException("文件保存失败");
        }

        return newFilename;
    }

    /**
     * 处理上传目录路径
     * 将相对路径转换为项目根目录的相对路径
     */
    private static String processUploadDir(String uploadDir) {
        // 如果以 "./" 开头，转换为相对于项目根目录的路径
        if (uploadDir.startsWith("./")) {
            // 获取项目根目录
            Path projectRoot = Paths.get("").toAbsolutePath();
            // 去掉 "./" 前缀
            String relativePath = uploadDir.substring(2);
            // 组合成相对于项目根目录的路径
            return projectRoot.resolve(relativePath).toString();
        }
        // 如果已经是绝对路径，直接返回
        return uploadDir;
    }

    /**
     * 删除旧头像文件 - 修复版
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
            String processedUploadDir = processUploadDir(uploadDir);
            Path oldFilePath = Paths.get(processedUploadDir, oldAvatarFilename);
            System.out.println("🗑️ 尝试删除旧头像: " + oldFilePath.toAbsolutePath());

            if (Files.exists(oldFilePath)) {
                Files.delete(oldFilePath);
                System.out.println("✅ 删除旧头像成功");
                return true;
            } else {
                System.out.println("ℹ️ 旧头像文件不存在，无需删除");
                return true;
            }
        } catch (Exception e) {
            System.err.println("⚠️ 删除旧头像文件失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取头像URL - 修复版
     */
    public static String getAvatarUrl(String filename, String urlPrefix) {
        if (filename == null || filename.trim().isEmpty()) {
            return getDefaultAvatarUrl();
        }

        // 如果头像不是默认头像，且不是以 http 开头，添加前缀
        if (!filename.startsWith("default_") && !filename.startsWith("http")) {
            return urlPrefix + filename;
        }

        return filename;
    }

    /**
     * 获取默认头像URL
     */
    public static String getDefaultAvatarUrl() {
        return "/static/images/default-avatars/default_avatar.png";
    }
}