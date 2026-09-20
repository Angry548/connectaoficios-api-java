package com.connectaoficios.api.modelos;

import com.connectaoficios.api.enums.InsigniaReputacion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "reputaciones_trabajador",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reputacion_perfil",
                        columnNames = "perfil_trabajador_id"
                )
        }
)
public class ReputacionTrabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "perfil_trabajador_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_reputacion_perfil")
    )
    private PerfilTrabajador perfilTrabajador;

    @Column(
            name = "promedio_calificacion",
            nullable = false,
            precision = 3,
            scale = 2
    )
    private BigDecimal promedioCalificacion = BigDecimal.ZERO;

    @Column(name = "total_resenas", nullable = false)
    private Integer totalResenas = 0;

    @Column(name = "servicios_completados", nullable = false)
    private Integer serviciosCompletados = 0;

    @Column(
            name = "puntuacion_ranking",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal puntuacionRanking = BigDecimal.ZERO;

    @Column(name = "posicion_ranking")
    private Integer posicionRanking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InsigniaReputacion insignia = InsigniaReputacion.NUEVO_TRABAJADOR;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        if (promedioCalificacion == null) {
            promedioCalificacion = BigDecimal.ZERO;
        }

        if (totalResenas == null) {
            totalResenas = 0;
        }

        if (serviciosCompletados == null) {
            serviciosCompletados = 0;
        }

        if (puntuacionRanking == null) {
            puntuacionRanking = BigDecimal.ZERO;
        }

        if (insignia == null) {
            insignia = InsigniaReputacion.NUEVO_TRABAJADOR;
        }

        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}