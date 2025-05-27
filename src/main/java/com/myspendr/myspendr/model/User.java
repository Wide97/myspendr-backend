package com.myspendr.myspendr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"capitale", "budgetMensili"})
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    private String nome;

    @NotBlank
    private String cognome;

    @NotBlank
    @Column(unique = true)
    private String username;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    private LocalDate dataRegistrazione;

    @Column(name = "tentativi_falliti")
    private Integer tentativiFalliti = 0;


    @Column(name = "bloccato_fino")
    private LocalDateTime bloccatoFino;

    private boolean emailConfirmed;

    @Column(unique = true)
    private String telegramToken;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Capitale capitale;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<BudgetMensile> budgetMensili = new ArrayList<>();


}
