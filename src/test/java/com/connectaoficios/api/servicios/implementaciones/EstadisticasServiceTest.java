package com.connectaoficios.api.servicios.implementaciones;
import com.connectaoficios.api.modelos.*;
import com.connectaoficios.api.repositorios.*;
import com.connectaoficios.api.dtos.perfil.*;
import com.connectaoficios.api.dtos.notificacion.*;
import com.connectaoficios.api.dtos.servicio.*;
import com.connectaoficios.api.enums.*;
import com.connectaoficios.api.excepciones.*;
import org.junit.jupiter.api.*;
import java.util.*;
import org.springframework.data.domain.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class EstadisticasServiceTest {

 final jakarta.persistence.EntityManager em=mock(jakarta.persistence.EntityManager.class);
 final EstadisticasService service=new EstadisticasService();
 @BeforeEach void preparar(){org.springframework.test.util.ReflectionTestUtils.setField(service,"entityManager",em);}
 @SuppressWarnings("unchecked") jakarta.persistence.TypedQuery<Long> query(long valor){var q=mock(jakarta.persistence.TypedQuery.class); when(q.getSingleResult()).thenReturn(valor); when(em.createQuery(anyString(),eq(Long.class))).thenReturn(q); return q;}
 @Test void generales(){query(3); var r=service.obtenerEstadisticas(null,null); assertEquals(3,r.getTotalServicios()); assertEquals(3,r.getTotalSolicitudes()); assertEquals(3,r.getTotalReportes());}
 @Test void serviciosNoIncluyenEliminados(){query(1); service.obtenerEstadisticas(null,null); verify(em).createQuery(contains("s.eliminado = false"),eq(Long.class));}
 @Test void solicitudesPorEstado(){var q=query(2); var r=service.obtenerEstadisticas(null,null); assertEquals(2,r.getSolicitudesPendientes()); assertEquals(2,r.getTotalSolicitudesCompletadas()); verify(q).setParameter("estado",EstadoSolicitud.PENDIENTE); verify(q).setParameter("estado",EstadoSolicitud.COMPLETADA);}
 @Test void periodoSinDatos(){var q=query(0); var desde=java.time.LocalDateTime.of(2026,1,1,0,0); var hasta=desde.plusDays(1); var r=service.obtenerEstadisticas(desde,hasta); assertEquals(0,r.getTotalServicios()); assertEquals(0,r.getTotalSolicitudes()); verify(q,atLeastOnce()).setParameter("fechaDesde",desde); verify(q,atLeastOnce()).setParameter("fechaHasta",hasta);}
 @Test void fechasInvalidas(){var ahora=java.time.LocalDateTime.now(); assertThrows(ReglaNegocioException.class,()->service.obtenerEstadisticas(ahora,ahora.minusDays(1))); verifyNoInteractions(em);}

}
