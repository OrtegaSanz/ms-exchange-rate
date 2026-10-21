package com.nttdata.ms.exchange.rate.domain;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CambioProxy {
    private String fecha;
    private BigDecimal sunat;
    private BigDecimal compra;
    private BigDecimal venta;

}
