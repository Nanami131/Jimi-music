package com.example.jimi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.jimi.mapper")
public class MusicApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicApplication.class, args);
    }

}

