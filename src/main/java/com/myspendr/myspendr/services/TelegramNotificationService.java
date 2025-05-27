package com.myspendr.myspendr.services;

import com.myspendr.myspendr.model.CategoriaMovimento;
import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.repositories.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService {

    private final TelegramUserRepository telegramUserRepository;
    private final TelegramBotService telegramBotService;

    public void inviaAvvisoBudgetSuperato(User user, CategoriaMovimento categoria, int mese, int anno, BigDecimal speso, BigDecimal limite) {
        telegramUserRepository.findByUser(user).ifPresent(telegramUser -> {
            String messaggio = """
                ⚠️ Hai superato il budget per *%s* nel mese %d/%d.
                Speso: *€%.2f* / Limite: *€%.2f*
                """.formatted(categoria.name(), mese, anno, speso, limite);
            telegramBotService.inviaMessaggioTelegram(telegramUser.getTelegramId(), messaggio);
        });
    }
}
