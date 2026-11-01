package com.connectaoficios.api.controladores;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.Date;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** HTTP + Spring Security + firma real + persistencia H2; no sustituye MySQL Docker. */
@SpringBootTest(properties = {"spring.profiles.active=local", "spring.datasource.url=jdbc:h2:mem:local_api;MODE=MySQL;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
class LocalApiSecurityTest {
    @Autowired MockMvc mvc;
    @Value("${app.jwt.secret}") String secret;
    @Value("${app.jwt.issuer}") String issuer;
    @Value("${app.jwt.audience}") String audience;
    @Autowired com.connectaoficios.api.repositorios.IPerfilTrabajadorRepository perfiles;
    @Autowired com.connectaoficios.api.repositorios.ICategoriaRepository categorias;
    @Autowired com.connectaoficios.api.repositorios.IZonaCoberturaRepository zonas;
    @Autowired com.connectaoficios.api.repositorios.IPlanPromocionRepository planes;

    String tokenRol(int id, String rol) throws Exception {
        var claims = new JWTClaimsSet.Builder().subject("" + id).issuer(issuer).audience(audience)
                .claim("role", rol).expirationTime(Date.from(Instant.now().plusSeconds(600))).build();
        var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(secret));
        return jwt.serialize();
    }

    org.springframework.test.web.servlet.ResultActions enviar(String metodo, String ruta, String token, String body) throws Exception {
        var request = request(org.springframework.http.HttpMethod.valueOf(metodo), ruta)
                .header("Authorization", "Bearer " + token);
        if (body != null) request.contentType("application/json").content(body);
        return mvc.perform(request);
    }

    long id(org.springframework.test.web.servlet.ResultActions result, String campo) throws Exception {
        Number value = com.jayway.jsonpath.JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$." + campo);
        return value.longValue();
    }

    @Test void flujoApiConPersistenciaYAutorizacion() throws Exception {
        // Perfiles de dominio con referencias ficticias externas, no cuentas de usuario Java.
        var perfil = new com.connectaoficios.api.modelos.PerfilTrabajador();
        perfil.setTrabajadorId(20010);
        perfil = perfiles.saveAndFlush(perfil);
        var categoria = new com.connectaoficios.api.modelos.Categoria();
        categoria.setNombre("LOCAL_HTTP_INTEGRATION");
        categoria = categorias.saveAndFlush(categoria);
        var zona = new com.connectaoficios.api.modelos.ZonaCobertura();
        zona.setDepartamento("Local"); zona.setMunicipio("Local");
        zona = zonas.saveAndFlush(zona);
        var plan = new com.connectaoficios.api.modelos.PlanPromocion();
        plan.setNombre("LOCAL_HTTP_PLAN"); plan.setDuracionDias(7); plan.setPrecio(new java.math.BigDecimal("10.00"));
        plan = planes.saveAndFlush(plan);
        String trabajador = tokenRol(20010, "Trabajador"), cliente = tokenRol(20020, "Cliente");
        String ajeno = tokenRol(20030, "Trabajador");
        String body = "{\"perfilTrabajadorId\":" + perfil.getId() + ",\"categoriaId\":" + categoria.getId()
                + ",\"titulo\":\"Servicio integración\",\"descripcion\":\"Local\",\"tarifaMinima\":10,\"zonasCoberturaIds\":[" + zona.getId() + "]}";
        enviar("POST", "/api/servicios", ajeno, body).andExpect(status().isForbidden());
        long servicio = id(enviar("POST", "/api/servicios", trabajador, body).andExpect(status().isCreated()), "id");
        enviar("GET", "/api/servicios/" + servicio, cliente, null).andExpect(status().isOk());
        long solicitud = id(enviar("POST", "/api/solicitudes", cliente,
                "{\"servicioId\":" + servicio + ",\"fechaPropuesta\":\"" + java.time.LocalDate.now().plusDays(1)
                        + "\",\"horaAproximada\":\"09:00:00\",\"direccionServicio\":\"Local\",\"descripcionTrabajo\":\"Local\"}")
                .andExpect(status().isCreated()).andExpect(jsonPath("$.clienteId").value(20020))
                .andExpect(jsonPath("$.trabajadorId").value(20010)), "idSolicitud");
        enviar("GET", "/api/solicitudes/" + solicitud, ajeno, null).andExpect(status().isForbidden());
        enviar("PATCH", "/api/solicitudes/" + solicitud + "/aceptar", ajeno, null).andExpect(status().isForbidden());
        enviar("PATCH", "/api/solicitudes/" + solicitud + "/aceptar", trabajador, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ACEPTADA"));
        long conversacion = id(enviar("POST", "/api/conversaciones", cliente, "{\"solicitudId\":" + solicitud + "}")
                .andExpect(status().isCreated()), "id");
        long mensaje = id(enviar("POST", "/api/mensajes", cliente, "{\"conversacionId\":" + conversacion + ",\"contenido\":\"Local\"}")
                .andExpect(status().isCreated()), "id");
        enviar("GET", "/api/mensajes/no-leidos/total", trabajador, null).andExpect(jsonPath("$.total").value(1));
        enviar("PUT", "/api/mensajes/" + mensaje + "/leido", trabajador, null).andExpect(status().isNoContent());
        enviar("GET", "/api/mensajes/no-leidos/total", trabajador, null).andExpect(jsonPath("$.total").value(0));
        enviar("PATCH", "/api/solicitudes/" + solicitud + "/iniciar", trabajador, null).andExpect(status().isOk());
        enviar("PATCH", "/api/solicitudes/" + solicitud + "/completar", trabajador, null).andExpect(status().isOk());
        enviar("POST", "/api/resenas", cliente, "{\"solicitudId\":" + solicitud + ",\"calificacion\":5}").andExpect(status().isCreated());
        enviar("GET", "/api/reputaciones/trabajador/" + perfil.getId(), cliente, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalResenas").value(1))
                .andExpect(jsonPath("$.serviciosCompletados").value(1));
        long promocion = id(enviar("POST", "/api/promociones", trabajador,
                "{\"servicioId\":" + servicio + ",\"planId\":" + plan.getId() + "}").andExpect(status().isCreated()), "id");
        long transaccion = id(enviar("POST", "/api/transacciones-pago", trabajador,
                "{\"promocionId\":" + promocion + ",\"monto\":10,\"moneda\":\"USD\"}").andExpect(status().isCreated()), "id");
        enviar("PUT", "/api/transacciones-pago/" + transaccion + "/pago-simulado", trabajador, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("APROBADA"));
        enviar("GET", "/api/promociones/" + promocion, trabajador, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ACTIVA"));
    }
    String token(String iss, String aud, long seconds, String key) throws Exception {
        var claims = new JWTClaimsSet.Builder().subject("42").issuer(iss).audience(aud)
                .claim("role", "Cliente").expirationTime(Date.from(Instant.now().plusSeconds(seconds))).build();
        var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(key));
        return jwt.serialize();
    }
    @Test void healthConsultaBaseLocal() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }
    @Test void openApiPublico() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.openapi").exists());
    }
    @Test void endpointProtegidoSinJwt() throws Exception {
        mvc.perform(get("/api/categorias")).andExpect(status().isUnauthorized());
    }
    @Test void jwtFirmadoPermiteConsulta() throws Exception {
        mvc.perform(get("/api/categorias").header("Authorization", "Bearer " + token(issuer,audience,300,secret)))
                .andExpect(status().isOk());
    }
    @Test void firmaInvalida() throws Exception {
        mvc.perform(get("/api/categorias").header("Authorization", "Bearer " + token(issuer,audience,300,"otra-clave-local-para-prueba-de-firma-de-64-caracteres-123456789")))
                .andExpect(status().isUnauthorized());
    }
    @Test void expirado() throws Exception {
        mvc.perform(get("/api/categorias").header("Authorization", "Bearer " + token(issuer,audience,-300,secret)))
                .andExpect(status().isUnauthorized());
    }
    @Test void issuerInvalido() throws Exception {
        mvc.perform(get("/api/categorias").header("Authorization", "Bearer " + token("otro",audience,300,secret)))
                .andExpect(status().isUnauthorized());
    }
    @Test void audienceInvalido() throws Exception {
        mvc.perform(get("/api/categorias").header("Authorization", "Bearer " + token(issuer,"otro",300,secret)))
                .andExpect(status().isUnauthorized());
    }
    @Test void rolClienteNoEsAdmin() throws Exception {
        mvc.perform(get("/api/admin/estadisticas").header("Authorization", "Bearer " + token(issuer,audience,300,secret)))
                .andExpect(status().isForbidden());
    }
    @Test void corsLocalPermitido() throws Exception {
        mvc.perform(options("/api/categorias").header("Origin","http://localhost:5173").header("Access-Control-Request-Method","GET"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:5173"));
    }
    @Test void corsAjenoDenegado() throws Exception {
        mvc.perform(options("/api/categorias").header("Origin","https://example.invalid").header("Access-Control-Request-Method","GET"))
                .andExpect(status().isForbidden());
    }
}
