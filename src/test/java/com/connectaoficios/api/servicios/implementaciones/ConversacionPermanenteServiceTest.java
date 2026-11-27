package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.modelos.Conversacion;
import com.connectaoficios.api.modelos.Servicio;
import com.connectaoficios.api.modelos.SolicitudServicio;
import com.connectaoficios.api.repositorios.IConversacionRepository;
import com.connectaoficios.api.repositorios.IServicioRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;

class ConversacionPermanenteServiceTest {
    @Test
    void trabajadoresDistintosFormanClavesDistintas() {
        var conversaciones = mock(IConversacionRepository.class);
        var servicios = mock(IServicioRepository.class);
        var service = new ConversacionPermanenteService(conversaciones, servicios);
        var servicio = new Servicio();
        servicio.setId(7L);
        when(servicios.bloquearPorId(7L)).thenReturn(Optional.of(servicio));
        var primero = new Conversacion();
        primero.setId(50L);
        var segundo = new Conversacion();
        segundo.setId(51L);
        when(conversaciones.buscarConBloqueo(10, 20, 7L)).thenReturn(Optional.of(primero));
        when(conversaciones.buscarConBloqueo(10, 30, 7L)).thenReturn(Optional.of(segundo));
        var solicitud1 = new SolicitudServicio();
        solicitud1.setClienteId(10);
        solicitud1.setTrabajadorId(20);
        solicitud1.setServicio(servicio);
        var solicitud2 = new SolicitudServicio();
        solicitud2.setClienteId(10);
        solicitud2.setTrabajadorId(30);
        solicitud2.setServicio(servicio);
        assertNotEquals(service.obtenerOCrear(solicitud1).getId(), service.obtenerOCrear(solicitud2).getId());
        verify(conversaciones, never()).save(any());
    }
}
