package com.connectaoficios.api.dtos.transaccionpago;

import com.connectaoficios.api.enums.EstadoTransaccion;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransaccionPagoSalida {

    private Long id;

    private Long promocionId;

    private Long servicioId;

    private Integer trabajadorId;

    private BigDecimal monto;

    private String moneda;

    private String referenciaExterna;

    private EstadoTransaccion estado;

    private LocalDateTime fecha;
}