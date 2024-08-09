package com.gy11233.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gy11233.model.domain.Team;
import com.gy11233.model.domain.User;

/**
* @author guoying
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-07-23 20:59:29
*/
public interface TeamService extends IService<Team> {
    long addTeam(Team team, User loginUser);
}
