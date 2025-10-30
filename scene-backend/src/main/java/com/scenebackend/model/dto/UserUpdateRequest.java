package com.scenebackend.model.dto;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class UserUpdateRequest {
    private Long id;
    private String username;
    private String avatarUrl;
    private Integer gender;
    private String phone;
    private String email;
    private String plantCode;
    @Getter
    private List<String> tagList;

    // 不包含敏感字段：userPassword, userRole, userStatus等
}
