package com.nttdata.ms.exchange.rate.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CambioConsultaResponse(
        String dni,
        String direccion,
        BigDecimal montoOrigen,
        String monedaOrigen,
        BigDecimal montoDestino,
        String monedaDestino,
        BigDecimal tipoCambioAplicado,
        Instant fechaOperacion,
        long operacionesRealizadasHoy,
        int limiteDiario) {
}
