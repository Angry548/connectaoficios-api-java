package com.connectaoficios.api.modelos;

import com.connectaoficios.api.enums.EstadoTransaccion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "transacciones_pago")
public class TransaccionPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "promocion_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_transaccion_promocion")
    )
    private Promocion promocion;

    @Column(name = "trabajador_id", nullable = false)
    private Integer trabajadorId;

    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal monto;

    @Column(nullable = false, length = 10)
    private String moneda;

    @Column(name = "referencia_externa", length = 255)
    private String referenciaExterna;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTransaccion estado = EstadoTransaccion.PENDIENTE;

    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = EstadoTransaccion.PENDIENTE;
        }

        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}