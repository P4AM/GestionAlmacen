package Gestion;

import Modelo.*;
import Controlador.UsuarioVista;

import java.util.Scanner;

public class GestionAlmacen {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        UsuarioVista sistema = new UsuarioVista();

        // Categorías
        UsuarioControlador laptops = new UsuarioControlador(1, "Laptops");
        UsuarioControlador accesorios = new UsuarioControlador(2, "Accesorios");

        sistema.agregarCategoria(laptops);
        sistema.agregarCategoria(accesorios);

        int opcion;

        do {
            System.out.println("\n=== MENU INVENTARIO ===");
            System.out.println("1. Agregar producto");
            System.out.println("2. Mostrar productos");
            System.out.println("3. Buscar producto");
            System.out.println("4. Salir");
            System.out.print("Opción: ");
            opcion = sc.nextInt();

            switch (opcion) {

                case 1:
                    agregarProducto(sistema, laptops, accesorios);
                    break;

                case 2:
                    sistema.mostrarProductos();
                    break;

                case 3:
                    buscarProducto(sistema);
                    break;

            }

        } while (opcion != 4);
    }

    // AGREGAR PRODUCTO
    public static void agregarProducto(UsuarioVista sistema, UsuarioControlador c1, UsuarioControlador c2) {

        System.out.print("ID: ");
        int id = sc.nextInt();

        sc.nextLine();
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();

        System.out.print("Precio: ");
        double precio = sc.nextDouble();

        System.out.print("Stock: ");
        int stock = validarStock(sc.nextInt());

        System.out.println("Categoria: 1=Laptops 2=Accesorios");
        int op = sc.nextInt();

        UsuarioControlador cat = (op == 1) ? c1 : c2;

        UsuarioModelo p = new UsuarioModelo(id, nombre, precio, stock, cat);
        sistema.agregarProducto(p);

        System.out.println("Producto agregado ✔");
    }

    // BUSCAR
    public static void buscarProducto(UsuarioVista sistema) {
        System.out.print("Ingrese ID: ");
        int id = sc.nextInt();

        UsuarioModelo p = sistema.buscarProducto(id);

        if (p != null)
            System.out.println(p);
        else
            System.out.println("No encontrado");
    }

    // RECURSIVIDAD
    public static int validarStock(int stock) {
        if (stock < 0) {
            System.out.println("Stock inválido, ingrese nuevamente:");
            return validarStock(sc.nextInt());
        }
        return stock;
    }
}