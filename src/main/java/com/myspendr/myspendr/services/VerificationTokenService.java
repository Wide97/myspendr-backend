package com.myspendr.myspendr.services;

import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.model.VerificationToken;
import com.myspendr.myspendr.repositories.UserRepository;
import com.myspendr.myspendr.repositories.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class VerificationTokenService {

    private final VerificationTokenRepository tokenRepo;
    private final UserRepository userRepo;

    public VerificationTokenService(VerificationTokenRepository tokenRepo, UserRepository userRepo) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
    }

    public VerificationToken createTokenForUser(User user) {
        try {
            VerificationToken token = new VerificationToken(user);
            VerificationToken saved = tokenRepo.save(token);
            log.info("✅ Token di verifica creato per utente {}: {}", user.getEmail(), saved.getToken());
            return saved;
        } catch (Exception e) {
            log.error("❌ Errore nella creazione del token per utente {}", user.getEmail(), e);
            throw new RuntimeException("Errore nella creazione del token", e);
        }
    }

    @Transactional
    public boolean verifyToken(String tokenValue) {
        try {
            Optional<VerificationToken> tokenOpt = tokenRepo.findByToken(tokenValue);

            if (tokenOpt.isEmpty()) {
                log.warn("❌ Token non trovato: {}", tokenValue);
                return false;
            }

            VerificationToken token = tokenOpt.get();

            if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
                log.warn("⌛ Token scaduto per utente {}: {}", token.getUser().getEmail(), tokenValue);
                tokenRepo.delete(token);
                return false;
            }

            User user = token.getUser();
            user.setEmailConfirmed(true);
            userRepo.save(user);
            tokenRepo.delete(token);

            log.info("📧 Email confermata per utente {} tramite token {}", user.getEmail(), tokenValue);
            return true;
        } catch (Exception e) {
            log.error("❌ Errore nella verifica del token {}", tokenValue, e);
            throw new RuntimeException("Errore nella verifica del token", e);
        }
    }
}
