package com.scenebackend.controller;


import com.scenebackend.common.ErrorCode;
import com.scenebackend.exception.BusinessException;
import com.scenebackend.service.TeamService;
import com.scenebackend.model.domain.Team;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
