package com.kob.backend.service.user.account.acwing;

import com.alibaba.fastjson.JSONObject;

public interface WebService {
    JSONObject applyCode();
    JSONObject receiveCOde(String code,String state);
}
