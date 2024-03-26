package com.kob.BotRunningSystem.service.impl.utils;

import com.kob.BotRunningSystem.service.impl.utils.Bot;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author asus
 */



public class BotPool extends Thread{



    private final static ReentrantLock lock  =new ReentrantLock();
    /**
     * 因为需要来一段代码评测一次，所以不能让他持续运行，当有任务时才唤醒它;
     */
    private final Condition condition = lock.newCondition();

    private final Queue<Bot> bots = new LinkedList<>();


    public void addBot(Integer userId,String botCode,String input){
        lock.lock();
        try{
            bots.add(new Bot(userId,botCode,input));
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }

    private void consume(Bot bot) throws InterruptedException {
        Consumer consumer = new Consumer();
        consumer.startTimeOut(2000,bot);
    }

    @Override
    public void run() {
        while(true){
            lock.lock();
            if(bots.isEmpty()){
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lock.unlock();
                    break;
                }
            }else {
                Bot bot = bots.remove();
                lock.unlock();
                try {
                    consume(bot);
                    ///这个函数比较耗时，所以需要加到unlock之后;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
