package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.mensaje.MensajeFiltroDTO;
import com.connectaoficios.api.dtos.mensaje.MensajeGuardar;
import com.connectaoficios.api.dtos.mensaje.MensajeSalida;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.Conversacion;
import com.connectaoficios.api.modelos.Mensaje;
import com.connectaoficios.api.modelos.SolicitudServicio;
import com.connectaoficios.api.repositorios.IConversacionRepository;
import com.connectaoficios.api.repositorios.IMensajeRepository;
import com.connectaoficios.api.servicios.interfaces.IChatTiempoRealService;
import com.connectaoficios.api.servicios.interfaces.IMensajeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MensajeService implements IMensajeService {

    private static final int TAMANIO_MAXIMO_PAGINA = 100;

    private final IMensajeRepository mensajeRepository;
    private final IConversacionRepository conversacionRepository;
    private final IChatTiempoRealService chatTiempoRealService;

    public MensajeService(
            IMensajeRepository mensajeRepository,
            IConversacionRepository conversacionRepository,
            IChatTiempoRealService chatTiempoRealService
    ) {
        this.mensajeRepository = mensajeRepository;
        this.conversacionRepository = conversacionRepository;
        this.chatTiempoRealService = chatTiempoRealService;
    }

    @Override
    @Transactional
    public MensajeSalida guardar(
            MensajeGuardar mensajeGuardar,
            Integer remitenteId
    ) {

        Conversacion conversacion =
                buscarConversacion(
                        mensajeGuardar.getConversacionId()
                );

        validarParticipante(
                conversacion,
                remitenteId
        );

        validarEnvioPermitido(
                conversacion
        );

        Mensaje mensaje =
                new Mensaje();

        mensaje.setConversacion(
                conversacion
        );

        mensaje.setRemitenteId(
                remitenteId
        );

        mensaje.setContenido(
                mensajeGuardar
                        .getContenido()
                        .trim()
        );

        mensaje.setLeido(
                false
        );

        Mensaje mensajeGuardado =
                mensajeRepository.save(
                        mensaje
                );

        MensajeSalida salida =
                convertirASalida(
                        mensajeGuardado
                );

        chatTiempoRealService.publicarMensaje(
                salida
        );

        return salida;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeSalida> obtenerPorConversacion(
            Long conversacionId,
            Integer usuarioId
    ) {

        Conversacion conversacion =
                buscarConversacion(
                        conversacionId
                );

        validarParticipante(
                conversacion,
                usuarioId
        );

        List<Mensaje> mensajes =
                mensajeRepository
                        .findByConversacionIdOrderByFechaEnvioAsc(
                                conversacionId
                        );

        List<MensajeSalida> salida =
                new ArrayList<>();

        for (Mensaje mensaje : mensajes) {

            salida.add(
                    convertirASalida(
                            mensaje
                    )
            );
        }

        return salida;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSalida<MensajeSalida> buscarConFiltros(
            MensajeFiltroDTO filtro,
            Integer usuarioId,
            int pagina,
            int tamanio
    ) {

        if (filtro.getConversacionId() == null) {

            throw new ReglaNegocioException(
                    "La conversación es obligatoria para consultar los mensajes"
            );
        }

        Conversacion conversacion =
                buscarConversacion(
                        filtro.getConversacionId()
                );

        validarParticipante(
                conversacion,
                usuarioId
        );

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

        Page<MensajeSalida> resultado =
                mensajeRepository
                        .buscarConFiltros(
                                filtro.getConversacionId(),
                                filtro.getRemitenteId(),
                                filtro.getLeido(),
                                texto,
                                filtro.getFechaDesde(),
                                filtro.getFechaHasta(),
                                pageable
                        )
                        .map(this::convertirASalida);

        return PaginaSalida.desde(
                resultado
        );
    }

    @Override
    @Transactional
    public void marcarComoLeido(
            Long mensajeId,
            Integer usuarioId
    ) {

        Mensaje mensaje =
                mensajeRepository
                        .findById(mensajeId)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró el mensaje"
                                )
                        );

        validarParticipante(
                mensaje.getConversacion(),
                usuarioId
        );

        if (usuarioId.equals(
                mensaje.getRemitenteId()
        )) {

            throw new ReglaNegocioException(
                    "El remitente no puede marcar su propio mensaje como leído"
            );
        }

        if (!Boolean.TRUE.equals(
                mensaje.getLeido()
        )) {

            mensaje.setLeido(
                    true
            );

            mensajeRepository.save(
                    mensaje
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidos(
            Long conversacionId,
            Integer usuarioId
    ) {

        Conversacion conversacion =
                buscarConversacion(
                        conversacionId
                );

        validarParticipante(
                conversacion,
                usuarioId
        );

        return mensajeRepository
                .countByConversacionIdAndLeidoFalseAndRemitenteIdNot(
                        conversacionId,
                        usuarioId
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
                        Sort.Direction.ASC,
                        "fechaEnvio"
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

    private Conversacion buscarConversacion(
            Long conversacionId
    ) {

        return conversacionRepository
                .findById(conversacionId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró la conversación"
                        )
                );
    }

    private void validarParticipante(
            Conversacion conversacion,
            Integer usuarioId
    ) {

        if (usuarioId == null) {

            throw new ReglaNegocioException(
                    "No se pudo identificar al usuario autenticado"
            );
        }

        SolicitudServicio solicitud =
                conversacion.getSolicitud();

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

    private void validarEnvioPermitido(
            Conversacion conversacion
    ) {

        EstadoSolicitud estado =
                conversacion
                        .getSolicitud()
                        .getEstado();

        if (estado != EstadoSolicitud.ACEPTADA
                && estado != EstadoSolicitud.EN_PROCESO) {

            throw new ReglaNegocioException(
                    "No se pueden enviar mensajes porque "
                            + "la conversación está en modo lectura"
            );
        }
    }

    private MensajeSalida convertirASalida(
            Mensaje mensaje
    ) {

        MensajeSalida salida =
                new MensajeSalida();

        salida.setId(
                mensaje.getId()
        );

        salida.setConversacionId(
                mensaje
                        .getConversacion()
                        .getId()
        );

        salida.setRemitenteId(
                mensaje.getRemitenteId()
        );

        salida.setContenido(
                mensaje.getContenido()
        );

        salida.setFechaEnvio(
                mensaje.getFechaEnvio()
        );

        salida.setLeido(
                mensaje.getLeido()
        );

        return salida;
    }
}