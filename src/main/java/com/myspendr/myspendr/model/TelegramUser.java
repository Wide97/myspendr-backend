package com.myspendr.myspendr.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramUser {

    @Id
    private Long telegramId;

    private String username;
    private String firstName;
    private String lastName;

    @OneToOne
    private User user;
}
