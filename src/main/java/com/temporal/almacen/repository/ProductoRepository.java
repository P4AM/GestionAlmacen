package com.temporal.almacen.repository;

import com.temporal.almacen.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria LEFT JOIN FETCH p.proveedor ORDER BY p.nombre ASC")
    List<Producto> findAllWithEagerRelationships();

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria LEFT JOIN FETCH p.proveedor WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> findByNombreContainingIgnoreCaseWithEagerRelationships(@Param("nombre") String nombre);

    @Query(value = "SELECT " +
            "(SELECT COUNT(*) FROM productos) as total_productos, " +
            "(SELECT COUNT(*) FROM productos WHERE stock < 10) as stock_bajo, " +
            "(SELECT COALESCE(SUM(precio * stock), 0.0) FROM productos) as valor_total, " +
            "(SELECT COUNT(*) FROM categorias) as total_categorias", nativeQuery = true)
    Object[][] getDashboardMetrics();

    @Query("SELECT p.categoria.nombre as categoria, COUNT(p) as cantidad FROM Producto p GROUP BY p.categoria.nombre")
    List<Object[]> countStockPorCategoria();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE Producto p SET p.stock = p.stock + :cantidad WHERE p.id = :id")
    void actualizarStock(@Param("id") Long id, @Param("cantidad") int cantidad);
}
