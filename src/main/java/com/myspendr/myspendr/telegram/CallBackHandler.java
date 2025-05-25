package com.myspendr.myspendr.telegram;

import com.myspendr.myspendr.model.CategoriaMovimento;
import com.myspendr.myspendr.model.TelegramUser;
import com.myspendr.myspendr.model.TipoMovimento;
import com.myspendr.myspendr.services.MovimentoService;
import com.myspendr.myspendr.services.TelegramBotService;
import com.myspendr.myspendr.services.TelegramUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallBackHandler {

    private final TelegramBotService telegramBotService;
    private final TelegramUserService telegramUserService;

    public void handleCallback(CallbackQuery callbackQuery) {
        Long telegramId = callbackQuery.getFrom().getId();
        String data = callbackQuery.getData();

        TelegramUser telegramUser = telegramUserService.findByTelegramId(telegramId);
        if (telegramUser == null) {
            telegramBotService.inviaMessaggioTelegram(telegramId, "❗ Devi prima collegare il tuo account.");
            return;
        }

        if (data.startsWith("TIPO_")) {
            TipoMovimento tipo = TipoMovimento.valueOf(data.replace("TIPO_", ""));
            telegramUserService.updateTipoTemporaneo(telegramUser, tipo);
            telegramBotService.mostraBottoniCategoria(telegramId);
        } else if (data.startsWith("CATEGORIA_")) {
            CategoriaMovimento categoria = CategoriaMovimento.valueOf(data.replace("CATEGORIA_", ""));
            telegramUserService.updateCategoriaTemporanea(telegramUser, categoria);
            telegramBotService.mostraBottoniFonte(telegramId);
        } else if (data.startsWith("FONTE_")) { // NUOVO STEP
            String fonte = data.replace("FONTE_", "");
            telegramUserService.updateFonteTemporanea(telegramUser, fonte);
            telegramBotService.richiediImportoEDescrizione(telegramId);
        }
    }
}
