package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.zona.ZonaCoberturaGuardar;
import com.connectaoficios.api.dtos.zona.ZonaCoberturaModificar;
import com.connectaoficios.api.dtos.zona.ZonaCoberturaSalida;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.ZonaCobertura;
import com.connectaoficios.api.repositorios.IZonaCoberturaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZonaCoberturaServiceTest {

    @Mock
    private IZonaCoberturaRepository zonaRepository;

    private ZonaCoberturaService zonaService;

    @BeforeEach
    void setUp() {
        zonaService = new ZonaCoberturaService(zonaRepository);
    }

    @Test
    void guardar_debeCrearZonaActiva() {

        ZonaCoberturaGuardar dto = new ZonaCoberturaGuardar();
        dto.setDepartamento("San Salvador");
        dto.setMunicipio("San Salvador Centro");
        dto.setLocalidad("Zona Rosa");

        when(
                zonaRepository
                        .existsByDepartamentoIgnoreCaseAndMunicipioIgnoreCase(
                                "San Salvador",
                                "San Salvador Centro"
                        )
        ).thenReturn(false);

        when(
                zonaRepository.save(any(ZonaCobertura.class))
        ).thenAnswer(invocation -> {

            ZonaCobertura guardada = invocation.getArgument(0);
            guardada.setId(1L);

            return guardada;
        });

        ZonaCoberturaSalida resultado =
                zonaService.guardar(dto);

        assertNotNull(resultado);
        assertEquals(
                "San Salvador",
                resultado.getDepartamento()
        );
        assertEquals(
                "San Salvador Centro",
                resultado.getMunicipio()
        );
        assertEquals(
                "Zona Rosa",
                resultado.getLocalidad()
        );
        assertEquals(true, resultado.getActivo());
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoYaExisteLaZona() {

        ZonaCoberturaGuardar dto = new ZonaCoberturaGuardar();
        dto.setDepartamento("San Salvador");
        dto.setMunicipio("San Salvador Centro");

        when(
                zonaRepository
                        .existsByDepartamentoIgnoreCaseAndMunicipioIgnoreCase(
                                "San Salvador",
                                "San Salvador Centro"
                        )
        ).thenReturn(true);

        assertThrows(
                ReglaNegocioException.class,
                () -> zonaService.guardar(dto)
        );

        verify(
                zonaRepository,
                never()
        ).save(any(ZonaCobertura.class));
    }

    @Test
    void modificar_debeActualizarZona() {

        ZonaCobertura zona = new ZonaCobertura();
        zona.setId(1L);
        zona.setDepartamento("San Salvador");
        zona.setMunicipio("San Salvador Centro");
        zona.setActivo(true);

        ZonaCoberturaModificar dto =
                new ZonaCoberturaModificar();

        dto.setDepartamento("La Libertad");
        dto.setMunicipio("Antiguo Cuscatlán");

        when(
                zonaRepository.findById(1L)
        ).thenReturn(Optional.of(zona));

        when(
                zonaRepository
                        .existsByDepartamentoIgnoreCaseAndMunicipioIgnoreCaseAndIdNot(
                                "La Libertad",
                                "Antiguo Cuscatlán",
                                1L
                        )
        ).thenReturn(false);

        when(
                zonaRepository.save(zona)
        ).thenReturn(zona);

        ZonaCoberturaSalida resultado =
                zonaService.modificar(1L, dto);

        assertEquals(
                "La Libertad",
                resultado.getDepartamento()
        );
        assertEquals(
                "Antiguo Cuscatlán",
                resultado.getMunicipio()
        );
    }

    @Test
    void obtenerPorId_debeLanzarExcepcionCuandoNoExiste() {

        when(
                zonaRepository.findById(99L)
        ).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> zonaService.obtenerPorId(99L)
        );
    }

    @Test
    void buscarPorDepartamento_debeRetornarZonas() {

        ZonaCobertura zona = new ZonaCobertura();
        zona.setId(1L);
        zona.setDepartamento("San Salvador");
        zona.setMunicipio("Soyapango");
        zona.setActivo(true);

        when(
                zonaRepository
                        .findAllByDepartamentoIgnoreCaseOrderByMunicipioAsc(
                                "San Salvador"
                        )
        ).thenReturn(List.of(zona));

        List<ZonaCoberturaSalida> resultado =
                zonaService.buscarPorDepartamento(" San Salvador ");

        assertEquals(1, resultado.size());
        assertEquals(
                "Soyapango",
                resultado.get(0).getMunicipio()
        );
    }

    @Test
    void eliminar_debeDesactivarZona() {

        ZonaCobertura zona = new ZonaCobertura();
        zona.setId(1L);
        zona.setActivo(true);

        when(
                zonaRepository.findById(1L)
        ).thenReturn(Optional.of(zona));

        when(
                zonaRepository.save(zona)
        ).thenReturn(zona);

        zonaService.eliminar(1L);

        assertEquals(false, zona.getActivo());
    }
}