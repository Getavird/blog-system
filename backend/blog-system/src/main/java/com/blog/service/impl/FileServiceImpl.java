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
        String normalizedBasePath = uploadBasePath;
        if (!normalizedBasePath.endsWith("/")) {
            normalizedBasePath += "/";
        }

        // 移除路径中的 "./" 前缀
        if (normalizedBasePath.startsWith("./")) {
            normalizedBasePath = normalizedBasePath.substring(2);
        }

        String fullPath = normalizedBasePath + subPath;

        // 4. 创建目录
        createDirectoryIfNotExists(fullPath);

        // 5. 保存文件到磁盘
        Path actualFullPath = Paths.get("").toAbsolutePath().resolve(fullPath);
        Path destinationPath = actualFullPath.resolve(saveName);
        file.transferTo(destinationPath.toFile());

        // 6. 保存记录到数据库
        UploadFile uploadFile = new UploadFile();
        uploadFile.setOriginalName(originalFilename);
        uploadFile.setSaveName(saveName);
        uploadFile.setFilePath(subPath + saveName);
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

        // 4. 处理上传路径
        String normalizedBasePath = uploadBasePath;
        if (!normalizedBasePath.endsWith("/")) {
            normalizedBasePath += "/";
        }

        // 移除路径中的 "./" 前缀
        if (normalizedBasePath.startsWith("./")) {
            normalizedBasePath = normalizedBasePath.substring(2);
        }

        String fullPath = normalizedBasePath + subPath;

        // 5. 创建目录
        createDirectoryIfNotExists(fullPath);

        // 6. 保存文件（使用绝对路径）
        Path actualFullPath = Paths.get("").toAbsolutePath().resolve(fullPath);
        Path destinationPath = actualFullPath.resolve(saveName);
        Files.write(destinationPath, fileBytes);

        // 7. 保存记录到数据库
        UploadFile uploadFile = new UploadFile();
        uploadFile.setOriginalName(originalFilename);
        uploadFile.setSaveName(saveName);
        uploadFile.setFilePath(subPath + saveName);
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
            // String fullPath = uploadPath + file.getFilePath();
            // Files.deleteIfExists(Paths.get(fullPath));

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
        // 格式：时间戳_随机UUID_原始文件名（确保唯一）
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String ext = FileUtil.getFileExtension(originalFilename);

        // 清理原始文件名中的特殊字符
        String safeName = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        safeName = safeName.substring(0, Math.min(safeName.length(), 50));

        return timestamp + "_" + uuid + "_" + safeName;
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
        return formatter.format(today);
    }

    /**
     * 创建目录（如果不存在）
     */
    private void createDirectoryIfNotExists(String path) throws IOException {
        // 确保路径是相对于项目根目录，不是Tomcat临时目录
        Path actualPath;
        if (path.startsWith("./")) {
            // 相对路径，从项目根目录开始
            actualPath = Paths.get("").toAbsolutePath().resolve(path.substring(2));
        } else if (path.startsWith("/")) {
            // 绝对路径
            actualPath = Paths.get(path);
        } else {
            // 相对路径，补充"./"
            actualPath = Paths.get("").toAbsolutePath().resolve("uploads").resolve(path);
        }

        File dir = actualPath.toFile();
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("无法创建目录: " + actualPath);
            }
            System.out.println("📁 创建目录: " + actualPath);
        }
    }
}