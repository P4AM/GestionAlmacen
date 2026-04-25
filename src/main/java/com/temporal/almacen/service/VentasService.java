package com.temporal.almacen.service;

import com.temporal.almacen.model.DetalleOrdenVenta;
import com.temporal.almacen.model.Movimiento;
import com.temporal.almacen.model.OrdenVenta;
import com.temporal.almacen.model.Producto;
import com.temporal.almacen.repository.MovimientoRepository;
import com.temporal.almacen.repository.OrdenVentaRepository;
import com.temporal.almacen.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentasService {

    private final OrdenVentaRepository ordenVentaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CacheService cacheService;

    @Cacheable("ventas")
    public List<OrdenVenta> listarOrdenes() {
        return ordenVentaRepository.findAllByOrderByFechaDesc();
    }

    @Cacheable("ventasTotal")
    public double calcularTotalIngresos() {
        return ordenVentaRepository.sumTotalIngresos();
    }

    @Transactional
    public OrdenVenta procesarVenta(OrdenVenta ordenVenta) {
        ordenVenta.setFecha(LocalDateTime.now());
        ordenVenta.setEstado("COMPLETADA");

        // Set references and validate stock
        for (DetalleOrdenVenta detalle : ordenVenta.getDetalles()) {
            detalle.setOrden(ordenVenta);

            Producto p = productoRepository.findById(detalle.getProducto().getId())
                    .orElseThrow(
                            () -> new RuntimeException("Producto no encontrado: " + detalle.getProducto().getId()));

            if (p.getStock() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + p.getNombre());
            }

            // Deduct stock
            p.setStock(p.getStock() - detalle.getCantidad());
            productoRepository.save(p);

            // Create movement history
            Movimiento mov = new Movimiento();
            mov.setProducto(p);
            mov.setCantidad(detalle.getCantidad());
            mov.setTipo("SALIDA");
            mov.setMotivo("Venta de orden #" + (ordenVenta.getId() != null ? ordenVenta.getId() : "Nueva"));
            mov.setFecha(LocalDateTime.now());
            movimientoRepository.save(mov);
        }

        OrdenVenta guardada = ordenVentaRepository.save(ordenVenta);

        // 1. Actualización incremental de la lista de ventas en RAM
        cacheService.updateListInCache("ventas", guardada, OrdenVenta::getId);
        
        // 2. Actualización incremental de cada producto afectado en la caché
        for (DetalleOrdenVenta detalle : guardada.getDetalles()) {
            cacheService.updateListInCache("productos", detalle.getProducto(), Producto::getId);
        }

        // 3. Invalidar solo métricas globales
        cacheService.evict("ventasTotal");
        
        eventPublisher.publishEvent(new CacheRefreshEvent(this, "dashboardMetricas", "dashboardCategorias", "dashboardTendencia"));

        return guardada;
    }
}
