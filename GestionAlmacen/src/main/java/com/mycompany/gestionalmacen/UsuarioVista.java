package Controlador;

import Modelo.*;
import java.util.ArrayList;

public class UsuarioVista {

    private ArrayList<UsuarioModelo> productos = new ArrayList<>();
    private ArrayList<UsuarioControlador> categorias = new ArrayList<>();

    public void agregarCategoria(UsuarioControlador c) {
        categorias.add(c);
    }

    public void agregarProducto(UsuarioModelo p) {
        productos.add(p);
    }

    public void mostrarProductos() {
        for (UsuarioModelo p : productos) {
            System.out.println(p);
        }
    }

    public UsuarioModelo buscarProducto(int id) {
        for (UsuarioModelo p : productos) {
            if (p.getId() == id) return p;
        }
        return null;
    }
}