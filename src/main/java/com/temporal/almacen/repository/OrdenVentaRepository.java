package com.temporal.almacen.repository;

import com.temporal.almacen.model.OrdenVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenVentaRepository extends JpaRepository<OrdenVenta, Long> {
    
    List<OrdenVenta> findAllByOrderByFechaDesc();

    @Query("SELECT COALESCE(SUM(o.total), 0.0) FROM OrdenVenta o WHERE o.estado = 'COMPLETADA'")
    double sumTotalIngresos();
}
