package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.servicio.ServicioGuardar;
import com.connectaoficios.api.enums.EstadoServicio;
import com.connectaoficios.api.modelos.Categoria;
import com.connectaoficios.api.modelos.PerfilTrabajador;
import com.connectaoficios.api.repositorios.ICategoriaRepository;
import com.connectaoficios.api.repositorios.IPerfilTrabajadorRepository;
import com.connectaoficios.api.repositorios.IServicioRepository;
import com.connectaoficios.api.seguridad.IdentidadJwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.datasource.url=jdbc:h2:mem:servicio_controller_integration;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ServicioControllerIntegrationTest {

    private static final int TRABAJADOR_ID = 20010;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private IPerfilTrabajadorRepository perfilTrabajadorRepository;

    @Autowired
    private ICategoriaRepository categoriaRepository;

    @Autowired
    private IServicioRepository servicioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.issuer}")
    private String jwtIssuer;

    @Value("${app.jwt.audience}")
    private String jwtAudience;

    private PerfilTrabajador perfilTrabajador;
    private Categoria categoria;

    @BeforeEach
    void prepararFixtures() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertTrue(
                    connection.getMetaData().getURL().startsWith("jdbc:h2:mem:"),
                    "La prueba debe ejecutarse exclusivamente contra H2 en memoria"
            );
        }

        perfilTrabajador = new PerfilTrabajador();
        perfilTrabajador.setTrabajadorId(TRABAJADOR_ID);
        perfilTrabajador.setOficioPrincipal("Fixture de integración");
        perfilTrabajador = perfilTrabajadorRepository.saveAndFlush(perfilTrabajador);

        categoria = new Categoria();
        categoria.setNombre("CON329_Categoria_Integracion");
        categoria.setActivo(true);
        categoria = categoriaRepository.saveAndFlush(categoria);
    }

    @Test
    void crearYConsultarServicio_debePersistirYDevolverLosDatos() throws Exception {
        ServicioGuardar solicitud = new ServicioGuardar();
        solicitud.setPerfilTrabajadorId(perfilTrabajador.getId());
        solicitud.setCategoriaId(categoria.getId());
        solicitud.setTitulo("Servicio CON-329");
        solicitud.setDescripcion("Descripción de integración para CON-329");
        solicitud.setTarifaMinima(new BigDecimal("25.00"));
        solicitud.setTarifaMaxima(new BigDecimal("60.00"));

        String token = tokenTrabajadorValido();

        String respuestaCreacion = mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.perfilTrabajadorId").value(perfilTrabajador.getId()))
                .andExpect(jsonPath("$.categoriaId").value(categoria.getId()))
                .andExpect(jsonPath("$.titulo").value(solicitud.getTitulo()))
                .andExpect(jsonPath("$.descripcion").value(solicitud.getDescripcion()))
                .andExpect(jsonPath("$.tarifaMinima").value(25.00))
                .andExpect(jsonPath("$.tarifaMaxima").value(60.00))
                .andExpect(jsonPath("$.estado").value(EstadoServicio.ACTIVO.name()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long servicioId = objectMapper.readTree(respuestaCreacion)
                .path("id")
                .longValue();

        servicioRepository.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/servicios/{id}", servicioId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(servicioId))
                .andExpect(jsonPath("$.perfilTrabajadorId").value(perfilTrabajador.getId()))
                .andExpect(jsonPath("$.categoriaId").value(categoria.getId()))
                .andExpect(jsonPath("$.titulo").value(solicitud.getTitulo()))
                .andExpect(jsonPath("$.descripcion").value(solicitud.getDescripcion()))
                .andExpect(jsonPath("$.tarifaMinima").value(25.00))
                .andExpect(jsonPath("$.tarifaMaxima").value(60.00))
                .andExpect(jsonPath("$.estado").value(EstadoServicio.ACTIVO.name()));
    }

    private String tokenTrabajadorValido() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(TRABAJADOR_ID))
                .issuer(jwtIssuer)
                .audience(jwtAudience)
                .claim(IdentidadJwt.NAME_IDENTIFIER, String.valueOf(TRABAJADOR_ID))
                .claim("role", "TRABAJADOR")
                .expirationTime(Date.from(Instant.now().plusSeconds(600)))
                .build();

        SignedJWT jwt = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claims
        );
        jwt.sign(new MACSigner(jwtSecret));
        return jwt.serialize();
    }
}
