package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.notificacion.NotificacionGuardar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioCancelar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioFiltroDTO;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioGuardar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioRespuesta;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioRechazar;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.Servicio;
import com.connectaoficios.api.modelos.SolicitudServicio;
import com.connectaoficios.api.repositorios.IServicioRepository;
import com.connectaoficios.api.repositorios.ISolicitudServicioRepository;
import com.connectaoficios.api.servicios.interfaces.INotificacionService;
import com.connectaoficios.api.servicios.interfaces.ISolicitudServicioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolicitudServicioService implements ISolicitudServicioService {

    private static final int TAMANIO_MAXIMO_PAGINA = 100;

    private final ISolicitudServicioRepository solicitudRepository;
    private final IServicioRepository servicioRepository;
    private final INotificacionService notificacionService;

    public SolicitudServicioService(
            ISolicitudServicioRepository solicitudRepository,
            IServicioRepository servicioRepository,
            INotificacionService notificacionService
    ) {
        this.solicitudRepository = solicitudRepository;
        this.servicioRepository = servicioRepository;
        this.notificacionService = notificacionService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioRespuesta> obtenerTodas() {
        return solicitudRepository
                .findAll()
                .stream()
                .map(this::convertirARespuesta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudServicioRespuesta obtenerPorId(
            Long id
    ) {
        SolicitudServicio solicitud =
                buscarPorId(id);

        return convertirARespuesta(
                solicitud
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSalida<SolicitudServicioRespuesta> buscarConFiltros(
            SolicitudServicioFiltroDTO filtro,
            int pagina,
            int tamanio
    ) {
        validarRangoFechas(
                filtro.getFechaDesde(),
                filtro.getFechaHasta()
        );

        String texto =
                normalizarTexto(
                        filtro.getTexto()
                );

        Pageable pageable =
                crearPageable(
                        pagina,
                        tamanio
                );

        Page<SolicitudServicioRespuesta> resultado =
                solicitudRepository
                        .buscarConFiltros(
                                texto,
                                filtro.getServicioId(),
                                filtro.getClienteId(),
                                filtro.getTrabajadorId(),
                                filtro.getEstado(),
                                filtro.getFechaDesde(),
                                filtro.getFechaHasta(),
                                pageable
                        )
                        .map(this::convertirARespuesta);

        return PaginaSalida.desde(
                resultado
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioRespuesta> obtenerPorCliente(
            Integer clienteId
    ) {
        return solicitudRepository
                .findByClienteId(
                        clienteId
                )
                .stream()
                .map(this::convertirARespuesta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioRespuesta> obtenerPorTrabajador(
            Integer trabajadorId
    ) {
        return solicitudRepository
                .findByTrabajadorId(
                        trabajadorId
                )
                .stream()
                .map(this::convertirARespuesta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSalida<SolicitudServicioRespuesta> obtenerPorClientePaginadas(
            Integer clienteId,
            int pagina,
            int tamanio
    ) {
        Pageable pageable =
                crearPageable(
                        pagina,
                        tamanio
                );

        Page<SolicitudServicioRespuesta> resultado =
                solicitudRepository
                        .findByClienteId(
                                clienteId,
                                pageable
                        )
                        .map(this::convertirARespuesta);

        return PaginaSalida.desde(
                resultado
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSalida<SolicitudServicioRespuesta> obtenerPorTrabajadorPaginadas(
            Integer trabajadorId,
            int pagina,
            int tamanio
    ) {
        Pageable pageable =
                crearPageable(
                        pagina,
                        tamanio
                );

        Page<SolicitudServicioRespuesta> resultado =
                solicitudRepository
                        .findByTrabajadorId(
                                trabajadorId,
                                pageable
                        )
                        .map(this::convertirARespuesta);

        return PaginaSalida.desde(
                resultado
        );
    }

    @Override
    @Transactional
    public SolicitudServicioRespuesta guardar(
            SolicitudServicioGuardar dto
    ) {
        Servicio servicio =
                servicioRepository
                        .findByIdAndEliminadoFalse(
                                dto.getServicioId()
                        )
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró el servicio con ID: "
                                                + dto.getServicioId()
                                )
                        );

        SolicitudServicio solicitud =
                new SolicitudServicio();

        solicitud.setServicio(
                servicio
        );

        solicitud.setClienteId(
                dto.getClienteId()
        );

        solicitud.setTrabajadorId(
                dto.getTrabajadorId()
        );

        solicitud.setFechaPropuesta(
                dto.getFechaPropuesta()
        );

        solicitud.setHoraAproximada(
                dto.getHoraAproximada()
        );

        solicitud.setDireccion(
                dto.getDireccionServicio().trim()
        );

        solicitud.setDescripcionTrabajo(
                dto.getDescripcionTrabajo().trim()
        );

        solicitud.setEstado(
                EstadoSolicitud.PENDIENTE
        );

        SolicitudServicio guardada =
                solicitudRepository.save(
                        solicitud
                );

        crearNotificacionNuevaSolicitud(
                guardada
        );

        return convertirARespuesta(
                guardada
        );
    }

    @Override
    @Transactional
    public void eliminar(
            Long id
    ) {
        SolicitudServicio solicitud =
                buscarPorId(id);

        solicitudRepository.delete(
                solicitud
        );
    }

    @Override
    @Transactional
    public SolicitudServicioRespuesta aceptar(
            Long id
    ) {
        SolicitudServicio solicitud =
                buscarPorId(id);

        validarTransicionEstado(
                solicitud,
                EstadoSolicitud.ACEPTADA
        );

        solicitud.setEstado(
                EstadoSolicitud.ACEPTADA
        );

        SolicitudServicio guardada =
                solicitudRepository.save(
                        solicitud
                );

        crearNotificacionCambioEstado(
                guardada,
                "Solicitud aceptada",
                "El trabajador aceptó tu solicitud de servicio."
        );

        return convertirARespuesta(
                guardada
        );
    }

    @Override
    @Transactional
    public SolicitudServicioRespuesta rechazar(
            Long id,
            SolicitudServicioRechazar dto
    ) {
        SolicitudServicio solicitud =
                buscarPorId(id);

        validarTransicionEstado(
                solicitud,
                EstadoSolicitud.RECHAZADA
        );

        solicitud.setEstado(
                EstadoSolicitud.RECHAZADA
        );

        solicitud.setMotivoRechazo(
                dto.getMotivoRechazo().trim()
        );

        SolicitudServicio guardada =
                solicitudRepository.save(
                        solicitud
                );

        crearNotificacionCambioEstado(
                guardada,
                "Solicitud rechazada",
                "El trabajador rechazó tu solicitud de servicio."
        );

        return convertirARespuesta(
                guardada
        );
    }

    @Override
    @Transactional
    public SolicitudServicioRespuesta iniciar(
            Long id
    ) {
        SolicitudServicio solicitud =
                buscarPorId(id);

        validarTransicionEstado(
                solicitud,
                EstadoSolicitud.EN_PROCESO
        );

        solicitud.setEstado(
                EstadoSolicitud.EN_PROCESO
        );

        SolicitudServicio guardada =
                solicitudRepository.save(
                        solicitud
                );

        crearNotificacionCambioEstado(
                guardada,
                "Servicio iniciado",
                "El trabajador inició el servicio solicitado."
        );

        return convertirARespuesta(
                guardada
        );
    }

    @Override
    @Transactional
    public SolicitudServicioRespuesta completar(
            Long id
    ) {
        SolicitudServicio solicitud =
                buscarPorId(id);

        validarTransicionEstado(
                solicitud,
                EstadoSolicitud.COMPLETADA
        );

        solicitud.setEstado(
                EstadoSolicitud.COMPLETADA
        );

        SolicitudServicio guardada =
                solicitudRepository.save(
                        solicitud
                );

        crearNotificacionCambioEstado(
                guardada,
                "Servicio completado",
                "El trabajador marcó tu servicio como completado."
        );

        return convertirARespuesta(
                guardada
        );
    }

    @Override
    @Transactional
    public SolicitudServicioRespuesta cancelar(
            Long id,
            SolicitudServicioCancelar dto
    ) {
        SolicitudServicio solicitud =
                buscarPorId(id);

        validarTransicionEstado(
                solicitud,
                EstadoSolicitud.CANCELADA
        );

        solicitud.setEstado(
                EstadoSolicitud.CANCELADA
        );

        solicitud.setMotivoCancelacion(
                dto.getMotivoCancelacion().trim()
        );

        SolicitudServicio guardada =
                solicitudRepository.save(
                        solicitud
                );

        crearNotificacionCancelacion(
                guardada
        );

        return convertirARespuesta(
                guardada
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esParticipante(
            Long id,
            Integer usuarioId
    ) {
        SolicitudServicio solicitud =
                buscarPorId(id);

        return usuarioId.equals(
                solicitud.getClienteId()
        ) || usuarioId.equals(
                solicitud.getTrabajadorId()
        );
    }

    private void crearNotificacionNuevaSolicitud(
            SolicitudServicio solicitud
    ) {
        NotificacionGuardar notificacion =
                new NotificacionGuardar();

        notificacion.setUsuarioDestinoId(
                solicitud.getTrabajadorId()
        );

        notificacion.setTipo(
                "NUEVA_SOLICITUD"
        );

        notificacion.setTitulo(
                "Nueva solicitud de servicio"
        );

        String tituloServicio =
                obtenerTituloServicio(
                        solicitud
                );

        notificacion.setMensaje(
                "Has recibido una nueva solicitud para el servicio \""
                        + tituloServicio
                        + "\"."
        );

        notificacion.setReferenciaId(
                solicitud.getId()
        );

        notificacionService.guardar(
                notificacion
        );
    }

    private void crearNotificacionCambioEstado(
            SolicitudServicio solicitud,
            String titulo,
            String mensaje
    ) {
        NotificacionGuardar notificacion =
                new NotificacionGuardar();

        notificacion.setUsuarioDestinoId(
                solicitud.getClienteId()
        );

        notificacion.setTipo(
                "CAMBIO_ESTADO_SOLICITUD"
        );

        notificacion.setTitulo(
                titulo
        );

        notificacion.setMensaje(
                mensaje
        );

        notificacion.setReferenciaId(
                solicitud.getId()
        );

        notificacionService.guardar(
                notificacion
        );
    }

    private void crearNotificacionCancelacion(
            SolicitudServicio solicitud
    ) {
        NotificacionGuardar notificacion =
                new NotificacionGuardar();

        notificacion.setUsuarioDestinoId(
                solicitud.getTrabajadorId()
        );

        notificacion.setTipo(
                "CAMBIO_ESTADO_SOLICITUD"
        );

        notificacion.setTitulo(
                "Solicitud cancelada"
        );

        notificacion.setMensaje(
                "La solicitud del servicio \""
                        + obtenerTituloServicio(solicitud)
                        + "\" fue cancelada."
        );

        notificacion.setReferenciaId(
                solicitud.getId()
        );

        notificacionService.guardar(
                notificacion
        );
    }

    private String obtenerTituloServicio(
            SolicitudServicio solicitud
    ) {
        if (
                solicitud.getServicio() != null
                        && solicitud.getServicio().getTitulo() != null
                        && !solicitud.getServicio()
                        .getTitulo()
                        .isBlank()
        ) {
            return solicitud
                    .getServicio()
                    .getTitulo()
                    .trim();
        }

        return "Servicio";
    }

    private Pageable crearPageable(
            int pagina,
            int tamanio
    ) {
        int paginaSegura =
                Math.max(
                        pagina,
                        0
                );

        int tamanioSeguro =
                Math.max(
                        1,
                        Math.min(
                                tamanio,
                                TAMANIO_MAXIMO_PAGINA
                        )
                );

        return PageRequest.of(
                paginaSegura,
                tamanioSeguro,
                Sort.by(
                        Sort.Direction.DESC,
                        "fechaCreacion"
                )
        );
    }

    private void validarRangoFechas(
            LocalDate fechaDesde,
            LocalDate fechaHasta
    ) {
        if (
                fechaDesde != null
                        && fechaHasta != null
                        && fechaDesde.isAfter(fechaHasta)
        ) {
            throw new ReglaNegocioException(
                    "La fecha inicial no puede ser posterior a la fecha final"
            );
        }
    }

    private String normalizarTexto(
            String texto
    ) {
        if (texto == null) {
            return null;
        }

        String textoLimpio =
                texto.trim();

        return textoLimpio.isBlank()
                ? null
                : textoLimpio;
    }

    private SolicitudServicio buscarPorId(
            Long id
    ) {
        return solicitudRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró la solicitud de servicio con ID: "
                                        + id
                        )
                );
    }

    private void validarTransicionEstado(
            SolicitudServicio solicitud,
            EstadoSolicitud nuevoEstado
    ) {
        EstadoSolicitud estadoActual =
                solicitud.getEstado();

        boolean transicionValida =
                switch (estadoActual) {
                    case PENDIENTE ->
                            nuevoEstado == EstadoSolicitud.ACEPTADA
                                    || nuevoEstado == EstadoSolicitud.RECHAZADA
                                    || nuevoEstado == EstadoSolicitud.CANCELADA;

                    case ACEPTADA ->
                            nuevoEstado == EstadoSolicitud.EN_PROCESO
                                    || nuevoEstado == EstadoSolicitud.CANCELADA;

                    case EN_PROCESO ->
                            nuevoEstado == EstadoSolicitud.COMPLETADA
                                    || nuevoEstado == EstadoSolicitud.CANCELADA;

                    case RECHAZADA, COMPLETADA, CANCELADA ->
                            false;
                };

        if (!transicionValida) {
            throw new ReglaNegocioException(
                    String.format(
                            "No se puede cambiar el estado de la solicitud de %s a %s",
                            estadoActual,
                            nuevoEstado
                    )
            );
        }
    }

    private SolicitudServicioRespuesta convertirARespuesta(
            SolicitudServicio entidad
    ) {
        SolicitudServicioRespuesta respuesta =
                new SolicitudServicioRespuesta();

        respuesta.setIdSolicitud(
                entidad.getId()
        );

        if (entidad.getServicio() != null) {
            respuesta.setServicioId(
                    entidad.getServicio().getId()
            );

            respuesta.setServicioTitulo(
                    entidad.getServicio().getTitulo()
            );
        }

        respuesta.setClienteId(
                entidad.getClienteId()
        );

        respuesta.setTrabajadorId(
                entidad.getTrabajadorId()
        );

        respuesta.setFechaPropuesta(
                entidad.getFechaPropuesta()
        );

        respuesta.setHoraAproximada(
                entidad.getHoraAproximada()
        );

        respuesta.setDireccionServicio(
                entidad.getDireccion()
        );

        respuesta.setDescripcionTrabajo(
                entidad.getDescripcionTrabajo()
        );

        respuesta.setEstado(
                entidad.getEstado() != null
                        ? entidad.getEstado().name()
                        : null
        );

        respuesta.setMotivoCancelacion(
                entidad.getMotivoCancelacion()
        );

        respuesta.setFechaCreacion(
                entidad.getFechaCreacion()
        );

        respuesta.setFechaActualizacion(
                entidad.getFechaActualizacion()
        );

        return respuesta;
    }
}