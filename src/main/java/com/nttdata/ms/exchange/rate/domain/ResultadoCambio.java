package com.nttdata.ms.exchange.rate.domain;

public record ResultadoCambio(
        ConsultaCambio operacion,
        long operacionesRealizadasHoy,
        int limiteDiario) {
}
