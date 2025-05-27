package com.myspendr.myspendr.dto;

import com.myspendr.myspendr.model.CategoriaMovimento;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetRequest {
    private CategoriaMovimento categoria;
    private BigDecimal limite;
    private int mese; // es. 5 = Maggio
    private int anno;
}
