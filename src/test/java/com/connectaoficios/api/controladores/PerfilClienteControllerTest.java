package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.perfil.PerfilClienteGuardar;
import com.connectaoficios.api.dtos.perfil.PerfilClienteModificar;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.servicios.interfaces.IPerfilClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerfilClienteControllerTest {
    private final IPerfilClienteService service = mock(IPerfilClienteService.class);
    private final PerfilClienteController controller = new PerfilClienteController(service);

    @Test void crearYModificarUsanSubjectDelJwt() {
        Jwt jwt = jwt("42");
        var guardar = new PerfilClienteGuardar();
        var modificar = new PerfilClienteModificar();
        assertEquals(201, controller.guardar(guardar, jwt).getStatusCode().value());
        assertEquals(200, controller.modificar(modificar, jwt).getStatusCode().value());
        verify(service).guardar(guardar, 42);
        verify(service).modificar(42, modificar);
    }

    @Test void rechazarSubjectInvalidoSinInvocarServicio() {
        for (String subject : new String[]{"abc", "0", "-1", "2147483648", " "}) {
            Jwt jwt = jwt(subject);
            assertThrows(ReglaNegocioException.class,
                    () -> controller.guardar(new PerfilClienteGuardar(), jwt));
            assertThrows(ReglaNegocioException.class,
                    () -> controller.modificar(new PerfilClienteModificar(), jwt));
        }
        verifyNoInteractions(service);
    }

    @Test void rechazarJwtOSubjectAusente() {
        Jwt sinSubject = mock(Jwt.class);
        assertThrows(ReglaNegocioException.class,
                () -> controller.guardar(new PerfilClienteGuardar(), null));
        assertThrows(ReglaNegocioException.class,
                () -> controller.modificar(new PerfilClienteModificar(), sinSubject));
        verifyNoInteractions(service);
    }

    private Jwt jwt(String subject) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(subject);
        return jwt;
    }
}
