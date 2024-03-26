package com.kob.backend.service.user.account.acwing;

import com.alibaba.fastjson.JSONObject;

/**
 * @author asus
 */
public interface AcappService {
    JSONObject applyCode();
    JSONObject receiveCOde(String code,String state);
}
