package com.connectaoficios.api.modelos;

import com.connectaoficios.api.enums.EstadoSolicitud;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "solicitudes_servicio")
public class SolicitudServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "servicio_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_solicitud_servicio")
    )
    private Servicio servicio;

    /*
     * ID externo del usuario Cliente proveniente de la API .NET.
     * No constituye una llave foránea en MySQL.
     */
    @Column(name = "cliente_id", nullable = false)
    private Integer clienteId;

    /*
     * ID externo del usuario Trabajador proveniente de la API .NET.
     * No constituye una llave foránea en MySQL.
     */
    @Column(name = "trabajador_id", nullable = false)
    private Integer trabajadorId;

    @Column(name = "fecha_propuesta", nullable = false)
    private LocalDate fechaPropuesta;

    @Column(name = "hora_aproximada", nullable = false)
    private LocalTime horaAproximada;

    @Column(nullable = false, length = 500)
    private String direccion;

    @Column(name = "descripcion_trabajo", nullable = false, length = 2000)
    private String descripcionTrabajo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

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
            estado = EstadoSolicitud.PENDIENTE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}