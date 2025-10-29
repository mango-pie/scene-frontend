package com.scenebackend.controller;

import com.scenebackend.model.domain.Tag;
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
    public List<Tag> searchTags(){
        return tagService.searchTags();
    }
}