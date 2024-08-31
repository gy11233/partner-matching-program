package com.gy11233.contant;

public interface RedisConstant {
    String SYSTEM_ID = "partner:team:join";
    String USER_JOIN_TEAM = "partner:team:join:";
    String USER_GEO_KEY = "partner:user:geo";
    String USER_ADD_KEY = "partner:user:add";
    String USER_RECOMMEND_KEY = "partner:user:recommend";
    String USER_COUNT_KEY = "partner:user:match:count";
    String  USER_PRECACHE_JOB = "partner:precachejob:recommend:lock";

    public static final String LOGIN_USER_KEY = "partner:login:token:";

    public static final Long LOGIN_USER_TTL = 15L;
    /**
     * 注册验证码键
     */
    public static final String REGISTER_CODE_KEY = "partner:register:";
    /**
     * 注册验证码过期时间
     */
    public static final Long REGISTER_CODE_TTL = 15L;
    /**
     * 用户更新电话键
     */
    public static final String USER_UPDATE_PHONE_KEY = "partner:user:update:phone:";
    /**
     * 用户更新电话过期时间
     */
    public static final Long USER_UPDATE_PHONE_TTL = 15L;
    /**
     * 用户更新邮件键
     */
    public static final String USER_UPDATE_EMAIL_KEY = "partner:user:update:email:";
    /**
     * 用户更新邮件过期时间
     */
    public static final Long USER_UPDATE_EMAIL_TTL = 15L;
    /**
     * 用户忘记密码键
     */
    public static final String USER_FORGET_PASSWORD_KEY = "partner:user:forget:";
    /**
     * 用户忘记密码过期时间
     */
    public static final Long USER_FORGET_PASSWORD_TTL = 15L;
    /**
     * 博客推送键
     */
    public static final String BLOG_FEED_KEY = "partner:feed:blog:";
    /**
     * 新博文消息键
     */
    public static final String MESSAGE_BLOG_NUM_KEY = "partner:message:blog:num:";
    /**
     * 新点赞消息键
     */
    public static final String MESSAGE_LIKE_NUM_KEY = "partner:message:like:num:";
    /**
     * 用户推荐缓存
     */
    /**
     * 最小缓存随机时间
     */
    public static final int MINIMUM_CACHE_RANDOM_TIME = 2;
    /**
     * 最大缓存随机时间
     */
    public static final int MAXIMUM_CACHE_RANDOM_TIME = 3;
    /**
     * 缓存时间偏移
     */
    public static final int CACHE_TIME_OFFSET = 10;
}
