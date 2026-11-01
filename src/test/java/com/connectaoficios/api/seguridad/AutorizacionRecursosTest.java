package com.connectaoficios.api.seguridad;
import com.connectaoficios.api.modelos.*;
import com.connectaoficios.api.repositorios.*;
import com.connectaoficios.api.dtos.servicio.*;
import com.connectaoficios.api.dtos.solicitud.*;
import com.connectaoficios.api.controladores.*;
import com.connectaoficios.api.servicios.interfaces.*;
import com.connectaoficios.api.seguridad.configuracion.JwtAuthenticationConverter;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class AutorizacionRecursosTest {
 @Configuration @EnableMethodSecurity static class Config {
 @Bean IPerfilTrabajadorRepository perfiles(){return mock(IPerfilTrabajadorRepository.class);}
 @Bean IServicioRepository servicios(){return mock(IServicioRepository.class);}
 @Bean ISolicitudServicioRepository solicitudes(){return mock(ISolicitudServicioRepository.class);}
 @Bean IConversacionRepository conversaciones(){return mock(IConversacionRepository.class);}
 @Bean(name="accesoJava") AccesoJava acceso(IPerfilTrabajadorRepository p, IServicioRepository s, ISolicitudServicioRepository so,IConversacionRepository c){return new AccesoJava(p,s,so,c);}
 @Bean IPerfilClienteService clienteService(){return mock(IPerfilClienteService.class);}
 @Bean IPerfilTrabajadorService perfilService(){return mock(IPerfilTrabajadorService.class);}
 @Bean PerfilClienteController clienteController(IPerfilClienteService s){return new PerfilClienteController(s);}
 @Bean PerfilTrabajadorController perfilController(IPerfilTrabajadorService s){return new PerfilTrabajadorController(s);}
 @Bean IServicioService service(){return mock(IServicioService.class);}
 @Bean ISolicitudServicioService solicitudService(){return mock(ISolicitudServicioService.class);}
 @Bean IServicioAdminService adminService(){return mock(IServicioAdminService.class);}
 @Bean ServicioController controller(IServicioService s){return new ServicioController(s);}
 @Bean SolicitudServicioController solicitudesController(ISolicitudServicioService s,AccesoJava a){return new SolicitudServicioController(s,a);}
 @Bean ServicioAdminController adminController(IServicioAdminService s){return new ServicioAdminController(s);}
 }
 AnnotationConfigApplicationContext ctx; PerfilTrabajador perfil; Servicio servicio; SolicitudServicio solicitud;
 @BeforeEach void preparar(){ctx=new AnnotationConfigApplicationContext(Config.class); perfil=new PerfilTrabajador(); perfil.setId(1L); perfil.setTrabajadorId(10); servicio=new Servicio(); servicio.setId(2L); servicio.setPerfilTrabajador(perfil); solicitud=new SolicitudServicio(); solicitud.setId(3L); solicitud.setClienteId(20); solicitud.setTrabajadorId(10); solicitud.setServicio(servicio);}
 @AfterEach void cerrar(){SecurityContextHolder.clearContext();ctx.close();}
 org.springframework.security.core.Authentication auth(int id,String rol){var jwt=Jwt.withTokenValue("test").header("alg","HS256").subject(""+id).claim("role",rol).build(); var a=new JwtAuthenticationConverter().convert(jwt); SecurityContextHolder.getContext().setAuthentication(a); return a;}
 ServicioController controller(){return ctx.getBean(ServicioController.class);}
 SolicitudServicioController solicitudes(){return ctx.getBean(SolicitudServicioController.class);}
 void recurso(){when(ctx.getBean(IServicioRepository.class).findByIdAndEliminadoFalse(2L)).thenReturn(Optional.of(servicio));}
 void solicitud(){when(ctx.getBean(ISolicitudServicioRepository.class).findById(3L)).thenReturn(Optional.of(solicitud));}
 @Test void crearServicioAjenoDenegado(){auth(99,"Trabajador"); when(ctx.getBean(IPerfilTrabajadorRepository.class).findById(1L)).thenReturn(Optional.of(perfil)); var dto=new ServicioGuardar(); dto.setPerfilTrabajadorId(1L); assertThrows(AccessDeniedException.class,()->controller().publicar(dto)); verifyNoInteractions(ctx.getBean(IServicioService.class));}
 @Test void crearServicioPropio(){auth(10,"Trabajador"); when(ctx.getBean(IPerfilTrabajadorRepository.class).findById(1L)).thenReturn(Optional.of(perfil)); var dto=new ServicioGuardar(); dto.setPerfilTrabajadorId(1L); controller().publicar(dto); verify(ctx.getBean(IServicioService.class)).publicar(dto);}
 @Test void operacionesServicioAjenoDenegadas(){auth(99,"Trabajador"); recurso(); assertThrows(AccessDeniedException.class,()->controller().modificar(2L,new ServicioModificar())); assertThrows(AccessDeniedException.class,()->controller().cambiarEstado(2L,new ServicioCambiarEstado())); assertThrows(AccessDeniedException.class,()->controller().eliminar(2L)); verifyNoInteractions(ctx.getBean(IServicioService.class));}
 @Test void operacionesServicioPropio(){auth(10,"Trabajador"); recurso(); controller().modificar(2L,new ServicioModificar()); controller().cambiarEstado(2L,new ServicioCambiarEstado()); controller().eliminar(2L); verify(ctx.getBean(IServicioService.class)).eliminar(2L);}
 @Test void crearSolicitudDerivaIdentidad(){var a=auth(20,"Cliente"); recurso(); var dto=new SolicitudServicioGuardar(); dto.setServicioId(2L); solicitudes().guardar(dto,a); assertEquals(20,dto.getClienteId()); assertEquals(10,dto.getTrabajadorId()); verify(ctx.getBean(ISolicitudServicioService.class)).guardar(dto);}
 @Test void crearSolicitudNoAceptaSuplantacion(){var a=auth(20,"Cliente"); recurso(); var dto=new SolicitudServicioGuardar(); dto.setServicioId(2L); dto.setClienteId(99); assertThrows(AccessDeniedException.class,()->solicitudes().guardar(dto,a)); dto.setClienteId(20); dto.setTrabajadorId(99); assertThrows(AccessDeniedException.class,()->solicitudes().guardar(dto,a)); verifyNoInteractions(ctx.getBean(ISolicitudServicioService.class));}
 @Test void consultarSolicitudAjenaDenegado(){auth(99,"Cliente"); solicitud(); assertThrows(AccessDeniedException.class,()->solicitudes().obtenerPorId(3L)); verifyNoInteractions(ctx.getBean(ISolicitudServicioService.class));}
 @Test void consultarSolicitudPropia(){auth(20,"Cliente"); solicitud(); solicitudes().obtenerPorId(3L); verify(ctx.getBean(ISolicitudServicioService.class)).obtenerPorId(3L);}
 @Test void transicionesSolicitudAjenaDenegadas(){auth(99,"Trabajador"); solicitud(); assertThrows(AccessDeniedException.class,()->solicitudes().aceptar(3L)); assertThrows(AccessDeniedException.class,()->solicitudes().rechazar(3L,new SolicitudServicioRechazar())); assertThrows(AccessDeniedException.class,()->solicitudes().iniciar(3L)); assertThrows(AccessDeniedException.class,()->solicitudes().completar(3L)); assertThrows(AccessDeniedException.class,()->solicitudes().cancelar(3L,new SolicitudServicioCancelar())); verifyNoInteractions(ctx.getBean(ISolicitudServicioService.class));}
 @Test void trabajadorPropioPuedeAceptar(){auth(10,"Trabajador"); solicitud(); solicitudes().aceptar(3L); verify(ctx.getBean(ISolicitudServicioService.class)).aceptar(3L);}
 @Test void clientePuedeCancelarPropia(){auth(20,"Cliente"); solicitud(); solicitudes().cancelar(3L,new SolicitudServicioCancelar()); verify(ctx.getBean(ISolicitudServicioService.class)).cancelar(eq(3L),any());}
 @Test void listadoClienteAjenoDenegado(){auth(20,"Cliente"); assertThrows(AccessDeniedException.class,()->solicitudes().obtenerPorCliente(99));}
 @Test void adminServicioRolInvalido(){auth(20,"Cliente"); assertThrows(AccessDeniedException.class,()->ctx.getBean(ServicioAdminController.class).obtenerPorId(2L)); verifyNoInteractions(ctx.getBean(IServicioAdminService.class));}
 @Test void ambosAdministradoresPuedenConsultarServicio(){for(String rol:List.of("Administrador","AdministradorPrincipal")){auth(30,rol);ctx.getBean(ServicioAdminController.class).obtenerPorId(2L);}verify(ctx.getBean(IServicioAdminService.class),times(2)).obtenerPorId(2L);}
 @Test void perfilClienteNoAceptaRolTrabajador(){var a=auth(10,"Trabajador");var jwt=((org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken)a).getToken();assertThrows(AccessDeniedException.class,()->ctx.getBean(PerfilClienteController.class).modificar(new com.connectaoficios.api.dtos.perfil.PerfilClienteModificar(),jwt));verifyNoInteractions(ctx.getBean(IPerfilClienteService.class));}
 @Test void perfilClienteModificaSoloIdentidadJwt(){var a=auth(20,"Cliente");var jwt=((org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken)a).getToken();var dto=new com.connectaoficios.api.dtos.perfil.PerfilClienteModificar();ctx.getBean(PerfilClienteController.class).modificar(dto,jwt);verify(ctx.getBean(IPerfilClienteService.class)).modificar(20,dto);}
 @Test void perfilTrabajadorNoAceptaRolCliente(){var a=auth(20,"Cliente");var jwt=((org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken)a).getToken();assertThrows(AccessDeniedException.class,()->ctx.getBean(PerfilTrabajadorController.class).modificar(new com.connectaoficios.api.dtos.perfil.PerfilTrabajadorModificar(),jwt));verifyNoInteractions(ctx.getBean(IPerfilTrabajadorService.class));}
}
