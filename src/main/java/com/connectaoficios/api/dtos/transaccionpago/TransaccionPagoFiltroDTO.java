package com.connectaoficios.api.dtos.transaccionpago;

import com.connectaoficios.api.enums.EstadoTransaccion;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransaccionPagoFiltroDTO {

    private Long promocionId;

    private Long servicioId;

    private Integer trabajadorId;

    private EstadoTransaccion estado;

    private String moneda;

    private String referenciaExterna;

    private BigDecimal montoMinimo;

    private BigDecimal montoMaximo;

    private LocalDateTime fechaDesde;

    private LocalDateTime fechaHasta;
}