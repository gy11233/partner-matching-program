package com.gy11233.contant;


/**
 * 布隆过滤器常量
 *
 */
public final class BloomFilterConstants {
    private BloomFilterConstants() {
    }

    public static final String TEAM_BLOOM_FILTER_NAME = "teamBloomFilter";
    public static final String TEAM_BLOOM_FILTER_TEMP = "teamBloomTemp";


    public static final String USER_BLOOM_FILTER_NAME = "userBloomFilter";
    public static final String USER_BLOOM_FILTER_TEMP = "userBloomTemp";
    /**
     * 预期包含记录
     */
    public static final long EXPECTED_INSERTIONS = 10000L;

    /**
     * 错误率
     */
    public static final double FALSE_PROBABILITY = 0.01;

}
