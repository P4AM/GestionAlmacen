package com.temporal.almacen.repository;

import com.temporal.almacen.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    @Query(value = "SELECT " +
            "TO_CHAR(fecha, 'Mon') as mes, " +
            "SUM(CASE WHEN tipo = 'ENTRADA' THEN cantidad ELSE 0 END) as entradas, " +
            "SUM(CASE WHEN tipo = 'SALIDA' THEN cantidad ELSE 0 END) as salidas " +
            "FROM movimientos " +
            "WHERE fecha >= CURRENT_DATE - INTERVAL '6 months' " +
            "GROUP BY TO_CHAR(fecha, 'Mon'), DATE_TRUNC('month', fecha) " +
            "ORDER BY DATE_TRUNC('month', fecha)", nativeQuery = true)
    List<Object[]> getMovimientosTrend();
}
