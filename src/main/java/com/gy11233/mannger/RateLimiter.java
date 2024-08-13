package com.gy11233.mannger;

import com.gy11233.common.ErrorCode;
import com.gy11233.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RateLimiter {

    @Resource
    private RedissonClient redissonClient;

    public void doRateLimiter(String key) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.setRate(RateType.OVERALL, 1, 1, RateIntervalUnit.MINUTES);
        boolean b = rateLimiter.tryAcquire();
        if (!b) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
