package com.gy11233.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
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
        //过滤器名称
        String filterName = "userBloomFilter";
        // 预期插入数量
        long expectedInsertions = 10000L;
        // 错误比率
        double falseProbability = 0.01;
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(filterName);
        bloomFilter.tryInit(expectedInsertions, falseProbability);
        return bloomFilter;
    }

    /**
     * 创建队伍布隆过滤器
     * @return
     */
    @Bean
    public RBloomFilter<Long> teamBloomFilter(){
        //过滤器名称
        String filterName = "teamBloomFilter";

        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(filterName);
        bloomFilter.tryInit(EXPECTEDINSERTIONS, FALSEPROBABILITY);
        return bloomFilter;
    }

}
