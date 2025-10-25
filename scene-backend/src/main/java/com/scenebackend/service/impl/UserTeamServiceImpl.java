package com.scenebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scenebackend.model.domain.UserTeam;
import com.scenebackend.service.UserTeamService;
import com.scenebackend.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 芒果派
* @description 针对表【user_team】的数据库操作Service实现
* @createDate 2025-10-19 20:15:52
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




