package com.kob.backend.consumer.utils;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author asus
 */
public class Game extends Thread {
    final private Integer rows;
    //列数

    final private Integer cols;
    final private Integer inner_walls_count;
    final private int[][] g;
    final private static int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
    private final Player playerA;
    private final Player playerB;

    private Integer nextStepA = null;

    //两名玩家的下一步操作;

    private Integer nextStepB = null;

    /**
     * 因为涉及到同时对nextStepA或者nextStepB的读写，所以需要加锁；
     */
    private ReentrantLock lock = new ReentrantLock();
    private final static String addBotUrl = "http://127.0.0.1:3002/bot/add/";

    private String status = "playing";
    // playing -> finished

    private String loser = "";

    // all: 平局，A：A输，B：B输

    public Game(Integer rows, Integer cols, Integer inner_walls_count, Integer idA, Bot botA, Integer idB,Bot botB) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];

        Integer botIdA = -1,botIdB = -1;
        String botCodeA = "",botCodeB = "";
        if(botA!=null){
            botIdA = botA.getId();
            botCodeA = botA.getContent();
        }

        if(botB!=null){
            botIdB = botB.getId();
            botCodeB = botB.getContent();
        }
        playerA = new Player(idA,botIdA,botCodeA, rows - 2, 1, new ArrayList<>());
        playerB = new Player(idB,botIdB, botCodeB, 1, cols - 2, new ArrayList<>());
    }

    public Player getPlayerA() {
        return playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }


    public void setNextStepA(Integer nextStep) {
        lock.lock();
        try {
            System.out.println("设置A的下一个方向为---" + nextStep);
            this.nextStepA = nextStep;
        } finally {
            lock.unlock();
        }
    }

    public void setNextStepB(Integer nextStep) {  //加锁
        lock.lock();
        try {
            System.out.println("设置B的下一个方向为---" + nextStep);
            this.nextStepB = nextStep;
        } finally {
            lock.unlock();
        }

    }

    public int[][] getG() {
        return g;
    }

    private boolean check_connectivity(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty) return true;
        g[sx][sy] = 1;

        for (int i = 0; i < 4; i++) {
            int x = sx + dx[i], y = sy + dy[i];
            if (x >= 0 && x < this.rows && y >= 0 && y < this.cols && g[x][y] == 0) {
                if (check_connectivity(x, y, tx, ty)) {
                    g[sx][sy] = 0;
                    return true;
                }
            }
        }

        g[sx][sy] = 0;
        return false;
    }

    private boolean draw() {  // 画地图
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                g[i][j] = 0;
            }
        }

        for (int r = 0; r < this.rows; r++) {
            g[r][0] = g[r][this.cols - 1] = 1;
        }
        for (int c = 0; c < this.cols; c++) {
            g[0][c] = g[this.rows - 1][c] = 1;
        }

        Random random = new Random();
        for (int i = 0; i < this.inner_walls_count / 2; i++) {
            for (int j = 0; j < 1000; j++) {
                int r = random.nextInt(this.rows);
                int c = random.nextInt(this.cols);

                if (g[r][c] == 1 || g[this.rows - 1 - r][this.cols - 1 - c] == 1)
                    continue;
                if (r == this.rows - 2 && c == 1 || r == 1 && c == this.cols - 2)
                    continue;

                g[r][c] = g[this.rows - 1 - r][this.cols - 1 - c] = 1;
                break;
            }
        }

        return check_connectivity(this.rows - 2, 1, 1, this.cols - 2);
    }

    public void createMap() {
        for (int i = 0; i < 1000; i++) {
            if (draw())
                break;
        }
    }

    private String getInput(Player player) {    // 将当前局面信息编码成字符串
        // 地图#my.sx#my.sy#my操作#you.sx#you.sy#you操作
        Player me, you;
        if(playerA.getId().equals(player.getId())) {
            me = playerA;
            you = playerB;
        } else {
            me = playerB;
            you = playerA;
        }

        return getMapString() + "#" +
                me.getSx() + "#" +
                me.getSy() + "#(" +
                me.getStepsString() + ")#" +    // 加()是为了预防操作序列为空
                you.getSx() + "#" +
                you.getSy() + "#(" +
                you.getStepsString() + ")";
    }

    public void sendBotCode(Player player){
        if(player.getBotId().equals(-1)) {return;}
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", player.getId().toString());
        data.add("bot_code", player.getBotCode());
        data.add("input", getInput(player));
        System.out.println("到这一步了！！！！----=====");
        WebSocketServer.restTemplate.postForObject(addBotUrl, data, String.class);
    }


    private boolean nextStep() throws InterruptedException { //等待蛇的下一步操作；
        /**
         * 此处是Game线程的读线程;
         * 所以需要加上锁，来保证读取nextStepA/B时数据的准确性;
         */
        Thread.sleep(200);
        // 每秒五步操作，因此第一步操作是在200ms后判断是否接收到输入。并给地图初始化时间

        sendBotCode(playerA);
        sendBotCode(playerB);

        for (int i = 0; i < 50; i++) {
            try {
                Thread.sleep(100);
                lock.lock();
                try {
                    if (nextStepA != null && nextStepB != null) {
                        playerA.getSteps().add(nextStepA);
                        playerB.getSteps().add(nextStepB);
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void sendResult() { //向两个Client公布结果;
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        saveToDataBase();
        sendAllMessage(resp.toJSONString());

    }

    private void sendAllMessage(String Message) {
        if(WebSocketServer.users.get(playerA.getId())!=null)
            WebSocketServer.users.get(playerA.getId()).sendMessage(Message);
        if(WebSocketServer.users.get(playerB.getId())!=null)
            WebSocketServer.users.get(playerB.getId()).sendMessage(Message);

    }

    private boolean check_valid(List<Cell> cellsA, List<Cell> cellsB) {
        int n = cellsA.size();
        Cell cell = cellsA.get(n - 1);
        //取出最后一位(蛇头)
        if (g[cell.x][cell.y]==1) {
            //如果最后一位是墙的话
            return false;
        }
        for (int i = 0; i < n - 1; i++) {
            if (cellsA.get(i).x == cell.x && cellsA.get(i).y == cell.y) {
                //如果自己碰到自己就算输
                return false;
            }
        }

        for (int i = 0; i < n - 1; i++) {
            if (cellsB.get(i).x == cell.x && cellsB.get(i).y == cell.y) {
                //如果主动碰到对手也算自己输
                return false;
            }
        }
        return true;
    }
    private void judge() { //判断下一步是否合法;
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();
        boolean validA = check_valid(cellsA, cellsB);
        boolean validB = check_valid(cellsB, cellsA);
        if (!validA || !validB) {
            status = "finished";
            if (!validA && !validB) {
                loser = "all";
            } else if (!validA) {
                loser = "A";
            } else {
                loser = "B";
            }
        }
    }

    private void sendMove() { //传递移动信息;
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_direction", nextStepA);
            resp.put("b_direction", nextStepB);
            nextStepA = nextStepB = null;
            sendAllMessage(resp.toJSONString());
        } finally {
            lock.unlock();
        }

    }

    private void updateUserRating(Player player,Integer rating){
        User user = WebSocketServer.userMapper.selectById(player.getId());
        user.setRating(rating);
        WebSocketServer.userMapper.updateById(user);
    }

    private void saveToDataBase(){

        Integer ratingA = WebSocketServer.userMapper.selectById(playerA.getId()).getRating();
        Integer ratingB = WebSocketServer.userMapper.selectById(playerB.getId()).getRating();

        if("A".equals(loser)){
            ratingA -= 2;
            ratingB += 5;
        }else if("B".equals(loser)){
            ratingA+=5;
            ratingB-=2;
        }
        updateUserRating(playerA,ratingA);
        updateUserRating(playerB,ratingB);

        Record record = new Record(
                null,
                //因为之前创建数据库时是把id定义为自动递增，所以这里不用手动传id
                playerA.getId(),
                playerA.getSx(),
                playerA.getSy(),
                playerB.getId(),
                playerB.getSx(),
                playerB.getSy(),
                playerA.getStepsString(),
                playerB.getStepsString(),
                getMapString(),
                loser,
                new Date()
        );

        WebSocketServer.recordMapper.insert(record);
        //ws里数据库的注入
    }

    private String getMapString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                res.append(g[i][j]);
            }
        }
        return res.toString();
    }


    @Override
    public void run() {
        for (int i = 0; i < 1000; i++) {
            try {
                if (nextStep()) {
                    judge();
                    if (status.equals("playing")) {
                        sendMove();
                    } else {
                        System.out.println("Run--->从这里发送的Result!!!!");
                        sendResult();
                        break;
                    }
                } else {
                    /**
                     * 涉及到变量的读取写入都看看是否需要加锁;
                     */
                    status = "finished";
                    lock.lock();
                    try {
                        if (nextStepA == null && nextStepB == null) {
                            loser = "all";
                        } else if (nextStepA == null) {
                            loser = "A";
                        } else {
                            loser = "B";
                        }
                    } finally {
                        lock.unlock();
                    }
                    System.out.println("两者都未曾输入--->从这里发送的Result!!!!");
                    sendResult();
                    break;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

