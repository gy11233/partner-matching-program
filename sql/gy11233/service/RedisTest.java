package com.gy11233.service;
import java.util.Date;

import com.gy11233.config.RedisTemplateConfig;
import com.gy11233.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate redisTemplate;
//    private RedisTemplateConfig redisTemplate;

    @Test
    public void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();

        // 增
        valueOperations.set("testString", "dog");
        valueOperations.set("testInt", 1);
        valueOperations.set("testDouble", 2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("dog");

        valueOperations.set("testUser", user);

        // 查
        String s = (String)valueOperations.get("testString");
        Assertions.assertTrue("dog".equals(s));

    }

}
