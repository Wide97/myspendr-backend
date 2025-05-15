package com.myspendr.myspendr.controllers;

import com.myspendr.myspendr.dto.*;
import com.myspendr.myspendr.services.AuthService;
import com.myspendr.myspendr.services.VerificationTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        authService.register(request);
        return ResponseEntity.ok("Registrazione completata");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        boolean success = verificationTokenService.verifyToken(token);
        if (success) {
            return ResponseEntity.ok("✅ Email verificata con successo!");
        } else {
            return ResponseEntity.badRequest().body("❌ Token non valido o scaduto.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("✅ Nuova password inviata via email");
    }

    @PutMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody com.myspendr.myspendr.dto.ResetPasswordRequest request,
            @RequestHeader("Authorization") String token) {

        authService.resetPassword(token, request);
        return ResponseEntity.ok("✅ Password aggiornata con successo");
    }



}