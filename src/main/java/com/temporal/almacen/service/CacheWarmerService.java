package com.temporal.almacen.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheWarmerService {

    private final InventarioService inventarioService;
    private final ClienteService clienteService;
    private final VentasService ventasService;
    private final ProveedorService proveedorService;

    // 1. Calentamiento Inicial (Ocurre 1 sola vez al encender)
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Iniciando Calentamiento de Caché (Startup)...");
        warmUpAll();
    }

    // 1.1 Marcapasos (Ocurre cada 15 min para asegurar que la RAM nunca esté vacía)
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 15, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    public void scheduledWarmUp() {
        warmUpAll();
    }

    /**
     * REGLA DE ORO: Las LISTAS se actualizan incrementalmente en RAM por los servicios.
     * El Warmer solo se encarga de refrescar las MÉTRICAS que requieren SQL pesado.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCacheRefreshEvent(CacheRefreshEvent event) {
        log.debug("Refrescando métricas complejas tras cambio en DB: {}", Arrays.toString(event.getCachesToRefresh()));
        
        for (String cacheName : event.getCachesToRefresh()) {
            switch (cacheName) {
                case "dashboardMetricas":
                    inventarioService.obtenerMetricasDashboard();
                    break;
                case "dashboardCategorias":
                    inventarioService.obtenerStockPorCategoria();
                    break;
                case "dashboardTendencia":
                    inventarioService.obtenerTendenciaMovimientos();
                    break;
                case "ventasTotal":
                    ventasService.calcularTotalIngresos();
                    break;
                // NOTA: 'productos', 'clientes', 'ventas', etc., ya NO se refrescan aquí
                // porque se actualizan quirúrgicamente en RAM por sus servicios.
            }
        }
    }

    private void warmUpAll() {
        inventarioService.listarProductos();
        inventarioService.listarCategorias();
        proveedorService.listarProveedores();
        inventarioService.obtenerMetricasDashboard();
        inventarioService.obtenerStockPorCategoria();
        inventarioService.obtenerTendenciaMovimientos();
        clienteService.listarClientes();
        ventasService.listarOrdenes();
        ventasService.calcularTotalIngresos();
    }
}
