package com.scenebackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 图片上传配置
 */
@Configuration
public class UploadConfig implements WebMvcConfigurer {

    // 图片上传路径(从配置文件读取或默认)
    @Value("${upload.path:/app/uploads/}")
    private String uploadPath;

    // 图片访问路径前缀
    @Value("${upload.access-path:/uploads/**}")
    private String accessPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源访问路径，让前端可以访问上传的图片
        registry.addResourceHandler(accessPath)
                .addResourceLocations("file:" + uploadPath);
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public String getAccessPath() {
        return accessPath;
    }
}