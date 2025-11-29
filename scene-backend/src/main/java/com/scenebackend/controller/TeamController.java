package com.scenebackend.controller;


import com.scenebackend.common.ErrorCode;
import com.scenebackend.exception.BusinessException;
import com.scenebackend.mapper.TeamMapper;
import com.scenebackend.model.dto.BaseResponse;
import com.scenebackend.model.dto.TeamQuery;
import com.scenebackend.service.TeamService;
import com.scenebackend.model.domain.Team;
import com.scenebackend.service.UserTeamService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team")
public class TeamController {
    @Resource
    private TeamService teamService;

    @PostMapping("/add")
    public BaseResponse<Team> addTeam(@RequestBody Team team) {
        if(team == null) {
           throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍为空");
        }

        boolean result = teamService.save(team);
        if(!result) {
           throw new BusinessException(ErrorCode.OPERATION_ERROR, "添加队伍失败");
        }
        return BaseResponse.success(team);
    }

    @DeleteMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteTeam(@PathVariable long id) {
        Team team = teamService.getById(id);
        if(team == null) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND, "队伍不存在");
        }
        boolean result = teamService.removeById(id);
        if(!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除队伍失败");
        }
        return BaseResponse.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody Team team) {
        if(team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Team is null");
        }
        boolean result = teamService.updateById(team);
        if(!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新队伍失败");
        }
        return BaseResponse.success(true);
    }

    @GetMapping("/get/{id}")
    public BaseResponse<Team> getTeam(@PathVariable long id) {
        Team team = teamService.getById(id);
        if(team == null) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND, "队伍不存在");
        }
        return BaseResponse.success(team);
    }

    @PostMapping("/list")
    public BaseResponse<List<Team>> listTeams(@RequestBody TeamQuery query) {
        if(query == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询参数为空");
        }
        List<Team> teams = teamService.searchTeams(query);
        if(teams == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取队伍列表失败");
        }
        return BaseResponse.success(teams);
    }
}
