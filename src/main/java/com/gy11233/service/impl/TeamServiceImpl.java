package com.gy11233.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gy11233.common.ErrorCode;
import com.gy11233.contant.TeamStatusEnum;
import com.gy11233.exception.BusinessException;
import com.gy11233.model.domain.Team;
import com.gy11233.mapper.TeamMapper;
import com.gy11233.model.domain.User;
import com.gy11233.model.domain.UserTeam;
import com.gy11233.model.dto.TeamQuery;
import com.gy11233.model.request.TeamJoinRequest;
import com.gy11233.model.request.TeamUpdateRequest;
import com.gy11233.model.vo.TeamUserVO;
import com.gy11233.model.vo.UserVO;
import com.gy11233.service.TeamService;
import com.gy11233.service.UserService;
import com.gy11233.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

/**
* @author guoying
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-07-23 20:59:29
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    UserTeamService userTeamService;

    @Resource
    UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        // 1. 请求参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 是否登录 不登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 3. 校验信息
        // 1. 队伍人数>1 <=20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(1);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符");
        }
        // 2. 队伍标题<=20
        if (StringUtils.isBlank(team.getName()) || team.getName().length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题不合格");
        }
        // 3. 描述 <=512
        if (StringUtils.isBlank(team.getDescription()) && team.getDescription().length() > 215) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述过长");
        }
        // 4. status是否公开  不传默认公开
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum enumByValues = TeamStatusEnum.getEnumByValues(status);
        if (enumByValues == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足");
        }
        // 5. status如果是加密状态一定要有密码 且密码<=32
        if (TeamStatusEnum.SECRET.equals(enumByValues)) {
            if (StringUtils.isBlank(team.getPassword()) || team.getPassword().length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍密码不满足");
            }
        }
        // 6. 超时时间 > 当前时间、
        // 是after还是before 目前看是after
        if (new Date().after(team.getExpireTime())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间不满足");
        }
        // 7. 校验用户最多创建5个队伍
        // todo:和加入队伍的个数统计在不同的表 但是限制都是五个可能存在冲突
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUser.getId());
        long count = this.count(queryWrapper);
        if (count >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建五个用户");
        }
        // 4. 插入队伍信息到队伍表
        team.setUserId(loginUser.getId());
        team.setId(null);
        boolean save = this.save(team);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建失败");
        }
        long teamId = team.getId();
        // 5. 插入 队伍用户信息到 关系表
        UserTeam userTeam = new UserTeam();
        // 这样子行不行 直接team.getId 目前看是可以的
        userTeam.setTeamId(teamId);
        userTeam.setUserId(loginUser.getId());
        userTeam.setCreateTime(new Date());
        userTeam.setJoinTime(new Date());
        save = userTeamService.save(userTeam);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建失败");
        }
        return teamId;

    }

    /**
     * 查询队伍列表
     * @param teamQuery
     * @return
     */
    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, Boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            // 只有管理员可以查全部
            Integer status = teamQuery.getStatus();
            TeamStatusEnum enumByValues = TeamStatusEnum.getEnumByValues(status);
            if (enumByValues == null) {
                enumByValues = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && !enumByValues.equals(TeamStatusEnum.PUBLIC)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status", enumByValues.getValue());

            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("max_num", maxNum);
            }
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("user_id", userId);
            }
        }
        // 不允许过期队伍展示
        // 队伍过期时间为null 或者没有过期 都 可以
        queryWrapper.and(qw -> qw.gt("expire_time", new Date())).or().isNull("expire_time");

        List<TeamUserVO> voList = new ArrayList<>();
        List<Team> listTeam = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(listTeam)) {
            listTeam = new ArrayList<>();
        }

        // todo: 扩展查询队伍所有成员信息 用到多表关联查询
        for (Team team: listTeam) {
            Long createUserId = team.getUserId();
            if (createUserId == null) {
                continue;
            }
            // 关联用户信息
            User user = userService.getById(createUserId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            if (user != null) {
                UserVO createUser = new UserVO();
                BeanUtils.copyProperties(user, createUser);
                teamUserVO.setCreateUser(createUser);
            }

            voList.add(teamUserVO);
        }

        return voList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest team, User loginUser) {
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        Long id = team.getId();
        if (id == null || id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Team oldTeam = this.getById(id);
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        TeamStatusEnum enumByValues = TeamStatusEnum.getEnumByValues(team.getStatus());
        if (enumByValues.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍需要密码");
            }
        }

        Team updateTeam = new Team();
        BeanUtils.copyProperties(team, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User user) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        // 不能加入私有队伍
        TeamStatusEnum enumByValues = TeamStatusEnum.getEnumByValues(team.getStatus());
        if (TeamStatusEnum.PRIVATE.equals(enumByValues)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能加入私有队伍");
        }
        // 加密队伍 密码需要相同
        String password = teamJoinRequest.getPassword();
        if(TeamStatusEnum.SECRET.equals(enumByValues)) {
            if (StringUtils.isBlank(password) || !team.getPassword().equals(password)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍密码错误");
            }
        }
        // 不能加入过期队伍
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍过期");
        }
        // 只能加入未满队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id", teamId);
        long countUserJoinTeam = userTeamService.count(queryWrapper);
        if (countUserJoinTeam >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
        }
        // 最多加入5个队伍
        long userId = user.getId();
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        long userJoinTeamNum = userTeamService.count(queryWrapper);
        if (userJoinTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多加入5个队伍");
        }
        // 不能重复加入队伍
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("team_id", teamId);
        long countNum = userTeamService.count(queryWrapper);
        if (countNum > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能重复加入队伍");
        }

        // 校验完成 更新用户队伍关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());

        return userTeamService.save(userTeam);
    }

}




