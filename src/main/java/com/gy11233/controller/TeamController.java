package com.gy11233.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gy11233.common.BaseResponse;
import com.gy11233.common.ErrorCode;
import com.gy11233.common.ResultUtils;
import com.gy11233.exception.BusinessException;
import com.gy11233.model.domain.Team;
import com.gy11233.model.domain.User;
import com.gy11233.model.dto.TeamQuery;
import com.gy11233.model.request.TeamAddRequest;
import com.gy11233.model.request.TeamJoinRequest;
import com.gy11233.model.request.TeamQuitRequest;
import com.gy11233.model.request.TeamUpdateRequest;
import com.gy11233.model.vo.TeamUserVO;
import com.gy11233.service.TeamService;

import com.gy11233.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/team")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials="true") // 解决跨域
public class TeamController {

    @Resource
    TeamService teamService;

    @Resource
    UserService userService;

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
    public BaseResponse<Boolean> deleteTeam(Long teamId, HttpServletRequest request) {
        if (teamId ==  null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User safetyUser = userService.getLoginUser(request);
        boolean remove = teamService.deleteTeam(teamId, safetyUser);
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
    @GetMapping("/get")
    public BaseResponse<Team> getTeam(long id) {
        if (id <= 0) {
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

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeam(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> list = teamService.listTeams(teamQuery, isAdmin);
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
    public BaseResponse<Boolean> joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
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
    public BaseResponse<Boolean> quitTeam(TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User safetyUser = userService.getLoginUser(request);

        boolean result = teamService.quitTeam(teamQuitRequest, safetyUser);
        return ResultUtils.success(result);
    }



}
