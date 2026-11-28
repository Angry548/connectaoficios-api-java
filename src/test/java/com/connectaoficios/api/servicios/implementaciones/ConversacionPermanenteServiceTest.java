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
    @Test void recontratacionReutilizaChatDeMismaClave() {
        var r=mock(IConversacionRepository.class); var servicios=mock(IServicioRepository.class);
        var servicio=new Servicio(); servicio.setId(7L); when(servicios.bloquearPorId(7L)).thenReturn(Optional.of(servicio));
        var chat=new Conversacion();chat.setId(50L);when(r.buscarConBloqueo(10,20,7L)).thenReturn(Optional.of(chat));
        var s1=new SolicitudServicio();s1.setId(1L);s1.setClienteId(10);s1.setTrabajadorId(20);s1.setServicio(servicio);
        var s2=new SolicitudServicio();s2.setId(2L);s2.setClienteId(10);s2.setTrabajadorId(20);s2.setServicio(servicio);
        var servicioChat=new ConversacionPermanenteService(r,servicios);
        org.junit.jupiter.api.Assertions.assertEquals(50L,servicioChat.obtenerOCrear(s1).getId());
        org.junit.jupiter.api.Assertions.assertEquals(50L,servicioChat.obtenerOCrear(s2).getId());verify(r,never()).save(any());
    }
    @Test void otroServicioUsaClaveDistinta() {
        var r=mock(IConversacionRepository.class);var servicios=mock(IServicioRepository.class);
        var primero=new Servicio();primero.setId(7L);var segundo=new Servicio();segundo.setId(8L);
        when(servicios.bloquearPorId(7L)).thenReturn(Optional.of(primero));when(servicios.bloquearPorId(8L)).thenReturn(Optional.of(segundo));
        var a=new Conversacion();a.setId(1L);var b=new Conversacion();b.setId(2L);
        when(r.buscarConBloqueo(10,20,7L)).thenReturn(Optional.of(a));when(r.buscarConBloqueo(10,20,8L)).thenReturn(Optional.of(b));
        var s1=new SolicitudServicio();s1.setClienteId(10);s1.setTrabajadorId(20);s1.setServicio(primero);
        var s2=new SolicitudServicio();s2.setClienteId(10);s2.setTrabajadorId(20);s2.setServicio(segundo);
        var service=new ConversacionPermanenteService(r,servicios);assertNotEquals(service.obtenerOCrear(s1).getId(),service.obtenerOCrear(s2).getId());
    }
}
