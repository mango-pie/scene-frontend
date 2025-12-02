package com.scenebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import java.util.Objects;
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

    // 分页查询默认配置（可根据业务调整）
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100; // 限制最大页大小，避免查询压力

    @Override
    public List<Team> searchTeams(TeamQuery teamQuery) {
        LambdaQueryWrapper<Team> queryWrapper = buildTeamQueryWrapper(teamQuery);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Page<Team> getTeamList(int pageNum, int pageSize, TeamQuery teamQuery) {
        // 1. 分页参数合法性校验（避免异常参数导致查询问题）
        int validPageNum = Math.max(pageNum, DEFAULT_PAGE_NUM);
        int validPageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);

        // 2. 构建查询条件（复用公共逻辑）
        LambdaQueryWrapper<Team> queryWrapper = buildTeamQueryWrapper(teamQuery);
        if (queryWrapper == null) {
            // 无匹配队长时，返回空分页结果（包含正确分页信息）
            return new Page<>(validPageNum, validPageSize);
        }

        // 3. 执行分页查询（使用MyBatis-Plus分页插件）
        return baseMapper.selectPage(new Page<>(validPageNum, validPageSize), queryWrapper);
    }

    /**
     * 抽取公共查询条件构建方法（复用+解耦）
     * 使用LambdaQueryWrapper避免硬编码列名，减少拼写错误
     */
    private LambdaQueryWrapper<Team> buildTeamQueryWrapper(TeamQuery teamQuery) {
        if (teamQuery == null) {
            return new LambdaQueryWrapper<>();
        }

        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();

        // 1. 队伍名称（模糊查询，仅当有有效文本时执行）
        if (StringUtils.hasText(teamQuery.getName())) {
            queryWrapper.like(Team::getName, teamQuery.getName());
        }

        // 2. 队伍描述（精确查询，仅当有有效文本时执行）
        if (StringUtils.hasText(teamQuery.getDescription())) {
            queryWrapper.eq(Team::getDescription, teamQuery.getDescription());
        }

        // 3. 最大人数（精确查询，非空时执行）
        if (teamQuery.getMaxNum() != null) {
            queryWrapper.eq(Team::getMaxNum, teamQuery.getMaxNum());
        }

        // 4. 队长ID（精确查询，非空时执行）
        if (teamQuery.getUserId() != null) {
            queryWrapper.eq(Team::getUserId, teamQuery.getUserId());
        }

        // 5. 队伍状态（-1=全部，0=公开，1=私有，2=加密）
        if (teamQuery.getStatus() != null && teamQuery.getStatus() != -1) {
            queryWrapper.eq(Team::getStatus, teamQuery.getStatus());
        }

        // 6. 队长名称查询（核心逻辑保持不变，优化空判断）
        if (StringUtils.hasText(teamQuery.getCaptainName())) {
            List<User> captains = userService.searchUserByName(teamQuery.getCaptainName());
            if (captains.isEmpty()) {
                return null; // 无匹配队长，返回null标识无需后续查询
            }
            // 提取队长ID列表，避免硬编码userId列名
            List<Long> captainIds = captains.stream()
                    .map(User::getId)
                    .filter(Objects::nonNull) // 过滤空ID，避免SQL异常
                    .collect(Collectors.toList());
            queryWrapper.in(Team::getUserId, captainIds);
        }

        return queryWrapper;
    }
}