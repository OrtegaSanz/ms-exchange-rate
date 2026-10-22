package com.nttdata.ms.exchange.rate.domain;

public enum DireccionCambio {

    SOLES_A_DOLARES("PEN", "USD"),

    DOLARES_A_SOLES("USD", "PEN");

    private final String monedaOrigen;
    private final String monedaDestino;

    DireccionCambio(String monedaOrigen, String monedaDestino) {
        this.monedaOrigen = monedaOrigen;
        this.monedaDestino = monedaDestino;
    }

    public String getMonedaOrigen() {
        return monedaOrigen;
    }

    public String getMonedaDestino() {
        return monedaDestino;
    }
}
