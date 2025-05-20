package com.myspendr.myspendr.bot;

import com.myspendr.myspendr.dto.MovimentoRequest;
import com.myspendr.myspendr.model.CategoriaMovimento;
import com.myspendr.myspendr.model.TipoMovimento;
import com.myspendr.myspendr.services.MovimentoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
public class MySpendrTelegramBot extends TelegramWebhookBot {

    private final MovimentoService movimentoService;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.webhook-url}")
    private String botWebhookUrl;

    public MySpendrTelegramBot(MovimentoService movimentoService) {
        this.movimentoService = movimentoService;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotPath() {
        return botWebhookUrl;
    }

    @Override
    public SendMessage onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText().trim();

            if (text.startsWith("/spesa")) {
                return handleSpesa(chatId, text);
            }

            return SendMessage.builder()
                    .chatId(chatId)
                    .text("❓ Comando non riconosciuto. Usa /spesa <importo> <categoria> <fonte> [data]")
                    .build();
        }

        return null;
    }

    private SendMessage handleSpesa(String chatId, String text) {
        String[] parts = text.split(" ");

        if (parts.length < 4) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("❌ Formato non valido.\nUsa: /spesa <importo> <categoria> <fonte> [YYYY-MM-DD]")
                    .build();
        }

        try {
            BigDecimal importo = new BigDecimal(parts[1]);
            String categoria = parts[2];
            String fonte = parts[3].toUpperCase();
            LocalDate data = LocalDate.now();

            if (parts.length == 5) {
                try {
                    data = LocalDate.parse(parts[4]);
                } catch (DateTimeParseException e) {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("❌ Formato data non valido. Usa YYYY-MM-DD.")
                            .build();
                }
            }

            // TODO: Mappare utente Telegram al JWT corretto (es. via TelegramUserService)
            String fakeToken = "Bearer TOKEN_DA_GENERARE"; // placeholder

            MovimentoRequest movimento = new MovimentoRequest();
            movimento.setImporto(importo);
            try {
                movimento.setCategoria(CategoriaMovimento.valueOf(categoria.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("❌ Categoria non valida. Usa una delle seguenti: SPESA, PRANZO, CASA, ecc.")
                        .build();
            }
            movimento.setFonte(fonte);
            movimento.setData(data);
            movimento.setTipo(TipoMovimento.USCITA); // solo spesa

            movimentoService.creaMovimento(fakeToken, movimento);

            return SendMessage.builder()
                    .chatId(chatId)
                    .text("✅ Spesa salvata: " + importo + "€ - " + categoria + " [" + fonte + "] " + data)
                    .build();

        } catch (Exception e) {
            log.error("Errore durante il salvataggio della spesa", e);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("❌ Errore durante il salvataggio. Riprova.")
                    .build();
        }
    }
}
