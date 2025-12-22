// FileService.java
package com.blog.service;

import com.blog.entity.UploadFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {
    
    /**
     * 上传文件
     */
    UploadFile uploadFile(MultipartFile file, Integer userId, String usageType) throws IOException;
    
    /**
     * 获取文件信息
     */
    UploadFile getFileById(Integer id);
    
    /**
     * 获取用户上传的文件列表
     */
    List<UploadFile> getUserFiles(Integer userId);
    
    /**
     * 获取指定用途的文件
     */
    List<UploadFile> getFilesByUsage(String usageType);
    
    /**
     * 删除文件
     */
    boolean deleteFile(Integer id, Integer userId);
    
    /**
     * 标记文件为已使用
     */
    boolean markFileAsUsed(Integer id, String usageType, Integer usageId);
    
    /**
     * 生成文件保存名
     */
    String generateSaveName(String originalFilename);
    
    /**
     * 检查文件类型是否允许
     */
    boolean isAllowedFileType(String contentType, String filename);
    
    /**
     * 获取文件存储路径
     */
    String getStoragePath();

    UploadFile uploadFile(byte[] fileBytes, String originalFilename, String contentType, Integer userId,
            String usageType) throws IOException;
}