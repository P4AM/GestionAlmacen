package com.temporal.almacen.controller;

import com.temporal.almacen.model.Cliente;
import com.temporal.almacen.model.OrdenVenta;
import com.temporal.almacen.model.Producto;
import com.temporal.almacen.service.ClienteService;
import com.temporal.almacen.service.InventarioService;
import com.temporal.almacen.service.VentasService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentasController {

    private final VentasService ventasService;
    private final ClienteService clienteService;
    private final InventarioService inventarioService;

    @GetMapping
    public String index(Model model) {
        List<OrdenVenta> ordenes = ventasService.listarOrdenes(); // Ahora está en caché
        model.addAttribute("ordenes", ordenes);
        model.addAttribute("totalOrdenes", ordenes.size());
        
        // El cálculo ahora se hace en Supabase (SUM) de forma súper rápida y se cachea
        double totalIngresos = ventasService.calcularTotalIngresos();
        model.addAttribute("totalIngresos", totalIngresos);

        model.addAttribute("view", "sales/index :: main");
        model.addAttribute("module", "ventas");
        return "layout";
    }

    @GetMapping("/nueva")
    public String nuevaVenta(Model model) {
        List<Cliente> clientes = clienteService.listarClientes(); // CACHED
        List<Producto> productos = inventarioService.listarProductos(); // CACHED
        
        model.addAttribute("clientes", clientes);
        model.addAttribute("productos", productos);
        model.addAttribute("ordenVenta", new OrdenVenta());
        
        model.addAttribute("view", "sales/form :: main");
        model.addAttribute("module", "ventas");
        return "layout";
    }

    @PostMapping("/procesar")
    @ResponseBody
    public String procesarVenta(@RequestBody OrdenVenta ordenVenta) {
        try {
            ventasService.procesarVenta(ordenVenta);
            return "{\"success\": true}";
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }
}
