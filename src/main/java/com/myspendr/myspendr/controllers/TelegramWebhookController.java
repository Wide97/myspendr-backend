package com.myspendr.myspendr.controllers;

import com.myspendr.myspendr.services.TelegramBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequiredArgsConstructor
@RequestMapping("/telegram")
public class TelegramWebhookController {

    private final TelegramBotService telegramBotService;

    @PostMapping
    public void onUpdateReceived(@RequestBody Update update) {
        if (update == null || update.getMessage() == null || update.getMessage().getText() == null) return;

        String text = update.getMessage().getText();

        if (text.startsWith("/start")) {
            telegramBotService.handleStartCommand(update.getMessage());
        } else if (text.startsWith("/spesa")) {
            telegramBotService.handleSpesaCommand(update.getMessage());
        } else if (text.startsWith("/test")) {
            telegramBotService.inviaMessaggioTelegram(update.getMessage().getChatId(), "✅ Il bot è attivo e funzionante! 🚀");
        } else {
            telegramBotService.inviaMessaggioTelegram(update.getMessage().getChatId(),
                    "❓ Comando non riconosciuto. Usa:\n/spesa 12.50 sushi\n/start <token>");
        }
    }


}
