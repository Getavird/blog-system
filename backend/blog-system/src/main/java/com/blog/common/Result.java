package com.blog.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一返回结果类
 * @param <T> 数据类型
 */
@Data
public class Result<T> implements Serializable {
    /**
     * 状态码：200-成功，其他-失败
     */
    private Integer code;
    
    /**
     * 返回消息
     */
    private String message;
    
    /**
     * 返回数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp = System.currentTimeMillis();
    
    /**
     * 成功（无数据）
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("成功");
        return result;
    }
    
    /**
     * 成功（有数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("成功");
        result.setData(data);
        return result;
    }
    
    /**
     * 成功（自定义消息和数据）
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    
    /**
     * 成功（仅自定义消息）
     */
    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 失败（默认500）
     */
    public static <T> Result<T> error() {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage("系统错误");
        return result;
    }
    
    /**
     * 失败（自定义消息）
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 失败（自定义状态码和消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 参数错误
     */
    public static <T> Result<T> badRequest(String message) {
        return error(400, message);
    }
    
    /**
     * 未授权
     */
    public static <T> Result<T> unauthorized(String message) {
        return error(401, message);
    }
    
    /**
     * 禁止访问
     */
    public static <T> Result<T> forbidden(String message) {
        return error(403, message);
    }
    
    /**
     * 资源不存在
     */
    public static <T> Result<T> notFound(String message) {
        return error(404, message);
    }
    
    /**
     * 创建成功
     */
    public static <T> Result<T> created(T data) {
        Result<T> result = new Result<>();
        result.setCode(201);
        result.setMessage("创建成功");
        result.setData(data);
        return result;
    }
}