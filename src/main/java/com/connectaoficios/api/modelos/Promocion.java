package com.connectaoficios.api.modelos;

import com.connectaoficios.api.enums.EstadoPromocion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "promociones")
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "servicio_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_promocion_servicio")
    )
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "plan_promocion_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_promocion_plan")
    )
    private PlanPromocion plan;

    @Column(name = "trabajador_id", nullable = false)
    private Integer trabajadorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPromocion estado = EstadoPromocion.PENDIENTE;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = EstadoPromocion.PENDIENTE;
        }

        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}