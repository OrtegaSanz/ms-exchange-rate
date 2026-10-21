package com.nttdata.ms.exchange.rate.api.mapper;

import com.nttdata.ms.exchange.rate.api.dto.ClienteResponse;
import com.nttdata.ms.exchange.rate.domain.Cliente;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClienteMapper {

    public ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getDni());
    }
}
