package com.myspendr.myspendr.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "capitale")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Capitale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal contoBancario = BigDecimal.ZERO;
    private BigDecimal liquidita = BigDecimal.ZERO;
    private BigDecimal altriFondi = BigDecimal.ZERO;
    private BigDecimal totale = BigDecimal.ZERO;

    private LocalDate dataAggiornamento;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @PrePersist
    @PreUpdate
    public void calcolaTotale() {
        this.totale = contoBancario.add(liquidita).add(altriFondi);
    }

}
