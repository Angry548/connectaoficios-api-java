package com.connectaoficios.api.servicios.implementaciones;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversacionService implements IConversacionService {

    private final IConversacionRepository conversacionRepository;
    private final ISolicitudServicioRepository solicitudServicioRepository;

    public ConversacionService(
            IConversacionRepository conversacionRepository,
            ISolicitudServicioRepository solicitudServicioRepository
    ) {
        this.conversacionRepository = conversacionRepository;
        this.solicitudServicioRepository = solicitudServicioRepository;
    }

    @Override
    @Transactional
    public ConversacionSalida guardar(
            ConversacionGuardar conversacionGuardar,
            Integer usuarioId
    ) {

        SolicitudServicio solicitud = solicitudServicioRepository
                .findById(conversacionGuardar.getSolicitudId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró la solicitud de servicio"
                        )
                );

        validarParticipante(solicitud, usuarioId);

        validarEstadoParaChat(solicitud);

        if (conversacionRepository.existsBySolicitudId(solicitud.getId())) {
            throw new ReglaNegocioException(
                    "La solicitud ya posee una conversación"
            );
        }

        Conversacion conversacion = new Conversacion();
        conversacion.setSolicitud(solicitud);

        Conversacion conversacionGuardada =
                conversacionRepository.save(conversacion);

        return convertirASalida(conversacionGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversacionSalida obtenerPorId(
            Long id,
            Integer usuarioId
    ) {

        Conversacion conversacion = conversacionRepository
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

        return convertirASalida(conversacion);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversacionSalida obtenerPorSolicitud(
            Long solicitudId,
            Integer usuarioId
    ) {

        Conversacion conversacion = conversacionRepository
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

        return convertirASalida(conversacion);
    }

    private void validarParticipante(
            SolicitudServicio solicitud,
            Integer usuarioId
    ) {

        if (usuarioId == null) {
            throw new ReglaNegocioException(
                    "No se pudo identificar al usuario autenticado"
            );
        }

        boolean esCliente =
                usuarioId.equals(solicitud.getClienteId());

        boolean esTrabajador =
                usuarioId.equals(solicitud.getTrabajadorId());

        if (!esCliente && !esTrabajador) {
            throw new ReglaNegocioException(
                    "El usuario no pertenece a esta conversación"
            );
        }
    }

    private void validarEstadoParaChat(
            SolicitudServicio solicitud
    ) {

        EstadoSolicitud estado = solicitud.getEstado();

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

        EstadoSolicitud estado = solicitud.getEstado();

        return estado == EstadoSolicitud.ACEPTADA
                || estado == EstadoSolicitud.EN_PROCESO;
    }

    private ConversacionSalida convertirASalida(
            Conversacion conversacion
    ) {

        SolicitudServicio solicitud =
                conversacion.getSolicitud();

        ConversacionSalida salida =
                new ConversacionSalida();

        salida.setId(conversacion.getId());
        salida.setSolicitudId(solicitud.getId());
        salida.setClienteId(solicitud.getClienteId());
        salida.setTrabajadorId(solicitud.getTrabajadorId());
        salida.setFechaCreacion(
                conversacion.getFechaCreacion()
        );
        salida.setPuedeEnviarMensajes(
                puedeEnviarMensajes(solicitud)
        );

        return salida;
    }
}