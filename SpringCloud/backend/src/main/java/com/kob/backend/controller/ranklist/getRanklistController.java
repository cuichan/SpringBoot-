package com.kob.backend.controller.ranklist;


import com.alibaba.fastjson.JSONObject;
import com.kob.backend.service.ranklist.getranklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class getRanklistController {


    @Autowired
    private getranklistService getranklistService;

    @GetMapping("/api/ranklist/getranklist/")
    public JSONObject getlist(@RequestParam Map<String,String>data){
        Integer page = Integer.valueOf(data.get("page"));
        return getranklistService.getLsit(page);
    }
}
