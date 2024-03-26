package com.kob.BotRunningSystem.service.impl;


import com.kob.BotRunningSystem.service.BotRuningService;
import com.kob.BotRunningSystem.service.impl.utils.BotPool;
import org.springframework.stereotype.Service;

@Service
public class BotRuningServiceImpl implements BotRuningService {

    public final static BotPool botPool = new BotPool();
    @Override
    public String addBot(Integer userId, String botCode, String input) {

        System.out.println("Add Bot " + userId+ " "+ botCode+ " "+ input);
        botPool.addBot(userId,botCode,input);
        return "add Bot success!!!";
    }
}
