package com.myspendr.myspendr.services;

import com.myspendr.myspendr.dto.LoginRequest;
import com.myspendr.myspendr.dto.LoginResponse;
import com.myspendr.myspendr.dto.RegisterRequest;
import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.repositories.UserRepository;
import com.myspendr.myspendr.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
            throw new RuntimeException("Email già registrata.");
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
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password errata");
        }

        String token = jwtUtils.generateJwtToken(user.getEmail());
        return new LoginResponse(token);
    }
}