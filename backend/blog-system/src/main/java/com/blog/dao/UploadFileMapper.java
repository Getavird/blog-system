// UploadFileMapper.java
package com.blog.dao;

import com.blog.entity.UploadFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UploadFileMapper {

        /**
         * 根据ID查询文件
         */
        @Select("SELECT uf.*, u.username as upload_username " +
                        "FROM upload_file uf " +
                        "LEFT JOIN user u ON uf.upload_user_id = u.id " +
                        "WHERE uf.id = #{id} AND uf.status = 1")
        UploadFile findById(Integer id);

        /**
         * 根据保存名查询文件
         */
        @Select("SELECT * FROM upload_file WHERE save_name = #{saveName} AND status = 1")
        UploadFile findBySaveName(String saveName);

        /**
         * 查询用户上传的文件
         */
        @Select("SELECT uf.*, u.username as upload_username " +
                        "FROM upload_file uf " +
                        "LEFT JOIN user u ON uf.upload_user_id = u.id " +
                        "WHERE uf.upload_user_id = #{userId} AND uf.status = 1 " +
                        "ORDER BY uf.upload_time DESC")
        List<UploadFile> findByUserId(Integer userId);

        /**
         * 查询使用类型的文件
         */
        @Select("SELECT * FROM upload_file WHERE usage_type = #{usageType} AND status = 1")
        List<UploadFile> findByUsageType(String usageType);

        /**
         * 查询特定使用的文件
         */
        @Select("SELECT * FROM upload_file WHERE usage_type = #{usageType} " +
                        "AND usage_id = #{usageId} AND status = 1")
        UploadFile findByUsage(@Param("usageType") String usageType,
                        @Param("usageId") Integer usageId);

        /**
         * 插入文件记录
         */
        @Insert("INSERT INTO upload_file(original_name, save_name, file_path, " +
                        "file_size, file_type, file_ext, upload_user_id, upload_time, " +
                        "used, usage_type, usage_id, status) " +
                        "VALUES(#{originalName}, #{saveName}, #{filePath}, " +
                        "#{fileSize}, #{fileType}, #{fileExt}, #{uploadUserId}, NOW(), " +
                        "#{used}, #{usageType}, #{usageId}, #{status})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(UploadFile uploadFile);

        /**
         * 更新文件信息
         */
        @Update("UPDATE upload_file SET " +
                        "original_name = #{originalName}, file_size = #{fileSize}, " +
                        "file_type = #{fileType}, file_ext = #{fileExt}, " +
                        "used = #{used}, usage_type = #{usageType}, " +
                        "usage_id = #{usageId}, status = #{status} " +
                        "WHERE id = #{id}")
        int update(UploadFile uploadFile);

        /**
         * 删除文件（软删除）
         */
        @Update("UPDATE upload_file SET status = 0 WHERE id = #{id}")
        int delete(Integer id);

        /**
         * 标记文件为已使用
         */
        @Update("UPDATE upload_file SET used = 1, usage_type = #{usageType}, " +
                        "usage_id = #{usageId} WHERE id = #{id}")
        int markAsUsed(@Param("id") Integer id,
                        @Param("usageType") String usageType,
                        @Param("usageId") Integer usageId);

        /**
         * 标记文件为未使用
         */
        @Update("UPDATE upload_file SET used = 0, usage_type = NULL, " +
                        "usage_id = NULL WHERE id = #{id}")
        int markAsUnused(Integer id);

        /**
         * 统计用户文件数量
         */
        @Select("SELECT COUNT(*) FROM upload_file WHERE upload_user_id = #{userId} AND status = 1")
        int countByUserId(Integer userId);

        /**
         * 统计总文件大小
         */
        @Select("SELECT SUM(file_size) FROM upload_file WHERE status = 1")
        Long getTotalFileSize();
}