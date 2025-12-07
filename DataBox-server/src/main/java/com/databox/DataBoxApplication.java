package com.databox;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.databox"})
@MapperScan(basePackages = {"com.databox.mappers"})
@EnableScheduling   //定时任务
@EnableAsync    //异步调用
@EnableTransactionManagement    //事务
public class DataBoxApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataBoxApplication.class, args);
    }
}
