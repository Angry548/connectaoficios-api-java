package com.connectaoficios.api.modelos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "perfiles_trabajador",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_perfil_trabajador",
                        columnNames = "trabajador_id"
                )
        }
)
public class PerfilTrabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trabajador_id", nullable = false)
    private Integer trabajadorId;

    @Column(name = "oficio_principal", length = 100)
    private String oficioPrincipal;

    @Column(name = "descripcion_profesional", length = 1000)
    private String descripcionProfesional;

    @Column(name = "experiencia_laboral", length = 2000)
    private String experienciaLaboral;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "porcentaje_completitud", nullable = false)
    private Integer porcentajeCompletitud = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "zona_principal_id",
            foreignKey = @ForeignKey(name = "fk_perfil_zona_principal")
    )
    private ZonaCobertura zonaPrincipal;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();

        fechaCreacion = ahora;
        fechaActualizacion = ahora;

        if (porcentajeCompletitud == null) {
            porcentajeCompletitud = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}