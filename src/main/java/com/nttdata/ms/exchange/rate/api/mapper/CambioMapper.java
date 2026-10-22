package com.nttdata.ms.exchange.rate.api.mapper;

import com.nttdata.ms.exchange.rate.api.dto.CambioConsultaResponse;
import com.nttdata.ms.exchange.rate.domain.ConsultaCambio;
import com.nttdata.ms.exchange.rate.domain.ResultadoCambio;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CambioMapper {

    public CambioConsultaResponse toResponse(ResultadoCambio resultado) {
        ConsultaCambio operacion = resultado.operacion();
        return new CambioConsultaResponse(
                operacion.getCliente().getDni(),
                operacion.getDireccion().name(),
                operacion.getMontoOrigen(),
                operacion.getDireccion().getMonedaOrigen(),
                operacion.getMontoDestino(),
                operacion.getDireccion().getMonedaDestino(),
                operacion.getTipoCambioAplicado(),
                operacion.getFechaAcceso().toInstant(),
                resultado.operacionesRealizadasHoy(),
                resultado.limiteDiario());
    }
}
