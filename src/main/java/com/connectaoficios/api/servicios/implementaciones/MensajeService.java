package com.connectaoficios.api.servicios.implementaciones;

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
import com.connectaoficios.api.servicios.interfaces.IMensajeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MensajeService implements IMensajeService {

    private final IMensajeRepository mensajeRepository;
    private final IConversacionRepository conversacionRepository;

    public MensajeService(
            IMensajeRepository mensajeRepository,
            IConversacionRepository conversacionRepository
    ) {
        this.mensajeRepository = mensajeRepository;
        this.conversacionRepository = conversacionRepository;
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

        validarEnvioPermitido(conversacion);

        Mensaje mensaje = new Mensaje();

        mensaje.setConversacion(conversacion);
        mensaje.setRemitenteId(remitenteId);
        mensaje.setContenido(
                mensajeGuardar.getContenido().trim()
        );
        mensaje.setLeido(false);

        Mensaje mensajeGuardado =
                mensajeRepository.save(mensaje);

        return convertirASalida(mensajeGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeSalida> obtenerPorConversacion(
            Long conversacionId,
            Integer usuarioId
    ) {

        Conversacion conversacion =
                buscarConversacion(conversacionId);

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
                    convertirASalida(mensaje)
            );
        }

        return salida;
    }

    @Override
    @Transactional
    public void marcarComoLeido(
            Long mensajeId,
            Integer usuarioId
    ) {

        Mensaje mensaje = mensajeRepository
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

        if (usuarioId.equals(mensaje.getRemitenteId())) {
            throw new ReglaNegocioException(
                    "El remitente no puede marcar su propio mensaje como leído"
            );
        }

        if (!Boolean.TRUE.equals(mensaje.getLeido())) {
            mensaje.setLeido(true);
            mensajeRepository.save(mensaje);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidos(
            Long conversacionId,
            Integer usuarioId
    ) {

        Conversacion conversacion =
                buscarConversacion(conversacionId);

        validarParticipante(
                conversacion,
                usuarioId
        );

        return mensajeRepository
                .countByConversacionIdAndLeidoFalse(
                        conversacionId
                );
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
                usuarioId.equals(solicitud.getClienteId());

        boolean esTrabajador =
                usuarioId.equals(solicitud.getTrabajadorId());

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

        salida.setId(mensaje.getId());
        salida.setConversacionId(
                mensaje.getConversacion().getId()
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