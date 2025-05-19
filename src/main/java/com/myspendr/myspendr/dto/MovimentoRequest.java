package com.myspendr.myspendr.dto;

import com.myspendr.myspendr.model.CategoriaMovimento;
import com.myspendr.myspendr.model.TipoMovimento;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MovimentoRequest {
    private BigDecimal importo;
    private TipoMovimento tipo;
    private String descrizione;
    private CategoriaMovimento categoria;
    private LocalDate data;
    private String fonte;

}
