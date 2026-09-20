package com.connectaoficios.api.modelos;

import com.connectaoficios.api.enums.EstadoServicio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "servicios")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "perfil_trabajador_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_servicio_perfil_trabajador")
    )
    private PerfilTrabajador perfilTrabajador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "categoria_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_servicio_categoria")
    )
    private Categoria categoria;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(
            name = "tarifa_minima",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal tarifaMinima;

    @Column(
            name = "tarifa_maxima",
            precision = 10,
            scale = 2
    )
    private BigDecimal tarifaMaxima;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoServicio estado = EstadoServicio.ACTIVO;

    @Column(nullable = false)
    private Boolean eliminado = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "servicio_zona_cobertura",
            joinColumns = @JoinColumn(name = "servicio_id"),
            inverseJoinColumns = @JoinColumn(name = "zona_cobertura_id"),
            foreignKey = @ForeignKey(name = "fk_servicio_zona_servicio"),
            inverseForeignKey = @ForeignKey(name = "fk_servicio_zona_cobertura")
    )
    private Set<ZonaCobertura> zonasCobertura = new HashSet<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();

        fechaCreacion = ahora;
        fechaActualizacion = ahora;

        if (estado == null) {
            estado = EstadoServicio.ACTIVO;
        }

        if (eliminado == null) {
            eliminado = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}