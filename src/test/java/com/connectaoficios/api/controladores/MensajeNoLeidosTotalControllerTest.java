package com.connectaoficios.api.controladores;

import com.connectaoficios.api.seguridad.configuracion.*;
import com.connectaoficios.api.seguridad.ManejadorAccesoDenegado;
import com.connectaoficios.api.seguridad.ManejadorNoAutenticado;
import com.connectaoficios.api.servicios.interfaces.IMensajeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MensajeController.class)
@Import({SecurityConfig.class, JwtAuthenticationConverter.class,
        com.connectaoficios.api.config.JacksonConfig.class,
        ManejadorNoAutenticado.class, ManejadorAccesoDenegado.class})
class MensajeNoLeidosTotalControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean IMensajeService service;
    @MockitoBean JwtDecoder decoder;

    @Test void jwtIdentificaUsuarioParaClienteYTrabajadorAunqueEnvienOtroId() throws Exception {
        when(service.contarTotalNoLeidos(10)).thenReturn(6L);
        for (String rol : new String[]{"CLIENTE", "TRABAJADOR"}) {
            mvc.perform(get("/api/mensajes/no-leidos/total").param("usuarioId", "99")
                            .with(jwt().jwt(j -> j.subject("10").claim("email", "otro@correo.com"))
                                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + rol))))
                    .andExpect(status().isOk()).andExpect(content().json("{\"total\":6}"));
        }
        verify(service, times(2)).contarTotalNoLeidos(10);
        verifyNoMoreInteractions(service);
    }

    @Test void ceroSeSerializaComoNumero() throws Exception {
        mvc.perform(get("/api/mensajes/no-leidos/total")
                        .with(jwt().jwt(j -> j.subject("20")).authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TRABAJADOR"))))
                .andExpect(status().isOk()).andExpect(content().json("{\"total\":0}"));
        verify(service).contarTotalNoLeidos(20);
    }

    @Test void sinAutenticacionNoPuedeConsultar() throws Exception {
        mvc.perform(get("/api/mensajes/no-leidos/total")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test void otroRolNoPuedeConsultar() throws Exception {
        mvc.perform(get("/api/mensajes/no-leidos/total").with(jwt()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test void subjectInvalidoNoConsultaServicio() throws Exception {
        mvc.perform(get("/api/mensajes/no-leidos/total")
                        .with(jwt().jwt(j -> j.subject("invalido")).authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
