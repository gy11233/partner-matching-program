package com.gy11233;



import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 *
 */
//@SpringBootApplication
@MapperScan("com.gy11233.mapper")
@SpringBootApplication(scanBasePackages ="com.gy11233")
@EnableScheduling
public class PartnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartnerApplication.class, args);
    }

}
