package com.temporal.almacen.controller;

import com.temporal.almacen.model.Cliente;
import com.temporal.almacen.service.ClienteService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public String index(Model model) {
        List<Cliente> clientes = clienteService.listarClientes();
        model.addAttribute("clientes", clientes);
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("totalClientes", clientes.size());
        model.addAttribute("view", "clients/index :: main");
        model.addAttribute("module", "clientes");
        return "layout";
    }

    @GetMapping("/form")
    public String getForm(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clients/form :: client-form";
    }
    
    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false) String q, Model model) {
        List<Cliente> clientes = clienteService.buscarClientes(q);
        model.addAttribute("clientes", clientes);
        model.addAttribute("totalClientes", clientes.size());
        return "clients/list :: client-list";
    }

    @PostMapping("/add")
    public String addClient(@ModelAttribute Cliente cliente, Model model, HtmxResponse htmxResponse) {
        clienteService.guardarCliente(cliente);
        List<Cliente> clientes = clienteService.listarClientes();
        model.addAttribute("clientes", clientes);
        model.addAttribute("totalClientes", clientes.size());

        htmxResponse.addTrigger("clientAdded");

        return "clients/list :: client-list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Cliente cliente = clienteService.obtenerCliente(id);
        model.addAttribute("cliente", cliente);
        return "clients/form :: client-form";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id, Model model, HtmxResponse htmxResponse) {
        clienteService.eliminarCliente(id);
        List<Cliente> clientes = clienteService.listarClientes();
        model.addAttribute("clientes", clientes);
        model.addAttribute("totalClientes", clientes.size());
        
        htmxResponse.addTrigger("clientDeleted");
        
        return "clients/list :: client-list";
    }
}
