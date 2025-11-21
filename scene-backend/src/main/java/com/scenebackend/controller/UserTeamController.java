package com.scenebackend.controller;

import com.scenebackend.common.ErrorCode;
import com.scenebackend.exception.BusinessException;
import com.scenebackend.model.domain.Team;
import com.scenebackend.model.domain.User;
import com.scenebackend.model.domain.UserTeam;
import com.scenebackend.service.TeamService;
import com.scenebackend.service.UserService;
import com.scenebackend.service.UserTeamService;
import com.scenebackend.utils.JwtUtil;
import com.scenebackend.utils.SessionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/user-team")
public class UserTeamController {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    @Resource
    private SessionService sessionService;

    @Resource
    private JwtUtil jwtUtil;

    /**
     * 加入队伍
     * @param teamId 队伍ID
     * @param password 队伍密码（加密队伍需要）
     * @return 加入结果
     */
    @PostMapping("/join")
    public UserTeam joinTeam(@RequestParam Long teamId,
                             @RequestParam(required = false) String password,
                             @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                             @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 验证参数
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID不能为空");
        }

        // 获取当前登录用户
        User currentUser = getCurrentLoginUser(sessionId, authHeader);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        // 验证队伍是否存在
        Team team = teamService.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "队伍不存在");
        }

        // 验证用户是否已经加入该队伍
        if (isUserInTeam(currentUser.getId(), teamId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已在队伍中");
        }

        // 验证队伍状态
        if (team.getStatus() == 1) { // 私有队伍
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "无法加入私有队伍");
        } else if (team.getStatus() == 2) { // 加密队伍
            if (password == null || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍密码错误");
            }
        }

        // 验证队伍是否过期
        if (team.getExpireTime() != null && team.getExpireTime().before(new Date())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "队伍已过期");
        }

        // 验证队伍人数是否已满
        long currentMemberCount = getTeamMemberCount(teamId);
        if (currentMemberCount >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "队伍人数已满");
        }

        // 创建用户队伍关联记录
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(currentUser.getId());
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        userTeam.setCreateTime(new Date());
        userTeam.setUpdateTime(new Date());
        userTeam.setIsDelete(0);

        boolean result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "加入队伍失败");
        }

        return userTeam;
    }

    /**
     * 退出队伍
     * @param teamId 队伍ID
     * @return 退出结果
     */
    @DeleteMapping("/quit")
    public boolean quitTeam(@RequestParam Long teamId,
                            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 验证参数
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID不能为空");
        }

        // 获取当前登录用户
        User currentUser = getCurrentLoginUser(sessionId, authHeader);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        // 验证队伍是否存在
        Team team = teamService.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "队伍不存在");
        }

        // 验证用户是否为队长
        if (team.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "队长不能退出队伍，请先解散队伍或转让队长");
        }

        // 验证用户是否在队伍中
        if (!isUserInTeam(currentUser.getId(), teamId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户不在该队伍中");
        }

        // 退出队伍
        boolean result = userTeamService.lambdaUpdate()
                .eq(UserTeam::getUserId, currentUser.getId())
                .eq(UserTeam::getTeamId, teamId)
                .remove();

        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "退出队伍失败");
        }

        return true;
    }

    /**
     * 获取用户加入的队伍列表
     * @return 队伍列表
     */
    @GetMapping("/my-teams")
    public List<Team> getMyTeams(@RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                                 @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 获取当前登录用户
        User currentUser = getCurrentLoginUser(sessionId, authHeader);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        // 查询用户加入的队伍
        List<UserTeam> userTeams = userTeamService.lambdaQuery()
                .eq(UserTeam::getUserId, currentUser.getId())
                .list();

        // 查询队伍详情
        return userTeams.stream()
                .map(userTeam -> teamService.getById(userTeam.getTeamId()))
                .filter(team -> team != null)
                .toList();
    }

    // 辅助方法：获取当前登录用户
    private User getCurrentLoginUser(String sessionId, String authHeader) {
        // 优先使用session认证
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            if (!sessionId.equals("redis-unavailable") && !sessionId.equals("session-creation-failed")) {
                try {
                    User user = sessionService.getUserBySession(sessionId);
                    if (user != null) {
                        return user;
                    }
                } catch (Exception e) {
                    System.err.println("Session认证失败，将回退到token认证: " + e.getMessage());
                    // 继续执行token认证
                }
            }
        }

        // 使用token认证
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                return userService.getById(userId);
            }
        }

        return null;
    }

    // 辅助方法：检查用户是否在队伍中
    private boolean isUserInTeam(Long userId, Long teamId) {
        return userTeamService.lambdaQuery()
                .eq(UserTeam::getUserId, userId)
                .eq(UserTeam::getTeamId, teamId)
                .exists();
    }

    // 辅助方法：获取队伍成员数量
    private Long getTeamMemberCount(Long teamId) {
        return userTeamService.lambdaQuery()
                .eq(UserTeam::getTeamId, teamId)
                .count();
    }
}