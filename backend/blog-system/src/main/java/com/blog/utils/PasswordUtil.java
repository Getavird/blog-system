package com.blog.utils;

import org.springframework.util.DigestUtils;

/**
 * 密码工具类
 */
public class PasswordUtil {
    
    /**
     * 盐值（实际项目中应该更复杂，这里简化）
     */
    private static final String SALT = "BLOG_SYSTEM_2024_SALT_@#$%";
    
    /**
     * 密码加密
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encrypt(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        // 使用MD5加密（盐值+密码+盐值）
        String str = SALT + password + SALT;
        return DigestUtils.md5DigestAsHex(str.getBytes());
    }
    
    /**
     * 验证密码
     * @param password 原始密码
     * @param encryptedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean verify(String password, String encryptedPassword) {
        if (password == null || encryptedPassword == null) {
            return false;
        }
        return encrypt(password).equals(encryptedPassword);
    }
    
    /**
     * 生成随机密码（用于忘记密码重置）
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        if (length < 6) {
            length = 6;
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        
        return password.toString();
    }
    
    /**
     * 检查密码强度
     * @param password 密码
     * @return 强度等级：0-弱，1-中，2-强
     */
    public static int checkPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return 0;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        
        int score = 0;
        if (hasUpper) score++;
        if (hasLower) score++;
        if (hasDigit) score++;
        if (hasSpecial) score++;
        
        if (password.length() >= 8) score++;
        
        if (score <= 2) return 0; // 弱
        else if (score <= 4) return 1; // 中
        else return 2; // 强
    }
}