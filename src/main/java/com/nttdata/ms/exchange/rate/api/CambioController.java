package com.nttdata.ms.exchange.rate.api;

import com.nttdata.ms.exchange.rate.domain.CambioProxy;
import com.nttdata.ms.exchange.rate.service.CambioService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("/api/exchange-rate")
public class CambioController {

    @Inject
    CambioService cambioService;

    @GET
    @RunOnVirtualThread
    @Path("/{dni}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Tipo de cambio obtenido correctamente"
            ),
            @APIResponse(
                    responseCode = "429",
                    description = "Límite diario de consultas alcanzado para el DNI"
            )
    })
    public Response getExchangeRate(@PathParam("dni") String dni) {
        CambioProxy cambio = cambioService.getExchangeRate(dni);
        if (cambio == null) {
            return Response.status(Response.Status.TOO_MANY_REQUESTS)
                    .entity("{ \"mensaje\": \"Límite diario de consultas alcanzado para el DNI " + dni + "\" }")
                    .build();
        }
        return Response.ok(cambio).build();
    }
}
