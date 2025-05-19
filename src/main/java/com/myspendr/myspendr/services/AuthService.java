package com.myspendr.myspendr.services;

import com.myspendr.myspendr.dto.LoginRequest;
import com.myspendr.myspendr.dto.LoginResponse;
import com.myspendr.myspendr.dto.RegisterRequest;
import com.myspendr.myspendr.dto.ResetPasswordRequest;
import com.myspendr.myspendr.exceptions.EmailAlreadyExistsException;
import com.myspendr.myspendr.exceptions.InvalidCredentialsException;
import com.myspendr.myspendr.exceptions.UserNotFoundException;
import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.model.VerificationToken;
import com.myspendr.myspendr.repositories.UserRepository;
import com.myspendr.myspendr.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, EmailService emailService, VerificationTokenService verificationTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.emailService = emailService;
        this.verificationTokenService = verificationTokenService;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Tentativo di registrazione con email già usata: {}", request.getEmail());
            throw new EmailAlreadyExistsException("Email già registrata.");
        }

        User user = User.builder()
                .nome(request.getNome())
                .cognome(request.getCognome())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .dataRegistrazione(LocalDate.now())
                .tentativiFalliti(0)
                .emailConfirmed(false)
                .build();

        userRepository.save(user);

        // 🔐 Genera token di verifica email
        VerificationToken token = verificationTokenService.createTokenForUser(user);

        // 🔗 Crea link di verifica
        String verifyLink = "https://myspendr.com/api/auth/verify-email?token=" + token.getToken();

        // 📬 Invia email personalizzata di benvenuto + verifica
        String subject = "Conferma la tua email - MySpendr";
        String body = """
                Ciao %s! 👋
                
                Grazie per esserti registrato su *MySpendr*.
                Conferma la tua email cliccando sul link qui sotto entro 24 ore:
                
                👉 %s
                
                Se non hai richiesto questa registrazione, puoi ignorare questo messaggio.
                
                A presto!  
                — Il team di MySpendr
                """.formatted(user.getNome(), verifyLink);

        emailService.sendVerificationEmail(user.getEmail(), user.getNome(), verifyLink);

        log.info("Nuovo utente registrato con email: {}", user.getEmail());
    }


    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Tentativo di login fallito: utente {} non trovato", request.getEmail());
                    return new RuntimeException("Utente non trovato");
                });

        // Check blocco
        if (user.getBloccatoFino() != null && LocalDateTime.now().isBefore(user.getBloccatoFino())) {
            long minutiRestanti = Duration.between(LocalDateTime.now(), user.getBloccatoFino()).toMinutes();
            log.warn("Login bloccato per {}: ancora {} minuti", user.getEmail(), minutiRestanti);
            throw new RuntimeException("Account bloccato. Riprova tra " + minutiRestanti + " minuti.");
        }

        // Password errata
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int nuoviTentativi = user.getTentativiFalliti() + 1;
            user.setTentativiFalliti(nuoviTentativi);

            if (nuoviTentativi >= 5) {
                user.setBloccatoFino(LocalDateTime.now().plusMinutes(10));
                user.setTentativiFalliti(0);
                userRepository.save(user);
                log.warn("Blocco account per utente {} dopo 5 tentativi falliti", user.getEmail());
                throw new RuntimeException("Hai superato i tentativi. Account bloccato per 10 minuti.");
            }

            userRepository.save(user);
            log.warn("Tentativo di login fallito per {} - Tentativi rimasti: {}", user.getEmail(), (5 - nuoviTentativi));
            throw new RuntimeException("Password errata. Tentativi rimasti: " + (5 - nuoviTentativi));
        }

        // Login corretto
        user.setTentativiFalliti(0);
        user.setBloccatoFino(null);
        userRepository.save(user);

        String token = jwtUtils.generateJwtToken(user.getEmail());
        log.info("Login riuscito per utente {}", user.getEmail());
        return new LoginResponse(token, user.getUsername());
    }

    public void forgotPassword(String email) {
        log.info("Richiesta reset password per: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("❌ Nessun utente trovato con email: {}", email);
                    return new UserNotFoundException("Email non trovata");
                });

        String tempPassword = generateSecurePassword();

        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getNome(), tempPassword);
        log.info("✅ Password temporanea inviata a {}", user.getEmail());
    }

    private String generateSecurePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public void resetPassword(String authHeader, ResetPasswordRequest request) {
        String token = authHeader.replace("Bearer ", "").trim();
        String userEmail = jwtUtils.getUsernameFromJwtToken(token);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Utente non trovato"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("❌ Password attuale errata per utente {}", userEmail);
            throw new InvalidCredentialsException("Password attuale non corretta");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("✅ Password aggiornata per {}", userEmail);
    }


}
