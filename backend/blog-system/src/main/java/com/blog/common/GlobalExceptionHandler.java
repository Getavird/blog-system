package com.blog.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理类（使用Jakarta EE）
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理所有未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 - URL: {}, Method: {}, Error: {}", 
                 request.getRequestURL(), request.getMethod(), e.getMessage(), e);
        
        // 生产环境隐藏详细错误信息
        if (isProduction()) {
            return Result.error("系统异常，请联系管理员");
        } else {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("url", request.getRequestURL());
            errorInfo.put("method", request.getMethod());
            errorInfo.put("timestamp", System.currentTimeMillis());
            errorInfo.put("exception", e.getClass().getName());
            errorInfo.put("message", e.getMessage());
            
            Result<Map<String, Object>> result = Result.error("系统异常：" + e.getMessage());
            result.setData(errorInfo);
            return result;
        }
    }
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }
    
    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("参数校验异常: {}", e.getMessage());
        
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        Result<Map<String, String>> result = Result.badRequest("参数校验失败");
        result.setData(errors);
        return result;
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Map<String, String>> handleBindException(BindException e) {
        log.warn("绑定异常: {}", e.getMessage());
        
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        Result<Map<String, String>> result = Result.badRequest("参数绑定失败");
        result.setData(errors);
        return result;
    }
    
    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<String> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常: ", e);
        return Result.error("系统错误：空指针异常");
    }
    
    /**
     * 判断是否为生产环境（简化判断）
     */
    private boolean isProduction() {
        String profile = System.getProperty("spring.profiles.active");
        return "prod".equals(profile) || "production".equals(profile);
    }
}