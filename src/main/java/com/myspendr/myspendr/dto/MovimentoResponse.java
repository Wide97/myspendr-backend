package com.myspendr.myspendr.dto;

import com.myspendr.myspendr.model.Movimento;
import com.myspendr.myspendr.model.TipoMovimento;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MovimentoResponse {
    private Long id;
    private BigDecimal importo;
    private TipoMovimento tipo;
    private String descrizione;
    private LocalDate data;

    public MovimentoResponse(Movimento movimento) {
        this.id = movimento.getId();
        this.importo = movimento.getImporto();
        this.tipo = movimento.getTipo();
        this.descrizione = movimento.getDescrizione();
        this.data = movimento.getData();
    }
}
