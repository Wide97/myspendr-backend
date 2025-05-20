package com.myspendr.myspendr.controllers;

import com.myspendr.myspendr.model.User;
import com.myspendr.myspendr.repositories.UserRepository;
import com.myspendr.myspendr.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<User> getMe(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        String email = jwtUtils.getUsernameFromJwtToken(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        return ResponseEntity.ok(user);
    }
}
