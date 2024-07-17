package com.gy11233.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gy11233.model.domain.User;
import com.gy11233.service.UserService;
import lombok.extern.slf4j.Slf4j;
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

    List<Long> importantUsers = Arrays.asList(1L, 2L, 3L, 8L);
    // 每天执行
    @Scheduled(cron = "0 58 23 * * *")
    public void DoPreCacheJob(){
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


}
