package com.nttdata.ms.exchange.rate.api;

import com.nttdata.ms.exchange.rate.api.dto.ClienteRequest;
import com.nttdata.ms.exchange.rate.api.dto.ClienteResponse;
import com.nttdata.ms.exchange.rate.domain.Cliente;
import com.nttdata.ms.exchange.rate.api.mapper.ClienteMapper;
import com.nttdata.ms.exchange.rate.service.ClienteService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/cliente")
public class ClienteController {

    @Inject
    ClienteService clienteService;

    @Inject
    ClienteMapper clienteMapper;

    @Context
    UriInfo uriInfo;

    @GET
    @Produces("application/json")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Lista de clientes obtenida correctamente"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public List<ClienteResponse> getAllClientes() {
        return clienteService.getAllClientes().stream()
                .map(clienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @POST
    @Produces("application/json")
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Cliente creado correctamente"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Solicitud incorrecta o el DNI ya existe"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public Response saveCliente(ClienteRequest clienteRequest) {
        Cliente cliente = clienteService.saveCliente(clienteRequest);
        return Response.created(uriInfo.getAbsolutePathBuilder()
                        .path(cliente.getDni())
                        .build())
                        .entity(clienteMapper.toResponse(cliente))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Produces("application/json")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Cliente actualizado correctamente"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Solicitud incorrecta o el DNI ya existe"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public Response updateCliente(@PathParam("id") Long id, ClienteRequest clienteRequest) {
        return Response.ok(clienteMapper.toResponse(clienteService.updateCliente(id, clienteRequest))).build();
    }

    @DELETE
    @Path("/{id}")
    @APIResponses({
            @APIResponse(
                    responseCode = "204",
                    description = "Cliente eliminado correctamente"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public Response deleteCliente(@PathParam("id") Long id) {
        clienteService.deleteCliente(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{dni}")
    @Produces("application/json")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Cliente encontrado correctamente"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public Response getClienteByDni(@PathParam("dni") String dni) {
        return Response.ok(clienteMapper.toResponse(clienteService.getClienteByDni(dni))).build();
    }
}
