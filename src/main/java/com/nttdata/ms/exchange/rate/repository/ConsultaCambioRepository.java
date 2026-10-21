package com.nttdata.ms.exchange.rate.repository;

import com.nttdata.ms.exchange.rate.domain.ConsultaCambio;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@ApplicationScoped
public class ConsultaCambioRepository implements PanacheRepository<ConsultaCambio> {

    private static final ZoneId ZONA = ZoneId.of("America/Lima");

    public long cantidadConsultasDia(Long clienteId) {
        LocalDate hoy = LocalDate.now(ZONA);
        Date desde = Date.from(hoy.atStartOfDay(ZONA).toInstant());
        Date hasta = Date.from(hoy.plusDays(1).atStartOfDay(ZONA).toInstant());

        return count("cliente.id = ?1 and fechaAcceso >= ?2 and fechaAcceso < ?3",
                clienteId, desde, hasta);
    }
}
