package com.myspendr.myspendr.services;

import com.myspendr.myspendr.dto.MovimentoRequest;
import com.myspendr.myspendr.model.*;
import com.myspendr.myspendr.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;



import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotService {

    private final TelegramUserRepository telegramUserRepository;
    private final UserRepository userRepository;
    private final TelegramUserService telegramUserService;
    private final CapitaleRepository capitaleRepository;
    private final MovimentoRepository movimentoRepository;
    private final BudgetMensileRepository budgetRepo;
    private final ApplicationContext context;

    private MovimentoService movimentoService() {
        return context.getBean(MovimentoService.class);
    }

    @Value("${telegram.bot.token}")
    private String botToken;

    public void handleSpesaCommand(Message message) {
        Long telegramId = message.getFrom().getId();

        TelegramUser telegramUser = telegramUserRepository.findById(telegramId).orElse(null);
        if (telegramUser == null) {
            inviaMessaggioTelegram(telegramId, "❗ Devi prima collegare il tuo account MySpendr.");
            return;
        }

        // Inizio flusso: scegli tipo movimento
        String testo = "Scegli il tipo di movimento:";
        inviaInlineKeyboardTipoMovimento(telegramId, testo);
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

    public void inviaInlineKeyboardTipoMovimento(Long chatId, String testo) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> keyboard = Map.of(
                "inline_keyboard", new Object[][]{
                        {Map.of("text", "Entrata", "callback_data", "TIPO_ENTRATA")},
                        {Map.of("text", "Uscita", "callback_data", "TIPO_USCITA")}
                }
        );

        Map<String, Object> request = Map.of(
                "chat_id", chatId,
                "text", testo,
                "reply_markup", keyboard
        );

        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            log.error("❌ Errore nell'invio dei bottoni tipo movimento", e);
        }
    }

    public void handleStartCommand(Message message) {
        String text = message.getText();
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

        TelegramUser telegramUser = TelegramUser.builder()
                .telegramId(telegramId)
                .username(message.getFrom().getUserName())
                .firstName(message.getFrom().getFirstName())
                .lastName(message.getFrom().getLastName())
                .user(user)
                .build();

        telegramUserRepository.save(telegramUser);
        user.setTelegramToken(null);
        userRepository.save(user);

        inviaMessaggioTelegram(telegramId, "✅ Il tuo account è stato collegato a MySpendr!");
    }

    public void richiediImportoEDescrizione(Long telegramId) {
        String messaggio = "✏️ Ora inviami l’importo e la descrizione.\n\n" +
                "Esempio:\n`12.50 sushi`\n\n" +
                "Puoi anche aggiungere una data:\n`12.50 sushi 2025-05-25`";
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = Map.of(
                "chat_id", telegramId,
                "text", messaggio,
                "parse_mode", "Markdown"
        );

        try {
            restTemplate.postForEntity(url, request, String.class);
            log.info("📨 Messaggio per inserimento importo/descrizione inviato a {}", telegramId);
        } catch (Exception e) {
            log.error("❌ Errore durante l'invio del messaggio per importo e descrizione", e);
        }
    }

    public void mostraBottoniFonte(Long telegramId) {
        List<List<InlineKeyboardButton>> righe = List.of(
                List.of(creaBottone("💳 BANCA", "FONTE_BANCA")),
                List.of(creaBottone("💶 CONTANTI", "FONTE_CONTANTI")),
                List.of(creaBottone("🔗 ALTRI", "FONTE_ALTRI"))
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(righe);

        SendMessage messaggio = new SendMessage();
        messaggio.setChatId(telegramId.toString());
        messaggio.setText("📌 Seleziona una fonte:");
        messaggio.setReplyMarkup(markup);

        inviaMessaggioConMarkup(messaggio);
    }

    private InlineKeyboardButton creaBottone(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private void inviaMessaggioConMarkup(SendMessage messaggio) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        restTemplate.postForObject(url, messaggio, String.class);
    }


    public void mostraBottoniCategoria(Long telegramId) {
        List<List<InlineKeyboardButton>> righe = new ArrayList<>();

        for (CategoriaMovimento categoria : CategoriaMovimento.values()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(categoria.name());
            button.setCallbackData("CATEGORIA_" + categoria.name());

            List<InlineKeyboardButton> riga = new ArrayList<>();
            riga.add(button);
            righe.add(riga);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(righe);

        SendMessage messaggio = new SendMessage();
        messaggio.setChatId(telegramId.toString());
        messaggio.setText("📌 Seleziona una categoria:");
        messaggio.setReplyMarkup(markup);

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://api.telegram.org/bot" + botToken + "/sendMessage")
                    .build().toUriString();

            restTemplate.postForObject(url, messaggio, String.class);
        } catch (Exception e) {
            log.error("❌ Errore nell'invio dei bottoni categoria", e);
        }
    }

    public void creaSpesaViaTelegram(User user,
                                     BigDecimal importo,
                                     String descrizione,
                                     LocalDate data,
                                     TipoMovimento tipo,
                                     CategoriaMovimento categoria,
                                     String fonte) {

        MovimentoRequest request = new MovimentoRequest();
        request.setImporto(importo);
        request.setDescrizione(descrizione);
        request.setData(data);
        request.setTipo(tipo);
        request.setCategoria(categoria);
        request.setFonte(fonte);

        movimentoService.creaMovimentoDaTelegram(user, request);

        // 🔔 Notifica se budget superato
        if (tipo == TipoMovimento.USCITA) {
            Long telegramId = telegramUserRepository.findByUser(user)
                    .map(TelegramUser::getTelegramId)
                    .orElse(null);
            if (telegramId != null) {
                controllaEBudgetSuperato(user, categoria, data.getMonthValue(), data.getYear(), telegramId);
            }
        }
    }


    private void controllaEBudgetSuperato(User user, CategoriaMovimento categoria, int mese, int anno, Long telegramId) {
        try {
            BudgetMensile budget = budgetRepo.findByUserAndCategoriaAndMeseAndAnno(user, categoria, mese, anno).orElse(null);

            if (budget != null) {
                BigDecimal limite = budget.getLimite();
                BigDecimal speso = movimentoRepository.findByCapitale_UserAndCategoriaAndTipoAndDataBetween(
                                user, categoria, TipoMovimento.USCITA,
                                LocalDate.of(anno, mese, 1),
                                LocalDate.of(anno, mese, 1).withDayOfMonth(LocalDate.of(anno, mese, 1).lengthOfMonth())
                        ).stream()
                        .map(Movimento::getImporto)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (speso.compareTo(limite) > 0) {
                    BigDecimal sforamento = speso.subtract(limite);
                    String messaggio = "⚠️ *Budget Superato!*\nHai speso *" + speso + "€* su un limite di *" + limite + "€* per la categoria *" + categoria + "*.\nSforamento: *" + sforamento + "€*";

                    inviaMessaggioTelegram(telegramId, messaggio);
                }
            }

        } catch (Exception e) {
            log.error("❌ Errore nel controllo del budget superato", e);
        }
    }



    public void handleTextMessage(Message message) {
        Long telegramId = message.getFrom().getId();
        TelegramUser telegramUser = telegramUserService.findByTelegramId(telegramId);

        if (telegramUser == null) {
            inviaMessaggioTelegram(telegramId, "❗ Devi prima collegare il tuo account MySpendr.");
            return;
        }

        String text = message.getText();
        try {
            String[] parti = text.split(" ", 3);
            BigDecimal importo = new BigDecimal(parti[0]);
            String descrizione = parti[1];
            LocalDate data = parti.length == 3 ? LocalDate.parse(parti[2]) : LocalDate.now();

            TipoMovimento tipo = telegramUserService.getTipoTemporaneo(telegramId);
            CategoriaMovimento categoria = telegramUserService.getCategoriaTemporanea(telegramId);
            String fonte = telegramUserService.getFonteTemporanea(telegramId);

            if (tipo == null || categoria == null || fonte == null) {
                inviaMessaggioTelegram(telegramId, "❌ Devi prima selezionare tipo, categoria e fonte del movimento.");
                return;
            }

            creaSpesaViaTelegram(telegramUser.getUser(), importo, descrizione, data, tipo, categoria, fonte);
            telegramUserService.clearSession(telegramId);
            inviaMessaggioTelegram(telegramId, "✅ Movimento salvato correttamente!");

        } catch (Exception e) {
            inviaMessaggioTelegram(telegramId, "❌ Formato errato. Usa: `12.50 sushi [2025-05-25]`");
        }
    }

    public void handleUltimiCommand(Long telegramId) {
        TelegramUser telegramUser = telegramUserService.findByTelegramId(telegramId);
        if (telegramUser == null) {
            inviaMessaggioTelegram(telegramId, "❗ Devi prima collegare il tuo account.");
            return;
        }

        UUID userId = telegramUser.getUser().getId();
        Capitale capitale = capitaleRepository.findByUserId(userId).orElse(null);
        if (capitale == null) {
            inviaMessaggioTelegram(telegramId, "⚠️ Nessun capitale trovato.");
            return;
        }

        List<Movimento> ultimi = movimentoRepository.findByCapitaleId(capitale.getId()).stream()
                .sorted(Comparator.comparing(Movimento::getData).reversed())
                .limit(5)
                .toList();

        StringBuilder sb = new StringBuilder("📄 Ultimi 5 movimenti:\n\n");
        for (Movimento mov : ultimi) {
            sb.append("• ").append(mov.getData()).append(" - ")
                    .append(mov.getCategoria()).append(": ")
                    .append(mov.getImporto()).append("€\n");
        }

        inviaMessaggioTelegram(telegramId, sb.toString());
    }

    public void handleRiepilogoCommand(Long telegramId) {
        TelegramUser telegramUser = telegramUserService.findByTelegramId(telegramId);
        if (telegramUser == null) {
            inviaMessaggioTelegram(telegramId, "❗ Devi prima collegare il tuo account MySpendr.");
            return;
        }

        Capitale capitale = capitaleRepository.findByUserId(telegramUser.getUser().getId())
                .orElse(null);

        if (capitale == null) {
            inviaMessaggioTelegram(telegramId, "📭 Nessun capitale trovato. Usa l’app per crearlo.");
            return;
        }

        String risposta = """
                💼 *Il tuo Capitale Attuale*:
                • Banca: €%.2f
                • Contanti: €%.2f
                • Altri fondi: €%.2f
                • Totale: €%.2f
                """.formatted(
                capitale.getContoBancario(),
                capitale.getLiquidita(),
                capitale.getAltriFondi(),
                capitale.getTotale()
        );

        inviaMessaggioTelegram(telegramId, risposta);
    }

    public void handleHelpCommand(Long chatId) {
        String messaggio = """
                📖 *Guida MySpendr Bot*:
                
                ▪️ /start `<token>` - Collega il tuo account MySpendr
                ▪️ /spesa - Avvia la registrazione guidata di un movimento
                ▪️ /ultimi - Mostra gli ultimi 5 movimenti
                ▪️ /riepilogo - Visualizza il tuo capitale attuale
                ▪️ /help - Mostra questo messaggio di aiuto
                
                💡 Quando crei una spesa:
                1. Scegli *tipo* (entrata o uscita)
                2. Scegli *categoria*
                3. Scegli *fonte* (contanti, banca, altri)
                4. Invia l'importo e la descrizione (es. `12.50 sushi`)
                
                📅 Puoi anche aggiungere la data: `12.50 sushi 2025-05-26`
                
                Buon tracciamento con MySpendr 💰
                """;

        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = Map.of(
                "chat_id", chatId,
                "text", messaggio,
                "parse_mode", "Markdown"
        );

        try {
            restTemplate.postForEntity(url, request, String.class);
            log.info("📘 Messaggio /help inviato a {}", chatId);
        } catch (Exception e) {
            log.error("❌ Errore durante l'invio del messaggio di help", e);
        }
    }


}