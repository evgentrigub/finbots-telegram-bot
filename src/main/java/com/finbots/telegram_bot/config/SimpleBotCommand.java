package com.finbots.telegram_bot.config;

public interface SimpleBotCommand {
    public String getResponseMessage(long chatId, String name);
}
