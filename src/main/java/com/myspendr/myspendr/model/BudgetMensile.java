package com.myspendr.myspendr.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "budget_mensile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetMensile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaMovimento categoria;

    @Column(nullable = false)
    private BigDecimal limite;

    @Column(nullable = false)
    private Integer mese;

    @Column(nullable = false)
    private Integer anno;
}
