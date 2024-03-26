package com.kob.matchingsystem.service.impl;

import com.kob.matchingsystem.service.MatchingService;
import com.kob.matchingsystem.service.impl.utils.MatchingPool;
import org.springframework.stereotype.Service;

@Service
public class MatchingServiceImpl implements MatchingService {


    public final static MatchingPool matchingPool = new MatchingPool();
    //匹配线程从始至终就一个池子；


    @Override
    public String addPlayer(Integer userId, Integer rating,Integer botId) {
        System.out.println("add player: "+ userId+ " "+ rating+" "+botId);

        matchingPool.addPlayer(userId,rating,botId);
        return "add player successes!!";

    }

    @Override
    public String removePlayer(Integer userId) {
        System.out.println("remove Player : "+ userId);
        matchingPool.removePlayer(userId);
        return "remove Player success!!!";

    }
}
