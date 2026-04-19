package Modelo;

public class UsuarioModelo {
    private int id;
    private String nombre;
    private double precio;
    private int stock;
    private UsuarioControlador categoria;

    public UsuarioModelo(int id, String nombre, double precio, int stock, UsuarioControlador categoria) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    public UsuarioControlador getCategoria() { return categoria; }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return id + " | " + nombre + " | S/" + precio + " | Stock: " + stock + " | " + categoria.getNombre();
    }
}