package com.connectaoficios.api.controladores;

import com.connectaoficios.api.enums.DiaSemana;
import com.connectaoficios.api.enums.EstadoServicio;
import com.connectaoficios.api.modelos.Categoria;
import com.connectaoficios.api.modelos.DisponibilidadServicio;
import com.connectaoficios.api.modelos.PerfilTrabajador;
import com.connectaoficios.api.modelos.Servicio;
import com.connectaoficios.api.modelos.ZonaCobertura;
import com.connectaoficios.api.repositorios.ICategoriaRepository;
import com.connectaoficios.api.repositorios.IDisponibilidadServicioRepository;
import com.connectaoficios.api.repositorios.IPerfilTrabajadorRepository;
import com.connectaoficios.api.repositorios.IServicioRepository;
import com.connectaoficios.api.repositorios.IZonaCoberturaRepository;
import com.connectaoficios.api.seguridad.IdentidadJwt;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.datasource.url=jdbc:h2:mem:servicio_filtros_controller_integration;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ServicioFiltrosControllerIntegrationTest {

    private static final int PRIMER_TRABAJADOR_ID = 23001;
    private static final int SEGUNDO_TRABAJADOR_ID = 23002;

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
    private IZonaCoberturaRepository zonaCoberturaRepository;

    @Autowired
    private IServicioRepository servicioRepository;

    @Autowired
    private IDisponibilidadServicioRepository disponibilidadRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.issuer}")
    private String jwtIssuer;

    @Value("${app.jwt.audience}")
    private String jwtAudience;

    private PerfilTrabajador primerPerfil;
    private PerfilTrabajador segundoPerfil;
    private Categoria primeraCategoria;
    private Categoria segundaCategoria;
    private ZonaCobertura primeraZona;
    private ZonaCobertura segundaZona;
    private Servicio soldadura;
    private Servicio pintura;
    private Servicio tuberias;
    private Servicio electricidadInactiva;

    @BeforeEach
    void prepararFixtures() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertTrue(
                    connection.getMetaData().getURL().startsWith("jdbc:h2:mem:"),
                    "La prueba debe ejecutarse exclusivamente contra H2 en memoria"
            );
        }

        primerPerfil = crearPerfil(PRIMER_TRABAJADOR_ID, "Soldador");
        segundoPerfil = crearPerfil(SEGUNDO_TRABAJADOR_ID, "Pintor");

        primeraCategoria = crearCategoria("CON330_Categoria_Metal");
        segundaCategoria = crearCategoria("CON330_Categoria_Hogar");

        primeraZona = crearZona("CON330_Departamento_A", "Municipio_A");
        segundaZona = crearZona("CON330_Departamento_B", "Municipio_B");

        soldadura = crearServicio(
                primerPerfil,
                primeraCategoria,
                "Soldadura residencial",
                "Reparación de portones metálicos",
                "10.00",
                "20.00",
                EstadoServicio.ACTIVO,
                primeraZona,
                DiaSemana.LUNES
        );
        pintura = crearServicio(
                segundoPerfil,
                primeraCategoria,
                "Pintura interior",
                "Acabado profesional de muros",
                "30.00",
                "45.00",
                EstadoServicio.ACTIVO,
                segundaZona,
                DiaSemana.MARTES
        );
        tuberias = crearServicio(
                primerPerfil,
                segundaCategoria,
                "Mantenimiento hidráulico",
                "Reparación de tuberías de agua",
                "50.00",
                "80.00",
                EstadoServicio.ACTIVO,
                primeraZona,
                DiaSemana.MARTES
        );
        electricidadInactiva = crearServicio(
                segundoPerfil,
                segundaCategoria,
                "Electricidad segura",
                "Instalación de luminarias",
                "20.00",
                "35.00",
                EstadoServicio.INACTIVO,
                segundaZona,
                DiaSemana.LUNES
        );

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void listarSinFiltros_devuelveSoloServiciosActivosPorDefecto() throws Exception {
        JsonNode resultado = buscar();

        assertEquals(
                Set.of(soldadura.getId(), pintura.getId(), tuberias.getId()),
                ids(resultado)
        );
        assertEquals(3, resultado.path("totalElementos").asInt());
        assertEquals(3, resultado.path("contenido").size());
        assertEquals(EstadoServicio.ACTIVO.name(), resultado.path("contenido").get(0).path("estado").asText());
    }

    @Test
    void filtrarPorTexto_admiteCoincidenciasParcialesYNoDistingueMayusculas() throws Exception {
        JsonNode porTitulo = buscar("texto", "sOLd");
        JsonNode porDescripcion = buscar("texto", "TUBERÍAS");

        assertEquals(Set.of(soldadura.getId()), ids(porTitulo));
        assertEquals(Set.of(tuberias.getId()), ids(porDescripcion));
    }

    @Test
    void filtrarPorCategoria_devuelveSoloServiciosDeLaCategoria() throws Exception {
        JsonNode resultado = buscar("categoriaId", primeraCategoria.getId().toString());

        assertEquals(Set.of(soldadura.getId(), pintura.getId()), ids(resultado));
        assertEquals(2, resultado.path("totalElementos").asInt());
    }

    @Test
    void filtrarPorPerfilTrabajador_devuelveServiciosDelPerfilIndicado() throws Exception {
        JsonNode resultado = buscar("perfilTrabajadorId", primerPerfil.getId().toString());

        assertEquals(Set.of(soldadura.getId(), tuberias.getId()), ids(resultado));
        assertEquals(2, resultado.path("totalElementos").asInt());
    }

    @Test
    void filtrarPorRangoTarifas_comparaLaTarifaMinimaDelServicio() throws Exception {
        JsonNode resultado = buscar(
                "tarifaMinima", "25.00",
                "tarifaMaxima", "40.00"
        );

        assertEquals(Set.of(pintura.getId()), ids(resultado));
        assertEquals(1, resultado.path("totalElementos").asInt());
    }

    @Test
    void combinarFiltros_aplicaLaLogicaAnd() throws Exception {
        JsonNode resultado = buscar(
                "texto", "sold",
                "categoriaId", primeraCategoria.getId().toString(),
                "perfilTrabajadorId", primerPerfil.getId().toString(),
                "tarifaMinima", "5.00",
                "tarifaMaxima", "15.00"
        );

        assertEquals(Set.of(soldadura.getId()), ids(resultado));
        assertEquals(1, resultado.path("totalElementos").asInt());
    }

    @Test
    void filtrarPorEstado_admiteActivosEInactivos() throws Exception {
        JsonNode activos = buscar("estado", EstadoServicio.ACTIVO.name());
        JsonNode inactivos = buscar("estado", EstadoServicio.INACTIVO.name());

        assertEquals(Set.of(soldadura.getId(), pintura.getId(), tuberias.getId()), ids(activos));
        assertEquals(Set.of(electricidadInactiva.getId()), ids(inactivos));
        assertTrue(ids(inactivos).stream().noneMatch(ids(activos)::contains));
    }

    @Test
    void busquedaSinCoincidencias_devuelve200YContenidoVacio() throws Exception {
        JsonNode resultado = buscar("texto", "no-existe-CON330");

        assertTrue(resultado.path("contenido").isArray());
        assertTrue(resultado.path("contenido").isEmpty());
        assertEquals(0, resultado.path("totalElementos").asInt());
    }

    @Test
    void paginar_devuelveContenidoYMetadatosCorrectos() throws Exception {
        JsonNode primeraPagina = buscar("page", "0", "size", "2");
        JsonNode segundaPagina = buscar("page", "1", "size", "2");

        assertEquals(0, primeraPagina.path("pagina").asInt());
        assertEquals(1, segundaPagina.path("pagina").asInt());
        assertEquals(2, primeraPagina.path("tamanio").asInt());
        assertEquals(2, segundaPagina.path("tamanio").asInt());
        assertEquals(3, primeraPagina.path("totalElementos").asInt());
        assertEquals(2, primeraPagina.path("totalPaginas").asInt());
        assertTrue(primeraPagina.path("primera").asBoolean());
        assertTrue(primeraPagina.path("tieneSiguiente").asBoolean());
        assertTrue(segundaPagina.path("ultima").asBoolean());
        assertTrue(segundaPagina.path("tieneAnterior").asBoolean());
        assertEquals(2, primeraPagina.path("contenido").size());
        assertEquals(1, segundaPagina.path("contenido").size());
        assertNotEquals(ids(primeraPagina), ids(segundaPagina));

        Set<Long> paginasCombinadas = new HashSet<>(ids(primeraPagina));
        paginasCombinadas.addAll(ids(segundaPagina));
        assertEquals(Set.of(soldadura.getId(), pintura.getId(), tuberias.getId()), paginasCombinadas);
    }

    @Test
    void rangoTarifasInvalido_devuelve400() throws Exception {
        mockMvc.perform(get("/api/servicios")
                        .header("Authorization", "Bearer " + tokenTrabajadorValido())
                        .param("tarifaMinima", "50.00")
                        .param("tarifaMaxima", "10.00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.ruta").value("/api/servicios"));
    }

    @Test
    void filtrarPorZona_devuelveServiciosAsociadosALaZona() throws Exception {
        JsonNode resultado = buscar("zonaId", primeraZona.getId().toString());

        assertEquals(Set.of(soldadura.getId(), tuberias.getId()), ids(resultado));
        assertEquals(2, resultado.path("totalElementos").asInt());
    }

    @Test
    void filtrarPorDiaSemana_devuelveServiciosConDisponibilidadActiva() throws Exception {
        JsonNode resultado = buscar("diaSemana", DiaSemana.MARTES.name());

        assertEquals(Set.of(pintura.getId(), tuberias.getId()), ids(resultado));
        assertEquals(2, resultado.path("totalElementos").asInt());
    }

    @Test
    void combinarZonaYDiaSemana_aplicaAmbosFiltros() throws Exception {
        JsonNode resultado = buscar(
                "zonaId", primeraZona.getId().toString(),
                "diaSemana", DiaSemana.MARTES.name()
        );

        assertEquals(Set.of(tuberias.getId()), ids(resultado));
        assertEquals(1, resultado.path("totalElementos").asInt());
    }

    private JsonNode buscar(String... parametros) throws Exception {
        MockHttpServletRequestBuilder solicitud = get("/api/servicios")
                .header("Authorization", "Bearer " + tokenTrabajadorValido());

        for (int indice = 0; indice < parametros.length; indice += 2) {
            solicitud.param(parametros[indice], parametros[indice + 1]);
        }

        String respuesta = mockMvc.perform(solicitud)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.contenido").isArray())
                .andExpect(jsonPath("$.pagina").isNumber())
                .andExpect(jsonPath("$.tamanio").isNumber())
                .andExpect(jsonPath("$.totalElementos").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(respuesta);
    }

    private Set<Long> ids(JsonNode pagina) {
        Set<Long> resultado = new HashSet<>();
        pagina.path("contenido").forEach(servicio ->
                resultado.add(servicio.path("id").asLong())
        );
        return resultado;
    }

    private PerfilTrabajador crearPerfil(int trabajadorId, String oficio) {
        PerfilTrabajador perfil = new PerfilTrabajador();
        perfil.setTrabajadorId(trabajadorId);
        perfil.setOficioPrincipal(oficio);
        return perfilTrabajadorRepository.saveAndFlush(perfil);
    }

    private Categoria crearCategoria(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setActivo(true);
        return categoriaRepository.saveAndFlush(categoria);
    }

    private ZonaCobertura crearZona(String departamento, String municipio) {
        ZonaCobertura zona = new ZonaCobertura();
        zona.setDepartamento(departamento);
        zona.setMunicipio(municipio);
        zona.setActivo(true);
        return zonaCoberturaRepository.saveAndFlush(zona);
    }

    private Servicio crearServicio(
            PerfilTrabajador perfil,
            Categoria categoria,
            String titulo,
            String descripcion,
            String tarifaMinima,
            String tarifaMaxima,
            EstadoServicio estado,
            ZonaCobertura zona,
            DiaSemana diaSemana
    ) {
        Servicio servicio = new Servicio();
        servicio.setPerfilTrabajador(perfil);
        servicio.setCategoria(categoria);
        servicio.setTitulo(titulo);
        servicio.setDescripcion(descripcion);
        servicio.setTarifaMinima(new BigDecimal(tarifaMinima));
        servicio.setTarifaMaxima(new BigDecimal(tarifaMaxima));
        servicio.setEstado(estado);
        servicio.setZonasCobertura(new HashSet<>(Set.of(zona)));
        servicio = servicioRepository.saveAndFlush(servicio);

        DisponibilidadServicio disponibilidad = new DisponibilidadServicio();
        disponibilidad.setServicio(servicio);
        disponibilidad.setDiaSemana(diaSemana);
        disponibilidad.setHoraInicio(LocalTime.of(9, 0));
        disponibilidad.setHoraFin(LocalTime.of(10, 0));
        disponibilidad.setActivo(true);
        disponibilidadRepository.saveAndFlush(disponibilidad);

        return servicio;
    }

    private String tokenTrabajadorValido() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(PRIMER_TRABAJADOR_ID))
                .issuer(jwtIssuer)
                .audience(jwtAudience)
                .claim(IdentidadJwt.NAME_IDENTIFIER, String.valueOf(PRIMER_TRABAJADOR_ID))
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
