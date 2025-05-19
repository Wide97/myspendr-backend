package com.myspendr.myspendr.telegram;

import com.myspendr.myspendr.dto.CapitaleResponse;
import com.myspendr.myspendr.model.CategoriaMovimento;
import com.myspendr.myspendr.model.TipoMovimento;
import com.myspendr.myspendr.telegram.MovimentoEnumParser;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class TelegramService {

    private final MovementApiClient movementClient;
    private final CapitaleApiClient capitaleClient;
    private final TelegramBot telegramBot;

    public TelegramService(MovementApiClient movementClient, CapitaleApiClient capitaleClient, TelegramBot telegramBot) {
        this.movementClient = movementClient;
        this.capitaleClient = capitaleClient;
        this.telegramBot = telegramBot;
    }

    public void handleMessage(Message message) {
        String text = message.getText().trim();
        Long chatId = message.getChatId();

        try {
            if (text.startsWith("/spesa")) {
                handleMovimento(text, chatId, TipoMovimento.USCITA);
            } else if (text.startsWith("/entrata")) {
                handleMovimento(text, chatId, TipoMovimento.ENTRATA);
            } else if (text.equals("/capitale")) {
                handleCapitale(chatId);
            } else {
                send(chatId, "❓ Comando non riconosciuto.");
            }
        } catch (Exception e) {
            send(chatId, "❌ Errore: " + e.getMessage());
        }
    }

    private void handleMovimento(String text, Long chatId, TipoMovimento tipoMovimento) {
        String[] parts = text.split(" ", 3);
        if (parts.length < 3) {
            send(chatId, "❗ Usa: /" + tipoMovimento.name().toLowerCase() + " importo categoria descrizione");
            return;
        }

        try {
            BigDecimal importo = new BigDecimal(parts[1]);
            String categoriaRaw = parts[2].split(" ")[0];
            String descrizione = text.substring(text.indexOf(categoriaRaw) + categoriaRaw.length()).trim();

            Optional<CategoriaMovimento> categoriaEnum = MovimentoEnumParser.parseCategoria(categoriaRaw);

            if (categoriaEnum.isEmpty()) {
                String categorieValide = MovimentoEnumParser.getCategorieValide();
                send(chatId, "⚠️ Categoria non valida. Valori validi:\n" + categorieValide);
                return;
            }

            movementClient.inviaMovimento(importo, categoriaEnum.get().name(), descrizione, tipoMovimento.name());

            String emoji = tipoMovimento == TipoMovimento.ENTRATA ? "💰" : "📤";
            send(chatId, emoji + " Movimento registrato: " + (tipoMovimento == TipoMovimento.ENTRATA ? "+" : "-") + importo + "€ [" + categoriaEnum.get().name() + "]");

        } catch (NumberFormatException e) {
            send(chatId, "❌ Importo non valido. Scrivi un numero, esempio: `/spesa 12.5 CIBO sushi`");
        }
    }

    private void handleCapitale(Long chatId) {
        CapitaleResponse capitale = capitaleClient.getCapitale();
        String msg = String.format("""
                🏦 *Capitale attuale*:
                • Conto Bancario: %.2f€
                • Liquidità: %.2f€
                • Altri Fondi: %.2f€
                """, capitale.getContoBancario(), capitale.getLiquidita(), capitale.getAltriFondi());
        send(chatId, msg);
    }

    private void send(Long chatId, String text) {
        SendMessage msg = new SendMessage(chatId.toString(), text);
        msg.enableMarkdown(true);
        try {
            telegramBot.execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
