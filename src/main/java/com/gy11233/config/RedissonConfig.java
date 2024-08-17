package com.gy11233.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.redisson.config.Config;

import javax.annotation.Resource;

/**
 * Redisson配置
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String host;

    private String port;

    private int redissonDB;

    private String password;

    @Bean
    public RedissonClient redissonClient(){
        // 1.创建配置
        Config config = new Config();
        String redissonAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setAddress(redissonAddress).setDatabase(redissonDB).setPassword(password);
        // 2. 获取实例
        return Redisson.create(config);
    }
}
