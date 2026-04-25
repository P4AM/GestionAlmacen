package com.temporal.almacen.controller;

import com.temporal.almacen.model.Categoria;
import com.temporal.almacen.service.InventarioService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final InventarioService inventarioService;

    @GetMapping
    public String index(Model model) {
        List<Categoria> categorias = inventarioService.listarCategorias();
        model.addAttribute("categorias", categorias);
        model.addAttribute("categoria", new Categoria());
        model.addAttribute("totalCategorias", categorias.size());
        model.addAttribute("view", "categories/index :: main");
        model.addAttribute("module", "categorias");
        return "layout";
    }

    @GetMapping("/form")
    public String getForm(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "categories/form :: category-form";
    }

    @PostMapping("/add")
    public HtmxResponse addCategory(@ModelAttribute Categoria categoria, Model model) {
        inventarioService.guardarCategoria(categoria);
        List<Categoria> categorias = inventarioService.listarCategorias();

        model.addAttribute("categorias", categorias);
        model.addAttribute("totalCategorias", categorias.size());
        model.addAttribute("categoria", new Categoria());

        return HtmxResponse.builder()
                .view("categories/list :: category-list")
                .view("categories/form :: category-form")
                .triggerAfterSwap("show-toast", Map.of("message", "Categoría guardada con éxito", "type", "success"))
                .build();
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Categoria categoria = inventarioService.listarCategorias().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst().orElse(null);
        model.addAttribute("categoria", categoria);
        return "categories/form :: category-form";
    }

    @DeleteMapping("/delete/{id}")
    public HtmxResponse deleteCategory(@PathVariable Long id, Model model) {
        try {
            inventarioService.eliminarCategoria(id);
            model.addAttribute("categorias", inventarioService.listarCategorias());
            model.addAttribute("totalCategorias", inventarioService.listarCategorias().size());

            return HtmxResponse.builder()
                    .view("categories/list :: category-list")
                    .triggerAfterSwap("show-toast",
                            Map.of("message", "Estado de categoría actualizado", "type", "success"))
                    .build();
        } catch (Exception e) {
            return HtmxResponse.builder()
                    .triggerAfterSwap("show-toast", Map.of("message", "Error al procesar", "type", "error"))
                    .build();
        }
    }
}
