package com.gnuboard.springboard;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = "com.gnuboard.springboard", annotationClass = Mapper.class)
public class SpringBoardApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBoardApplication.class, args);
    }
}
