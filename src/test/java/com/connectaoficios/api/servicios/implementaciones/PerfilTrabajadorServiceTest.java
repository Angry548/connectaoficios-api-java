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
class PerfilTrabajadorServiceTest {

 final IPerfilTrabajadorRepository repo=mock(IPerfilTrabajadorRepository.class);
 final IZonaCoberturaRepository zonas=mock(IZonaCoberturaRepository.class);
 final PerfilTrabajadorService service=new PerfilTrabajadorService(repo,zonas);
 PerfilTrabajador perfil;
 @BeforeEach void preparar(){perfil=new PerfilTrabajador(); perfil.setId(1L); perfil.setTrabajadorId(10);}
 @Test void crear(){var dto=new PerfilTrabajadorGuardar(); dto.setZonaPrincipalId(2L); dto.setOficioPrincipal("Electricista");
 when(zonas.findById(2L)).thenReturn(Optional.of(new ZonaCobertura())); when(repo.save(any())).thenAnswer(i->i.getArgument(0));
 var r=service.guardar(dto,10); assertEquals(10,r.getTrabajadorId()); verify(repo).save(argThat(p->p.getTrabajadorId()==10));}
 @Test void duplicado(){when(repo.existsByTrabajadorId(10)).thenReturn(true); assertThrows(ReglaNegocioException.class,()->service.guardar(new PerfilTrabajadorGuardar(),10)); verify(repo,never()).save(any());}
 @Test void consultar(){when(repo.findById(1L)).thenReturn(Optional.of(perfil)); assertEquals(10,service.obtenerPorId(1L).getTrabajadorId());}
 @Test void consultarPorUsuario(){when(repo.findByTrabajadorId(10)).thenReturn(Optional.of(perfil)); assertEquals(1L,service.obtenerPorTrabajadorId(10).getId());}
 @Test void modificarPropio(){when(repo.findByTrabajadorId(10)).thenReturn(Optional.of(perfil)); when(repo.save(any())).thenAnswer(i->i.getArgument(0)); var dto=new PerfilTrabajadorModificar(); dto.setOficioPrincipal("Plomero"); assertEquals("Plomero",service.modificar(10,dto).getOficioPrincipal());}
 @Test void inexistente(){assertThrows(RecursoNoEncontradoException.class,()->service.obtenerPorId(99L));}
 @Test void modificarNoBuscaOtroUsuario(){assertThrows(RecursoNoEncontradoException.class,()->service.modificar(99,new PerfilTrabajadorModificar())); verify(repo).findByTrabajadorId(99); verify(repo,never()).save(any());}

}
