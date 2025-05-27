package com.myspendr.myspendr.repositories;

import com.myspendr.myspendr.model.TelegramUser;
import com.myspendr.myspendr.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {

    Optional<TelegramUser> findByUser(User user);
}
