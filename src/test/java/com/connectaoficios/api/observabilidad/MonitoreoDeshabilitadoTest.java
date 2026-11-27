package com.connectaoficios.api.observabilidad;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "monitoring.ui.enabled=false")
@AutoConfigureMockMvc
class MonitoreoDeshabilitadoTest {
    @Autowired MockMvc mvc;

    @Test void noExponeVistaNiDatosCuandoEstaDeshabilitado() throws Exception {
        mvc.perform(get("/monitoring")).andExpect(status().isNotFound());
        mvc.perform(get("/monitoring/data").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))))
            .andExpect(status().isNotFound());
    }
}
