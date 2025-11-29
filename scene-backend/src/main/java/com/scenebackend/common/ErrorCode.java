package com.scenebackend.common;

/**
 * 错误码枚举类
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    REDIS_UNAVAILABLE_ERROR(50010, "Redis服务不可用，系统将使用token认证"),
    
    // 用户相关错误码
    USER_NOT_FOUND(40401, "用户不存在"),
    USERNAME_DUPLICATE(40001, "用户名已存在"),
    PHONE_DUPLICATE(40002, "手机号已被注册"),
    EMAIL_DUPLICATE(40003, "邮箱已被注册"),
    PASSWORD_ERROR(40004, "密码错误"),
    ACCOUNT_LOCKED(40301, "账号已被锁定"),
    
    // 团队相关错误码
    TEAM_NOT_FOUND(40402, "队伍不存在"),
    TEAM_NAME_DUPLICATE(40005, "队伍名称已存在"),
    TEAM_FULL(40006, "队伍人数已满"),
    TEAM_EXPIRED(40302, "队伍已过期"),
    MEMBER_ALREADY_IN_TEAM(40007, "用户已在队伍中"),
    NO_PERMISSION_TO_UPDATE_TEAM(40303, "无权限修改队伍信息"),
    NO_PERMISSION_TO_DELETE_TEAM(40304, "无权限删除队伍"),
    TEAM_PASSWORD_ERROR(40008, "队伍密码错误"),
    MEMBER_NOT_IN_TEAM(40009, "用户不在该队伍中"),
    // 文件上传相关错误码
    FILE_UPLOAD_FAILED(50002, "文件上传失败"),
    FILE_SIZE_EXCEEDED(40009, "文件大小超过限制"),
    FILE_TYPE_NOT_ALLOWED(40010, "不支持的文件类型"), UPDATE_ERROR(50003, "更新失败"),
    AUTHENTICATION_ERROR(40102, "认证失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 错误信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}