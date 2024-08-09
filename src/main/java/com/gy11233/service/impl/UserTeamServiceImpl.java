package com.gy11233.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gy11233.model.domain.UserTeam;
import com.gy11233.mapper.UserTeamMapper;
import com.gy11233.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author guoying
* @description 针对表【user_team(队伍-用户关系表)】的数据库操作Service实现
* @createDate 2024-07-23 21:00:35
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




