package com.gy11233.aop;


import cn.hutool.bloomfilter.BloomFilter;
import com.gy11233.common.ErrorCode;
import com.gy11233.exception.BusinessException;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.redisson.api.RBloomFilter;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;



/**
 * 布隆过滤器搜索通知
 *
 */
@Component
@Aspect
@Log4j2
public class BloomFilterSearchAdvice {
    @Resource
    RBloomFilter<Long> userBloomFilter;

    @Resource
    RBloomFilter<Long> teamBloomFilter;


    /**
     * 发现用户通过id
     *
     * @param joinPoint 连接点
     */
    @Before("execution(* com.gy11233.controller.UserController.getUserById(..))")
    public void findUserById(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        boolean contains = userBloomFilter.contains((Long) args[0]);
        if (!contains) {
            log.error("没有在 BloomFilter 中找到该 userId");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有找到该用户");
        }
    }

    /**
     * 发现团队通过id
     *
     * @param joinPoint 连接点
     */
    @Before("execution(* com.gy11233.controller.TeamController.getTeam(..))")
    public void findTeamById(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        boolean contains = teamBloomFilter.contains((Long) args[0]);
        if (!contains) {
            log.error("没有在 BloomFilter 中找到该 teamId");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有找到该队伍");
        }
    }

}
