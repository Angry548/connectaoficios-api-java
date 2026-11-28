package com.connectaoficios.api.servicios.implementaciones;
import com.connectaoficios.api.modelos.DispositivoPush;
import com.connectaoficios.api.repositorios.IDispositivoPushRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class DispositivoPushServiceTest {
 @Test void registroIdempotenteYPropietario(){var r=mock(IDispositivoPushRepository.class);var d=new DispositivoPush();d.setId(4L);d.setUsuarioId(1);when(r.findByToken(anyString())).thenReturn(Optional.of(d));when(r.save(d)).thenReturn(d);var s=new DispositivoPushService(r);assertEquals(4L,s.registrar(1,"ExpoPushToken[abcd]","ios"));assertThrows(org.springframework.security.access.AccessDeniedException.class,()->s.registrar(2,"ExpoPushToken[abcd]","ios"));}
 @Test void noEliminaAjeno(){var r=mock(IDispositivoPushRepository.class);var d=new DispositivoPush();d.setUsuarioId(1);when(r.findById(2L)).thenReturn(Optional.of(d));assertThrows(org.springframework.security.access.AccessDeniedException.class,()->new DispositivoPushService(r).desregistrar(2,2L));verify(r,never()).save(any());}
}
