package com.gy11233.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gy11233.common.BaseResponse;
import com.gy11233.common.ErrorCode;
import com.gy11233.common.ResultUtils;
import com.gy11233.common.DeleteRequest;
import com.gy11233.exception.BusinessException;
import com.gy11233.model.domain.Team;
import com.gy11233.model.domain.User;
import com.gy11233.model.domain.UserTeam;
import com.gy11233.model.dto.TeamQuery;
import com.gy11233.model.request.*;
import com.gy11233.model.vo.TeamUserVO;
import com.gy11233.service.TeamService;

import com.gy11233.service.UserService;
import com.gy11233.service.UserTeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/team")
//@CrossOrigin(origins = "http://localhost:5173", allowCredentials="true") // 解决跨域
public class TeamController {

    @Resource
    TeamService teamService;

    @Resource
    UserService userService;


    @Resource
    UserTeamService userTeamService;

    /**
     * 增加队伍
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId =  teamService.addTeam(team, user);
        return ResultUtils.success(teamId);
    }

    /**
     * 删除队伍
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest teamDeleteRequest, HttpServletRequest request) {
        if (teamDeleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamDeleteRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User safetyUser = userService.getLoginUser(request);
        boolean remove = teamService.deleteTeam(id, safetyUser);
        if (!remove) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 更改队伍
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest team, HttpServletRequest request) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);

        boolean update = teamService.updateTeam(team, loginUser);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 根据id搜索队伍
     */
    @GetMapping("/{id}")
    public BaseResponse<Team> getTeam(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询失败");
        }
        return ResultUtils.success(team);
    }

    /**
     * 获取队伍列表 可以根据分页获取或者直接使用默认分页
     */
    // todo 改成分页的方法
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeam(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> list = teamService.listTeams(teamQuery, isAdmin, 1);
        if (CollectionUtils.isEmpty(list)) {
            return  ResultUtils.success(list);
        }
        // 对用户已经加入的队伍 设置hasJoin为true
        teamService.hasJoinTeam(list, request);
        // 统计每个小组的人数
        teamService.hasJoinTeamNum(list);
        return ResultUtils.success(list);
    }



//    /**
//     * 根据分页获取队伍列表
//     */
//    @GetMapping("/list/page")
//    public BaseResponse<Page<Team>> pageTeam(TeamQuery teamQuery) {
//        if (teamQuery == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        int pageNum = teamQuery.getPageNum();
//        int pageSize = teamQuery.getPageSize();
//        Team team = new Team();
//        BeanUtils.copyProperties(teamQuery, team);
//        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
//        Page<Team> page = teamService.page(new Page<>(pageNum, pageSize), queryWrapper);
//        return ResultUtils.success(page);
//    }

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param request
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User safetyUser = userService.getLoginUser(request);

        boolean result = teamService.joinTeam(teamJoinRequest, safetyUser);
        return ResultUtils.success(result);
    }


    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param request
     * @return
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User safetyUser = userService.getLoginUser(request);

        boolean result = teamService.quitTeam(teamQuitRequest, safetyUser);
        return ResultUtils.success(result);
    }


    /**
     * 获取用户创建的所有队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = userService.isAdmin(loginUser);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> list = teamService.listTeams(teamQuery, true, 0);
        if (CollectionUtils.isEmpty(list)) {
            return  ResultUtils.success(list);
        }
        teamService.hasJoinTeam(list, request);
        // 统计每个小组的人数
        teamService.hasJoinTeamNum(list);
        return ResultUtils.success(list);
    }

    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long userId = loginUser.getId();
        boolean isAdmin = userService.isAdmin(loginUser);
        // 查询队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        if (CollectionUtils.isEmpty(userTeamList)) {
            return ResultUtils.success(null);
        }
        // 过滤到userTeamList中teamId重复的
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idlist = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idlist);
        List<TeamUserVO> list = teamService.listTeams(teamQuery, true, 0);
        if (CollectionUtils.isEmpty(list)) {
            return  ResultUtils.success(list);
        }
        teamService.hasJoinTeam(list, request);
        // 统计每个小组的人数
        teamService.hasJoinTeamNum(list);
        return ResultUtils.success(list);
    }

}
