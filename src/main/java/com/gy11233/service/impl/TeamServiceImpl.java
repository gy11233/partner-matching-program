package com.gy11233.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.Team;
import generator.mapper.TeamMapper;
import generator.service.TeamService;
import org.springframework.stereotype.Service;

/**
* @author guoying
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-07-23 20:59:29
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




