package com.myspendr.myspendr.dto;

import com.myspendr.myspendr.model.CategoriaMovimento;
import com.myspendr.myspendr.model.TipoMovimento;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MovimentoRequest {

    @NotNull(message = "Importo obbligatorio")
    private BigDecimal importo;

    @NotNull(message = "Tipo obbligatorio (ENTRATA o USCITA)")
    private TipoMovimento tipo;

    private String descrizione;

    @NotNull(message = "Categoria obbligatoria")
    private CategoriaMovimento categoria;

    @NotNull(message = "Data obbligatoria")
    private LocalDate data;

    @NotNull(message = "Fonte obbligatoria")
    private String fonte;
}
