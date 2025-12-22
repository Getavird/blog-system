// UploadFile.java
package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class UploadFile extends BaseEntity {
    /**
     * 文件ID
     */
    private Integer id;
    
    /**
     * 原始文件名
     */
    private String originalName;
    
    /**
     * 保存文件名
     */
    private String saveName;
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 文件大小(字节)
     */
    private Long fileSize;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 文件扩展名
     */
    private String fileExt;
    
    /**
     * 上传用户ID
     */
    private Integer uploadUserId;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
    
    /**
     * 是否已使用：0-未使用，1-已使用
     */
    private Integer used;
    
    /**
     * 使用类型：avatar头像, cover封面, article文章
     */
    private String usageType;
    
    /**
     * 关联ID（如文章ID）
     */
    private Integer usageId;
    
    /**
     * 状态：1-正常，0-删除
     */
    private Integer status = 1;
    
    /**
     * 文件URL（计算属性）
     */
    private String fileUrl;
    
    /**
     * 文件缩略图URL（用于图片）
     */
    private String thumbnailUrl;
    
    /**
     * 文件大小格式化显示（如：2.5MB）
     */
    private String fileSizeFormatted;
    
    /**
     * 关联字段：上传用户名
     */
    private String uploadUsername;
    
    /**
     * 格式化文件大小
     */
    public String getFileSizeFormatted() {
        if (fileSize == null) return "0B";
        
        if (fileSize < 1024) {
            return fileSize + "B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2fKB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2fMB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.2fGB", fileSize / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * 获取文件完整URL
     */
    public String getFileUrl() {
        if (filePath == null) return null;
        // 这里根据实际部署环境返回完整的URL
        // 例如：http://localhost:8080/uploads/filename.jpg
        return "/uploads/" + this.filePath;
    }
}