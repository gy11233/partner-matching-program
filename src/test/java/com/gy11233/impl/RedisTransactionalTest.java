package com.gy11233.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class RedisTransactionalTest {

    @Resource
    RedisTemplate redisTemplate;

    @Test
    @Transactional(rollbackFor = Exception.class)
    public void test1(){
        redisTemplate.opsForValue().set("test1", "1");
//        int i = 1 / 0;
        // 如果不开启redis的事务支持，就算有Transactional注解也无法回滚
        redisTemplate.opsForValue().set("test2", "2");
        redisTemplate.exec(); // 走事务流程之后需要主动提交事务
    }
}
