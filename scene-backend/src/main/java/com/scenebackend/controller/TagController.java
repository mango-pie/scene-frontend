package com.scenebackend.controller;

import com.scenebackend.common.ErrorCode;
import com.scenebackend.exception.BusinessException;
import com.scenebackend.model.domain.Tag;
import com.scenebackend.model.dto.BaseResponse;
import com.scenebackend.service.TagService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tag")
public class TagController {
    @Resource
    private TagService tagService;
    @GetMapping("/search")
    public BaseResponse<List<Tag>> searchTags(){

       if(tagService.searchTags() == null)
       {
           throw new BusinessException(ErrorCode.OPERATION_ERROR, "标签为空");
       }
       return BaseResponse.success(tagService.searchTags());
    }
}