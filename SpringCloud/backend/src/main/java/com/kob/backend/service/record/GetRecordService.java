package com.kob.backend.service.record;

import com.alibaba.fastjson.JSONObject;

public interface GetRecordService {
    JSONObject getList(Integer page);
}
