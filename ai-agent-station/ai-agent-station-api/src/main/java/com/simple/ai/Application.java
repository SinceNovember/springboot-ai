package com.simple.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@Configurable
@EnableScheduling
public class Application implements CommandLineRunner {
//
//    @Resource
//    private IAiAgentPreheatService aiAgentArmoryService;

    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }

    @Override
    public void run(String... args) throws Exception {
//        log.info("预热AiAgent服务，开始");
//        aiAgentArmoryService.preheat();
//        log.info("预热AiAgent服务，完成");
    }

}