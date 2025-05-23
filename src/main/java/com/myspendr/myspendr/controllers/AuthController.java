package com.myspendr.myspendr.controllers;

import com.myspendr.myspendr.dto.*;
import com.myspendr.myspendr.services.AuthService;
import com.myspendr.myspendr.services.VerificationTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final VerificationTokenService verificationTokenService;
    private final AuthService authService;

    public AuthController(AuthService authService, VerificationTokenService verificationTokenService) {
        this.authService = authService;
        this.verificationTokenService = verificationTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            log.info("✅ Registrazione avvenuta per {}", request.getEmail());
            return ResponseEntity.ok("Registrazione completata");
        } catch (Exception e) {
            log.error("❌ Errore durante la registrazione per {}", request.getEmail(), e);
            return ResponseEntity.internalServerError().body("Errore nella registrazione");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Login fallito per {}", request.getEmail(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        try {
            boolean success = verificationTokenService.verifyToken(token);
            if (success) {
                log.info("📧 Verifica email riuscita con token {}", token);
                return ResponseEntity.ok("✅ Email verificata con successo!");
            } else {
                log.warn("❌ Token non valido o scaduto: {}", token);
                return ResponseEntity.badRequest().body("❌ Token non valido o scaduto.");
            }
        } catch (Exception e) {
            log.error("❌ Errore durante verifica token {}", token, e);
            return ResponseEntity.internalServerError().body("Errore durante la verifica dell'email.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request.getEmail());
            log.info("🔐 Reset password avviato per {}", request.getEmail());
            return ResponseEntity.ok("✅ Nuova password inviata via email");
        } catch (Exception e) {
            log.error("❌ Errore durante reset password per {}", request.getEmail(), e);
            return ResponseEntity.badRequest().body("Errore durante l'invio della nuova password");
        }
    }

    @PutMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ResetPasswordRequest request
    ) {
        try {
            authService.resetPassword(authHeader, request);
            log.info("🔁 Password aggiornata per utente");
            return ResponseEntity.ok("Password aggiornata con successo");
        } catch (Exception e) {
            log.error("❌ Errore durante reset password", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
