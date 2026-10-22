package com.nttdata.ms.exchange.rate.service;

import com.nttdata.ms.exchange.rate.api.CambioApi;
import com.nttdata.ms.exchange.rate.domain.CambioProxy;
import com.nttdata.ms.exchange.rate.domain.Cliente;
import com.nttdata.ms.exchange.rate.domain.ConsultaCambio;
import com.nttdata.ms.exchange.rate.domain.DireccionCambio;
import com.nttdata.ms.exchange.rate.domain.ResultadoCambio;
import com.nttdata.ms.exchange.rate.repository.ClienteRepository;
import com.nttdata.ms.exchange.rate.repository.ConsultaCambioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServiceUnavailableException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@ApplicationScoped
public class CambioService {

    private static final int ESCALA_MONTO = 2;

    @Inject
    @RestClient
    CambioApi api;

    @ConfigProperty(name = "app.consultas.limite-diario", defaultValue = "10")
    int limiteDiarioConsultas;

    @Inject
    ClienteRepository clienteRepository;

    @Inject
    ConsultaCambioRepository consultaCambioRepository;

    public int getLimiteDiario() {
        return limiteDiarioConsultas;
    }

    public CambioProxy getExchangeRate() {
        return api.getExchangeRate();
    }

    @Transactional
    public ResultadoCambio cambiarSolesADolares(String dni, BigDecimal montoSoles) {
        return cambiar(dni, montoSoles, DireccionCambio.SOLES_A_DOLARES);
    }

    @Transactional
    public ResultadoCambio cambiarDolaresASoles(String dni, BigDecimal montoDolares) {
        return cambiar(dni, montoDolares, DireccionCambio.DOLARES_A_SOLES);
    }

    private ResultadoCambio cambiar(String dni, BigDecimal monto, DireccionCambio direccion) {
        Cliente cliente = clienteRepository.findByDni(dni);
        if (cliente == null) {
            throw new NotFoundException("Cliente not found with dni: " + dni);
        }

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto a cambiar debe ser mayor a cero");
        }

        long operacionesHoy = consultaCambioRepository.cantidadConsultasDia(cliente.getId());
        if (operacionesHoy >= limiteDiarioConsultas) {
            return null;
        }

        CambioProxy cambio = api.getExchangeRate();
        BigDecimal tipoCambio = obtenerTipoCambio(cambio, direccion);

        BigDecimal montoSoles;
        BigDecimal montoDolares;
        if (direccion == DireccionCambio.SOLES_A_DOLARES) {
            montoSoles = monto;
            montoDolares = monto.divide(tipoCambio, ESCALA_MONTO, RoundingMode.HALF_UP);
        } else {
            montoDolares = monto;
            montoSoles = monto.multiply(tipoCambio).setScale(ESCALA_MONTO, RoundingMode.HALF_UP);
        }

        ConsultaCambio operacion = new ConsultaCambio(
                cliente,
                new Date(),
                direccion,
                montoSoles,
                montoDolares,
                tipoCambio);
        consultaCambioRepository.persist(operacion);

        return new ResultadoCambio(operacion, operacionesHoy + 1, limiteDiarioConsultas);
    }

    private BigDecimal obtenerTipoCambio(CambioProxy cambio, DireccionCambio direccion) {
        BigDecimal tipoCambio = cambio == null ? null
                : (direccion == DireccionCambio.SOLES_A_DOLARES ? cambio.getVenta() : cambio.getCompra());

        if (tipoCambio == null || tipoCambio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceUnavailableException(
                    "No se pudo obtener un tipo de cambio valido del proveedor externo");
        }
        return tipoCambio;
    }
}
