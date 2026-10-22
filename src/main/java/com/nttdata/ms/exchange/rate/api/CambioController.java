package com.nttdata.ms.exchange.rate.api;

import com.nttdata.ms.exchange.rate.api.dto.CambioRequest;
import com.nttdata.ms.exchange.rate.api.mapper.CambioMapper;
import com.nttdata.ms.exchange.rate.domain.ResultadoCambio;
import com.nttdata.ms.exchange.rate.service.CambioService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.math.BigDecimal;

@Path("/api/exchange-rate")
public class CambioController {

    @Inject
    CambioService cambioService;

    @Inject
    CambioMapper cambioMapper;


    @GET
    @RunOnVirtualThread
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Tipo de cambio del dia obtenido correctamente"
            ),
            @APIResponse(
                    responseCode = "503",
                    description = "El proveedor externo de tipo de cambio no esta disponible"
            )
    })
    public Response getExchangeRate() {
        return Response.ok(cambioService.getExchangeRate()).build();
    }

    @POST
    @RunOnVirtualThread
    @Path("/{dni}/soles-a-dolares")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Cambio de soles a dolares realizado y registrado correctamente"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "El monto a cambiar debe ser mayor a cero"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado"
            ),
            @APIResponse(
                    responseCode = "429",
                    description = "Limite diario de operaciones de cambio alcanzado para el DNI"
            ),
            @APIResponse(
                    responseCode = "503",
                    description = "El proveedor externo de tipo de cambio no esta disponible"
            )
    })
    public Response cambiarSolesADolares(@PathParam("dni") String dni, CambioRequest cambioRequest) {
        return responder(
                cambioService.cambiarSolesADolares(dni, montoDe(cambioRequest)),
                dni);
    }


    @POST
    @RunOnVirtualThread
    @Path("/{dni}/dolares-a-soles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Cambio de dolares a soles realizado y registrado correctamente"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "El monto a cambiar debe ser mayor a cero"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado"
            ),
            @APIResponse(
                    responseCode = "429",
                    description = "Limite diario de operaciones de cambio alcanzado para el DNI"
            ),
            @APIResponse(
                    responseCode = "503",
                    description = "El proveedor externo de tipo de cambio no esta disponible"
            )
    })
    public Response cambiarDolaresASoles(@PathParam("dni") String dni, CambioRequest cambioRequest) {
        return responder(
                cambioService.cambiarDolaresASoles(dni, montoDe(cambioRequest)),
                dni);
    }

    private BigDecimal montoDe(CambioRequest cambioRequest) {
        return cambioRequest == null ? null : cambioRequest.monto();
    }

    private Response responder(ResultadoCambio resultado, String dni) {
        if (resultado == null) {
            return Response.status(Response.Status.TOO_MANY_REQUESTS)
                    .entity("{ \"mensaje\": \"Limite diario de " + cambioService.getLimiteDiario()
                            + " operaciones de cambio alcanzado para el DNI " + dni + "\" }")
                    .build();
        }
        return Response.ok(cambioMapper.toResponse(resultado)).build();
    }
}
