package com.kob.BotRunningSystem;


import com.kob.BotRunningSystem.service.impl.BotRuningServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author asus
 */
@SpringBootApplication
public class BotRuningSyatemApplication {
    public static void main(String[] args) {
        BotRuningServiceImpl.botPool.start();
        SpringApplication.run(BotRuningSyatemApplication.class,args);
    }
}