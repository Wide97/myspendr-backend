package com.myspendr.myspendr.dto;

import com.myspendr.myspendr.model.CategoriaMovimento;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetResponse {
    private CategoriaMovimento categoria;
    private BigDecimal limite;
    private BigDecimal speso;
    private BigDecimal residuo;
    private boolean superato;
    private int mese;
    private int anno;
}
