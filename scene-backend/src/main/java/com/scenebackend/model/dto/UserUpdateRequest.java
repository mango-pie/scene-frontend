package com.scenebackend.model.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private Long id;
    private String username;
    private String avatarUrl;
    private Integer gender;
    private String phone;
    private String email;
    private String plantCode;
    private String tags;
    // 不包含敏感字段：userPassword, userRole, userStatus等
}
