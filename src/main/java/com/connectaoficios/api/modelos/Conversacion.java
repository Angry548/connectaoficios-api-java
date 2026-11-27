package com.connectaoficios.api.modelos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "conversaciones",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_conversacion_solicitud",
                        columnNames = "solicitud_id"
                ),
                @UniqueConstraint(name = "uk_conversacion_participantes_servicio",
                        columnNames = {"cliente_id", "trabajador_id", "servicio_id"})
        }
)
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "solicitud_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_conversacion_solicitud")
    )
    private SolicitudServicio solicitud;

    @Column(name = "cliente_id", nullable = false, updatable = false)
    private Integer clienteId;

    @Column(name = "trabajador_id", nullable = false, updatable = false)
    private Integer trabajadorId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_conversacion_servicio"))
    private Servicio servicio;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (solicitud != null) {
            if (clienteId == null) clienteId = solicitud.getClienteId();
            if (trabajadorId == null) trabajadorId = solicitud.getTrabajadorId();
            if (servicio == null) servicio = solicitud.getServicio();
        }
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
