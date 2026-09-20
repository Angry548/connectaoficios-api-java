package com.connectaoficios.api.modelos;

import com.connectaoficios.api.enums.DiaSemana;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(
        name = "disponibilidades_servicio",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_disponibilidad_servicio_dia",
                        columnNames = {
                                "servicio_id",
                                "dia_semana",
                                "hora_inicio",
                                "hora_fin"
                        }
                )
        }
)
public class DisponibilidadServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "servicio_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_disponibilidad_servicio")
    )
    private Servicio servicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 15)
    private DiaSemana diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private Boolean activo = true;

    @PrePersist
    public void prePersist() {
        if (activo == null) {
            activo = true;
        }
    }
}