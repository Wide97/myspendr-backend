package com.myspendr.myspendr.dto;

import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CapitaleResponse {
    private BigDecimal contoBancario;
    private BigDecimal liquidita;
    private BigDecimal altriFondi;
    private BigDecimal totale;
    private LocalDate dataAggiornamento;

    public CapitaleResponse(com.myspendr.myspendr.model.Capitale capitale) {
        this.contoBancario = capitale.getContoBancario();
        this.liquidita = capitale.getLiquidita();
        this.altriFondi = capitale.getAltriFondi();
        this.totale = capitale.getTotale();
        this.dataAggiornamento = capitale.getDataAggiornamento();
    }

    public CapitaleResponse(BigDecimal contoBancario, BigDecimal liquidita, BigDecimal altriFondi, BigDecimal totale) {
        this.contoBancario = contoBancario;
        this.liquidita = liquidita;
        this.altriFondi = altriFondi;
        this.totale = totale;
        this.dataAggiornamento = LocalDate.now();
    }

    public static CapitaleResponse vuoto() {
        return new CapitaleResponse(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

}
