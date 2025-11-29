package com.scenebackend.exception;

import com.scenebackend.common.ErrorCode;
import com.scenebackend.model.dto.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * @param e 业务异常
     * @return 统一响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<Object> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage(), e);
        // 创建响应结果
        BaseResponse<Object> response = new BaseResponse<>(e.getCode(), e.getMessage(), null);
        
        // 如果有详情信息，添加到响应数据中
        if (e.getDetail() != null) {
            // 创建一个包含详情的对象作为data
            Map<String, String> detailMap = new HashMap<>();
            detailMap.put("detail", e.getDetail());
            response.setData(detailMap);
        }
        
        return response;
    }
    
    /**
     * 添加对参数验证异常的处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<Object> handleValidationException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage(), e);
        // 获取所有验证错误信息
        List<String> errorMessages = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        String errorMessage = String.join(", ", errorMessages);
        return BaseResponse.error(ErrorCode.PARAMS_ERROR, errorMessage);
    }

    /**
     * 处理参数验证异常
     * @param e 参数验证异常
     * @return 统一响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public BaseResponse<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage(), e);
        return BaseResponse.error(ErrorCode.PARAMS_ERROR, e.getMessage());
    }

    /**
     * 处理其他所有异常
     * @param e 异常
     * @return 统一响应结果
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse<Object> handleException(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        return BaseResponse.error(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage());
    }
}