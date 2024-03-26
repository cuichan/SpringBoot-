package com.kob.backend.controller.record;


import com.alibaba.fastjson.JSONObject;
import com.kob.backend.service.record.GetRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GeRecordtListController {

    @Autowired
    private GetRecordService getRecordService;

    @GetMapping("/api/record/getlist/")
    public JSONObject getrecordList(@RequestParam Map<String,String> data){
        Integer page = Integer.valueOf(data.get("page"));
        return getRecordService.getList(page);
    }

}
