package com.temporal.almacen.controller;

import com.temporal.almacen.model.Proveedor;
import com.temporal.almacen.service.ProveedorService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    public String index(Model model) {
        List<Proveedor> proveedores = proveedorService.listarProveedores();
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("proveedor", new Proveedor());
        model.addAttribute("totalProveedores", proveedores.size());
        model.addAttribute("view", "providers/index :: main");
        model.addAttribute("module", "proveedores");
        return "layout";
    }

    @GetMapping("/form")
    public String getForm(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "providers/form :: provider-form";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false) String q, Model model) {
        // En un ERP real, las búsquedas podrían no estar en caché si son muy dinámicas,
        // pero aquí devolvemos la lista completa si no hay query.
        List<Proveedor> proveedores = proveedorService.listarProveedores();
        if (q != null && !q.isBlank()) {
            proveedores = proveedores.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(q.toLowerCase()) || 
                             p.getRuc().contains(q))
                .toList();
        }
        model.addAttribute("proveedores", proveedores);
        return "providers/list :: provider-list";
    }

    @PostMapping("/add")
    public String addProvider(@ModelAttribute Proveedor proveedor, Model model, HtmxResponse htmxResponse) {
        proveedorService.guardarProveedor(proveedor);
        List<Proveedor> proveedores = proveedorService.listarProveedores();
        
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("totalProveedores", proveedores.size());
        htmxResponse.addTrigger("providerAdded");

        return "providers/list :: provider-list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Proveedor proveedor = proveedorService.obtenerProveedor(id);
        model.addAttribute("proveedor", proveedor);
        return "providers/form :: provider-form";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteProvider(@PathVariable Long id, Model model, HtmxResponse htmxResponse) {
        proveedorService.eliminarProveedor(id);
        List<Proveedor> proveedores = proveedorService.listarProveedores();
        
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("totalProveedores", proveedores.size());
        htmxResponse.addTrigger("providerDeleted");
        
        return "providers/list :: provider-list";
    }
}
