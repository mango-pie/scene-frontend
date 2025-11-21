package com.scenebackend.service;

import com.scenebackend.model.dto.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传服务接口
 */
public interface ImageUploadService {
    /**
     * 上传图片
     * @param file 图片文件
     * @param userId 上传用户ID
     * @return 上传结果
     */
    ImageUploadResponse uploadImage(MultipartFile file, Long userId);

    /**
     * 删除图片
     * @param filename 图片文件名
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteImage(String filename, Long userId);
}