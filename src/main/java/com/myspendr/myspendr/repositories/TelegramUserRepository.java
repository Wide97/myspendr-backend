package com.myspendr.myspendr.repositories;

import com.myspendr.myspendr.model.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
}
