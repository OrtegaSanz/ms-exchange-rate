package com.nttdata.ms.exchange.rate.service;

import com.nttdata.ms.exchange.rate.api.dto.ClienteRequest;
import com.nttdata.ms.exchange.rate.domain.Cliente;
import com.nttdata.ms.exchange.rate.repository.ClienteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class ClienteService {
    @Inject ClienteRepository clienteRepository;

    @Transactional
    public Cliente saveCliente(ClienteRequest clienteRequest) {
        if (clienteRequest == null
                || isBlank(clienteRequest.nombre())
                || isBlank(clienteRequest.apellido())
                || isBlank(clienteRequest.dni())) {
            throw new BadRequestException("Nombre, apellido y DNI son obligatorios");
        }

        if (clienteRepository.findByDni(clienteRequest.dni().trim()) != null) {
            throw new BadRequestException("El DNI ya existe");
        }

        Cliente cliente = new Cliente(
                clienteRequest.nombre().trim(),
                clienteRequest.apellido().trim(),
                clienteRequest.dni().trim());
        clienteRepository.persist(cliente);
        return cliente;
    }

    @Transactional
    public void deleteCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id);
        if (cliente == null) {
            throw new NotFoundException("Cliente not found with id: " + id);
        }
        clienteRepository.delete(cliente);
    }

    @Transactional
    public Cliente updateCliente(Long id, ClienteRequest clienteRequest) {
        Cliente cliente = clienteRepository.findById(id);
        if (cliente == null) {
            throw new NotFoundException("Cliente not found with id: " + id);
        }
        if (clienteRequest == null
                || isBlank(clienteRequest.nombre())
                || isBlank(clienteRequest.apellido())
                || isBlank(clienteRequest.dni())) {
            throw new BadRequestException("Nombre, apellido y DNI son obligatorios");
        }

        Cliente existente = clienteRepository.findByDni(clienteRequest.dni().trim());
        if (existente != null && !existente.getId().equals(id)) {
            throw new BadRequestException("El DNI ya existe");
        }

        cliente.setNombre(clienteRequest.nombre().trim());
        cliente.setApellido(clienteRequest.apellido().trim());
        cliente.setDni(clienteRequest.dni().trim());
        return cliente;
    }

    public List<Cliente> getAllClientes() {
        return clienteRepository.listAll();
    }

    public Cliente getClienteByDni(String dni) {

        if (dni == null || dni.isBlank()) {
            throw new BadRequestException("El DNI no puede estar vacío");
        }
        Cliente cliente = clienteRepository.findByDni(dni);
        if (cliente == null) {
            throw new NotFoundException("Cliente not found with dni: " + dni.trim());
        }
        return cliente;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
