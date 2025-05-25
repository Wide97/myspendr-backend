package com.myspendr.myspendr.controllers;

import com.myspendr.myspendr.services.TelegramBotService;
import com.myspendr.myspendr.telegram.CallBackHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequiredArgsConstructor
@RequestMapping("/telegram")
public class TelegramWebhookController {

    private final TelegramBotService telegramBotService;
    private final CallBackHandler callBackHandler;

    @PostMapping
    public void onUpdateReceived(@RequestBody Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();

            if (text.startsWith("/start")) {
                telegramBotService.handleStartCommand(update.getMessage());
            } else if (text.startsWith("/spesa")) {
                telegramBotService.handleSpesaCommand(update.getMessage());
            } else if (text.startsWith("/test")) {
                telegramBotService.inviaMessaggioTelegram(update.getMessage().getChatId(), "✅ Il bot è attivo e funzionante! 🚀");
            } else {
                telegramBotService.handleTextMessage(update.getMessage()); // NUOVO METODO
            }
        } else if (update.hasCallbackQuery()) {
            callBackHandler.handleCallback(update.getCallbackQuery());
        }
    }


}
