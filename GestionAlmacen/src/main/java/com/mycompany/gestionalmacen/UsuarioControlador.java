package Modelo;

public class UsuarioControlador {
    private int id;
    private String nombre;

    public UsuarioControlador(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() {
        return id + " - " + nombre;
    }
}
