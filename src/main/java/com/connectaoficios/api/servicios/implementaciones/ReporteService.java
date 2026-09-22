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
import com.connectaoficios.api.servicios.interfaces.IReporteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReporteService implements IReporteService {

    private final IReporteRepository reporteRepository;
    private final IServicioRepository servicioRepository;

    public ReporteService(IReporteRepository reporteRepository,
                          IServicioRepository servicioRepository) {
        this.reporteRepository = reporteRepository;
        this.servicioRepository = servicioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteSalida> obtenerTodos() {
        return reporteRepository.findAll()
                .stream()
                .map(this::convertirASalida)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReporteSalida> obtenerTodosPaginados(Pageable pageable) {
        return reporteRepository.findAll(pageable)
                .map(this::convertirASalida);
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteSalida obtenerPorId(Long id) {
        Reporte reporte = buscarPorIdOLanzar(id);
        return convertirASalida(reporte);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReporteSalida> obtenerPorEstado(EstadoReporte estado, Pageable pageable) {
        return reporteRepository.findByEstado(estado, pageable)
                .map(this::convertirASalida);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReporteSalida> obtenerPorTipo(TipoReporte tipo, Pageable pageable) {
        return reporteRepository.findByTipo(tipo, pageable)
                .map(this::convertirASalida);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteSalida> obtenerPorUsuarioReportante(Integer usuarioReportanteId) {
        return reporteRepository.findByUsuarioReportanteId(usuarioReportanteId)
                .stream()
                .map(this::convertirASalida)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteSalida> obtenerPorUsuarioReportado(Integer usuarioReportadoId) {
        return reporteRepository.findByUsuarioReportadoId(usuarioReportadoId)
                .stream()
                .map(this::convertirASalida)
                .toList();
    }

    @Override
    @Transactional
    public ReporteSalida guardar(ReporteGuardar dto) {
        validarConsistenciaPorTipo(dto.getTipo(), dto.getUsuarioReportadoId(), dto.getServicioId());

        Reporte reporte = new Reporte();
        reporte.setUsuarioReportanteId(dto.getUsuarioReportanteId());
        reporte.setUsuarioReportadoId(dto.getUsuarioReportadoId());
        reporte.setTipo(dto.getTipo());
        reporte.setMotivo(dto.getMotivo());
        reporte.setDescripcion(dto.getDescripcion());
        reporte.setEstado(EstadoReporte.PENDIENTE);
        reporte.setFechaCreacion(LocalDateTime.now());

        if (dto.getServicioId() != null) {
            Servicio servicio = servicioRepository.findById(dto.getServicioId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No se encontró el servicio con id " + dto.getServicioId()));
            reporte.setServicio(servicio);
        }

        Reporte guardado = reporteRepository.save(reporte);
        return convertirASalida(guardado);
    }

    @Override
    @Transactional
    public ReporteSalida iniciarRevision(Long id) {
        Reporte reporte = buscarPorIdOLanzar(id);

        if (reporte.getEstado() != EstadoReporte.PENDIENTE) {
            throw new ReglaNegocioException(
                    "Solo se puede iniciar revisión de un reporte en estado PENDIENTE");
        }

        reporte.setEstado(EstadoReporte.EN_REVISION);
        Reporte actualizado = reporteRepository.save(reporte);
        return convertirASalida(actualizado);
    }

    @Override
    @Transactional
    public ReporteSalida resolver(Long id, ReporteResolucion dto, Integer administradorId) {
        Reporte reporte = buscarPorIdOLanzar(id);

        if (reporte.getEstado() != EstadoReporte.EN_REVISION) {
            throw new ReglaNegocioException(
                    "Solo se puede resolver un reporte que esté en estado EN_REVISION");
        }

        reporte.setEstado(EstadoReporte.RESUELTO);
        reporte.setResolucion(dto.getResolucion());
        reporte.setAccionTomada(dto.getAccionTomada());
        reporte.setAdministradorId(administradorId);
        reporte.setFechaResolucion(LocalDateTime.now());

        Reporte actualizado = reporteRepository.save(reporte);
        return convertirASalida(actualizado);
    }

    @Override
    @Transactional
    public ReporteSalida rechazar(Long id, ReporteRechazo dto, Integer administradorId) {
        Reporte reporte = buscarPorIdOLanzar(id);

        if (reporte.getEstado() != EstadoReporte.EN_REVISION) {
            throw new ReglaNegocioException(
                    "Solo se puede rechazar un reporte que esté en estado EN_REVISION");
        }

        reporte.setEstado(EstadoReporte.RECHAZADO);
        reporte.setResolucion(dto.getResolucion());
        reporte.setAdministradorId(administradorId);
        reporte.setFechaResolucion(LocalDateTime.now());

        Reporte actualizado = reporteRepository.save(reporte);
        return convertirASalida(actualizado);
    }

    private Reporte buscarPorIdOLanzar(Long id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el reporte con id " + id));
    }

    private void validarConsistenciaPorTipo(TipoReporte tipo, Integer usuarioReportadoId, Long servicioId) {
        if (tipo == TipoReporte.USUARIO && usuarioReportadoId == null) {
            throw new ReglaNegocioException(
                    "Un reporte de tipo USUARIO requiere el usuario reportado");
        }
        if (tipo == TipoReporte.SERVICIO && servicioId == null) {
            throw new ReglaNegocioException(
                    "Un reporte de tipo SERVICIO requiere el servicio relacionado");
        }
    }

    private ReporteSalida convertirASalida(Reporte reporte) {
        ReporteSalida salida = new ReporteSalida();
        salida.setIdReporte(reporte.getIdReporte());
        salida.setUsuarioReportanteId(reporte.getUsuarioReportanteId());
        salida.setUsuarioReportadoId(reporte.getUsuarioReportadoId());
        salida.setServicioId(reporte.getServicio() != null ? reporte.getServicio().getIdServicio() : null);
        salida.setTipo(reporte.getTipo());
        salida.setMotivo(reporte.getMotivo());
        salida.setDescripcion(reporte.getDescripcion());
        salida.setEstado(reporte.getEstado());
        salida.setResolucion(reporte.getResolucion());
        salida.setAccionTomada(reporte.getAccionTomada());
        salida.setAdministradorId(reporte.getAdministradorId());
        salida.setFechaCreacion(reporte.getFechaCreacion());
        salida.setFechaResolucion(reporte.getFechaResolucion());
        return salida;
    }
}