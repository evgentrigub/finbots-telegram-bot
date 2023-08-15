package com.finbots.telegram_bot.services;

import com.finbots.telegram_bot.config.BotConfig;
import com.finbots.telegram_bot.config.StartCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig botConfig;

    TelegramBot(BotConfig botConfig){
        super(botConfig.getToken());
        this.botConfig = botConfig;
    }

    @Override
    public String getBotUsername() {
        return this.botConfig.getBotName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        var message = update.getMessage();
        var isMessageValid = update.hasMessage() && message.hasText();
        if (isMessageValid) {
            long chatId = message.getChatId();

            switch (message.getText()) {
                case "/start" -> {
                    var response = new StartCommand().getResponseMessage(chatId, update.getMessage().getChat().getFirstName());
                    sendMessage(chatId, response);
                }
                case "/login" -> sendMessage(chatId, "login: command is not implemented yet");
                case "/get_all_bots" -> sendMessage(chatId, "get_all_bot: command is not implemented yet");
                case "/new_bot" -> sendMessage(chatId, "new_bot: command is not implemented yet");
                default -> sendMessage(chatId, "Sorry, this command is not existed");
            }
        }
    }

    private void sendMessage(Long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        executeMessage(message);
    }

    private void executeMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
//            log.error(ERROR_TEXT + e.getMessage());
        }
    }
}
