package com.temporal.almacen.repository;

import com.temporal.almacen.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    java.util.List<Proveedor> findByNombreContainingIgnoreCaseOrRucContainingIgnoreCase(String nombre, String ruc);
}
