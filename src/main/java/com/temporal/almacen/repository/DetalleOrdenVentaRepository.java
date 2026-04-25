package com.temporal.almacen.repository;

import com.temporal.almacen.model.DetalleOrdenVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleOrdenVentaRepository extends JpaRepository<DetalleOrdenVenta, Long> {
}
