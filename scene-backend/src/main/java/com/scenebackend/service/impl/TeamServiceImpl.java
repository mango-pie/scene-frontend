package com.scenebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scenebackend.model.domain.Team;
import com.scenebackend.model.dto.TeamQuery;
import com.scenebackend.service.TeamService;
import com.scenebackend.mapper.TeamMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 芒果派
* @description 针对表【team】的数据库操作Service实现
* @createDate 2025-10-19 20:13:38
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{
        @Override
        public List<Team> searchTeams(TeamQuery teamQuery) {
            QueryWrapper<Team> queryWrapper = new QueryWrapper<Team>();
            if(teamQuery != null) {
                queryWrapper.eq(teamQuery.getName() != null, "name", teamQuery.getName());
                queryWrapper.eq(teamQuery.getDescription() != null, "description", teamQuery.getDescription());
                queryWrapper.eq(teamQuery.getMaxNum() != null, "maxNum", teamQuery.getMaxNum());
                queryWrapper.eq(teamQuery.getUserId() != null, "userId", teamQuery.getUserId());
            }
            return baseMapper.selectList(queryWrapper);
        }

}




