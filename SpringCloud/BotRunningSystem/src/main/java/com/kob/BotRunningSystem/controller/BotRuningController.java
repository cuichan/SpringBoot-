package com.kob.BotRunningSystem.controller;

import com.kob.BotRunningSystem.service.BotRuningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author asus
 */

@RestController
public class BotRuningController {

    @Autowired
    private BotRuningService botRunningService;

    @PostMapping("/bot/add/")
    public String addBot(@RequestParam MultiValueMap<String, String> data) {
        Integer userID = Integer.parseInt(Objects.requireNonNull(data.getFirst("user_id")));
        String botCode = data.getFirst("bot_code");
        String input = data.getFirst("input");
        System.out.println("Controller收到了！！！");
        return botRunningService.addBot(userID, botCode, input);
    }
}
