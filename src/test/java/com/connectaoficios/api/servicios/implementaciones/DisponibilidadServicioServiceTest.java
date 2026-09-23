package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.disponibilidad.DisponibilidadServicioGuardar;
import com.connectaoficios.api.dtos.disponibilidad.DisponibilidadServicioModificar;
import com.connectaoficios.api.dtos.disponibilidad.DisponibilidadServicioSalida;
import com.connectaoficios.api.enums.DiaSemana;
import com.connectaoficios.api.enums.EstadoServicio;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.DisponibilidadServicio;
import com.connectaoficios.api.modelos.Servicio;
import com.connectaoficios.api.repositorios.IDisponibilidadServicioRepository;
import com.connectaoficios.api.repositorios.IServicioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibilidadServicioServiceTest {

    @Mock
    private IDisponibilidadServicioRepository disponibilidadRepository;

    @Mock
    private IServicioRepository servicioRepository;

    private DisponibilidadServicioService disponibilidadService;

    private Servicio servicio;

    private DisponibilidadServicio disponibilidad;

    @BeforeEach
    void setUp() {

        disponibilidadService =
                new DisponibilidadServicioService(
                        disponibilidadRepository,
                        servicioRepository
                );

        servicio = new Servicio();
        servicio.setId(1L);
        servicio.setEstado(EstadoServicio.ACTIVO);
        servicio.setEliminado(false);

        disponibilidad = new DisponibilidadServicio();
        disponibilidad.setId(1L);
        disponibilidad.setServicio(servicio);
        disponibilidad.setDiaSemana(DiaSemana.LUNES);
        disponibilidad.setHoraInicio(LocalTime.of(9, 0));
        disponibilidad.setHoraFin(LocalTime.of(12, 0));
        disponibilidad.setActivo(true);
    }

    @Test
    void guardar_debeCrearDisponibilidad() {

        DisponibilidadServicioGuardar dto =
                new DisponibilidadServicioGuardar();

        dto.setServicioId(1L);
        dto.setDiaSemana(DiaSemana.LUNES);
        dto.setHoraInicio(LocalTime.of(9, 0));
        dto.setHoraFin(LocalTime.of(12, 0));

        when(
                servicioRepository.findByIdAndEliminadoFalse(1L)
        ).thenReturn(Optional.of(servicio));

        when(
                disponibilidadRepository
                        .existsByServicioIdAndDiaSemanaAndHoraInicioAndHoraFin(
                                1L,
                                DiaSemana.LUNES,
                                LocalTime.of(9, 0),
                                LocalTime.of(12, 0)
                        )
        ).thenReturn(false);

        when(
                disponibilidadRepository.existeSolapamiento(
                        1L,
                        DiaSemana.LUNES,
                        LocalTime.of(9, 0),
                        LocalTime.of(12, 0)
                )
        ).thenReturn(false);

        when(
                disponibilidadRepository.save(
                        any(DisponibilidadServicio.class)
                )
        ).thenAnswer(invocation -> {

            DisponibilidadServicio guardada =
                    invocation.getArgument(0);

            guardada.setId(1L);

            return guardada;
        });

        DisponibilidadServicioSalida resultado =
                disponibilidadService.guardar(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getServicioId());
        assertEquals(DiaSemana.LUNES, resultado.getDiaSemana());
        assertEquals(LocalTime.of(9, 0),
                resultado.getHoraInicio());
        assertEquals(LocalTime.of(12, 0),
                resultado.getHoraFin());
        assertEquals(true, resultado.getActivo());
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoServicioNoExiste() {

        DisponibilidadServicioGuardar dto =
                new DisponibilidadServicioGuardar();

        dto.setServicioId(99L);
        dto.setDiaSemana(DiaSemana.LUNES);
        dto.setHoraInicio(LocalTime.of(9, 0));
        dto.setHoraFin(LocalTime.of(12, 0));

        when(
                servicioRepository.findByIdAndEliminadoFalse(99L)
        ).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> disponibilidadService.guardar(dto)
        );

        verify(
                disponibilidadRepository,
                never()
        ).save(any(DisponibilidadServicio.class));
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoHoraFinNoEsPosterior() {

        DisponibilidadServicioGuardar dto =
                new DisponibilidadServicioGuardar();

        dto.setServicioId(1L);
        dto.setDiaSemana(DiaSemana.LUNES);
        dto.setHoraInicio(LocalTime.of(12, 0));
        dto.setHoraFin(LocalTime.of(9, 0));

        when(
                servicioRepository.findByIdAndEliminadoFalse(1L)
        ).thenReturn(Optional.of(servicio));

        assertThrows(
                ReglaNegocioException.class,
                () -> disponibilidadService.guardar(dto)
        );
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoYaExisteDuplicado() {

        DisponibilidadServicioGuardar dto =
                new DisponibilidadServicioGuardar();

        dto.setServicioId(1L);
        dto.setDiaSemana(DiaSemana.LUNES);
        dto.setHoraInicio(LocalTime.of(9, 0));
        dto.setHoraFin(LocalTime.of(12, 0));

        when(
                servicioRepository.findByIdAndEliminadoFalse(1L)
        ).thenReturn(Optional.of(servicio));

        when(
                disponibilidadRepository
                        .existsByServicioIdAndDiaSemanaAndHoraInicioAndHoraFin(
                                1L,
                                DiaSemana.LUNES,
                                LocalTime.of(9, 0),
                                LocalTime.of(12, 0)
                        )
        ).thenReturn(true);

        assertThrows(
                ReglaNegocioException.class,
                () -> disponibilidadService.guardar(dto)
        );

        verify(
                disponibilidadRepository,
                never()
        ).save(any(DisponibilidadServicio.class));
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoSeSolapa() {

        DisponibilidadServicioGuardar dto =
                new DisponibilidadServicioGuardar();

        dto.setServicioId(1L);
        dto.setDiaSemana(DiaSemana.LUNES);
        dto.setHoraInicio(LocalTime.of(10, 0));
        dto.setHoraFin(LocalTime.of(11, 0));

        when(
                servicioRepository.findByIdAndEliminadoFalse(1L)
        ).thenReturn(Optional.of(servicio));

        when(
                disponibilidadRepository
                        .existsByServicioIdAndDiaSemanaAndHoraInicioAndHoraFin(
                                1L,
                                DiaSemana.LUNES,
                                LocalTime.of(10, 0),
                                LocalTime.of(11, 0)
                        )
        ).thenReturn(false);

        when(
                disponibilidadRepository.existeSolapamiento(
                        1L,
                        DiaSemana.LUNES,
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0)
                )
        ).thenReturn(true);

        assertThrows(
                ReglaNegocioException.class,
                () -> disponibilidadService.guardar(dto)
        );
    }

    @Test
    void modificar_debeActualizarDisponibilidad() {

        DisponibilidadServicioModificar dto =
                new DisponibilidadServicioModificar();

        dto.setDiaSemana(DiaSemana.MARTES);
        dto.setHoraInicio(LocalTime.of(8, 0));
        dto.setHoraFin(LocalTime.of(13, 0));

        when(
                disponibilidadRepository.findById(1L)
        ).thenReturn(Optional.of(disponibilidad));

        when(
                disponibilidadRepository
                        .existsByServicioIdAndDiaSemanaAndHoraInicioAndHoraFinAndIdNot(
                                1L,
                                DiaSemana.MARTES,
                                LocalTime.of(8, 0),
                                LocalTime.of(13, 0),
                                1L
                        )
        ).thenReturn(false);

        when(
                disponibilidadRepository.existeSolapamientoExceptuando(
                        1L,
                        DiaSemana.MARTES,
                        LocalTime.of(8, 0),
                        LocalTime.of(13, 0),
                        1L
                )
        ).thenReturn(false);

        when(
                disponibilidadRepository.save(disponibilidad)
        ).thenReturn(disponibilidad);

        DisponibilidadServicioSalida resultado =
                disponibilidadService.modificar(1L, dto);

        assertEquals(DiaSemana.MARTES, resultado.getDiaSemana());
    }

    @Test
    void eliminar_debeDesactivarDisponibilidad() {

        when(
                disponibilidadRepository.findById(1L)
        ).thenReturn(Optional.of(disponibilidad));

        when(
                disponibilidadRepository.save(disponibilidad)
        ).thenReturn(disponibilidad);

        disponibilidadService.eliminar(1L);

        assertEquals(false, disponibilidad.getActivo());
    }

    @Test
    void buscarPorDia_debeRetornarDisponibilidades() {

        when(
                disponibilidadRepository
                        .findAllByDiaSemanaAndActivoTrueOrderByHoraInicioAsc(
                                DiaSemana.LUNES
                        )
        ).thenReturn(List.of(disponibilidad));

        List<DisponibilidadServicioSalida> resultado =
                disponibilidadService.buscarPorDia(DiaSemana.LUNES);

        assertEquals(1, resultado.size());
        assertEquals(
                DiaSemana.LUNES,
                resultado.get(0).getDiaSemana()
        );
    }
}