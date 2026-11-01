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
class ServicioAdminServiceTest {

 final IServicioRepository repo=mock(IServicioRepository.class);
 final ICategoriaRepository categorias=mock(ICategoriaRepository.class);
 final ServicioAdminService service=new ServicioAdminService(repo,categorias);
 Servicio servicio;
 @BeforeEach void preparar(){servicio=new Servicio(); servicio.setId(1L); servicio.setEstado(EstadoServicio.ACTIVO); var p=new PerfilTrabajador(); p.setId(2L); p.setTrabajadorId(10); servicio.setPerfilTrabajador(p); var c=new Categoria(); c.setId(3L); c.setNombre("Categoría"); servicio.setCategoria(c);}
 @Test void listado(){when(repo.buscarParaAdministracion(isNull(),isNull(),isNull(),isNull(),any())).thenReturn(new PageImpl<>(List.of(servicio))); assertNotNull(service.buscar(new ServicioAdminFiltroDTO(),0,20));}
 @Test void filtros(){var f=new ServicioAdminFiltroDTO(); f.setTexto(" local "); f.setTrabajadorId(10); f.setCategoriaId(3L); f.setEstado(EstadoServicio.ACTIVO); when(repo.buscarParaAdministracion(eq("local"),eq(10),eq(3L),eq(EstadoServicio.ACTIVO),any())).thenReturn(Page.empty()); service.buscar(f,0,20); verify(repo).buscarParaAdministracion(eq("local"),eq(10),eq(3L),eq(EstadoServicio.ACTIVO),any());}
 @Test void detalle(){when(repo.findByIdAndEliminadoFalse(1L)).thenReturn(Optional.of(servicio)); assertEquals(10,service.obtenerPorId(1L).getTrabajadorId());}
 @Test void cambiarEstado(){when(repo.findByIdAndEliminadoFalse(1L)).thenReturn(Optional.of(servicio)); when(repo.save(any())).thenAnswer(i->i.getArgument(0)); var dto=new ServicioCambiarEstado(); dto.setEstado(EstadoServicio.INACTIVO); assertEquals(EstadoServicio.INACTIVO,service.cambiarEstado(1L,dto).getEstado());}
 @Test void inexistente(){assertThrows(RecursoNoEncontradoException.class,()->service.obtenerPorId(99L));}

}
