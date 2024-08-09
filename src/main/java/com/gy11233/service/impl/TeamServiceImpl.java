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
import com.gy11233.service.TeamService;
import com.gy11233.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

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

}




