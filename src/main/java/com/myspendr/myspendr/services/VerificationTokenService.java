package com.myspendr.myspendr.services;

import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.model.VerificationToken;
import com.myspendr.myspendr.repositories.UserRepository;
import com.myspendr.myspendr.repositories.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VerificationTokenService {

    private final VerificationTokenRepository tokenRepo;
    private final UserRepository userRepo;

    public VerificationTokenService(VerificationTokenRepository tokenRepo, UserRepository userRepo) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
    }

    public VerificationToken createTokenForUser(User user) {
        VerificationToken token = new VerificationToken(user);
        return tokenRepo.save(token);
    }

    @Transactional
    public boolean verifyToken(String tokenValue) {
        Optional<VerificationToken> tokenOpt = tokenRepo.findByToken(tokenValue);

        if (tokenOpt.isEmpty()) return false;

        VerificationToken token = tokenOpt.get();
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepo.delete(token);
            return false;
        }

        User user = token.getUser();
        user.setEmailConfirmed(true);
        userRepo.save(user);
        tokenRepo.delete(token);
        return true;
    }
}

