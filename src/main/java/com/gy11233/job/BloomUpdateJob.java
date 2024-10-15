package com.gy11233.job;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gy11233.common.ErrorCode;
import com.gy11233.contant.RedisConstant;
import com.gy11233.exception.BusinessException;
import com.gy11233.model.domain.Team;
import com.gy11233.model.domain.User;
import com.gy11233.service.TeamService;
import com.gy11233.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.gy11233.contant.BloomFilterConstants.*;

@Component
@Slf4j
public class BloomUpdateJob {
    @Resource
    RBloomFilter<Long> userBloomFilter;

    @Resource
    RBloomFilter<Long> teamBloomFilter;


    @Resource
    UserService userService;

    @Resource
    TeamService teamService;

    @Resource
    RedissonClient redissonClient;

    @Resource
    RedisTemplate<String, Object> redisTemplate;



    @Scheduled(cron = "0 0 4 5,15,25 * *")
    public void updateUserBloomFilter(){
        RLock lock = redissonClient.getLock(RedisConstant.USER_BLOOM_LOCK);
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                log.info("start updateUserBloomFilter");
                // 定义一个新的布隆过滤器
                RBloomFilter<Long> bloomFilterNew = redissonClient.getBloomFilter(USER_BLOOM_FILTER_TEMP);
                bloomFilterNew.tryInit(EXPECTED_INSERTIONS, FALSE_PROBABILITY);
                // 更新数据
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.select("id");
                List<User> userList = userService.list(queryWrapper);
                for (int i = 0; i < 1000 & i < userList.size(); i ++) {
                    bloomFilterNew.add(userList.get(i).getId());
                }
//                 删除旧的布隆过滤器 并重命名
                String script = "redis.call('del' , KEYS[1])\n" +
                        "redis.call('del' , \"\".. KEYS[1]..\":config\")\n" +
                        "redis.call('rename' , KEYS[2] , KEYS[1])\n" +
                        "redis.call('rename' , \"\".. KEYS[2]..\":config\" , \"{\".. KEYS[1]..\"}:config\" )\n" +
                        "return 1 " ;
                //执行lua脚本
                Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(USER_BLOOM_FILTER_NAME, USER_BLOOM_FILTER_TEMP));
                if (result == null || result == 0) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "lua脚本更新失败");
                }
                log.info("更新结束");
            }
        } catch (InterruptedException e) {
            log.error("updateUserBloomFilter lock error");
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                log.info("unlock{}", Thread.currentThread().getId());
                lock.unlock();
            }
        }

    }

    @Scheduled(cron = "0 0 5 5,15,25 * *")
    public void updateTeamBloomFilter(){
        RLock lock = redissonClient.getLock(RedisConstant.TEAM_BLOOM_LOCK);
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                log.info("start updateTeamBloomFilter");
                // 删除旧的布隆过滤器
                boolean delete = teamBloomFilter.delete();
                if (!delete) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍布隆过滤器失败");
                }
                // 重新初始化布隆过滤器
                teamBloomFilter.tryInit(EXPECTED_INSERTIONS, FALSE_PROBABILITY);


                RBloomFilter<Long> bloomFilterNew = redissonClient.getBloomFilter(TEAM_BLOOM_FILTER_TEMP);
                bloomFilterNew.tryInit(EXPECTED_INSERTIONS, FALSE_PROBABILITY);
                // 更新数据
                // 更新数据
                QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
                queryWrapper.select("id");
                List<Team> teamList = teamService.list(queryWrapper);
                for (Team team : teamList) {
                    teamBloomFilter.add(team.getId());
                }
//                 删除旧的布隆过滤器 并重命名
                String script = "redis.call('del' , KEYS[1])\n" +
                        "redis.call('del' , \"\".. KEYS[1]..\":config\")\n" +
                        "redis.call('rename' , KEYS[2] , KEYS[1])\n" +
                        "redis.call('rename' , \"\".. KEYS[2]..\":config\" , \"{\".. KEYS[1]..\"}:config\" )\n" +
                        "return 1 " ;
                //执行lua脚本
                Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(TEAM_BLOOM_FILTER_NAME, TEAM_BLOOM_FILTER_TEMP));
                if (result == null || result == 0) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "lua脚本更新失败");
                }
                log.info("更新结束");
            }
        } catch (InterruptedException e) {
            log.error("updateTeamBloomFilter lock error");
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                log.info("unlock{}", Thread.currentThread().getId());
                lock.unlock();
            }
        }

    }

}
