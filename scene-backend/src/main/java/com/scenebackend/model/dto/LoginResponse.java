package com.scenebackend.model.dto;

import lombok.Data;
import java.io.Serializable;
// 登录响应DTO`
@Data
public class LoginResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String userAccount;
    private String avatarUrl;
    private Integer gender;
    private String phone;
    private String email;
    private Integer userStatus;
    private Integer userRole;
    private String plantCode;
    private String[] tagList;
    private String token;
    private String sessionId; // 添加sessionId字段
    private Long expireTime;

    public LoginResponse() {}

    public LoginResponse(String token, Long expireTime) {
        this.token = token;
        this.expireTime = expireTime;
    }
}