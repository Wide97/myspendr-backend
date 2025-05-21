package com.myspendr.myspendr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ReportCapitaleDTO {
    private String periodo;
    private BigDecimal valoreTotale;
    private BigDecimal variazione;
}
