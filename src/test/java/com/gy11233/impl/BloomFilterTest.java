package com.gy11233.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gy11233.config.BloomFilterConfig;
import com.gy11233.model.domain.Team;
import com.gy11233.model.domain.User;
import com.gy11233.service.TeamService;
import com.gy11233.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBloomFilter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

import static com.gy11233.contant.RedisConstant.TEAM_BLOOM_PREFIX;
import static com.gy11233.contant.RedisConstant.USER_BLOOM_PREFIX;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class BloomFilterTest {

    @Resource
    RBloomFilter<Long> userBloomFilter;

    @Resource
    RBloomFilter<Long> teamBloomFilter;

    @Resource
    UserService userService;

    @Resource
    TeamService teamService;


    @Test
    public void updateUserBloomFilter(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id");
        List<User> userList = userService.list(queryWrapper);

        for (int i = 0; i < 500; i ++) {
            userBloomFilter.add(userList.get(i).getId());
        }
    }

    @Test
    public void updateTeamBloomFilter(){
        List<Team> teamList = teamService.list(null);
        for (Team team : teamList) {
            teamBloomFilter.add(team.getId());
        }
    }
}
