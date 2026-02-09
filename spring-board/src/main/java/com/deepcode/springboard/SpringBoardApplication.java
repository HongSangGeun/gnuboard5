package com.deepcode.springboard;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan(value = "com.deepcode.springboard", annotationClass = Mapper.class)
@EnableAsync
@EnableScheduling
public class SpringBoardApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBoardApplication.class, args);
    }
}

