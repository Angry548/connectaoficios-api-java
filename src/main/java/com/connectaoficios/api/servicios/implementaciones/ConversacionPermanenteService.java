package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.modelos.Conversacion;
import com.connectaoficios.api.modelos.SolicitudServicio;
import com.connectaoficios.api.repositorios.IConversacionRepository;
import com.connectaoficios.api.repositorios.IServicioRepository;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversacionPermanenteService {
    private final IConversacionRepository conversaciones;
    private final IServicioRepository servicios;

    public ConversacionPermanenteService(IConversacionRepository conversaciones, IServicioRepository servicios) {
        this.conversaciones = conversaciones;
        this.servicios = servicios;
    }

    @Transactional
    public Conversacion obtenerOCrear(SolicitudServicio solicitud) {
        Long servicioId = solicitud.getServicio().getId();
        bloquearServicio(servicioId);
        return conversaciones.buscarConBloqueo(solicitud.getClienteId(), solicitud.getTrabajadorId(), servicioId)
                .orElseGet(() -> {
                    Conversacion nueva = new Conversacion();
                    nueva.setSolicitud(solicitud);
                    nueva.setClienteId(solicitud.getClienteId());
                    nueva.setTrabajadorId(solicitud.getTrabajadorId());
                    nueva.setServicio(solicitud.getServicio());
                    return conversaciones.save(nueva);
                });
    }

    public void bloquearServicio(Long servicioId) {
        servicios.bloquearPorId(servicioId).orElseThrow(() -> new RecursoNoEncontradoException("Servicio inexistente"));
    }
}
