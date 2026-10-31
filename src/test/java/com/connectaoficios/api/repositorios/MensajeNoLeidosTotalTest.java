package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.*;
import com.connectaoficios.api.servicios.implementaciones.MensajeService;
import com.connectaoficios.api.servicios.interfaces.IChatTiempoRealService;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class MensajeNoLeidosTotalTest {
    @Autowired EntityManager em;
    @Autowired IMensajeRepository repository;

    @Test void usuarioSinMensajes() { comprobar(10, 0); }

    @Test void unMensajeNoLeido() {
        mensaje(conversacion(10, 20), 20, false);
        comprobar(10, 1);
    }

    @Test void variosNoLeidosDeUnaConversacion() {
        var c = conversacion(10, 20);
        mensaje(c, 20, false);
        mensaje(c, 20, false);
        mensaje(c, 20, false);
        comprobar(10, 3);
    }

    @Test void noLeidosDeVariasConversacionesYAmbosRoles() {
        mensaje(conversacion(10, 20), 20, false);
        mensaje(conversacion(30, 10), 30, false);
        comprobar(10, 2);
    }

    @Test void mensajesLeidosNoCuentan() {
        var c = conversacion(10, 20);
        mensaje(c, 20, true);
        mensaje(c, 20, false);
        comprobar(10, 1);
    }

    @Test void mensajesPropiosNoCuentanComoRecibidos() {
        mensaje(conversacion(10, 20), 10, false);
        mensaje(conversacion(30, 10), 10, false);
        comprobar(10, 0);
    }

    @Test void mensajesDeConversacionesAjenasNoCuentan() {
        mensaje(conversacion(30, 20), 20, false);
        comprobar(10, 0);
    }

    private void comprobar(int usuarioId, long esperado) {
        em.flush();
        em.clear();
        var statistics = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        var conversaciones = mock(IConversacionRepository.class);
        var tiempoReal = mock(IChatTiempoRealService.class);
        var service = new MensajeService(repository, conversaciones, tiempoReal);
        assertEquals(esperado, service.contarTotalNoLeidos(usuarioId));
        assertEquals(1, statistics.getPrepareStatementCount());
        assertEquals(0, statistics.getEntityLoadCount());
        verifyNoInteractions(conversaciones, tiempoReal);
    }

    private Conversacion conversacion(int cliente, int trabajador) {
        var perfil = new PerfilTrabajador();
        perfil.setTrabajadorId(trabajador);
        // Reutilizar el perfil cuando hay varias conversaciones con el mismo trabajador.
        var perfiles = em.createQuery("from PerfilTrabajador where trabajadorId = :id", PerfilTrabajador.class)
                .setParameter("id", trabajador).getResultList();
        if (perfiles.isEmpty()) em.persist(perfil);
        else perfil = perfiles.getFirst();
        var categoria = new Categoria();
        categoria.setNombre("Categoria " + cliente + " " + trabajador);
        em.persist(categoria);
        var servicio = new Servicio();
        servicio.setPerfilTrabajador(perfil);
        servicio.setCategoria(categoria);
        servicio.setTitulo("Servicio");
        servicio.setDescripcion("Descripcion");
        servicio.setTarifaMinima(BigDecimal.ONE);
        em.persist(servicio);
        var solicitud = new SolicitudServicio();
        solicitud.setServicio(servicio);
        solicitud.setClienteId(cliente);
        solicitud.setTrabajadorId(trabajador);
        solicitud.setFechaPropuesta(LocalDate.now());
        solicitud.setHoraAproximada(LocalTime.NOON);
        solicitud.setDireccion("Direccion");
        solicitud.setDescripcionTrabajo("Trabajo");
        em.persist(solicitud);
        var conversacion = new Conversacion();
        conversacion.setSolicitud(solicitud);
        em.persist(conversacion);
        return conversacion;
    }

    private void mensaje(Conversacion conversacion, int remitente, boolean leido) {
        var mensaje = new Mensaje();
        mensaje.setConversacion(conversacion);
        mensaje.setRemitenteId(remitente);
        mensaje.setContenido("Mensaje");
        mensaje.setLeido(leido);
        em.persist(mensaje);
    }
}
