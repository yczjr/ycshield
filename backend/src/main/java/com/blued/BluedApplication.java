package com.blued;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan("com.blued.**.mapper")
@SpringBootApplication
public class BluedApplication {
    public static void main(String[] args) {
        SpringApplication.run(BluedApplication.class, args);
    }
}
