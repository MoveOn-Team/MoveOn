package com.moveon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling // 대관 정보를 매달 자동으로 다시 받아오기 위해 켠다
@SpringBootApplication
public class MoveOnApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoveOnApplication.class, args);
    }

}
