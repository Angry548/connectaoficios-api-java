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
class NotificacionServiceTest {

 final INotificacionRepository repo=mock(INotificacionRepository.class);
 final NotificacionService service=new NotificacionService(repo);
 Notificacion notificacion;
 @BeforeEach void preparar(){notificacion=new Notificacion(); notificacion.setId(1L); notificacion.setUsuarioDestinoId(10); notificacion.setTipo(TipoNotificacion.values()[0]);}
 @Test void crear(){var dto=new NotificacionGuardar(); dto.setUsuarioDestinoId(10); dto.setTipo(TipoNotificacion.values()[0].name()); dto.setTitulo(" Título "); dto.setMensaje(" Mensaje "); when(repo.save(any())).thenAnswer(i->i.getArgument(0)); assertFalse(service.guardar(dto).getLeida()); verify(repo).save(argThat(n->n.getTitulo().equals("Título")));}
 @Test void consultarUsuario(){when(repo.findByUsuarioDestinoId(eq(10),any(Pageable.class))).thenReturn(new PageImpl<>(List.of(notificacion))); assertEquals(1,service.listarPorUsuario(10,null,0,20).getTotalElements());}
 @Test void consultarPropia(){when(repo.findById(1L)).thenReturn(Optional.of(notificacion)); assertEquals(1L,service.obtenerPorId(1L,10).getIdNotificacion());}
 @Test void marcarLeidaIdempotente(){when(repo.findById(1L)).thenReturn(Optional.of(notificacion)); when(repo.save(any())).thenAnswer(i->i.getArgument(0)); service.marcarComoLeida(1L,10); service.marcarComoLeida(1L,10); assertTrue(notificacion.getLeida()); verify(repo,times(1)).save(notificacion);}
 @Test void inexistente(){assertThrows(RecursoNoEncontradoException.class,()->service.obtenerPorId(99L,10));}
 @Test void ajena(){when(repo.findById(1L)).thenReturn(Optional.of(notificacion)); assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service.marcarComoLeida(1L,99)); verify(repo,never()).save(any());}

}
