package com.connectaoficios.api.observabilidad;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest(properties = "monitoring.ui.enabled=true")
@AutoConfigureMockMvc
class MonitoreoIntegracionTest {
    @Autowired MockMvc mvc;
    @Test void healthPublicoYTrace() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"))
            .andExpect(header().exists("X-Trace-Id"));
    }
    @Test void vistaPublicaYDatosExigenAdministrador() throws Exception {
        mvc.perform(get("/monitoring")).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Monitoreo ConnectaOficios")));
        mvc.perform(get("/monitoring/data")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.traceId").exists());
        mvc.perform(get("/monitoring/data").header("Authorization", "Bearer token-invalido"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/monitoring/data").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
            .andExpect(status().isForbidden());
        mvc.perform(get("/monitoring").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Monitoreo ConnectaOficios")));
        mvc.perform(get("/monitoring/data").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.baseDatos").value("UP"));
        mvc.perform(get("/monitoring/data").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADORPRINCIPAL"))))
            .andExpect(status().isOk());
    }
    @Test void datosInvalidosTienenRespuestaComprensible() throws Exception {
        mvc.perform(post("/api/servicios").with(jwt()).contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensaje").exists())
            .andExpect(jsonPath("$.traceId").exists());
    }
    @Test void errorInexistenteTieneTrace() throws Exception {
        mvc.perform(get("/api/servicios/2147483647").with(jwt())).andExpect(status().isNotFound())
            .andExpect(header().exists("X-Trace-Id")).andExpect(jsonPath("$.traceId").exists());
    }
}
