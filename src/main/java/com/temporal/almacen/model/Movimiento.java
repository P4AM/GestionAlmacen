package com.temporal.almacen.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos")
@Data
public class Movimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad; // Positivo para entrada, negativo para salida

    @Column(nullable = false)
    private String tipo; // "ENTRADA", "SALIDA"

    private String motivo;
    private LocalDateTime fecha = LocalDateTime.now();
}
