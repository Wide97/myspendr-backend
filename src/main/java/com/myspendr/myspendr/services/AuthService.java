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

import java.time.LocalDate;


@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
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
                .build();

        userRepository.save(user);
        log.info("Nuovo utente registrato con email: {}", user.getEmail());
    }


    public LoginResponse login(LoginRequest request) {
        log.info("Login in corso per email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login fallito: utente non trovato per email {}", request.getEmail());
                    return new UserNotFoundException("Utente non trovato");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login fallito: password errata per email {}", request.getEmail());
            throw new InvalidCredentialsException("Password errata");
        }

        String token = jwtUtils.generateJwtToken(user.getEmail());
        log.info("Login riuscito per {}", user.getEmail());
        return new LoginResponse(token);
    }
}
