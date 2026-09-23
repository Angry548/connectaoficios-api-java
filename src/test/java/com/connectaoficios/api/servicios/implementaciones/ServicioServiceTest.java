package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.servicio.ServicioCambiarEstado;
import com.connectaoficios.api.dtos.servicio.ServicioFiltroDTO;
import com.connectaoficios.api.dtos.servicio.ServicioGuardar;
import com.connectaoficios.api.dtos.servicio.ServicioModificar;
import com.connectaoficios.api.dtos.servicio.ServicioSalida;
import com.connectaoficios.api.enums.EstadoServicio;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.Categoria;
import com.connectaoficios.api.modelos.PerfilTrabajador;
import com.connectaoficios.api.modelos.Servicio;
import com.connectaoficios.api.modelos.ZonaCobertura;
import com.connectaoficios.api.repositorios.ICategoriaRepository;
import com.connectaoficios.api.repositorios.IPerfilTrabajadorRepository;
import com.connectaoficios.api.repositorios.IServicioRepository;
import com.connectaoficios.api.repositorios.IZonaCoberturaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock
    private IServicioRepository servicioRepository;

    @Mock
    private IPerfilTrabajadorRepository perfilTrabajadorRepository;

    @Mock
    private ICategoriaRepository categoriaRepository;

    @Mock
    private IZonaCoberturaRepository zonaCoberturaRepository;

    private ServicioService servicioService;

    private PerfilTrabajador perfilTrabajador;

    private Categoria categoria;

    private Servicio servicio;

    @BeforeEach
    void setUp() {

        servicioService = new ServicioService(
                servicioRepository,
                perfilTrabajadorRepository,
                categoriaRepository,
                zonaCoberturaRepository
        );

        perfilTrabajador = new PerfilTrabajador();
        perfilTrabajador.setId(1L);

        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setActivo(true);

        servicio = new Servicio();
        servicio.setId(1L);
        servicio.setPerfilTrabajador(perfilTrabajador);
        servicio.setCategoria(categoria);
        servicio.setTitulo("Fontanería básica");
        servicio.setTarifaMinima(new BigDecimal("25.00"));
        servicio.setTarifaMaxima(new BigDecimal("60.00"));
        servicio.setEstado(EstadoServicio.ACTIVO);
        servicio.setEliminado(false);
        servicio.setFechaCreacion(LocalDateTime.now());
        servicio.setFechaActualizacion(LocalDateTime.now());
    }

    @Test
    void publicar_debeCrearServicioActivo() {

        ServicioGuardar dto = new ServicioGuardar();
        dto.setPerfilTrabajadorId(1L);
        dto.setCategoriaId(1L);
        dto.setTitulo("Fontanería básica");
        dto.setDescripcion("Instalación y reparación");
        dto.setTarifaMinima(new BigDecimal("25.00"));
        dto.setTarifaMaxima(new BigDecimal("60.00"));

        when(
                perfilTrabajadorRepository.findById(1L)
        ).thenReturn(Optional.of(perfilTrabajador));

        when(
                categoriaRepository.findById(1L)
        ).thenReturn(Optional.of(categoria));

        when(
                servicioRepository.save(any(Servicio.class))
        ).thenAnswer(invocation -> {

            Servicio guardado = invocation.getArgument(0);
            guardado.setId(1L);

            return guardado;
        });

        ServicioSalida resultado =
                servicioService.publicar(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getPerfilTrabajadorId());
        assertEquals(1L, resultado.getCategoriaId());
        assertEquals(
                "Fontanería básica",
                resultado.getTitulo()
        );
        assertEquals(
                EstadoServicio.ACTIVO,
                resultado.getEstado()
        );
        assertTrue(resultado.getZonasCoberturaIds().isEmpty());

        verify(
                servicioRepository,
                times(1)
        ).save(any(Servicio.class));
    }

    @Test
    void publicar_debeLanzarExcepcionCuandoPerfilNoExiste() {

        ServicioGuardar dto = new ServicioGuardar();
        dto.setPerfilTrabajadorId(99L);
        dto.setCategoriaId(1L);
        dto.setTitulo("Fontanería básica");
        dto.setDescripcion("Instalación y reparación");
        dto.setTarifaMinima(new BigDecimal("25.00"));

        when(
                perfilTrabajadorRepository.findById(99L)
        ).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> servicioService.publicar(dto)
        );

        verify(
                servicioRepository,
                never()
        ).save(any(Servicio.class));
    }

    @Test
    void publicar_debeLanzarExcepcionCuandoCategoriaNoExiste() {

        ServicioGuardar dto = new ServicioGuardar();
        dto.setPerfilTrabajadorId(1L);
        dto.setCategoriaId(99L);
        dto.setTitulo("Fontanería básica");
        dto.setDescripcion("Instalación y reparación");
        dto.setTarifaMinima(new BigDecimal("25.00"));

        when(
                perfilTrabajadorRepository.findById(1L)
        ).thenReturn(Optional.of(perfilTrabajador));

        when(
                categoriaRepository.findById(99L)
        ).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> servicioService.publicar(dto)
        );
    }

    @Test
    void publicar_debeLanzarExcepcionCuandoCategoriaInactiva() {

        categoria.setActivo(false);

        ServicioGuardar dto = new ServicioGuardar();
        dto.setPerfilTrabajadorId(1L);
        dto.setCategoriaId(1L);
        dto.setTitulo("Fontanería básica");
        dto.setDescripcion("Instalación y reparación");
        dto.setTarifaMinima(new BigDecimal("25.00"));

        when(
                perfilTrabajadorRepository.findById(1L)
        ).thenReturn(Optional.of(perfilTrabajador));

        when(
                categoriaRepository.findById(1L)
        ).thenReturn(Optional.of(categoria));

        assertThrows(
                ReglaNegocioException.class,
                () -> servicioService.publicar(dto)
        );
    }

    @Test
    void publicar_debeLanzarExcepcionCuandoTarifaMinimaEsMayor() {

        ServicioGuardar dto = new ServicioGuardar();
        dto.setPerfilTrabajadorId(1L);
        dto.setCategoriaId(1L);
        dto.setTitulo("Fontanería básica");
        dto.setDescripcion("Instalación y reparación");
        dto.setTarifaMinima(new BigDecimal("60.00"));
        dto.setTarifaMaxima(new BigDecimal("25.00"));

        when(
                perfilTrabajadorRepository.findById(1L)
        ).thenReturn(Optional.of(perfilTrabajador));

        when(
                categoriaRepository.findById(1L)
        ).thenReturn(Optional.of(categoria));

        assertThrows(
                ReglaNegocioException.class,
                () -> servicioService.publicar(dto)
        );
    }

    @Test
    void publicar_debeLanzarExcepcionCuandoFaltaZona() {

        ServicioGuardar dto = new ServicioGuardar();
        dto.setPerfilTrabajadorId(1L);
        dto.setCategoriaId(1L);
        dto.setTitulo("Fontanería básica");
        dto.setDescripcion("Instalación y reparación");
        dto.setTarifaMinima(new BigDecimal("25.00"));
        dto.setZonasCoberturaIds(
                Set.of(1L, 2L)
        );

        when(
                perfilTrabajadorRepository.findById(1L)
        ).thenReturn(Optional.of(perfilTrabajador));

        when(
                categoriaRepository.findById(1L)
        ).thenReturn(Optional.of(categoria));

        ZonaCobertura zonaEncontrada =
                new ZonaCobertura();

        zonaEncontrada.setId(1L);

        when(
                zonaCoberturaRepository.findAllById(Set.of(1L, 2L))
        ).thenReturn(List.of(zonaEncontrada));

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> servicioService.publicar(dto)
        );
    }

    @Test
    void cambiarEstado_debeActualizarEstado() {

        ServicioCambiarEstado dto =
                new ServicioCambiarEstado();

        dto.setEstado(EstadoServicio.INACTIVO);

        when(
                servicioRepository.findByIdAndEliminadoFalse(1L)
        ).thenReturn(Optional.of(servicio));

        when(
                servicioRepository.save(servicio)
        ).thenReturn(servicio);

        ServicioSalida resultado =
                servicioService.cambiarEstado(1L, dto);

        assertEquals(
                EstadoServicio.INACTIVO,
                resultado.getEstado()
        );
    }

    @Test
    void eliminar_debeMarcarServicioEliminado() {

        when(
                servicioRepository.findByIdAndEliminadoFalse(1L)
        ).thenReturn(Optional.of(servicio));

        when(
                servicioRepository.save(servicio)
        ).thenReturn(servicio);

        servicioService.eliminar(1L);

        assertEquals(true, servicio.getEliminado());

        verify(
                servicioRepository,
                times(1)
        ).save(servicio);
    }

    @Test
    void obtenerPorId_debeLanzarExcepcionCuandoNoExiste() {

        when(
                servicioRepository.findByIdAndEliminadoFalse(99L)
        ).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> servicioService.obtenerPorId(99L)
        );
    }

    @Test
    void listarActivos_debeRetornarSoloActivos() {

        when(
                servicioRepository
                        .findAllByEliminadoFalseAndEstadoOrderByFechaCreacionDesc(
                                EstadoServicio.ACTIVO
                        )
        ).thenReturn(List.of(servicio));

        List<ServicioSalida> resultado =
                servicioService.listarActivos();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getCategoriaId());
    }

    @Test
    void buscarConFiltros_debeDelegarAlRepositorio() {

        ServicioFiltroDTO filtro = new ServicioFiltroDTO();
        filtro.setCategoriaId(1L);

        when(
                servicioRepository.buscarConFiltros(
                        1L,
                        null,
                        null,
                        null,
                        null
                )
        ).thenReturn(List.of(servicio));

        List<ServicioSalida> resultado =
                servicioService.buscarConFiltros(filtro);

        assertEquals(1, resultado.size());

        verify(
                servicioRepository,
                times(1)
        ).buscarConFiltros(1L, null, null, null, null);
    }
}