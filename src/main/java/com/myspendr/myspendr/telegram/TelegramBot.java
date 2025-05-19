
package com.myspendr.myspendr.telegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;




@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramService telegramService;

    public TelegramBot(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            telegramService.handleMessage(update.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return "MySpendrBot";
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN"); // oppure hardcoded temporaneamente
    }
}
