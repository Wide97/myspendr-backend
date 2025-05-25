package com.myspendr.myspendr.repositories;

import com.myspendr.myspendr.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByTelegramToken(String telegramToken);
}