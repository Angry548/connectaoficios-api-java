package com.connectaoficios.api.dtos.reporte;

import com.connectaoficios.api.enums.TipoReporte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReporteGuardar {

    @NotNull(message = "El usuario reportante es obligatorio")
    private Integer usuarioReportanteId;

    private Integer usuarioReportadoId;

    private Long servicioId;

    @NotNull(message = "El tipo de reporte es obligatorio")
    private TipoReporte tipo;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
    private String motivo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String descripcion;
}