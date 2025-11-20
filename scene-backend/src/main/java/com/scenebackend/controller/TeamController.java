package com.scenebackend.controller;


import com.scenebackend.common.ErrorCode;
import com.scenebackend.exception.BusinessException;
import com.scenebackend.model.dto.TeamQuery;
import com.scenebackend.service.TeamService;
import com.scenebackend.model.domain.Team;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team")
public class TeamController {
    @Resource
    private TeamService teamService;

    @PostMapping("/add")
    public Team addTeam(@RequestBody Team team) {
        if(team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Team is null");
        }

        boolean result = teamService.save(team);
        if(!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Add team failed");
        }
        return team;
    }

    @DeleteMapping("/delete/{id}")
    public boolean deleteTeam(@PathVariable long id) {
        Team team = teamService.getById(id);
        if(team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Team not found");
        }
        boolean result = teamService.removeById(id);
        if(!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Delete team failed");
        }
       return true;
    }

    @PostMapping("/update")
    public boolean updateTeam(@RequestBody Team team) {
        if(team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Team is null");
        }
        boolean result = teamService.updateById(team);
        if(!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Update team failed");
        }
        return true;
    }

    @GetMapping("/get/{id}")
    public Team getTeam(@PathVariable long id) {
        Team team = teamService.getById(id);
        if(team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Team not found");
        }
        return team;
    }

    @PostMapping("/list")
    public List<Team> listTeams(@RequestBody TeamQuery query) {
        if(query == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Query is null");
        }
        List<Team> teams = teamService.searchTeams(query);
        if(teams == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "List teams failed");
        }
        return teams;
    }
}
