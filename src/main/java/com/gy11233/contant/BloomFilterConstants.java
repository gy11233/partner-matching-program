package com.gy11233.contant;


/**
 * 布隆过滤器常量
 *
 */
public final class BloomFilterConstants {
    private BloomFilterConstants() {
    }
    /**
     * 预期包含记录
     */
    public static final long EXPECTEDINSERTIONS = 10000L;

    /**
     * 错误率
     */
    public static final double FALSEPROBABILITY = 0.01;

}
