package com.scenebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scenebackend.model.domain.Team;
import com.scenebackend.service.TeamService;
import com.scenebackend.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 芒果派
* @description 针对表【team】的数据库操作Service实现
* @createDate 2025-10-19 20:13:38
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




