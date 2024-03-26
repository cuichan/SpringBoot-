package com.kob.matchingsystem;

import com.kob.matchingsystem.service.impl.MatchingServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author asus
 */
@SpringBootApplication
public class MatchSystemApplication {
    public static void main(String[] args) {
        MatchingServiceImpl.matchingPool.start();
        SpringApplication.run(MatchSystemApplication.class, args);
    }
}