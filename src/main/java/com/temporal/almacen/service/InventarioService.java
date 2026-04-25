package com.temporal.almacen.service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.temporal.almacen.model.Categoria;
import com.temporal.almacen.model.Producto;
import com.temporal.almacen.model.Proveedor;
import com.temporal.almacen.model.Movimiento;
import com.temporal.almacen.repository.CategoriaRepository;
import com.temporal.almacen.repository.ProductoRepository;
import com.temporal.almacen.repository.ProveedorRepository;
import com.temporal.almacen.repository.MovimientoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoRepository movimientoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CacheService cacheService;

    @Cacheable("productos")
    public List<Producto> listarProductos() {
        return productoRepository.findAllWithEagerRelationships();
    }

    public List<Producto> buscarProductos(String query) {
        if (query == null || query.isBlank()) {
            return listarProductos();
        }
        return productoRepository.findByNombreContainingIgnoreCaseWithEagerRelationships(query);
    }

    @Cacheable("categorias")
    @Transactional(readOnly = true)
    public List<Categoria> listarCategorias() {
        List<Categoria> cats = categoriaRepository.findAll();
        // Inicializamos la colección para evitar LazyInitializationException en la
        // vista/caché
        cats.forEach(c -> {
            if (c.getProductos() != null)
                c.getProductos().size();
        });
        return cats;
    }

    public List<Categoria> listarCategoriasActivas() {
        return listarCategorias().stream()
                .filter(Categoria::isActivo)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public Producto guardarProducto(Producto producto) {
        Producto guardado = productoRepository.save(producto);

        // Uso del servicio global de caché
        cacheService.updateListInCache("productos", guardado, Producto::getId);

        eventPublisher.publishEvent(
                new CacheRefreshEvent(this, "dashboardMetricas", "dashboardCategorias", "dashboardTendencia"));
        return guardado;
    }

    @Transactional
    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
        cacheService.removeFromListInCache("productos", id, Producto::getId);
        eventPublisher.publishEvent(
                new CacheRefreshEvent(this, "dashboardMetricas", "dashboardCategorias", "dashboardTendencia"));
    }

    @Transactional
    public void registrarEntrada(Long productoId, int cantidadAdicional, String motivo) {
        productoRepository.actualizarStock(productoId, cantidadAdicional);

        Movimiento mov = new Movimiento();
        Producto p = new Producto();
        p.setId(productoId);
        mov.setProducto(p);
        mov.setCantidad(cantidadAdicional);
        mov.setTipo("ENTRADA");
        mov.setMotivo(motivo != null ? motivo : "Entrada manual de stock");
        mov.setFecha(java.time.LocalDateTime.now());
        movimientoRepository.save(mov);

        // Actualizar stock en la caché de RAM inmediatamente sin re-descargar todo
        Producto pConNuevoStock = productoRepository.findById(productoId).orElse(null);
        if (pConNuevoStock != null) {
            cacheService.updateListInCache("productos", pConNuevoStock, Producto::getId);
        }

        eventPublisher.publishEvent(
                new CacheRefreshEvent(this, "dashboardMetricas", "dashboardCategorias", "dashboardTendencia"));
    }

    public Producto obtenerProducto(Long id) {
        // Cache-First: Buscamos en la lista que ya está en RAM
        return listarProductos().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseGet(() -> productoRepository.findById(id).orElse(null));
    }

    @Transactional
    public Categoria guardarCategoria(Categoria categoria) {
        // Buscamos si ya existe una categoría con ese nombre (ignorar mayúsculas/minúsculas si es posible)
        Optional<Categoria> existente = categoriaRepository.findByNombre(categoria.getNombre());
        
        if (existente.isPresent()) {
            Categoria cat = existente.get();
            cat.setActivo(true); // Siempre la reactivamos si se intenta guardar de nuevo
            // Si venían otros datos (en el futuro), se podrían actualizar aquí
            Categoria guardada = categoriaRepository.save(cat);
            cacheService.updateListInCache("categorias", guardada, Categoria::getId);
            return guardada;
        }

        Categoria guardada = categoriaRepository.save(categoria);
        // Actualización en RAM para categorías
        cacheService.updateListInCache("categorias", guardada, Categoria::getId);
        return guardada;
    }

    @Transactional
    public void eliminarCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id).orElseThrow();
        // Alternamos el estado: si está activa se desactiva, y viceversa
        categoria.setActivo(!categoria.isActivo());
        categoriaRepository.save(categoria);
        // Actualización quirúrgica en RAM
        cacheService.updateListInCache("categorias", categoria, Categoria::getId);
    }

    public long contarProductos() {
        return productoRepository.count();
    }

    public long contarCategorias() {
        return categoriaRepository.count();
    }

    @Cacheable("dashboardMetricas")
    public Object[] obtenerMetricasDashboard() {
        Object[][] result = productoRepository.getDashboardMetrics();
        if (result != null && result.length > 0) {
            return result[0];
        }
        return new Object[] { 0L, 0L, 0.0, 0L };
    }

    @Cacheable("dashboardCategorias")
    public java.util.Map<String, Long> obtenerStockPorCategoria() {
        return productoRepository.countStockPorCategoria().stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]));
    }

    @Cacheable("dashboardTendencia")
    public List<Object[]> obtenerTendenciaMovimientos() {
        return movimientoRepository.getMovimientosTrend();
    }

    public List<Producto> obtenerProductosBajos() {
        // Aprovechamos la RAM (Cache-First) para filtrar instantáneamente
        return listarProductos().stream()
                .filter(p -> p.getStock() < 10)
                .collect(java.util.stream.Collectors.toList());
    }
}