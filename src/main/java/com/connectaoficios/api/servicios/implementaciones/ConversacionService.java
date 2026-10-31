package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.conversacion.ConversacionFiltroDTO;
import com.connectaoficios.api.dtos.conversacion.ConversacionGuardar;
import com.connectaoficios.api.dtos.conversacion.ConversacionSalida;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.Conversacion;
import com.connectaoficios.api.modelos.SolicitudServicio;
import com.connectaoficios.api.repositorios.IConversacionRepository;
import com.connectaoficios.api.repositorios.ISolicitudServicioRepository;
import com.connectaoficios.api.servicios.interfaces.IConversacionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.connectaoficios.api.repositorios.IPerfilClienteRepository;
import com.connectaoficios.api.repositorios.IPerfilTrabajadorRepository;

@Service
public class ConversacionService implements IConversacionService {

    private static final int TAMANIO_MAXIMO_PAGINA = 100;

    private final IConversacionRepository conversacionRepository;
    private final ISolicitudServicioRepository solicitudServicioRepository;
    private final IPerfilClienteRepository perfilClienteRepository;
    private final IPerfilTrabajadorRepository perfilTrabajadorRepository;

    public ConversacionService(
            IConversacionRepository conversacionRepository,
            ISolicitudServicioRepository solicitudServicioRepository,
            IPerfilClienteRepository perfilClienteRepository,
            IPerfilTrabajadorRepository perfilTrabajadorRepository
    ) {
        this.conversacionRepository = conversacionRepository;
        this.solicitudServicioRepository = solicitudServicioRepository;
        this.perfilClienteRepository = perfilClienteRepository;
        this.perfilTrabajadorRepository = perfilTrabajadorRepository;
    }

    @Override
    @Transactional
    public ConversacionSalida guardar(
            ConversacionGuardar conversacionGuardar,
            Integer usuarioId
    ) {

        SolicitudServicio solicitud =
                solicitudServicioRepository
                        .findById(conversacionGuardar.getSolicitudId())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró la solicitud de servicio"
                                )
                        );

        validarParticipante(
                solicitud,
                usuarioId
        );

        validarEstadoParaChat(
                solicitud
        );

        if (conversacionRepository.existsBySolicitudId(
                solicitud.getId()
        )) {

            throw new ReglaNegocioException(
                    "La solicitud ya posee una conversación"
            );
        }

        Conversacion conversacion =
                new Conversacion();

        conversacion.setSolicitud(
                solicitud
        );

        Conversacion conversacionGuardada =
                conversacionRepository.save(
                        conversacion
                );

        return convertirASalida(
                conversacionGuardada
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ConversacionSalida obtenerPorId(
            Long id,
            Integer usuarioId
    ) {

        Conversacion conversacion =
                conversacionRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró la conversación"
                                )
                        );

        validarParticipante(
                conversacion.getSolicitud(),
                usuarioId
        );

        return convertirASalida(
                conversacion
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ConversacionSalida obtenerPorSolicitud(
            Long solicitudId,
            Integer usuarioId
    ) {

        Conversacion conversacion =
                conversacionRepository
                        .findBySolicitudId(solicitudId)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró una conversación para la solicitud"
                                )
                        );

        validarParticipante(
                conversacion.getSolicitud(),
                usuarioId
        );

        return convertirASalida(
                conversacion
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSalida<ConversacionSalida> buscarConversaciones(
            ConversacionFiltroDTO filtro,
            Integer usuarioId,
            int pagina,
            int tamanio
    ) {

        validarUsuario(
                usuarioId
        );

        validarRangoFechas(
                filtro.getFechaDesde(),
                filtro.getFechaHasta()
        );

        Pageable pageable =
                crearPageable(
                        pagina,
                        tamanio
                );

        Page<Conversacion> conversaciones =
                conversacionRepository
                        .buscarConversacionesUsuario(
                                usuarioId,
                                filtro.getSolicitudId(),
                                filtro.getEstadoSolicitud(),
                                filtro.getFechaDesde(),
                                filtro.getFechaHasta(),
                                filtro.getPuedeEnviarMensajes(),
                                pageable
                        );

        Map<Integer, String> fotosClientes = new HashMap<>();
        Map<Integer, String> fotosTrabajadores = new HashMap<>();
        if (!conversaciones.isEmpty()) {
            Set<Integer> clientes = conversaciones.stream()
                    .map(c -> c.getSolicitud().getClienteId()).collect(Collectors.toSet());
            Set<Integer> trabajadores = conversaciones.stream()
                    .map(c -> c.getSolicitud().getTrabajadorId()).collect(Collectors.toSet());
            perfilClienteRepository.findByClienteIdIn(clientes)
                    .forEach(p -> fotosClientes.put(p.getClienteId(), p.getFotoUrl()));
            perfilTrabajadorRepository.findByTrabajadorIdIn(trabajadores)
                    .forEach(p -> fotosTrabajadores.put(p.getTrabajadorId(), p.getFotoUrl()));
        }
        Page<ConversacionSalida> resultado = conversaciones.map(c ->
                convertirASalida(c, fotosClientes, fotosTrabajadores));

        return PaginaSalida.desde(
                resultado
        );
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
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    ) {

        if (fechaDesde != null
                && fechaHasta != null
                && fechaDesde.isAfter(fechaHasta)) {

            throw new ReglaNegocioException(
                    "La fecha inicial no puede ser posterior a la fecha final"
            );
        }
    }

    private void validarUsuario(
            Integer usuarioId
    ) {

        if (usuarioId == null) {

            throw new ReglaNegocioException(
                    "No se pudo identificar al usuario autenticado"
            );
        }
    }

    private void validarParticipante(
            SolicitudServicio solicitud,
            Integer usuarioId
    ) {

        validarUsuario(
                usuarioId
        );

        boolean esCliente =
                usuarioId.equals(
                        solicitud.getClienteId()
                );

        boolean esTrabajador =
                usuarioId.equals(
                        solicitud.getTrabajadorId()
                );

        if (!esCliente && !esTrabajador) {

            throw new ReglaNegocioException(
                    "El usuario no pertenece a esta conversación"
            );
        }
    }

    private void validarEstadoParaChat(
            SolicitudServicio solicitud
    ) {

        EstadoSolicitud estado =
                solicitud.getEstado();

        if (estado != EstadoSolicitud.ACEPTADA
                && estado != EstadoSolicitud.EN_PROCESO) {

            throw new ReglaNegocioException(
                    "La conversación solo puede habilitarse cuando "
                            + "la solicitud está aceptada o en proceso"
            );
        }
    }

    private boolean puedeEnviarMensajes(
            SolicitudServicio solicitud
    ) {

        EstadoSolicitud estado =
                solicitud.getEstado();

        return estado == EstadoSolicitud.ACEPTADA
                || estado == EstadoSolicitud.EN_PROCESO;
    }

    private ConversacionSalida convertirASalida(
            Conversacion conversacion
    ) {
        Map<Integer, String> clientes = new HashMap<>();
        Map<Integer, String> trabajadores = new HashMap<>();
        perfilClienteRepository.findByClienteId(conversacion.getSolicitud().getClienteId())
                .ifPresent(p -> clientes.put(p.getClienteId(), p.getFotoUrl()));
        perfilTrabajadorRepository.findByTrabajadorId(conversacion.getSolicitud().getTrabajadorId())
                .ifPresent(p -> trabajadores.put(p.getTrabajadorId(), p.getFotoUrl()));
        return convertirASalida(conversacion, clientes, trabajadores);
    }

    private ConversacionSalida convertirASalida(Conversacion conversacion,
            Map<Integer, String> clientes, Map<Integer, String> trabajadores) {

        SolicitudServicio solicitud =
                conversacion.getSolicitud();

        ConversacionSalida salida =
                new ConversacionSalida();
        salida.setClienteFotoUrl(clientes.get(solicitud.getClienteId()));
        salida.setTrabajadorFotoUrl(trabajadores.get(solicitud.getTrabajadorId()));

        salida.setId(
                conversacion.getId()
        );

        salida.setSolicitudId(
                solicitud.getId()
        );

        salida.setClienteId(
                solicitud.getClienteId()
        );

        salida.setTrabajadorId(
                solicitud.getTrabajadorId()
        );

        salida.setFechaCreacion(
                conversacion.getFechaCreacion()
        );

        salida.setPuedeEnviarMensajes(
                puedeEnviarMensajes(
                        solicitud
                )
        );

        return salida;
    }
}
