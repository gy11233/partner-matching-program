package com.gy11233.job;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gy11233.contant.RedisConstant;
import com.gy11233.model.domain.User;
import com.gy11233.model.vo.UserFriendsVo;
import com.gy11233.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 缓存预热
 */

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    RBloomFilter<Long> userBloomFilter;

    List<Long> importantUsers = Arrays.asList(1L, 2L, 3L, 8L);

    /**
     * 用户推荐列表缓存预热
     */
    // 每天执行
    @Scheduled(cron = "0 0 3 * * *")
    public void DoPreCacheJob(){

        RLock lock = redissonClient.getLock(RedisConstant.USER_PRECACHE_JOB);
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                log.info("start DoPreCacheJob");
                for (Long userId : importantUsers) {
                    User user = userService.getById(userId);
                    String key = RedisConstant.USER_RECOMMEND_KEY + ":" + userId;
                    ListOperations<String, String> operations = stringRedisTemplate.opsForList();
                    // 删除旧的缓存
                    stringRedisTemplate.delete(key);
                    // 更新缓存
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    queryWrapper.ne("id", userId);
                    Page<User> list = userService.page(new Page<>(1, 20), queryWrapper);
                    List<UserFriendsVo> userV0List = list.getRecords().stream().map(
                            userTemp -> userService.getUserFriendsVo(userTemp, user)).collect(Collectors.toList());
                    List<String> userVOJsonList = userV0List.stream().map(JSONUtil::toJsonStr).collect(Collectors.toList());

                    try {
                        operations.rightPushAll(key, userVOJsonList);
                        stringRedisTemplate.expire(key, 24, TimeUnit.HOURS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("PreCacheJob.DoPreCacheJob lock error");
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                log.info("unlock{}", Thread.currentThread().getId());
                lock.unlock();
            }
        }

    }

}
