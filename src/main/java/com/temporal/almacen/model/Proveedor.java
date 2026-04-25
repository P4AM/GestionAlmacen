package com.temporal.almacen.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.util.List;

@Entity
@Table(name = "proveedores")
@Data
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String ruc;
    private String telefono;
    private String email;
    private String direccion;

    @OneToMany(mappedBy = "proveedor")
    @ToString.Exclude
    private List<Producto> productos;
}
