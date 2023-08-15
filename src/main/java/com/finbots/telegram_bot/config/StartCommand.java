package com.finbots.telegram_bot.config;

public class StartCommand implements SimpleBotCommand {
    public String getResponseMessage(long chatId, String name) {
        String answer = "Hi, " + name + ", nice to meet you!";
//        log.info("Replied to user " + name);

        return answer;
    }
}
