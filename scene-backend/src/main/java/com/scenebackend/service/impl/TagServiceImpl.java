package com.scenebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scenebackend.model.domain.Tag;
import com.scenebackend.service.TagService;
import com.scenebackend.mapper.TagMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 芒果派
* @description 针对表【tag】的数据库操作Service实现
* @createDate 2025-10-18 19:38:01
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {
    @Override
    public List<Tag> searchTags() {
       QueryWrapper<Tag> queryWrapper =new QueryWrapper<>();
        return baseMapper.selectList(queryWrapper);
    }
}




