package com.scenebackend.model.dto;

import lombok.Data;

/**
 * 图片上传响应DTO
 */
@Data
public class ImageUploadResponse {
    private int code;
    private String message;
    private ImageUploadData data;

    public static ImageUploadResponse success(String url, String filename) {
        ImageUploadResponse response = new ImageUploadResponse();
        response.setCode(200);
        response.setMessage("上传成功");

        ImageUploadData data = new ImageUploadData();
        data.setUrl(url);
        data.setFilename(filename);
        response.setData(data);

        return response;
    }

    public static ImageUploadResponse error(String message) {
        ImageUploadResponse response = new ImageUploadResponse();
        response.setCode(500);
        response.setMessage(message);
        return response;
    }

    @Data
    public static class ImageUploadData {
        private String url;
        private String filename;
    }
}