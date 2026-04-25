package com.temporal.almacen.service;

import com.temporal.almacen.model.Proveedor;
import com.temporal.almacen.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final CacheService cacheService;

    @Cacheable("proveedores")
    public List<Proveedor> listarProveedores() {
        return proveedorRepository.findAll();
    }

    @Transactional
    public Proveedor guardarProveedor(Proveedor proveedor) {
        Proveedor guardado = proveedorRepository.save(proveedor);
        // Actualización quirúrgica en RAM
        cacheService.updateListInCache("proveedores", guardado, Proveedor::getId);
        return guardado;
    }

    @Transactional
    public void eliminarProveedor(Long id) {
        proveedorRepository.deleteById(id);
        // Eliminación quirúrgica en RAM
        cacheService.removeFromListInCache("proveedores", id, Proveedor::getId);
    }

    public Proveedor obtenerProveedor(Long id) {
        return listarProveedores().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseGet(() -> proveedorRepository.findById(id).orElse(null));
    }
}
