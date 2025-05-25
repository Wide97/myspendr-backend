package com.myspendr.myspendr.services;

import com.myspendr.myspendr.model.TelegramUser;
import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.repositories.TelegramUserRepository;
import com.myspendr.myspendr.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotService {

    private final TelegramUserRepository telegramUserRepository;
    private final MovimentoService movimentoService;
    private final UserRepository userRepository;

    @Value("${telegram.bot.token}")
    private String botToken;

    public void handleSpesaCommand(Message message) {
        Long telegramId = message.getFrom().getId();
        String testo = message.getText();

        TelegramUser telegramUser = telegramUserRepository.findById(telegramId).orElse(null);
        if (telegramUser == null) {
            inviaMessaggioTelegram(telegramId, "❗ Devi prima collegare il tuo account MySpendr.");
            return;
        }

        try {
            String[] parts = testo.trim().split(" ", 4);
            if (parts.length < 2) {
                inviaMessaggioTelegram(telegramId, "❌ Formato errato. Usa: /spesa 12.50 sushi 2024-05-22");
                return;
            }

            BigDecimal importo = new BigDecimal(parts[1]);
            String descrizione = (parts.length >= 3) ? parts[2] : "Senza descrizione";
            String dataTesto = (parts.length == 4) ? parts[3] : null;

            LocalDate data = LocalDate.now();
            if (dataTesto != null) {
                try {
                    data = LocalDate.parse(dataTesto);
                } catch (Exception e) {
                    inviaMessaggioTelegram(telegramId, "❌ Data non valida. Usa il formato: yyyy-MM-dd");
                    return;
                }
            }

            User user = telegramUser.getUser();
            movimentoService.creaSpesaViaTelegram(user, importo, descrizione, data);

            inviaMessaggioTelegram(telegramId, "✅ Spesa salvata:\n💶 €" + importo + "\n📄 " + descrizione + "\n📅 " + data);
        } catch (Exception e) {
            log.error("❌ Errore durante il parsing comando /spesa", e);
            inviaMessaggioTelegram(telegramId, "❌ Formato errato. Usa: /spesa 12.50 sushi 2024-05-22");
        }
    }

    public void inviaMessaggioTelegram(Long chatId, String testo) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> request = Map.of("chat_id", chatId, "text", testo);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            log.info("📨 Risposta Telegram: {}", response.getBody());
        } catch (Exception e) {
            log.error("❌ Errore durante l'invio del messaggio Telegram", e);
        }
    }

    public void handleStartCommand(Message message) {
        String text = message.getText(); // es: "/start <token>"
        String[] parts = text.trim().split(" ");
        Long telegramId = message.getFrom().getId();

        if (parts.length != 2) {
            inviaMessaggioTelegram(telegramId, "❌ Usa: /start <token>");
            return;
        }

        String token = parts[1];
        Optional<User> userOpt = userRepository.findByTelegramToken(token);

        if (userOpt.isEmpty()) {
            inviaMessaggioTelegram(telegramId, "❌ Token non valido o già usato.");
            return;
        }

        User user = userOpt.get();

        // Evita duplicati: se già registrato, aggiorna
        TelegramUser telegramUser = TelegramUser.builder()
                .telegramId(telegramId)
                .username(message.getFrom().getUserName())
                .firstName(message.getFrom().getFirstName())
                .lastName(message.getFrom().getLastName())
                .user(user)
                .build();

        telegramUserRepository.save(telegramUser);

        // 🔐 Rendi il token non più utilizzabile
        user.setTelegramToken(null);
        userRepository.save(user);

        inviaMessaggioTelegram(telegramId, "✅ Il tuo account è stato collegato a MySpendr!");
    }

}
