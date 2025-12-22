// FileController.java
package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.UploadFile;
import com.blog.service.FileService;
import com.blog.utils.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {
    
    @Autowired
    private FileService fileService;
    
    @Value("${file.max-size:10485760}")
    private Long maxFileSize;
    
    /**
     * 上传文件（Spring方式）
     */
    @PostMapping("/upload")
    public Result<UploadFile> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "usageType", required = false, defaultValue = "general") String usageType,
            HttpServletRequest request) {
        
        // 检查登录
        if (!SessionUtil.isLogin(request)) {
            return Result.unauthorized("请先登录");
        }
        
        Integer userId = SessionUtil.getCurrentUserId(request);
        
        try {
            UploadFile uploadFile = fileService.uploadFile(file, userId, usageType);
            
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("url", uploadFile.getFileUrl());
            extraData.put("fileSizeFormatted", uploadFile.getFileSizeFormatted());
            
            Result<UploadFile> result = Result.success("文件上传成功", uploadFile);
            result.setData(uploadFile);
            
            return result;
            
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量上传文件
     */
    @PostMapping("/upload/batch")
    public Result<List<UploadFile>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "usageType", required = false, defaultValue = "general") String usageType,
            HttpServletRequest request) {
        
        // 检查登录
        if (!SessionUtil.isLogin(request)) {
            return Result.unauthorized("请先登录");
        }
        
        Integer userId = SessionUtil.getCurrentUserId(request);
        
        try {
            List<UploadFile> uploadFiles = new java.util.ArrayList<>();
            Map<String, Object> errors = new HashMap<>();
            
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                try {
                    UploadFile uploadFile = fileService.uploadFile(file, userId, usageType);
                    uploadFiles.add(uploadFile);
                } catch (Exception e) {
                    errors.put(file.getOriginalFilename(), e.getMessage());
                }
            }
            
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("success", uploadFiles);
            resultData.put("errors", errors);
            
            if (uploadFiles.isEmpty()) {
                return Result.error("所有文件上传失败");
            } else if (!errors.isEmpty()) {
                return Result.success("部分文件上传成功", uploadFiles);
            } else {
                return Result.success("所有文件上传成功", uploadFiles);
            }
            
        } catch (Exception e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件信息
     */
    @GetMapping("/{id}")
    public Result<UploadFile> getFile(@PathVariable Integer id) {
        UploadFile file = fileService.getFileById(id);
        if (file == null) {
            return Result.notFound("文件不存在");
        }
        return Result.success(file);
    }
    
    /**
     * 获取用户上传的文件列表
     */
    @GetMapping("/my-files")
    public Result<Map<String, Object>> getMyFiles(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            HttpServletRequest request) {
        
        // 检查登录
        if (!SessionUtil.isLogin(request)) {
            return Result.unauthorized("请先登录");
        }
        
        Integer userId = SessionUtil.getCurrentUserId(request);
        List<UploadFile> files = fileService.getUserFiles(userId);
        
        // 简单分页
        int total = files.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        
        if (start >= total) {
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("files", java.util.Collections.emptyList());
            emptyResult.put("total", total);
            emptyResult.put("page", page);
            emptyResult.put("size", size);
            emptyResult.put("pages", 0);
            return Result.success(emptyResult);
        }
        
        List<UploadFile> pageFiles = files.subList(start, end);
        
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("files", pageFiles);
        resultData.put("total", total);
        resultData.put("page", page);
        resultData.put("size", size);
        resultData.put("pages", (int) Math.ceil((double) total / size));
        
        return Result.success(resultData);
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteFile(@PathVariable Integer id, HttpServletRequest request) {
        // 检查登录
        if (!SessionUtil.isLogin(request)) {
            return Result.unauthorized("请先登录");
        }
        
        Integer userId = SessionUtil.getCurrentUserId(request);
        
        try {
            boolean success = fileService.deleteFile(id, userId);
            return success ? Result.success("文件删除成功") : Result.error("文件删除失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取上传配置信息
     */
    @GetMapping("/upload-config")
    public Result<Map<String, Object>> getUploadConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxFileSize", maxFileSize);
        config.put("maxFileSizeFormatted", formatFileSize(maxFileSize));
        config.put("allowedTypes", new String[]{"jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx", "txt"});
        config.put("uploadPath", fileService.getStoragePath());
        return Result.success(config);
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1fKB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1fMB", size / (1024.0 * 1024));
        } else {
            return String.format("%.1fGB", size / (1024.0 * 1024 * 1024));
        }
    }
}