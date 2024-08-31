package com.gy11233.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gy11233.common.ErrorCode;
import com.gy11233.contant.RedisConstant;
import com.gy11233.contant.TeamStatusEnum;
import com.gy11233.exception.BusinessException;
import com.gy11233.model.domain.Team;
import com.gy11233.mapper.TeamMapper;
import com.gy11233.model.domain.User;
import com.gy11233.model.domain.UserTeam;
import com.gy11233.model.dto.TeamQuery;
import com.gy11233.model.request.TeamJoinRequest;
import com.gy11233.model.request.TeamQuitRequest;
import com.gy11233.model.request.TeamUpdateRequest;
import com.gy11233.model.vo.TeamUserVO;
import com.gy11233.model.vo.UserVO;
import com.gy11233.service.TeamService;
import com.gy11233.service.UserService;
import com.gy11233.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Resource
    RedissonClient redissonClient;

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
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, Boolean isAdmin, Integer hasStatus) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            List<Long> idList = teamQuery.getIdList();
            if (!CollectionUtils.isEmpty(idList)) {
                queryWrapper.in("id", idList);
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
            if (!isAdmin && enumByValues.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            // 通过hasStatus来选择加不加status的查询条件
            if (hasStatus != null && hasStatus == 1) {
                queryWrapper.eq("status", enumByValues.getValue());
            }

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
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        // 不能加入私有队伍
        TeamStatusEnum enumByValues = TeamStatusEnum.getEnumByValues(team.getStatus());
        if (TeamStatusEnum.PRIVATE.equals(enumByValues)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能加入私有队伍");
        }
        // 加密队伍 密码需要相同
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(enumByValues)) {
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
//        queryWrapper.eq("team_id", teamId);
        long countUserJoinTeam = this.countTeamUserByUserId(teamId);
        if (countUserJoinTeam >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
        }

        RLock lock = redissonClient.getLock(RedisConstant.USER_JOIN_TEAM + teamId);
        try {
            if (lock.tryLock(10000, -1, TimeUnit.MILLISECONDS)) { // 10s的时间抢锁
                System.out.println("getlock" + Thread.currentThread().getId());
                // 最多加入5个队伍
                QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
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

        } catch (InterruptedException e) {
            log.error("PreCacheJob.DoPreCacheJob lock error");
            return false;
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unlock" + Thread.currentThread().getId());
                lock.unlock();
            }
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User user) {
        //1. 校验请求参数
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        //3. 校验是否加入队伍
        long userId = user.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id", teamId);
        queryWrapper.eq("user_id", userId);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        //4. 如果队伍
        Long teamCount = this.countTeamUserByUserId(teamId);
        //1. 只剩下1个人队伍解散
        if (teamCount == 1) {
            this.removeById(teamId);
        } else {//2. 还有其他人
            // 至少还有两个人
            //1. 如果是队长退出，权限交给第二早加入的用户 user-team关系表中id第二小的用户
                if (team.getUserId() == userId) {
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("team_id", teamId);
                    userTeamQueryWrapper.last("order by id asc limit 2");
                    List<UserTeam> list = userTeamService.list(userTeamQueryWrapper);
                    if (CollectionUtils.isEmpty(list) || list.size() == 1) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR);
                    }
                    // 更改权限
                    Long nextUserId = list.get(1).getUserId();
                    Team updateTeam = new Team();
                    updateTeam.setId(teamId);
                    updateTeam.setUserId(nextUserId);
                    boolean result = this.updateById(updateTeam);
                    if (!result) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队长信息失败");
                    }

                }
                    //2. 如果不是队长 直接退出
        }
        // 所有退出操作集中到这里
        return userTeamService.remove(queryWrapper);


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(Long teamId, User user) {
//        1. 校验请求参数
//        2. 校验队伍存在
        Team team = getTeamById(teamId);
//        3. 校验是不是队长
        if (team.getUserId() != user.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
//        4. 移除关联信息
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id", teamId);
        boolean remove = userTeamService.remove(queryWrapper);
        if (!remove) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍移除失败");
        }
//        5. 删除队伍
        return this.removeById(teamId);
    }

    @Override
    public void hasJoinTeam(List<TeamUserVO> list, HttpServletRequest request) {
        // 判断当前用户是否已经加入队伍
        List<Long> teamIdList = list.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        try {
            User safetyUser = userService.getLoginUser(request);
            long userId = safetyUser.getId();
            QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.in("team_id", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            for (TeamUserVO teamUserVO: list) {
                boolean hasJoin = hasJoinTeamIdSet.contains(teamUserVO.getId());
                teamUserVO.setHasJoin(hasJoin);
            }
        } catch (Exception e) {}

    }

    @Override
    public void hasJoinTeamNum(List<TeamUserVO> list) {
        // 判断当前用户是否已经加入队伍
        List<Long> teamIdList = list.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("team_id", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        // teamId => 用户1 用户2
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        for (TeamUserVO teamUserVO : list) {
            teamUserVO.setHasJoinNum(teamIdUserTeamList.getOrDefault(teamUserVO.getId(), new ArrayList<>()).size());
        }
    }


    /**
     * 获取某个队伍加入人数
     *
     */

    private Long countTeamUserByUserId(Long teamId) {
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id", teamId);
        return userTeamService.count(queryWrapper);
    }


    /**
     * 根据teamId获取队伍
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 校验队伍是否存在
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        return team;
    }

}




