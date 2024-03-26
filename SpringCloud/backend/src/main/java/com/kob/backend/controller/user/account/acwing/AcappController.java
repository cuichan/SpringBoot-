package com.kob.backend.controller.user.account.acwing;


import com.alibaba.fastjson.JSONObject;
import com.kob.backend.service.user.account.acwing.AcappService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AcappController {

    @Autowired
    private AcappService acappService;

    @GetMapping("/api/user/account/acwing/acapp/apply_code/")
    public JSONObject applyCode() {
        return acappService.applyCode();
    }

    @GetMapping("/api/user/account/acwing/acapp/receive_code/")
    public JSONObject receiveCode(@RequestParam Map<String, String> data) {
        String code = data.get("code");
        String state = data.get("state");
        return acappService.receiveCOde(code, state);
    }

}
