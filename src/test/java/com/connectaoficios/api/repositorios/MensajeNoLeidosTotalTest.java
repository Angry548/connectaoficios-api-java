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
    @Autowired IConversacionRepository conversacionRepository;

    @Test
    void marcarRecibidoComoLeidoActualizaTotalGlobal() {
        // Angel Mate: comprobar la transicion real con H2, sin modificar tests de Oscar.
        var conversacion = conversacion(10, 20);
        mensaje(conversacion, 20, false);
        mensaje(conversacion, 20, false);
        em.flush();
        var recibido = repository.findByConversacionIdOrderByFechaEnvioAsc(conversacion.getId()).getFirst();
        var conversaciones = mock(IConversacionRepository.class);
        var chat = mock(IChatTiempoRealService.class);
        var service = new MensajeService(repository, conversaciones, chat);
        assertEquals(2, service.contarTotalNoLeidos(10));
        service.marcarComoLeido(recibido.getId(), 10);
        em.flush();
        em.clear();
        assertEquals(1, service.contarTotalNoLeidos(10));
        org.junit.jupiter.api.Assertions.assertTrue(repository.findById(recibido.getId()).orElseThrow().getLeido());
        verifyNoInteractions(conversaciones, chat);
    }

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

    @Test
    void baseDeDatosRechazaDosChatsConLaMismaCombinacion() {
        var primera = conversacion(10, 20);
        var original = primera.getSolicitud();
        var segundaSolicitud = new SolicitudServicio();
        segundaSolicitud.setServicio(original.getServicio());
        segundaSolicitud.setClienteId(10);
        segundaSolicitud.setTrabajadorId(20);
        segundaSolicitud.setFechaPropuesta(LocalDate.now());
        segundaSolicitud.setHoraAproximada(LocalTime.NOON);
        segundaSolicitud.setDireccion("Direccion");
        segundaSolicitud.setDescripcionTrabajo("Trabajo");
        em.persist(segundaSolicitud);
        var duplicada = new Conversacion();
        duplicada.setSolicitud(segundaSolicitud);
        org.junit.jupiter.api.Assertions.assertThrows(jakarta.persistence.PersistenceException.class, () -> {
            em.persist(duplicada);
            em.flush();
        });
    }

    @Test
    void ordenaActividadGlobalAntesDePaginar() {
        var primera = conversacion(10, 20);
        var segunda = conversacion(10, 30);
        var m = new Mensaje(); m.setConversacion(primera); m.setRemitenteId(20); m.setContenido("reciente");
        m.setFechaEnvio(java.time.LocalDateTime.now().plusMinutes(5)); em.persist(m); em.flush(); em.clear();
        var pagina = conversacionRepository.buscarConversacionesUsuario(10, null, null, null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 1));
        assertEquals(primera.getId(), pagina.getContent().getFirst().getId());
        var siguiente = conversacionRepository.buscarConversacionesUsuario(10, null, null, null, null, null,
                org.springframework.data.domain.PageRequest.of(1, 1));
        assertEquals(segunda.getId(), siguiente.getContent().getFirst().getId());
    }

    @Test
    void ordenDinamicoSubeChatEnAmbosSentidosYNoDependeDeLectura() {
        var a = conversacion(10,20); var b = conversacion(10,30); var c = conversacion(10,40);
        var base = java.time.LocalDateTime.now().plusDays(1);
        var ma = mensajeEn(a,20,base.plusHours(10),com.connectaoficios.api.enums.TipoMensaje.TEXT);
        mensajeEn(b,30,base.plusHours(11),com.connectaoficios.api.enums.TipoMensaje.IMAGE);
        mensajeEn(c,40,base.plusHours(12),com.connectaoficios.api.enums.TipoMensaje.DOCUMENT);
        assertEquals(java.util.List.of(c.getId(),b.getId(),a.getId()), idsPagina(10,0,10));
        mensajeEn(a,10,base.plusHours(13),com.connectaoficios.api.enums.TipoMensaje.STICKER);
        assertEquals(java.util.List.of(a.getId(),c.getId(),b.getId()), idsPagina(10,0,10));
        mensajeEn(b,30,base.plusHours(14),com.connectaoficios.api.enums.TipoMensaje.TEXT);
        assertEquals(java.util.List.of(b.getId(),a.getId(),c.getId()), idsPagina(10,0,10));
        ma.setLeido(true); em.flush();
        assertEquals(java.util.List.of(b.getId(),a.getId(),c.getId()), idsPagina(10,0,10));
        assertEquals(b.getId(),idsPagina(10,0,1).getFirst());
        assertEquals(a.getId(),idsPagina(10,1,1).getFirst());
    }

    @Test
    void actividadOrdenaTambienParaTrabajadorYFallbackDeCreacion() {
        var a = conversacion(10,99); var b = conversacion(11,99); var vacia = conversacion(12,99);
        var base = java.time.LocalDateTime.now().plusDays(1);
        mensajeEn(a,10,base.plusHours(1),com.connectaoficios.api.enums.TipoMensaje.IMAGE);
        mensajeEn(b,99,base.plusHours(2),com.connectaoficios.api.enums.TipoMensaje.STICKER);
        assertEquals(java.util.List.of(b.getId(),a.getId(),vacia.getId()), idsPagina(99,0,10));
        mensajeEn(a,99,base.plusHours(3),com.connectaoficios.api.enums.TipoMensaje.DOCUMENT);
        assertEquals(java.util.List.of(a.getId(),b.getId(),vacia.getId()), idsPagina(99,0,10));
    }

    @Test
    void ordenConEmpateEsEstablePorIdDescendente() {
        var a = conversacion(10,20); var b = conversacion(10,30);
        var instante = java.time.LocalDateTime.now().plusDays(1);
        mensajeEn(a,20,instante,com.connectaoficios.api.enums.TipoMensaje.TEXT);
        mensajeEn(b,30,instante,com.connectaoficios.api.enums.TipoMensaje.TEXT);
        assertEquals(java.util.List.of(b.getId(),a.getId()), idsPagina(10,0,10));
    }

    private Mensaje mensajeEn(Conversacion c, int remitente, java.time.LocalDateTime fecha, com.connectaoficios.api.enums.TipoMensaje tipo) {
        var m=new Mensaje(); m.setConversacion(c); m.setRemitenteId(remitente); m.setContenido("prueba");
        m.setFechaEnvio(fecha); m.setTipo(tipo); m.setLeido(false); em.persist(m); return m;
    }
    private java.util.List<Long> idsPagina(int usuario, int pagina, int tamanio) {
        em.flush();
        return conversacionRepository.buscarConversacionesUsuario(usuario,null,null,null,null,null,
                org.springframework.data.domain.PageRequest.of(pagina,tamanio)).stream().map(Conversacion::getId).toList();
    }

    @Test void noLeidosCuentanTodosLosTiposYExcluyenPropios() {
        var c=conversacion(10,20); var ahora=java.time.LocalDateTime.now();
        for(var tipo:com.connectaoficios.api.enums.TipoMensaje.values()) mensajeEn(c,20,ahora,tipo);
        mensajeEn(c,10,ahora,com.connectaoficios.api.enums.TipoMensaje.TEXT); em.flush();
        assertEquals(4,repository.countByConversacionIdAndLeidoFalseAndRemitenteIdNot(c.getId(),10));
        assertEquals(1,repository.countByConversacionIdAndLeidoFalseAndRemitenteIdNot(c.getId(),20));
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
