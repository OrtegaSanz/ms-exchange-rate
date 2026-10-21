package com.nttdata.ms.exchange.rate.repository;

import com.nttdata.ms.exchange.rate.domain.Cliente;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClienteRepository implements PanacheRepository<Cliente> {
    public Cliente findByDni(String dni) {
        String normalizedDni = dni.trim();
        return find("dni = ?1", normalizedDni).firstResult();
    }
}
