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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
     * 上传头像（需要登录）
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            HttpServletRequest request) {
        
        // 1. 检查登录
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }
        
        // 2. 验证文件
        if (!AvatarUtil.validateAvatarFile(file)) {
            return Result.badRequest("请上传有效的图片文件（JPG/PNG/GIF，最大2MB）");
        }
        
        try {
            // 3. 保存文件
            String newFilename = AvatarUtil.saveAvatarFile(file, avatarUploadPath);
            
            // 4. 获取旧头像文件名
            String oldAvatar = currentUser.getAvatar();
            
            // 5. 更新用户头像信息
            boolean updateSuccess = updateUserAvatar(currentUser.getId(), newFilename);
            
            if (updateSuccess) {
                // 6. 删除旧头像文件（如果不是默认头像）
                if (oldAvatar != null && !oldAvatar.equals(newFilename)) {
                    AvatarUtil.deleteOldAvatar(oldAvatar, avatarUploadPath);
                }
                
                // 7. 返回结果
                Map<String, Object> result = new HashMap<>();
                result.put("filename", newFilename);
                result.put("url", AvatarUtil.getAvatarUrl(newFilename, avatarUrlPrefix));
                result.put("userId", currentUser.getId());
                result.put("username", currentUser.getUsername());
                
                System.out.println("✅ 用户头像更新成功 - 用户: " + currentUser.getUsername() + 
                                 ", 新头像: " + newFilename);
                
                return Result.success("头像上传成功", result);
            } else {
                // 如果更新数据库失败，删除已上传的文件
                Path filePath = Paths.get(avatarUploadPath, newFilename);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
                return Result.error("更新用户头像失败");
            }
            
        } catch (IOException e) {
            System.err.println("❌ 头像上传IO异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("头像上传失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ 头像上传异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("头像上传失败");
        }
    }
    
    /**
     * 更新用户头像（供内部调用）
     */
    private boolean updateUserAvatar(Integer userId, String avatarFilename) {
        try {
            // 这里需要先在UserService中添加更新头像的方法
            // 我们将在下面修改UserService
            User user = new User();
            user.setId(userId);
            user.setAvatar(avatarFilename);
            
            return userService.updateUserAvatar(user);
            
        } catch (Exception e) {
            System.err.println("❌ 更新用户头像异常: " + e.getMessage());
            e.printStackTrace();
            return false;
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
        avatarInfo.put("avatarUrl", AvatarUtil.getAvatarUrl(currentUser.getAvatar(), avatarUrlPrefix));
        
        return Result.success(avatarInfo);
    }
    
    /**
     * 重置为默认头像
     */
    @PostMapping("/reset")
    public Result<Map<String, Object>> resetToDefaultAvatar(HttpServletRequest request) {
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            return Result.unauthorized("请先登录");
        }
        
        try {
            String oldAvatar = currentUser.getAvatar();
            String defaultAvatar = "default_avatar.png";
            
            // 更新用户头像为默认头像
            User user = new User();
            user.setId(currentUser.getId());
            user.setAvatar(defaultAvatar);
            
            boolean updateSuccess = userService.updateUserAvatar(user);
            
            if (updateSuccess) {
                // 删除旧头像文件（如果不是默认头像）
                if (oldAvatar != null && !oldAvatar.equals(defaultAvatar)) {
                    AvatarUtil.deleteOldAvatar(oldAvatar, avatarUploadPath);
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("userId", currentUser.getId());
                result.put("newAvatar", defaultAvatar);
                result.put("avatarUrl", AvatarUtil.getDefaultAvatarUrl());
                
                System.out.println("✅ 用户头像重置为默认 - 用户: " + currentUser.getUsername());
                
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
     * 获取默认头像列表（可选功能）
     */
    @GetMapping("/defaults")
    public Result<Map<String, Object>> getDefaultAvatars() {
        Map<String, Object> defaultAvatars = new HashMap<>();
        
        // 假设我们有一些内置的默认头像
        String[] defaultAvatarFiles = {
            "default_avatar.png",
            "default_avatar_male.png", 
            "default_avatar_female.png",
            "default_avatar_cat.png",
            "default_avatar_dog.png"
        };
        
        Map<String, String> avatarMap = new HashMap<>();
        for (String filename : defaultAvatarFiles) {
            String url = AvatarUtil.getAvatarUrl(filename, avatarUrlPrefix);
            avatarMap.put(filename, url);
        }
        
        defaultAvatars.put("defaults", avatarMap);
        defaultAvatars.put("count", avatarMap.size());
        
        return Result.success(defaultAvatars);
    }
}