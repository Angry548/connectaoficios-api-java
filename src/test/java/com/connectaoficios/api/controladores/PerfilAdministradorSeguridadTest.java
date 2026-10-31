package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.perfil.*;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.seguridad.configuracion.JwtAuthenticationConverter;
import com.connectaoficios.api.servicios.interfaces.*;
import com.connectaoficios.api.servicios.implementaciones.UsuarioAvatarService;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.mock.web.MockMultipartFile;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerfilAdministradorSeguridadTest {
    private AnnotationConfigApplicationContext context;

    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean IPerfilAdministradorService perfilService() { return mock(IPerfilAdministradorService.class); }
        @Bean IImagenService imagenService() { return mock(IImagenService.class); }
        @Bean UsuarioAvatarService avatarService() { return mock(UsuarioAvatarService.class); }
        @Bean PerfilAdministradorController controller(IPerfilAdministradorService s) {
            return new PerfilAdministradorController(s);
        }
        @Bean ImagenController imagenController(IImagenService s) { return new ImagenController(s); }
        @Bean UsuarioAvatarController avatarController(UsuarioAvatarService s) { return new UsuarioAvatarController(s); }
    }

    @BeforeEach void preparar() { context = new AnnotationConfigApplicationContext(Config.class); }
    @AfterEach void cerrar() { SecurityContextHolder.clearContext(); context.close(); }

    @Test void ambosRolesAdministrativosPuedenCrearModificarSubirYConsultarLote() {
        var controller = context.getBean(PerfilAdministradorController.class);
        var imagen = context.getBean(ImagenController.class);
        var lote = new UsuarioFotosConsulta();
        lote.setUsuarios(List.of());
        for (String rol : List.of("Administrador", "AdministradorPrincipal")) {
            Jwt jwt = autenticar("42", rol);
            assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol.toUpperCase(java.util.Locale.ROOT))));
            controller.guardar(new PerfilAdministradorGuardar(), jwt);
            controller.modificar(new PerfilAdministradorModificar(), jwt);
            imagen.subirFotoPerfil(new MockMultipartFile("archivo", new byte[]{1}), jwt,
                    SecurityContextHolder.getContext().getAuthentication());
            context.getBean(UsuarioAvatarController.class).obtenerFotos(lote);
        }
        verify(context.getBean(IPerfilAdministradorService.class), times(2)).guardar(any(), eq(42));
        verify(context.getBean(IPerfilAdministradorService.class), times(2)).modificar(eq(42), any());
        verify(context.getBean(IImagenService.class), times(2)).subirFotoPerfilAdministrador(any(), eq(42));
        verify(context.getBean(UsuarioAvatarService.class), times(2)).resolverFotos(List.of());
    }

    @Test void clienteYTrabajadorNoPuedenEscribirPerfilAdministrativoNiConsultarLote() {
        for (String rol : List.of("Cliente", "Trabajador")) {
            Jwt jwt = autenticar("42", rol);
            var controller = context.getBean(PerfilAdministradorController.class);
            assertThrows(AccessDeniedException.class, () -> controller.guardar(new PerfilAdministradorGuardar(), jwt));
            assertThrows(AccessDeniedException.class, () -> controller.modificar(new PerfilAdministradorModificar(), jwt));
            assertThrows(AccessDeniedException.class,
                    () -> context.getBean(UsuarioAvatarController.class).obtenerFotos(new UsuarioFotosConsulta()));
        }
        verifyNoInteractions(context.getBean(IPerfilAdministradorService.class), context.getBean(UsuarioAvatarService.class));
    }

    @Test void jwtConSubjectInvalidoNoInvocaServicio() {
        Jwt jwt = autenticar("invalido", "Administrador");
        assertThrows(ReglaNegocioException.class,
                () -> context.getBean(PerfilAdministradorController.class).guardar(new PerfilAdministradorGuardar(), jwt));
        verifyNoInteractions(context.getBean(IPerfilAdministradorService.class));
    }

    @Test void imagenMantieneEnrutamientoDeClienteYTrabajador() {
        for (String rol : List.of("Cliente", "Trabajador")) {
            Jwt jwt = autenticar("42", rol);
            context.getBean(ImagenController.class).subirFotoPerfil(
                    new MockMultipartFile("archivo", new byte[]{1}), jwt,
                    SecurityContextHolder.getContext().getAuthentication());
        }
        var service = context.getBean(IImagenService.class);
        verify(service).subirFotoPerfilCliente(any(), eq(42));
        verify(service).subirFotoPerfil(any(), eq(42));
        verify(service, never()).subirFotoPerfilAdministrador(any(), any());
    }

    private Jwt autenticar(String subject, String rol) {
        Jwt jwt = Jwt.withTokenValue("test").header("alg", "HS256").subject(subject)
                .claim("http://schemas.microsoft.com/ws/2008/06/identity/claims/role", rol).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationConverter().convert(jwt));
        return jwt;
    }
}
