package com.scenebackend.service;

import com.scenebackend.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scenebackend.model.dto.TeamQuery;

import java.util.List;

/**
* @author 芒果派
* @description 针对表【team】的数据库操作Service
* @createDate 2025-10-19 20:13:38
*/
public interface TeamService extends IService<Team> {

    List<Team> searchTeams(TeamQuery teamQuery);

}
