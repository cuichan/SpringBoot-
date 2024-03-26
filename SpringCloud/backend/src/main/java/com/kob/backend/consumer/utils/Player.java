package com.kob.backend.consumer.utils;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {

    private Integer id;
    private Integer botId;
    //-1的话表示人工操作;

    private String botCode;
    private Integer sx;
    private Integer sy;
    private List<Integer> steps;


    private boolean check_tail_increasing(int step) { //检测当前回合蛇的长度是否增加
        if (step <= 10) return true;
        else {
            return step % 3 == 1;
        }
    }


    public List<Cell> getCells() {
        List<Cell> res = new ArrayList<>();
        //存放蛇的身体
        int[][] fx = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
        int x = sx, y = sy;
        res.add(new Cell(x, y));
        int step = 0;
        //回合数
        for (int d : steps) {
            x += fx[d][0];
            y += fx[d][1];
            res.add(new Cell(x, y));
            if (!check_tail_increasing(++step)) {
                res.remove(0);
            }
        }
        return res;
    }
    public String getStepsString(){
        StringBuilder res = new StringBuilder();
        for(int d : steps){
            res.append(d);
        }
        return res.toString();
    }
}
