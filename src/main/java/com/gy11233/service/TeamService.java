package com.gy11233.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gy11233.model.domain.Team;
import com.gy11233.model.domain.User;
import com.gy11233.model.dto.TeamQuery;
import com.gy11233.model.request.TeamJoinRequest;
import com.gy11233.model.request.TeamQuitRequest;
import com.gy11233.model.request.TeamUpdateRequest;
import com.gy11233.model.vo.TeamUserVO;

import java.util.List;

/**
* @author guoying
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-07-23 20:59:29
*/
public interface TeamService extends IService<Team> {
    long addTeam(Team team, User loginUser);

    List<TeamUserVO> listTeams(TeamQuery teamQuery, Boolean isAdmin);

    boolean updateTeam(TeamUpdateRequest team, User loginUser);

    boolean joinTeam(TeamJoinRequest teamJoinRequest, User user);

    boolean quitTeam(TeamQuitRequest teamQuitRequest, User user);

    boolean deleteTeam(Long teamId, User user);

}
