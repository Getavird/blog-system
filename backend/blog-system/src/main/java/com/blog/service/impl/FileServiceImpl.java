package com.blog.service.impl;

import com.blog.dao.UploadFileMapper;
import com.blog.entity.UploadFile;
import com.blog.service.FileService;
import com.blog.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FileServiceImpl implements FileService {

    @Autowired
    private UploadFileMapper uploadFileMapper;

    // 从配置文件中读取
    @Value("${blog.upload.path:./uploads/}")
    private String uploadBasePath;

    @Value("${file.max-size:10485760}") // 10MB
    private Long maxFileSize;

    @Value("${file.allowed-types:image/jpeg,image/png,image/gif,image/webp,application/pdf}")
    private String allowedTypes;

    // 允许的文件扩展名
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx", "txt");

    @Override
    public UploadFile uploadFile(MultipartFile file, Integer userId, String usageType) throws IOException {
        // 1. 验证文件
        validateFile(file);

        // 2. 生成保存信息
        String originalFilename = file.getOriginalFilename();
        String saveName = generateSaveName(originalFilename);
        String fileExt = FileUtil.getFileExtension(originalFilename);
        String subPath = generateSubPath();

        // 3. 处理上传路径
        // 获取项目根目录
        Path projectRoot = Paths.get("").toAbsolutePath();
        System.out.println("📁 项目根目录: " + projectRoot.toString());

        // 处理基础路径
        String basePath = uploadBasePath;
        if (basePath.startsWith("./")) {
            basePath = basePath.substring(2);
        }

        // 确保 basePath 不以斜杠开头，并以斜杠结尾
        if (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }
        if (!basePath.endsWith("/")) {
            basePath = basePath + "/";
        }

        System.out.println("📁 基础路径: " + basePath);
        System.out.println("📁 子路径: " + subPath);
        System.out.println("📁 保存名称: " + saveName);

        // 构建正确的相对路径（仅目录路径）
        String dirRelativePath = basePath + subPath; // 应该是 "uploads/2025/12/28/"
        System.out.println("📁 目录相对路径: " + dirRelativePath);

        // 转换为绝对路径
        Path dirAbsolutePath = projectRoot.resolve(dirRelativePath).normalize();
        System.out.println("📁 目录绝对路径: " + dirAbsolutePath.toString());

        // 4. 创建目录
        createDirectoryIfNotExists(dirAbsolutePath);

        // 5. 保存文件到磁盘
        Path destinationPath = dirAbsolutePath.resolve(saveName);
        System.out.println("📁 目标文件路径: " + destinationPath.toString());

        // 确保父目录存在
        Files.createDirectories(destinationPath.getParent());

        file.transferTo(destinationPath.toFile());

        // 6. 保存记录到数据库
        UploadFile uploadFile = new UploadFile();
        uploadFile.setOriginalName(originalFilename);
        uploadFile.setSaveName(saveName);
        uploadFile.setFilePath(subPath + saveName); // 只存储相对路径，如 "2025/12/28/filename.png"
        uploadFile.setFileSize(file.getSize());
        uploadFile.setFileType(file.getContentType());
        uploadFile.setFileExt(fileExt);
        uploadFile.setUploadUserId(userId);
        uploadFile.setUsed(0);
        uploadFile.setUsageType(usageType);
        uploadFile.setStatus(1);

        int result = uploadFileMapper.insert(uploadFile);
        if (result > 0) {
            System.out.println("✅ 文件上传成功: " + originalFilename +
                    " -> " + uploadFile.getFilePath());
            return uploadFile;
        } else {
            // 如果数据库保存失败，删除已上传的文件
            Files.deleteIfExists(destinationPath);
            throw new RuntimeException("文件上传失败：数据库保存错误");
        }
    }

    @Override
    public UploadFile uploadFile(byte[] fileBytes, String originalFilename,
            String contentType, Integer userId, String usageType) throws IOException {
        // 1. 验证文件大小
        if (fileBytes.length > maxFileSize) {
            throw new RuntimeException("文件大小超过限制");
        }

        // 2. 验证文件类型
        if (!isAllowedFileType(contentType, originalFilename)) {
            throw new RuntimeException("文件类型不允许");
        }

        // 3. 生成保存信息
        String saveName = generateSaveName(originalFilename);
        String fileExt = FileUtil.getFileExtension(originalFilename);
        String subPath = generateSubPath();
        String relativePath = subPath + saveName;

        // 4. 处理上传路径
        Path projectRoot = Paths.get("").toAbsolutePath();
        System.out.println("📁 项目根目录: " + projectRoot.toString());

        // 处理基础路径
        String basePath = uploadBasePath;
        if (basePath.startsWith("./")) {
            basePath = basePath.substring(2);
        }

        // 构建完整路径
        Path fullPath = projectRoot.resolve(relativePath).normalize();

        // 5. 创建目录
        createDirectoryIfNotExists(fullPath);

        // 6. 保存文件
        Path destinationPath = fullPath.resolve(saveName);
        System.out.println("📁 保存文件到: " + destinationPath.toString());

        // 确保父目录存在
        Files.createDirectories(destinationPath.getParent());

        Files.write(destinationPath, fileBytes);

        // 7. 保存记录到数据库
        UploadFile uploadFile = new UploadFile();
        uploadFile.setOriginalName(originalFilename);
        uploadFile.setSaveName(saveName);
        uploadFile.setFilePath(relativePath);
        uploadFile.setFileSize((long) fileBytes.length);
        uploadFile.setFileType(contentType);
        uploadFile.setFileExt(fileExt);
        uploadFile.setUploadUserId(userId);
        uploadFile.setUsed(0);
        uploadFile.setUsageType(usageType);
        uploadFile.setStatus(1);

        int result = uploadFileMapper.insert(uploadFile);
        if (result > 0) {
            System.out.println("✅ 文件上传成功: " + originalFilename);
            System.out.println("📍 实际保存位置: " + destinationPath.toAbsolutePath());
            return uploadFile;
        } else {
            Files.deleteIfExists(destinationPath);
            throw new RuntimeException("文件上传失败");
        }
    }

    @Override
    public UploadFile getFileById(Integer id) {
        return uploadFileMapper.findById(id);
    }

    @Override
    public List<UploadFile> getUserFiles(Integer userId) {
        return uploadFileMapper.findByUserId(userId);
    }

    @Override
    public List<UploadFile> getFilesByUsage(String usageType) {
        return uploadFileMapper.findByUsageType(usageType);
    }

    @Override
    public boolean deleteFile(Integer id, Integer userId) {
        UploadFile file = uploadFileMapper.findById(id);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }

        // 检查权限：只能删除自己的文件或管理员
        if (userId != null && !userId.equals(file.getUploadUserId())) {
            throw new RuntimeException("没有权限删除此文件");
        }

        // 软删除：只修改状态
        int result = uploadFileMapper.delete(id);
        if (result > 0) {
            System.out.println("✅ 文件删除成功（软删除）: ID=" + id);

            // 可选的：物理删除文件
            // 如果需要物理删除，取消下面的注释
            /*
             * try {
             * Path projectRoot = Paths.get("").toAbsolutePath();
             * Path filePath = projectRoot.resolve(file.getFilePath()).normalize();
             * Files.deleteIfExists(filePath);
             * System.out.println("🗑️ 物理删除文件: " + filePath);
             * } catch (IOException e) {
             * System.err.println("⚠️ 物理删除文件失败: " + e.getMessage());
             * }
             */

            return true;
        }
        return false;
    }

    @Override
    public boolean markFileAsUsed(Integer id, String usageType, Integer usageId) {
        int result = uploadFileMapper.markAsUsed(id, usageType, usageId);
        return result > 0;
    }

    @Override
    public String generateSaveName(String originalFilename) {
        // 格式：时间戳_随机UUID_文件名
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String ext = FileUtil.getFileExtension(originalFilename);

        // 获取不含扩展名的文件名
        String nameWithoutExt = FileUtil.getFileNameWithoutExtension(originalFilename);

        // 清理文件名：只保留字母、数字、下划线、中划线
        String cleanName = nameWithoutExt.replaceAll("[^a-zA-Z0-9\u4e00-\u9fa5_-]", "_");

        // 限制长度
        cleanName = cleanName.substring(0, Math.min(cleanName.length(), 50));

        // 构建保存名
        String saveName = timestamp + "_" + uuid + "_" + cleanName;
        if (!ext.isEmpty()) {
            saveName += "." + ext;
        }

        System.out.println("📁 生成的保存名: " + saveName);
        return saveName;
    }

    @Override
    public boolean isAllowedFileType(String contentType, String filename) {
        // 1. 检查MIME类型
        if (allowedTypes != null && !allowedTypes.isEmpty()) {
            List<String> allowedList = Arrays.asList(allowedTypes.split(","));
            if (!allowedList.contains(contentType.toLowerCase())) {
                return false;
            }
        }

        // 2. 检查文件扩展名
        String ext = FileUtil.getFileExtension(filename).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    @Override
    public String getStoragePath() {
        return uploadBasePath;
    }

    // =========== 私有方法 ===========

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("文件大小超过限制（最大 " +
                    FileUtil.formatFileSize(maxFileSize) + "）");
        }

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();

        if (!isAllowedFileType(contentType, originalFilename)) {
            throw new RuntimeException("文件类型不允许，支持类型：" +
                    String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    /**
     * 生成子路径（按日期组织）
     */
    private String generateSubPath() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/");
        String subPath = formatter.format(today);
        System.out.println("📁 生成的子路径: " + subPath);
        return subPath;
    }

    /**
     * 创建目录（如果不存在）- 简化版本
     */
    private void createDirectoryIfNotExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            System.out.println("📁 尝试创建目录: " + path.toAbsolutePath());
            Files.createDirectories(path);
            System.out.println("✅ 目录创建成功: " + path.toAbsolutePath());
        }
    }
}