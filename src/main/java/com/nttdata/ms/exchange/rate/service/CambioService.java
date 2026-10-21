package com.nttdata.ms.exchange.rate.service;

import com.nttdata.ms.exchange.rate.api.CambioApi;
import com.nttdata.ms.exchange.rate.domain.CambioProxy;
import com.nttdata.ms.exchange.rate.domain.Cliente;
import com.nttdata.ms.exchange.rate.domain.ConsultaCambio;
import com.nttdata.ms.exchange.rate.repository.ClienteRepository;
import com.nttdata.ms.exchange.rate.repository.ConsultaCambioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Date;

@ApplicationScoped
public class CambioService {


    @Inject
    @RestClient
    CambioApi api;

    @ConfigProperty(name = "app.consultas.limite-diario", defaultValue = "10")
    int limiteDiarioConsultas;

    @Inject
    ClienteRepository clienteRepository;

    @Inject
    ConsultaCambioRepository consultaCambioRepository;

    @Transactional
    public CambioProxy getExchangeRate(String dni) {
        Cliente cliente = clienteRepository.findByDni(dni);
        if (cliente == null) {
            throw new NotFoundException("Cliente not found with dni: " + dni);
        }

        long consultasHoy = consultaCambioRepository.cantidadConsultasDia(cliente.getId());
        if (consultasHoy >= limiteDiarioConsultas) {
            return null;
        }

        ConsultaCambio consulta = new ConsultaCambio(cliente, new Date());
        consultaCambioRepository.persist(consulta);
        return api.getExchangeRate();
    }
}
