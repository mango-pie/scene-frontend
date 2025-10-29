package com.scenebackend.service;

import com.scenebackend.model.domain.Tag;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 芒果派
* @description 针对表【tag】的数据库操作Service
* @createDate 2025-10-18 19:38:01
*/
public interface TagService extends IService<Tag> {
    /**
     * 搜索标签
     * @return 标签列表
     */
    List<Tag> searchTags();
}
