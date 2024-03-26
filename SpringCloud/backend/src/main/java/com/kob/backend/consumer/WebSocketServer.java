package com.kob.backend.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAuthentication;
import com.kob.backend.mapper.BotMapper;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{token}")
// 注意不要以'/'结尾

public class WebSocketServer {


    /**
     * 当我们接收到一个信息之后，比如当我们从匹配系统接收到某一局匹配成功的信息之后
     * 我们需要将这个消息发送给匹配成功的两名玩家、我们需要能够根据用户的id找到它对
     * 应的链接是谁，才可以利用这个链接向前端发请求，所以对于所有的websocket可见的一个全局变量
     * 用来存储所有的链接、对于所有的实列可见定义为静态变量
     * 由于每个实例在每个线程里面，所以公共的变量是线程安全的用线程安全的哈希表
     * 将userid映射到我们的websocket实例
     */
    final public static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();


    //用来认证session来自哪个用户

    private User user;

    //每个链接使用session来维护的

    private Session session = null;
    public Game game = null;

    /**
     * websocketserver，并不是一个标准的spring里的组件，注入的时候跟controller是有所区别的
     * 静态变量访问的时候需要用类名访问
     */
    public static UserMapper userMapper;
    public static RecordMapper recordMapper;
    private static BotMapper botMapper;
    public static RestTemplate restTemplate;
    private final static String addPlayerUrl = "http://127.0.0.1:3001/player/add/";
    private final static String removePlayerUrl = "http://127.0.0.1:3001/player/remove/";


    /**
     * 因为WebSocketServer是多实例的，所以需要此种方式注入;
     *
     * @param userMapper
     */

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }

    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
    }

    @Autowired
    public void setBotMapper(BotMapper botMapper){WebSocketServer.botMapper = botMapper;}

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        // 建立连接
        this.session = session;
        //建立连接的时候需要把它存下来;
        System.out.println("connected!!!");
        Integer userId = JwtAuthentication.getUserId(token);
        //从token中验证userId是否合法;
        this.user = userMapper.selectById(userId);
        if (this.user != null) {
            users.put(userId, this);
        } else {
            this.session.close();
        }
    }

    @OnClose
    public void onClose() {
        // 关闭链接
        System.out.println("disconnected!!!");
        if (this.user != null) {
            users.remove(this.user.getId());
        }
    }


    public static void startGame(Integer aId,Integer aBotId, Integer bId,Integer bBotId) {
        User a = userMapper.selectById(aId), b = userMapper.selectById(bId);

        Bot botA = botMapper.selectById(aBotId),botB = botMapper.selectById(bBotId);

        Game game = new Game(13, 14, 20,
                a.getId(),botA, b.getId(),botB);
        System.out.println("A是===" + a.getUsername());
        System.out.println("B是===" + b.getUsername());
        game.createMap();
        if(users.get(a.getId())!=null)
            users.get(a.getId()).game = game;
        if(users.get(b.getId())!=null)
            users.get(b.getId()).game = game;

        game.start();

        JSONObject respGame = new JSONObject();
        // 玩家的id以及横纵信息
        respGame.put("a_id", game.getPlayerA().getId());
        respGame.put("a_sx", game.getPlayerA().getSx());
        respGame.put("a_sy", game.getPlayerA().getSy());
        respGame.put("b_id", game.getPlayerB().getId());
        respGame.put("b_sx", game.getPlayerB().getSx());
        respGame.put("b_sy", game.getPlayerB().getSy());
        respGame.put("map", game.getG());

        JSONObject respA = new JSONObject();

        respA.put("event", "start-matching");
        respA.put("opponent_username", b.getUsername());
        respA.put("opponent_photo", b.getPhoto());
        respA.put("game", respGame);
        //用users获取a的链接
        if(users.get(a.getId())!=null)
            users.get(a.getId()).sendMessage(respA.toJSONString());
        //用这个链接将信息传回给前端
        JSONObject respB = new JSONObject();
        respB.put("event", "start-matching");
        respB.put("opponent_username", a.getUsername());
        respB.put("opponent_photo", a.getPhoto());
        respB.put("game", respGame);
        //用users获取b的链接
        if(users.get(b.getId())!=null)
            users.get(b.getId()).sendMessage(respB.toJSONString());
        //用这个链接将信息传回给前端
    }

    private void startMatching(Integer botId) {
        System.out.println("开始匹配-------====");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", this.user.getId().toString());
        data.add("rating",this.user.getRating().toString());
        data.add("bot_id",botId.toString());
        restTemplate.postForObject(addPlayerUrl, data, String.class);

    }

    private void stopMatching() {
        System.out.println("取消取消匹配-------====");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", this.user.getId().toString());
        restTemplate.postForObject(removePlayerUrl, data, String.class);
    }

    private void move(int direction) {
        if (game.getPlayerA().getId().equals(user.getId())) {
            if(game.getPlayerA().getBotId().equals(-1))
                game.setNextStepA(direction);
        } else if (game.getPlayerB().getId().equals(user.getId())) {
            if(game.getPlayerB().getBotId().equals(-1))
                game.setNextStepB(direction);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 从Client接收消息
        System.out.println("receive message!!!");
        JSONObject data = JSON.parseObject(message);
        String event = data.getString("event");
        if ("start-matching".equals(event)) {
            startMatching(data.getInteger("bot_id"));
        } else if ("stop-matching".equals(event)) {
            stopMatching();
        } else if ("move".equals(event)) {
            move(data.getInteger("direction"));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message) {
        // 向前端发送信息;
        //异步通信需要加一个锁、我们要知道这个链接对应的是谁

        synchronized (this.session) {
            try {
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
