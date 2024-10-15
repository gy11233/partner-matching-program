package com.gy11233.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

import static com.gy11233.contant.BloomFilterConstants.*;

/**
 * 布隆过滤器初始化配置
 */
@Configuration
public class BloomFilterConfig {
    @Resource
    private RedissonClient redissonClient;

    /**
     * 创建用户布隆过滤器
     * @return
     */
    @Bean
    public RBloomFilter<Long> userBloomFilter(){

        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(USER_BLOOM_FILTER_NAME);
        bloomFilter.tryInit(EXPECTED_INSERTIONS, FALSE_PROBABILITY);
        return bloomFilter;
    }

    /**
     * 创建队伍布隆过滤器
     * @return
     */
    @Bean
    public RBloomFilter<Long> teamBloomFilter(){
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(TEAM_BLOOM_FILTER_NAME);
        bloomFilter.tryInit(EXPECTED_INSERTIONS, FALSE_PROBABILITY);
        return bloomFilter;
    }

}
