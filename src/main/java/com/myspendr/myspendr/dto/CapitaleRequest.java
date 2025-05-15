package com.myspendr.myspendr.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class CapitaleRequest {
    private BigDecimal contoBancario;
    private BigDecimal liquidita;
    private BigDecimal altriFondi;
}

