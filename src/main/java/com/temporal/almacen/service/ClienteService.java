package com.temporal.almacen.service;

import com.temporal.almacen.model.Cliente;
import com.temporal.almacen.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CacheService cacheService;

    @Cacheable("clientes")
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public List<Cliente> buscarClientes(String query) {
        if (query == null || query.isBlank()) {
            return listarClientes();
        }
        return clienteRepository.findByNombreContainingIgnoreCaseOrDocumentoIdentidadContainingIgnoreCase(query, query);
    }

    @Transactional
    public Cliente guardarCliente(Cliente cliente) {
        Cliente guardado = clienteRepository.save(cliente);

        // Actualización incremental en caché de RAM
        cacheService.updateListInCache("clientes", guardado, Cliente::getId);

        return guardado;
    }

    public Cliente obtenerCliente(Long id) {
        return listarClientes().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseGet(() -> clienteRepository.findById(id).orElse(null));
    }

    @Transactional
    public void eliminarCliente(Long id) {
        clienteRepository.deleteById(id);

        // Eliminación incremental de la caché de RAM
        cacheService.removeFromListInCache("clientes", id, Cliente::getId);
    }
}
