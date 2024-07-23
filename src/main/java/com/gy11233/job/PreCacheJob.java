package com.gy11233.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gy11233.model.domain.User;
import com.gy11233.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.*;
/**
 * 缓存预热
 */

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    List<Long> importantUsers = Arrays.asList(1L, 2L, 3L, 8L);
    // 每天执行
    @Scheduled(cron = "0 37 17 * * *")
    public void DoPreCacheJob(){

        RLock lock = redissonClient.getLock("partner:precachejob:docache:lock");
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                System.out.println("getlock" + Thread.currentThread().getId());
                for (Long userId: importantUsers) {

                    String key = String.format("partner:user:recommend:%s", userId) ;
                    ValueOperations<String, Object> operations = redisTemplate.opsForValue();
                    // 没有缓存查询并加入缓存
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> list = userService.page(new Page<>(1, 20), queryWrapper);
                    try {
                        operations.set(key, list, 24, TimeUnit.HOURS);
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
                System.out.println("unlock" + Thread.currentThread().getId());
                lock.unlock();
            }
        }

    }


}
