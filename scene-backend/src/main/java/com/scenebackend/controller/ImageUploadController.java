package com.scenebackend.controller;

import com.scenebackend.model.domain.User;
import com.scenebackend.model.dto.ImageUploadResponse;
import com.scenebackend.service.ImageUploadService;
import com.scenebackend.service.UserService;
import com.scenebackend.utils.JwtUtil;
import com.scenebackend.utils.SessionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传控制器
 */
@RestController
@RequestMapping("/api/upload")
public class ImageUploadController {

    @Resource
    private UserService userService;

    @Resource
    private ImageUploadService imageUploadService;

    @Resource
    private SessionService sessionService;

    @Resource
    private JwtUtil jwtUtil;

    /**
     * 上传图片
     */
    @PostMapping("/image")
    public ImageUploadResponse uploadImage(@RequestParam("file") MultipartFile file,
                                           @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 获取当前登录用户ID
        Long userId = getCurrentUserId(sessionId, authHeader);
        if (userId == null) {
            return ImageUploadResponse.error("未登录，无法上传图片");
        }

        // 只调用一次uploadImage方法
        ImageUploadResponse response = imageUploadService.uploadImage(file, userId);

        if (response.getCode() == 200 && sessionId != null && !sessionId.trim().isEmpty()) {
            // 获取更新后的用户信息
            User updatedUser = userService.getUserById(userId);
            if (updatedUser != null) {
                // 使用SessionService更新session数据
                sessionService.updateSessionUser(sessionId, updatedUser);
            }
        }

        return response;
    }

    /**
     * 删除图片
     */
    @DeleteMapping("/image")
    public ImageUploadResponse deleteImage(@RequestParam String filename,
                                           @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 获取当前登录用户ID
        Long userId = getCurrentUserId(sessionId, authHeader);
        if (userId == null) {
            return ImageUploadResponse.error("未登录，无法删除图片");
        }

        boolean result = imageUploadService.deleteImage(filename, userId);
        if (result) {
            return ImageUploadResponse.success("", filename);
        } else {
            return ImageUploadResponse.error("删除图片失败");
        }
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId(String sessionId, String authHeader) {
        // 优先从session获取
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            try {
                com.scenebackend.model.domain.User user = sessionService.getUserBySession(sessionId);
                if (user != null) {
                    return user.getId();
                }
            } catch (Exception e) {
                // 忽略异常，尝试token验证
            }
        }

        // 从token获取
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                return jwtUtil.getUserIdFromToken(token);
            }
        }

        return null;
    }
}