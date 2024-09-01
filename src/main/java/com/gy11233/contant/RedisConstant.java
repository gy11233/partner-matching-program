package com.gy11233.contant;

public interface RedisConstant {
    String SYSTEM_ID = "partner:team:join";
    String USER_JOIN_TEAM = "partner:team:join:";
    String USER_GEO_KEY = "partner:user:geo";
    String USER_ADD_KEY = "partner:user:add";
    String USER_RECOMMEND_KEY = "partner:user:recommend";
    String USER_COUNT_KEY = "partner:user:match:count";
    String  USER_PRECACHE_JOB = "partner:precachejob:recommend:lock";
    String USER_RECOMMEND_LOCK = "partner:user:recommend:lock";

    String USER_BLOOM_LOCK = "partner:user:bloom";
    String TEAM_BLOOM_LOCK = "partner:team:bloom";

    /**
     * 用户布隆前缀
     */
    String USER_BLOOM_PREFIX = "partner:user:bloom";
    /**
     * 队伍布隆前缀
     */
    String TEAM_BLOOM_PREFIX = "partner:team:bloom";

    /**
     * 最小缓存随机时间
     */
    int MINIMUM_CACHE_RANDOM_TIME = 2;
    /**
     * 最大缓存随机时间
     */
    int MAXIMUM_CACHE_RANDOM_TIME = 3;
    /**
     * 缓存时间偏移
     */
    int CACHE_TIME_OFFSET = 10;
}
