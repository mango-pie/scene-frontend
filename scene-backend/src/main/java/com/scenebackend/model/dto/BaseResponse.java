package com.scenebackend.model.dto;

import com.scenebackend.common.ErrorCode;
import lombok.Data;
import java.io.Serializable;

/**
 * 统一响应结果类
 * @param <T> 响应数据类型
 */
@Data
public class BaseResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private int code;

    /**
     * 响应信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 构造函数
     * @param code 状态码
     * @param message 响应信息
     * @param data 响应数据
     */
    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return BaseResponse
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, "ok", data);
    }

    /**
     * 错误响应（使用ErrorCode枚举）
     * @param errorCode 错误码枚举
     * @param <T> 响应数据类型
     * @return BaseResponse
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 错误响应（使用ErrorCode枚举和自定义消息）
     * @param errorCode 错误码枚举
     * @param message 自定义错误信息
     * @param <T> 响应数据类型
     * @return BaseResponse
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), message, null);
    }

    /**
     * 错误响应（使用ErrorCode枚举和详情数据）
     * @param errorCode 错误码枚举
     * @param data 详情数据
     * @param <T> 响应数据类型
     * @return BaseResponse
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, T data) {
        return new BaseResponse<>(errorCode.getCode(), errorCode.getMessage(), data);
    }

    /**
     * 成功响应（无数据）
     * @param <T> 响应数据类型
     * @return BaseResponse
     */
    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(0, "ok", null);
    }
}