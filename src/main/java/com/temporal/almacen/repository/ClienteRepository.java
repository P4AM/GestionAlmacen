package com.temporal.almacen.repository;

import com.temporal.almacen.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByNombreContainingIgnoreCaseOrDocumentoIdentidadContainingIgnoreCase(String nombre, String doc);
}
