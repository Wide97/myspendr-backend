package com.myspendr.myspendr.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "movimenti")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal importo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoMovimento tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", length = 20)
    private CategoriaMovimento categoria;

    private String descrizione;

    private LocalDate data;

    private String fonte;

    @ManyToOne
    @JoinColumn(name = "capitale_id")
    private Capitale capitale;
}
