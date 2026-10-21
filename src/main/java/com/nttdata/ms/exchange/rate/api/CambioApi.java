package com.nttdata.ms.exchange.rate.api;

import com.nttdata.ms.exchange.rate.domain.CambioProxy;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/tipo-cambio/today.json")
@RegisterRestClient(configKey = "exchange-rate")
@Produces("application/json")
public interface CambioApi {
    @GET
    CambioProxy getExchangeRate();
}
