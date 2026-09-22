package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.reporte.ReporteGuardar;
import com.connectaoficios.api.dtos.reporte.ReporteRechazo;
import com.connectaoficios.api.dtos.reporte.ReporteResolucion;
import com.connectaoficios.api.dtos.reporte.ReporteSalida;
import com.connectaoficios.api.enums.EstadoReporte;
import com.connectaoficios.api.enums.TipoReporte;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.Reporte;
import com.connectaoficios.api.modelos.Servicio;
import com.connectaoficios.api.repositorios.IReporteRepository;
import com.connectaoficios.api.repositorios.IServicioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private IReporteRepository reporteRepository;

    @Mock
    private IServicioRepository servicioRepository;

    private ReporteService reporteService;

    private Reporte reporte;

    private Servicio servicio;

    @BeforeEach
    void setUp() {

        reporteService = new ReporteService(
                reporteRepository,
                servicioRepository
        );

        servicio = new Servicio();
        servicio.setId(1L);

        reporte = new Reporte();
        reporte.setId(1L);
        reporte.setUsuarioReportanteId(10);
        reporte.setUsuarioReportadoId(20);
        reporte.setTipo(TipoReporte.USUARIO);
        reporte.setMotivo("Comportamiento inapropiado");
        reporte.setDescripcion("El usuario fue grosero durante el servicio");
        reporte.setEstado(EstadoReporte.PENDIENTE);
    }

    @Test
    void guardar_debeCrearReporteDeTipoUsuario() {

        ReporteGuardar dto = new ReporteGuardar();
        dto.setUsuarioReportanteId(10);
        dto.setUsuarioReportadoId(20);
        dto.setTipo(TipoReporte.USUARIO);
        dto.setMotivo("Comportamiento inapropiado");
        dto.setDescripcion("El usuario fue grosero durante el servicio");

        when(
                reporteRepository.save(any(Reporte.class))
        ).thenAnswer(invocation -> {

            Reporte guardado = invocation.getArgument(0);
            guardado.setId(1L);
            return guardado;
        });

        ReporteSalida resultado = reporteService.guardar(dto);

        assertNotNull(resultado);
        assertEquals(EstadoReporte.PENDIENTE, resultado.getEstado());
        assertEquals(TipoReporte.USUARIO, resultado.getTipo());
        assertEquals(20, resultado.getUsuarioReportadoId());

        verify(servicioRepository, never()).findById(anyLong());
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    @Test
    void guardar_debeCrearReporteDeTipoServicioYAsociarlo() {

        ReporteGuardar dto = new ReporteGuardar();
        dto.setUsuarioReportanteId(10);
        dto.setTipo(TipoReporte.SERVICIO);
        dto.setServicioId(1L);
        dto.setMotivo("Servicio incompleto");
        dto.setDescripcion("El trabajador no terminó el trabajo acordado");

        when(
                servicioRepository.findById(1L)
        ).thenReturn(Optional.of(servicio));

        when(
                reporteRepository.save(any(Reporte.class))
        ).thenAnswer(invocation -> {

            Reporte guardado = invocation.getArgument(0);
            guardado.setId(2L);
            return guardado;
        });

        ReporteSalida resultado = reporteService.guardar(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getServicioId());
        assertEquals(TipoReporte.SERVICIO, resultado.getTipo());

        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoTipoUsuarioSinUsuarioReportado() {

        ReporteGuardar dto = new ReporteGuardar();
        dto.setUsuarioReportanteId(10);
        dto.setTipo(TipoReporte.USUARIO);
        dto.setMotivo("Motivo");
        dto.setDescripcion("Descripción");

        assertThrows(
                ReglaNegocioException.class,
                () -> reporteService.guardar(dto)
        );

        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoTipoServicioSinServicioId() {

        ReporteGuardar dto = new ReporteGuardar();
        dto.setUsuarioReportanteId(10);
        dto.setTipo(TipoReporte.SERVICIO);
        dto.setMotivo("Motivo");
        dto.setDescripcion("Descripción");

        assertThrows(
                ReglaNegocioException.class,
                () -> reporteService.guardar(dto)
        );

        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoServicioNoExiste() {

        ReporteGuardar dto = new ReporteGuardar();
        dto.setUsuarioReportanteId(10);
        dto.setTipo(TipoReporte.SERVICIO);
        dto.setServicioId(99L);
        dto.setMotivo("Motivo");
        dto.setDescripcion("Descripción");

        when(
                servicioRepository.findById(99L)
        ).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> reporteService.guardar(dto)
        );

        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    void iniciarRevision_debeCambiarEstadoAEnRevisionCuandoEstaPendiente() {

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(reporteRepository.save(reporte)).thenReturn(reporte);

        ReporteSalida resultado = reporteService.iniciarRevision(1L);

        assertEquals(EstadoReporte.EN_REVISION, resultado.getEstado());
        verify(reporteRepository, times(1)).save(reporte);
    }

    @Test
    void iniciarRevision_debeLanzarExcepcionCuandoNoEstaPendiente() {

        reporte.setEstado(EstadoReporte.EN_REVISION);

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));

        assertThrows(
                ReglaNegocioException.class,
                () -> reporteService.iniciarRevision(1L)
        );

        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    void iniciarRevision_debeLanzarExcepcionCuandoReporteNoExiste() {

        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> reporteService.iniciarRevision(99L)
        );
    }

    @Test
    void resolver_debeMarcarComoResueltoCuandoEstaEnRevision() {

        reporte.setEstado(EstadoReporte.EN_REVISION);

        ReporteResolucion dto = new ReporteResolucion();
        dto.setResolucion("Se verificó la denuncia");
        dto.setAccionTomada("Se advirtió al usuario");

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(reporteRepository.save(reporte)).thenReturn(reporte);

        ReporteSalida resultado = reporteService.resolver(1L, dto, 5);

        assertEquals(EstadoReporte.RESUELTO, resultado.getEstado());
        assertEquals("Se advirtió al usuario", resultado.getAccionTomada());
        assertEquals(5, resultado.getAdministradorId());
        assertNotNull(resultado.getFechaResolucion());

        verify(reporteRepository, times(1)).save(reporte);
    }

    @Test
    void resolver_debeLanzarExcepcionCuandoNoEstaEnRevision() {

        ReporteResolucion dto = new ReporteResolucion();
        dto.setResolucion("Se verificó la denuncia");
        dto.setAccionTomada("Se advirtió al usuario");

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));

        assertThrows(
                ReglaNegocioException.class,
                () -> reporteService.resolver(1L, dto, 5)
        );

        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    void rechazar_debeMarcarComoRechazadoCuandoEstaEnRevision() {

        reporte.setEstado(EstadoReporte.EN_REVISION);

        ReporteRechazo dto = new ReporteRechazo();
        dto.setResolucion("No se encontró evidencia suficiente");

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(reporteRepository.save(reporte)).thenReturn(reporte);

        ReporteSalida resultado = reporteService.rechazar(1L, dto, 5);

        assertEquals(EstadoReporte.RECHAZADO, resultado.getEstado());
        assertEquals("No se encontró evidencia suficiente", resultado.getResolucion());
        assertEquals(5, resultado.getAdministradorId());

        verify(reporteRepository, times(1)).save(reporte);
    }

    @Test
    void rechazar_debeLanzarExcepcionCuandoNoEstaEnRevision() {

        ReporteRechazo dto = new ReporteRechazo();
        dto.setResolucion("No se encontró evidencia suficiente");

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));

        assertThrows(
                ReglaNegocioException.class,
                () -> reporteService.rechazar(1L, dto, 5)
        );

        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    void obtenerPorId_debeLanzarExcepcionCuandoNoExiste() {

        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> reporteService.obtenerPorId(99L)
        );
    }

    @Test
    void obtenerPorId_debeRetornarReporteConvertido() {

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));

        ReporteSalida resultado = reporteService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(EstadoReporte.PENDIENTE, resultado.getEstado());
    }
}