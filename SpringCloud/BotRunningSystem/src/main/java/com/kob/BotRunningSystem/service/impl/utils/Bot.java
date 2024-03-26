package com.kob.BotRunningSystem.service.impl.utils;


import jdk.nashorn.internal.runtime.linker.InvokeByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author asus
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bot {
    Integer userId;
    String botCode;
    String input;
}
