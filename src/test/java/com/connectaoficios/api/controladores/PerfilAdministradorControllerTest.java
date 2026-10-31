package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.perfil.PerfilAdministradorGuardar;
import com.connectaoficios.api.dtos.perfil.PerfilAdministradorModificar;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.servicios.interfaces.IPerfilAdministradorService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerfilAdministradorControllerTest {
    private final IPerfilAdministradorService service = mock(IPerfilAdministradorService.class);
    private final PerfilAdministradorController controller = new PerfilAdministradorController(service);

    @Test void crearYModificarUsanSubjectDelJwt() {
        Jwt jwt = jwt("42");
        var guardar = new PerfilAdministradorGuardar();
        var modificar = new PerfilAdministradorModificar();
        assertEquals(201, controller.guardar(guardar, jwt).getStatusCode().value());
        assertEquals(200, controller.modificar(modificar, jwt).getStatusCode().value());
        verify(service).guardar(guardar, 42);
        verify(service).modificar(42, modificar);
    }

    @Test void rechazarSubjectInvalidoSinInvocarServicio() {
        for (String subject : new String[]{"abc", "0", "-1", "2147483648", " "}) {
            Jwt jwt = jwt(subject);
            assertThrows(ReglaNegocioException.class,
                    () -> controller.guardar(new PerfilAdministradorGuardar(), jwt));
            assertThrows(ReglaNegocioException.class,
                    () -> controller.modificar(new PerfilAdministradorModificar(), jwt));
        }
        verifyNoInteractions(service);
    }

    @Test void rechazarJwtOSubjectAusente() {
        Jwt sinSubject = mock(Jwt.class);
        assertThrows(ReglaNegocioException.class,
                () -> controller.guardar(new PerfilAdministradorGuardar(), null));
        assertThrows(ReglaNegocioException.class,
                () -> controller.modificar(new PerfilAdministradorModificar(), sinSubject));
        verifyNoInteractions(service);
    }

    private Jwt jwt(String subject) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(subject);
        return jwt;
    }
}
