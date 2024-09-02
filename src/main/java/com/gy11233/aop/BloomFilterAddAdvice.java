package com.gy11233.aop;


import cn.hutool.bloomfilter.BloomFilter;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.redisson.api.RBloomFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 布隆过滤器添加通知
 */
@Component
@Aspect
@Log4j2
public class BloomFilterAddAdvice {

    @Resource
    RBloomFilter<Long> userBloomFilter;

    @Resource
    RBloomFilter<Long> teamBloomFilter;

    /**
     * 之后插入用户
     *
     */
    @AfterReturning(value = "execution(* com.gy11233.service.impl.UserServiceImpl.userRegister(..))", returning = "returnValue")
    public void afterInsertUser(Object returnValue) {
        userBloomFilter.add((Long) returnValue);
        log.info("add userId " + returnValue + " to BloomFilter");
    }

    /**
     * 添加团队后
     *
     */
    @AfterReturning(value = "execution(* com.gy11233.service.impl.TeamServiceImpl.addTeam(..))", returning = "returnValue")
    public void afterAddTeam(Object returnValue) {
        teamBloomFilter.add((Long) returnValue);
        log.info("add teamId " + returnValue + " to BloomFilter");
    }

}
