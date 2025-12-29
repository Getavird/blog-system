package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.User;
import com.blog.service.UserService;
import com.blog.utils.AvatarUtil;
import com.blog.utils.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/avatar")
public class AvatarController {
    
    @Autowired
    private UserService userService;
    
    @Value("${blog.upload.avatar-path:./uploads/avatars/}")
    private String avatarUploadPath;
    
    @Value("${blog.upload.avatar-url:/uploads/avatars/}")
    private String avatarUrlPrefix;
    
    /**
     * 上传头像（需要登录）- 简化修复版
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            HttpServletRequest request) {
        
        try {
            // 1. 检查登录
            User currentUser = SessionUtil.getCurrentUser(request);
            if (currentUser == null) {
                return Result.unauthorized("请先登录");
            }
            
            System.out.println("🖼️ 开始上传头像...");
            System.out.println("  用户: " + currentUser.getUsername() + " (ID: " + currentUser.getId() + ")");
            System.out.println("  当前头像: " + currentUser.getAvatar());
            System.out.println("  配置文件路径: " + avatarUploadPath);
            
            // 2. 使用 AvatarUtil 验证和保存文件
            if (!AvatarUtil.validateAvatarFile(file)) {
                return Result.badRequest("请上传有效的图片文件（JPG/PNG/GIF，最大2MB）");
            }
            
            String newFilename = AvatarUtil.saveAvatarFile(file, avatarUploadPath);
            System.out.println("✅ 文件保存成功: " + newFilename);
            
            // 3. 获取旧头像文件名
            String oldAvatar = currentUser.getAvatar();
            
            // 4. 更新用户头像信息
            boolean updateSuccess = userService.updateAvatar(currentUser.getId(), newFilename);
            
            if (updateSuccess) {
                // 5. 删除旧头像文件（如果不是默认头像）
                if (oldAvatar != null && !oldAvatar.startsWith("default_") && !oldAvatar.equals("default_avatar.png")) {
                    AvatarUtil.deleteOldAvatar(oldAvatar, avatarUploadPath);
                }
                
                // 6. 构建返回结果
                Map<String, Object> result = new HashMap<>();
                result.put("avatar", newFilename);
                
                // 构建完整的URL - 确保以 /uploads/avatars/ 开头
                String avatarUrl = "/uploads/avatars/" + newFilename;
                result.put("url", avatarUrl);
                result.put("fullUrl", "http://localhost:8080" + avatarUrl);
                result.put("userId", currentUser.getId());
                result.put("username", currentUser.getUsername());
                
                System.out.println("✅ 头像上传成功");
                System.out.println("  新头像URL: " + avatarUrl);
                
                return Result.success("头像上传成功", result);
            } else {
                return Result.error("更新用户头像失败");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 头像上传异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户的头像信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getAvatarInfo(HttpServletRequest request) {
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }
        
        Map<String, Object> avatarInfo = new HashMap<>();
        avatarInfo.put("userId", currentUser.getId());
        avatarInfo.put("username", currentUser.getUsername());
        avatarInfo.put("currentAvatar", currentUser.getAvatar());
        
        // 构建正确的头像URL
        String avatarUrl;
        if (currentUser.getAvatar() == null || currentUser.getAvatar().startsWith("default_")) {
            avatarUrl = AvatarUtil.getDefaultAvatarUrl();
        } else {
            avatarUrl = "/uploads/avatars/" + currentUser.getAvatar();
        }
        
        avatarInfo.put("avatarUrl", avatarUrl);
        avatarInfo.put("fullAvatarUrl", "http://localhost:8080" + avatarUrl);
        
        return Result.success(avatarInfo);
    }
    
    /**
     * 重置为默认头像
     */
    @PostMapping("/reset")
    public Result<Map<String, Object>> resetToDefaultAvatar(HttpServletRequest request) {
        try {
            User currentUser = SessionUtil.getCurrentUser(request);
            if (currentUser == null) {
                return Result.unauthorized("请先登录");
            }
            
            System.out.println("🔄 重置用户头像为默认...");
            System.out.println("  用户: " + currentUser.getUsername());
            System.out.println("  当前头像: " + currentUser.getAvatar());
            
            // 获取旧头像
            String oldAvatar = currentUser.getAvatar();
            String defaultAvatar = "default_avatar.png";
            
            // 更新用户头像为默认头像
            boolean updateSuccess = userService.updateAvatar(currentUser.getId(), defaultAvatar);
            
            if (updateSuccess) {
                // 删除旧头像文件（如果不是默认头像）
                if (oldAvatar != null && !oldAvatar.startsWith("default_") && !oldAvatar.equals(defaultAvatar)) {
                    AvatarUtil.deleteOldAvatar(oldAvatar, avatarUploadPath);
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("userId", currentUser.getId());
                result.put("newAvatar", defaultAvatar);
                
                // 返回默认头像的URL
                String defaultAvatarUrl = AvatarUtil.getDefaultAvatarUrl();
                result.put("avatarUrl", defaultAvatarUrl);
                result.put("fullAvatarUrl", "http://localhost:8080" + defaultAvatarUrl);
                
                System.out.println("✅ 用户头像重置为默认");
                System.out.println("  默认头像URL: " + defaultAvatarUrl);
                
                return Result.success("已重置为默认头像", result);
            } else {
                return Result.error("重置头像失败");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 重置头像异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("重置头像失败");
        }
    }
    
    /**
     * 获取默认头像列表
     */
    @GetMapping("/defaults")
    public Result<Map<String, Object>> getDefaultAvatars() {
        Map<String, Object> defaultAvatars = new HashMap<>();
        
        // 假设我们有一些内置的默认头像
        String[] defaultAvatarFiles = {
            "default_avatar.png",
            "default_avatar_male.png", 
            "default_avatar_female.png"
        };
        
        Map<String, String> avatarMap = new HashMap<>();
        for (String filename : defaultAvatarFiles) {
            String url = "/static/images/default-avatars/" + filename;
            avatarMap.put(filename, url);
        }
        
        defaultAvatars.put("defaults", avatarMap);
        defaultAvatars.put("count", avatarMap.size());
        
        return Result.success(defaultAvatars);
    }
}