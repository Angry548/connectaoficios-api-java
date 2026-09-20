package com.connectaoficios.api.modelos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "resenas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_resena_solicitud",
                        columnNames = "solicitud_id"
                )
        }
)
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "solicitud_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_resena_solicitud")
    )
    private SolicitudServicio solicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "servicio_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_resena_servicio")
    )
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "perfil_trabajador_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_resena_perfil_trabajador")
    )
    private PerfilTrabajador perfilTrabajador;

    @Column(name = "cliente_id", nullable = false)
    private Integer clienteId;

    @Column(nullable = false)
    private Integer calificacion;

    @Column(length = 1000)
    private String comentario;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}