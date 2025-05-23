package com.myspendr.myspendr.controllers;

import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.repositories.UserRepository;
import com.myspendr.myspendr.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    public UserController(UserRepository userRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            String email = jwtUtils.getUsernameFromJwtToken(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("❌ Utente non trovato per email: {}", email);
                        return new RuntimeException("Utente non trovato");
                    });

            log.info("👤 Profilo recuperato per utente {}", user.getEmail());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("❌ Errore durante il recupero del profilo utente", e);
            return ResponseEntity.badRequest().body("Errore durante il recupero dell'utente");
        }
    }
}
