package com.kob.matchingsystem.service.impl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author asus
 */

@Component
public class MatchingPool extends Thread{

    /**
     * Players数组涉及到对他add或者remove的操作，所以改变时需要加锁;
     *
     */

    private static List<Player> players = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();
    private static RestTemplate restTemplate;

    private final static String startGameUrl = "http://127.0.0.1:3000/pk/start/game/";


    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        MatchingPool.restTemplate = restTemplate;
    }
    public void addPlayer(Integer userId,Integer rating,Integer botId){
        lock.lock();
        try{
            players.add(new Player(userId,rating,botId,0));
        }finally {
            lock.unlock();
        }
    }

    public void removePlayer(Integer userId){
        lock.lock();
        try{
            /**
             * 在ArrayList里只能够线性删除，所以先开一个新的数组，遍历;
             */
            List<Player> newPlayers = new ArrayList<>();
            for(Player player : players) {
                if(!player.getUserId().equals(userId)) {
                    newPlayers.add(player);
                }
            }
            players = newPlayers;
        }finally {
            lock.unlock();
        }
    }

    private boolean checkMatched(Player a,Player b){
        // 获取两名分差
        int ratingDelta = Math.abs(a.getRating() - b.getRating());
        // min: 若取min则代表两者分差都小于 等待时间 * 10，实力差距最接近
        // max: 若取max则代表有一方分差小于 等待时间 * 10，实力差距可能会大
        int waitingTime = Math.min(a.getWaitingtime(), b.getWaitingtime());
        return ratingDelta <= waitingTime * 10;
    }
    private void sendResult(Player a,Player b){
        System.out.println("send result: " + a + " " + b);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("a_id", a.getUserId().toString());
        data.add("a_bot_id",a.getBotId().toString());
        data.add("b_id", b.getUserId().toString());
        data.add("b_bot_id",b.getBotId().toString());
        restTemplate.postForObject(startGameUrl, data, String.class);
    }
    private void matchingPlayer(){
        boolean[] used = new boolean[players.size()];
        for (int i = 0; i < players.size(); i++) {
            if(used[i])continue;
            for (int j = i+1; j < players.size(); j++) {
                if(used[j])continue;
                Player a = players.get(i);
                Player b = players.get(j);
                if(checkMatched(a,b)){
                    used[i] = used[j] = true;
                    sendResult(a,b);
                    break;
                }

            }
        }
        List<Player> newPlayers = new ArrayList<>();
        for(int i = 0; i < players.size(); i++) {
            if(!used[i]) {
                newPlayers.add(players.get(i));
            }
        }
        players = newPlayers;
    }

    private void increaseWaiting(){
        for (Player player : players){
            player.setWaitingtime(player.getWaitingtime()+1);
        }
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(1000);
                lock.lock();
                try{
                    increaseWaiting();
                    matchingPlayer();
                }finally {
                    lock.unlock();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
