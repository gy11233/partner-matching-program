package com.gy11233.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gy11233.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
public class RedissonTest {

    @Resource
    RedissonClient redissonClient;

    @Test
    public void test(){

        // list
        RList<Object> list = redissonClient.getList("test-list");
        list.add("test");
        list.add("cook");
        list.add("yes");
        System.out.println(list);
        list.remove(0);
    }

    @Test
    public void testWatchDog() {

        RLock lock = redissonClient.getLock("partner:precachejob:docache:lock");

        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                System.out.println("getlock" + Thread.currentThread().getId());
                Thread.sleep(30000);
            }
            } catch(InterruptedException e){
                log.error("PreCacheJob.DoPreCacheJob lock error");
            } finally{
                // 只能释放自己的锁
                if (lock.isHeldByCurrentThread()) {
                    System.out.println("unlock" + Thread.currentThread().getId());
                    lock.unlock();
                }
            }

    }

}
