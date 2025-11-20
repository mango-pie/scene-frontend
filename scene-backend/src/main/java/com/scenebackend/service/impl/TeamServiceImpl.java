package com.scenebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scenebackend.model.domain.Team;
import com.scenebackend.model.domain.User;
import com.scenebackend.model.dto.TeamQuery;
import com.scenebackend.service.TeamService;
import com.scenebackend.service.UserService;
import com.scenebackend.mapper.TeamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author 芒果派
* @description 针对表【team】的数据库操作Service实现
* @createDate 2025-10-19 20:13:38
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{
        
        @Autowired
        private UserService userService;
        
        @Override
        public List<Team> searchTeams(TeamQuery teamQuery) {
            QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
            if(teamQuery != null) {
                queryWrapper.like(teamQuery.getName() != null, "name", teamQuery.getName());
                queryWrapper.eq(teamQuery.getDescription() != null, "description", teamQuery.getDescription());
                queryWrapper.eq(teamQuery.getMaxNum() != null, "maxNum", teamQuery.getMaxNum());
                queryWrapper.eq(teamQuery.getUserId() != null, "userId", teamQuery.getUserId());
                // 队伍状态（-1-全部，0-公开，1-私有，2-加密）
                if(teamQuery.getStatus() != null) {
                    if(teamQuery.getStatus() != -1) {
                        queryWrapper.eq("status", teamQuery.getStatus());
                    }
                }
                
                // 处理队长名称查询 - 优化版
                if(StringUtils.hasText(teamQuery.getCaptainName())) {
                    // 使用searchUserByName获取所有匹配的用户
                    List<User> captains = userService.searchUserByName(teamQuery.getCaptainName());
                    if(!captains.isEmpty()) {
                        // 提取所有匹配用户的ID并使用in条件查询
                        List<Long> captainIds = captains.stream()
                                .map(User::getId)
                                .collect(Collectors.toList());
                        queryWrapper.in("userId", captainIds);
                    } else {
                        // 如果没有找到匹配的用户，直接返回空列表，避免不必要的数据库查询
                        return List.of();
                    }
                }
            }
            return baseMapper.selectList(queryWrapper);
        }
}