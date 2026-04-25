package com.temporal.almacen.controller;

import com.temporal.almacen.model.Producto;
import com.temporal.almacen.service.InventarioService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;
    private final com.temporal.almacen.service.ProveedorService proveedorService;

    @GetMapping
    public String index(Model model) {
        List<Producto> productos = inventarioService.listarProductos();
        long stockBajo = productos.stream().filter(p -> p.getStock() < 10).count();

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", inventarioService.listarCategorias());
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        model.addAttribute("producto", new Producto());
        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("stockBajo", stockBajo);
        model.addAttribute("view", "inventory/index :: main");
        return "layout";
    }

    @GetMapping("/form")
    public String getForm(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", inventarioService.listarCategoriasActivas());
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        return "inventory/form :: product-form";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false) String q,
            @RequestParam(defaultValue = "nombre") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model) {
        List<Producto> productos = inventarioService.buscarProductos(q);

        // Ordenación en RAM (Super rápida)
        productos.sort((p1, p2) -> {
            int compare = 0;
            switch (sort) {
                case "nombre":
                    compare = p1.getNombre().compareToIgnoreCase(p2.getNombre());
                    break;
                case "categoria":
                    compare = p1.getCategoria().getNombre().compareToIgnoreCase(p2.getCategoria().getNombre());
                    break;
                case "stock":
                    compare = p1.getStock().compareTo(p2.getStock());
                    break;
                case "precio":
                    compare = p1.getPrecio().compareTo(p2.getPrecio());
                    break;
            }
            return "asc".equals(dir) ? compare : -compare;
        });

        model.addAttribute("productos", productos);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDir", dir);
        agregarEstadisticas(model, productos);
        return "inventory/list :: product-list";
    }

    @PostMapping("/add")
    public HtmxResponse addProduct(@ModelAttribute Producto producto, Model model) {
        try {
            inventarioService.guardarProducto(producto);

            model.addAttribute("productos", inventarioService.listarProductos());
            model.addAttribute("producto", new Producto());
            model.addAttribute("categorias", inventarioService.listarCategoriasActivas());
            model.addAttribute("proveedores", proveedorService.listarProveedores());

            return HtmxResponse.builder()
                    .view("inventory/list :: product-list")
                    .view("inventory/form :: product-form")
                    .triggerAfterSwap("show-toast",
                            Map.of("message", "¡Producto guardado con éxito!", "type", "success"))
                    .build();
        } catch (Exception e) {
            return HtmxResponse.builder()
                    .triggerAfterSwap("show-toast", Map.of("message", "Error: " + e.getMessage(), "type", "error"))
                    .build();
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Producto producto = inventarioService.obtenerProducto(id);
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", inventarioService.listarCategorias());
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        return "inventory/form :: product-form";
    }

    @PostMapping("/stock-entry")
    public String registerStockEntry(@RequestParam Long id, @RequestParam int cantidad, Model model,
            HtmxResponse htmxResponse) {
        inventarioService.registrarEntrada(id, cantidad, "Entrada rápida desde inventario");
        List<Producto> productos = inventarioService.listarProductos();
        model.addAttribute("productos", productos);
        agregarEstadisticas(model, productos);
        htmxResponse.addTrigger("stockUpdated");
        return "inventory/list :: product-list";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, Model model, HtmxResponse htmxResponse) {
        inventarioService.eliminarProducto(id);
        List<Producto> productos = inventarioService.listarProductos();
        model.addAttribute("productos", productos);
        agregarEstadisticas(model, productos);

        // Notify client
        htmxResponse.addTrigger("productDeleted");

        return "inventory/list :: product-list";
    }

    private void agregarEstadisticas(Model model, List<Producto> productos) {
        long stockBajo = productos.stream().filter(p -> p.getStock() < 10).count();
        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("stockBajo", stockBajo);
    }
}
