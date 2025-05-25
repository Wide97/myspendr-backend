package com.myspendr.myspendr.services;

import com.myspendr.myspendr.model.CategoriaMovimento;
import com.myspendr.myspendr.model.TelegramUser;
import com.myspendr.myspendr.model.TipoMovimento;
import com.myspendr.myspendr.repositories.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelegramUserService {

    private final TelegramUserRepository telegramUserRepository;

    private final Map<Long, TipoMovimento> tipoTemporaneo = new HashMap<>();
    private final Map<Long, CategoriaMovimento> categoriaTemporanea = new HashMap<>();
    private final Map<Long, String> fonteTemporanea = new HashMap<>(); // NUOVA MAPPA FONTE

    public TelegramUser findByTelegramId(Long telegramId) {
        return telegramUserRepository.findById(telegramId).orElse(null);
    }

    public void updateTipoTemporaneo(TelegramUser user, TipoMovimento tipo) {
        tipoTemporaneo.put(user.getTelegramId(), tipo);
    }

    public TipoMovimento getTipoTemporaneo(Long telegramId) {
        return tipoTemporaneo.get(telegramId);
    }

    public void updateCategoriaTemporanea(TelegramUser user, CategoriaMovimento categoria) {
        categoriaTemporanea.put(user.getTelegramId(), categoria);
    }

    public CategoriaMovimento getCategoriaTemporanea(Long telegramId) {
        return categoriaTemporanea.get(telegramId);
    }

    public void updateFonteTemporanea(TelegramUser user, String fonte) {
        fonteTemporanea.put(user.getTelegramId(), fonte);
    }

    public String getFonteTemporanea(Long telegramId) {
        return fonteTemporanea.get(telegramId);
    }

    public void clearSession(Long telegramId) {
        tipoTemporaneo.remove(telegramId);
        categoriaTemporanea.remove(telegramId);
        fonteTemporanea.remove(telegramId);
    }
}

