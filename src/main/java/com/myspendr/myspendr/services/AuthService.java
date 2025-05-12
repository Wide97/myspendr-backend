package com.myspendr.myspendr.services;

import com.myspendr.myspendr.dto.LoginRequest;
import com.myspendr.myspendr.dto.LoginResponse;
import com.myspendr.myspendr.dto.RegisterRequest;
import com.myspendr.myspendr.exceptions.EmailAlreadyExistsException;
import com.myspendr.myspendr.exceptions.InvalidCredentialsException;
import com.myspendr.myspendr.exceptions.UserNotFoundException;
import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.repositories.UserRepository;
import com.myspendr.myspendr.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.emailService = emailService;
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
                .build();

        emailService.sendWelcomeEmail(user.getEmail(), user.getNome());
        userRepository.save(user);
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
        return new LoginResponse(token);
    }



}
