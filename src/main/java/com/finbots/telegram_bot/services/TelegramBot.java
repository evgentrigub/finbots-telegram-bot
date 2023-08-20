package com.finbots.telegram_bot.services;

import com.finbots.telegram_bot.config.BotConfig;
import com.finbots.telegram_bot.config.StartCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private boolean screaming = false;

    private InlineKeyboardMarkup keyboardM1;
    private InlineKeyboardMarkup keyboardM2;

    final BotConfig botConfig;

    TelegramBot(BotConfig botConfig){
        super(botConfig.getToken());
        this.botConfig = botConfig;
        initButtons();
    }

    @Override
    public String getBotUsername() {
        return this.botConfig.getBotName();
    }

//    @Override
//    public void onUpdateReceived(Update update) {
//        var message = update.getMessage();
//        var isMessageValid = update.hasMessage() && message.hasText();
//        if (isMessageValid) {
//            long chatId = message.getChatId();
//
//            switch (message.getText()) {
//                case "/start" -> {
//                    var response = new StartCommand().getResponseMessage(chatId, update.getMessage().getChat().getFirstName());
//                    sendMessage(chatId, response);
//                }
//                case "/login" -> sendMessage(chatId, "login: command is not implemented yet");
//                case "/get_all_bots" -> sendMessage(chatId, "get_all_bot: command is not implemented yet");
//                case "/new_bot" -> sendMessage(chatId, "new_bot: command is not implemented yet");
//                default -> sendMessage(chatId, "Sorry, this command is not existed");
//            }
//        }
//    }
//
//    private void sendMessage(Long chatId, String textToSend) {
//        SendMessage message = new SendMessage();
//        message.setChatId(chatId);
//        message.setText(textToSend);
//        executeMessage(message);
//    }
//
//    private void executeMessage(SendMessage message){
//        try {
//            execute(message);
//        } catch (TelegramApiException e) {
////            log.error(ERROR_TEXT + e.getMessage());
//        }
//    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage()){
            handleSimpleMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleButton(update);
        }
    }

    private void handleButton(Update update) {
        var query = update.getCallbackQuery();

        var msg = query.getMessage();
        var userId = msg.getChatId();
        var messageId = msg.getMessageId();
        var queryId = query.getId();

        try {
            buttonTap(userId, queryId, query.getData(), messageId);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleSimpleMessage(Update update) {
        var msg = update.getMessage();
        var user = msg.getFrom();
        var id = user.getId();
        var txt = msg.getText();

//        sendText(id, msg.getText());
//        copyMessage(id, msg.getMessageId());

        if(msg.isCommand()){
            if(msg.getText().equals("/scream"))         //If the command was /scream, we switch gears
                screaming = true;
            else if (msg.getText().equals("/whisper"))  //Otherwise, we return to normal
                screaming = false;
            else if (txt.equals("/menu"))
                sendMenu(id, "<b>Menu 1</b>", keyboardM1);
            return;     //We don't want to echo commands, so we exit
        }

        if (screaming) {
            scream(id, update.getMessage());
        } else {
            copyMessage(id, msg.getMessageId());
        }
    }

    public void sendMenu(Long who, String txt, InlineKeyboardMarkup kb){
        SendMessage sm = SendMessage.builder().chatId(who.toString())
                .parseMode("HTML").text(txt)
                .replyMarkup(kb).build();

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void buttonTap(Long chatId, String queryId, String data, int msgId) throws TelegramApiException {

        EditMessageText newTxt = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(msgId).text("").build();

        EditMessageReplyMarkup newKb = EditMessageReplyMarkup.builder()
                .chatId(chatId.toString()).messageId(msgId).build();

        if(data.equals("next")) {
            newTxt.setText("MENU 2");
            newKb.setReplyMarkup(keyboardM2);
        } else if(data.equals("back")) {
            newTxt.setText("MENU 1");
            newKb.setReplyMarkup(keyboardM1);
        }

        AnswerCallbackQuery close = AnswerCallbackQuery.builder()
                .callbackQueryId(queryId).build();

        execute(close);
        execute(newTxt);
        execute(newKb);
    }

    private void initButtons() {
        var next = InlineKeyboardButton.builder()
                .text("Next").callbackData("next")
                .build();

        var back = InlineKeyboardButton.builder()
                .text("Back").callbackData("back")
                .build();

        var url = InlineKeyboardButton.builder()
                .text("Tutorial")
                .url("https://core.telegram.org/bots/api")
                .build();

        keyboardM1 = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(next)).build();

        //Buttons are wrapped in lists since each keyboard is a set of button rows
        keyboardM2 = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(back))
                .keyboardRow(List.of(url))
                .build();
    }

    private void scream(Long id, Message msg) {
        if(msg.hasText())
            sendText(id, msg.getText().toUpperCase());
        else
            copyMessage(id, msg.getMessageId());  //We can't really scream a sticker
    }

    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    public void copyMessage(Long who, Integer msgId){
        CopyMessage cm = CopyMessage.builder()
                .fromChatId(who.toString())  //We copy from the user
                .chatId(who.toString())      //And send it back to him
                .messageId(msgId)            //Specifying what message
                .build();
        try {
            execute(cm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
