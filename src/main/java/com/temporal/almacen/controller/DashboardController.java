package com.temporal.almacen.controller;

import com.temporal.almacen.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final InventarioService inventarioService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // SUPER OPTIMIZACIÓN: 1 solo viaje a la base de datos para todas las métricas
        Object[] metricas = inventarioService.obtenerMetricasDashboard();
        
        long totalProductos = ((Number) metricas[0]).longValue();
        long stockBajo = ((Number) metricas[1]).longValue();
        double valorTotal = ((Number) metricas[2]).doubleValue();
        long totalCategorias = ((Number) metricas[3]).longValue();

        // 1 viaje más para el gráfico de torta (GROUP BY)
        Map<String, Long> stockPorCategoria = inventarioService.obtenerStockPorCategoria();

        // 1 viaje más para el gráfico de tendencia
        List<Object[]> tendencia = inventarioService.obtenerTendenciaMovimientos();

        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("stockBajo", stockBajo);
        model.addAttribute("valorTotal", valorTotal);
        model.addAttribute("totalCategorias", totalCategorias);
        model.addAttribute("stockPorCategoria", stockPorCategoria);
        model.addAttribute("tendencia", tendencia);
        model.addAttribute("productosBajos", inventarioService.obtenerProductosBajos());
        
        model.addAttribute("module", "dashboard");
        model.addAttribute("view", "dashboard :: main");
        
        return "layout";
    }
}
