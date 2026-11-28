package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.conversacion.ConversacionFiltroDTO;
import com.connectaoficios.api.dtos.conversacion.ConversacionGuardar;
import com.connectaoficios.api.dtos.conversacion.ConversacionSalida;
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
    @org.springframework.beans.factory.annotation.Autowired
    private com.connectaoficios.api.repositorios.IMensajeRepository mensajes;
    @org.springframework.beans.factory.annotation.Autowired
    private com.connectaoficios.api.repositorios.IAdjuntoMensajeRepository adjuntos;

    private final IConversacionRepository conversacionRepository;
    private final ISolicitudServicioRepository solicitudServicioRepository;
    private final IPerfilClienteRepository perfilClienteRepository;
    private final IPerfilTrabajadorRepository perfilTrabajadorRepository;
    private final ConversacionPermanenteService conversacionPermanenteService;
    private final ReintentoConversacion reintentoConversacion;

    public ConversacionService(
            IConversacionRepository conversacionRepository,
            ISolicitudServicioRepository solicitudServicioRepository,
            IPerfilClienteRepository perfilClienteRepository,
            IPerfilTrabajadorRepository perfilTrabajadorRepository,
            ConversacionPermanenteService conversacionPermanenteService,
            ReintentoConversacion reintentoConversacion
    ) {
        this.conversacionRepository = conversacionRepository;
        this.solicitudServicioRepository = solicitudServicioRepository;
        this.perfilClienteRepository = perfilClienteRepository;
        this.perfilTrabajadorRepository = perfilTrabajadorRepository;
        this.conversacionPermanenteService = conversacionPermanenteService;
        this.reintentoConversacion = reintentoConversacion;
    }

    @Override
    public ConversacionSalida guardar(
            ConversacionGuardar conversacionGuardar,
            Integer usuarioId
    ) {
        return reintentoConversacion.ejecutar(() -> guardarEnTransaccion(conversacionGuardar, usuarioId));
    }

    private ConversacionSalida guardarEnTransaccion(ConversacionGuardar conversacionGuardar, Integer usuarioId) {

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

        Conversacion conversacionGuardada = conversacionPermanenteService.obtenerOCrear(solicitud);
        solicitud.setConversacion(conversacionGuardada);

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
        Map<Long, com.connectaoficios.api.modelos.Mensaje> ultimos = new HashMap<>();
        Map<Long, Long> noLeidos = new HashMap<>();
        if (!conversaciones.isEmpty() && mensajes != null) {
            var ids = conversaciones.stream().map(Conversacion::getId).toList();
            mensajes.ultimosPorConversacion(ids).forEach(m -> ultimos.put(m.getConversacion().getId(), m));
            mensajes.contarNoLeidosPorConversacion(ids, usuarioId).forEach(row -> noLeidos.put((Long) row[0], (Long) row[1]));
        }
        Map<Long, java.util.List<com.connectaoficios.api.dtos.mensaje.AdjuntoSalida>> adjuntosPorMensaje = new HashMap<>();
        if (!ultimos.isEmpty() && adjuntos != null) {
            adjuntos.findByMensajeIdIn(ultimos.values().stream().map(com.connectaoficios.api.modelos.Mensaje::getId).toList())
                .forEach(a -> adjuntosPorMensaje.computeIfAbsent(a.getMensaje().getId(), k -> new ArrayList<>()).add(AdjuntoChatService.dto(a)));
        }
        Page<ConversacionSalida> resultado = conversaciones.map(c -> {
            ConversacionSalida dto = convertirASalida(c, fotosClientes, fotosTrabajadores);
            var m = ultimos.get(c.getId());
            dto.setUltimaActividad(m == null ? c.getFechaCreacion() : m.getFechaEnvio());
            dto.setNoLeidos(noLeidos.getOrDefault(c.getId(), 0L));
            if (m != null) {
                var ultimo = new com.connectaoficios.api.dtos.mensaje.MensajeSalida();
                ultimo.setId(m.getId()); ultimo.setConversacionId(c.getId()); ultimo.setRemitenteId(m.getRemitenteId());
                ultimo.setContenido(m.getContenido()); ultimo.setFechaEnvio(m.getFechaEnvio()); ultimo.setLeido(m.getLeido());
                ultimo.setTipo(m.getTipo() == null ? com.connectaoficios.api.enums.TipoMensaje.TEXT : m.getTipo());
                ultimo.setAdjuntos(adjuntosPorMensaje.getOrDefault(m.getId(), List.of()));
                dto.setUltimoMensaje(ultimo);
            }
            return dto;
        });

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
                Sort.unsorted()
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

        salida.setPuedeEnviarMensajes(true);
        if (solicitud.getServicio() != null) {
            salida.setServicioId(solicitud.getServicio().getId());
            salida.setServicioTitulo(solicitud.getServicio().getTitulo());
        }

        return salida;
    }
}
