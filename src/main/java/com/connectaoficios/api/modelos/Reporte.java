package com.connectaoficios.api.modelos;

import com.connectaoficios.api.enums.EstadoReporte;
import com.connectaoficios.api.enums.TipoReporte;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reportes")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_reportante_id", nullable = false)
    private Integer usuarioReportanteId;

    @Column(name = "usuario_reportado_id")
    private Integer usuarioReportadoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "servicio_id",
            foreignKey = @ForeignKey(name = "fk_reporte_servicio")
    )
    private Servicio servicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoReporte tipo;

    @Column(nullable = false, length = 200)
    private String motivo;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReporte estado = EstadoReporte.PENDIENTE;

    @Column(length = 2000)
    private String resolucion;

    @Column(name = "accion_tomada", length = 500)
    private String accionTomada;

    @Column(name = "administrador_id")
    private Integer administradorId;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = EstadoReporte.PENDIENTE;
        }

        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}