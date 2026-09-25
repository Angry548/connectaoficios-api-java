package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorGuardar;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorModificar;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorSalida;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.enums.InsigniaReputacion;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.PerfilTrabajador;
import com.connectaoficios.api.modelos.ReputacionTrabajador;
import com.connectaoficios.api.modelos.Resena;
import com.connectaoficios.api.repositorios.IPerfilTrabajadorRepository;
import com.connectaoficios.api.repositorios.IReputacionTrabajadorRepository;
import com.connectaoficios.api.repositorios.IResenaRepository;
import com.connectaoficios.api.repositorios.ISolicitudServicioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReputacionTrabajadorServiceTest {

    @Mock
    private IReputacionTrabajadorRepository reputacionRepository;

    @Mock
    private IPerfilTrabajadorRepository perfilTrabajadorRepository;

    @Mock
    private IResenaRepository resenaRepository;

    @Mock
    private ISolicitudServicioRepository solicitudServicioRepository;

    private ReputacionTrabajadorService reputacionService;

    private PerfilTrabajador perfilTrabajador;
    private ReputacionTrabajador reputacion;

    @BeforeEach
    void setUp() {

        reputacionService =
                new ReputacionTrabajadorService(
                        reputacionRepository,
                        perfilTrabajadorRepository,
                        resenaRepository,
                        solicitudServicioRepository
                );

        perfilTrabajador = new PerfilTrabajador();
        perfilTrabajador.setId(1L);

        perfilTrabajador.setTrabajadorId(7);

        reputacion = new ReputacionTrabajador();
        reputacion.setId(1L);
        reputacion.setPerfilTrabajador(perfilTrabajador);
        reputacion.setPromedioCalificacion(BigDecimal.ZERO);
        reputacion.setTotalResenas(0);
        reputacion.setServiciosCompletados(0);
        reputacion.setPuntuacionRanking(BigDecimal.ZERO);
        reputacion.setPosicionRanking(null);
        reputacion.setInsignia(
                InsigniaReputacion.NUEVO_TRABAJADOR
        );
    }

    @Test
    void guardar_debeCrearReputacionNueva() {

        ReputacionTrabajadorGuardar dto =
                new ReputacionTrabajadorGuardar();

        dto.setPerfilTrabajadorId(1L);

        when(
                reputacionRepository
                        .existsByPerfilTrabajadorId(1L)
        ).thenReturn(false);

        when(
                perfilTrabajadorRepository.findById(1L)
        ).thenReturn(Optional.of(perfilTrabajador));

        when(
                reputacionRepository.save(
                        any(ReputacionTrabajador.class)
                )
        ).thenAnswer(invocation -> {

            ReputacionTrabajador guardada =
                    invocation.getArgument(0);

            guardada.setId(1L);

            return guardada;
        });

        ReputacionTrabajadorSalida resultado =
                reputacionService.guardar(dto);

        assertNotNull(resultado);

        assertEquals(
                1L,
                resultado.getPerfilTrabajadorId()
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        resultado.getPromedioCalificacion()
                )
        );

        assertEquals(
                0,
                resultado.getTotalResenas()
        );

        assertEquals(
                0,
                resultado.getServiciosCompletados()
        );

        assertEquals(
                InsigniaReputacion.NUEVO_TRABAJADOR,
                resultado.getInsignia()
        );

        verify(
                reputacionRepository,
                times(1)
        ).save(any(ReputacionTrabajador.class));
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoYaExisteReputacion() {

        ReputacionTrabajadorGuardar dto =
                new ReputacionTrabajadorGuardar();

        dto.setPerfilTrabajadorId(1L);

        when(
                reputacionRepository
                        .existsByPerfilTrabajadorId(1L)
        ).thenReturn(true);

        assertThrows(
                ReglaNegocioException.class,
                () -> reputacionService.guardar(dto)
        );

        verify(
                perfilTrabajadorRepository,
                never()
        ).findById(anyLong());

        verify(
                reputacionRepository,
                never()
        ).save(any(ReputacionTrabajador.class));
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoPerfilNoExiste() {

        ReputacionTrabajadorGuardar dto =
                new ReputacionTrabajadorGuardar();

        dto.setPerfilTrabajadorId(99L);

        when(
                reputacionRepository
                        .existsByPerfilTrabajadorId(99L)
        ).thenReturn(false);

        when(
                perfilTrabajadorRepository.findById(99L)
        ).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> reputacionService.guardar(dto)
        );

        verify(
                reputacionRepository,
                never()
        ).save(any(ReputacionTrabajador.class));
    }

    @Test
    void obtenerPorPerfilTrabajador_debeRetornarReputacion() {

        when(
                reputacionRepository
                        .findByPerfilTrabajadorId(1L)
        ).thenReturn(Optional.of(reputacion));

        ReputacionTrabajadorSalida resultado =
                reputacionService
                        .obtenerPorPerfilTrabajador(1L);

        assertNotNull(resultado);

        assertEquals(
                1L,
                resultado.getPerfilTrabajadorId()
        );

        assertEquals(
                InsigniaReputacion.NUEVO_TRABAJADOR,
                resultado.getInsignia()
        );
    }

    @Test
    void obtenerPorPerfilTrabajador_debeLanzarExcepcionCuandoNoExiste() {

        when(
                reputacionRepository
                        .findByPerfilTrabajadorId(99L)
        ).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> reputacionService
                        .obtenerPorPerfilTrabajador(99L)
        );
    }

    @Test
    void obtenerRanking_debeOrdenarYAsignarPosiciones() {

        PerfilTrabajador perfilDos =
                new PerfilTrabajador();

        perfilDos.setId(2L);
        perfilDos.setTrabajadorId(8);

        ReputacionTrabajador reputacionDos =
                new ReputacionTrabajador();

        reputacionDos.setId(2L);
        reputacionDos.setPerfilTrabajador(perfilDos);

        reputacionDos.setPromedioCalificacion(
                new BigDecimal("4.90")
        );

        reputacionDos.setTotalResenas(25);
        reputacionDos.setServiciosCompletados(40);

        reputacionDos.setPuntuacionRanking(
                new BigDecimal("139.00")
        );

        reputacionDos.setInsignia(
                InsigniaReputacion.TOP_PLATAFORMA
        );

        reputacion.setPuntuacionRanking(
                new BigDecimal("50.00")
        );

        when(
                reputacionRepository
                        .findAllByOrderByPuntuacionRankingDesc()
        ).thenReturn(
                List.of(
                        reputacionDos,
                        reputacion
                )
        );

        List<ReputacionTrabajadorSalida> resultado =
                reputacionService.obtenerRanking();

        assertEquals(2, resultado.size());

        assertEquals(
                1,
                resultado.get(0).getPosicionRanking()
        );

        assertEquals(
                2,
                resultado.get(1).getPosicionRanking()
        );

        assertEquals(
                2L,
                resultado.get(0).getPerfilTrabajadorId()
        );

        assertEquals(
                1L,
                resultado.get(1).getPerfilTrabajadorId()
        );
    }

    @Test
    void recalcular_debeCrearReputacionSiNoExiste() {

        when(
                perfilTrabajadorRepository.findById(1L)
        ).thenReturn(Optional.of(perfilTrabajador));

        when(
                reputacionRepository
                        .findByPerfilTrabajadorId(1L)
        ).thenReturn(Optional.empty());

        when(
                reputacionRepository.save(
                        any(ReputacionTrabajador.class)
                )
        ).thenAnswer(invocation -> {

            ReputacionTrabajador guardada =
                    invocation.getArgument(0);

            if (guardada.getId() == null) {
                guardada.setId(1L);
            }

            return guardada;
        });

        when(
                resenaRepository
                        .findByPerfilTrabajadorId(1L)
        ).thenReturn(List.of());

        when(
                solicitudServicioRepository
                        .countByTrabajadorIdAndEstado(
                                7,
                                EstadoSolicitud.COMPLETADA
                        )
        ).thenReturn(0L);

        ReputacionTrabajadorSalida resultado =
                reputacionService.recalcular(1L);

        assertNotNull(resultado);

        assertEquals(
                1L,
                resultado.getPerfilTrabajadorId()
        );

        assertEquals(
                0,
                resultado.getTotalResenas()
        );

        assertEquals(
                0,
                resultado.getServiciosCompletados()
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        resultado.getPromedioCalificacion()
                )
        );

        assertEquals(
                InsigniaReputacion.NUEVO_TRABAJADOR,
                resultado.getInsignia()
        );

        verify(
                solicitudServicioRepository,
                times(1)
        ).countByTrabajadorIdAndEstado(
                7,
                EstadoSolicitud.COMPLETADA
        );

        verify(
                reputacionRepository,
                times(2)
        ).save(any(ReputacionTrabajador.class));
    }

    @Test
    void recalcular_debeLanzarExcepcionCuandoPerfilNoExiste() {

        when(
                perfilTrabajadorRepository.findById(99L)
        ).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> reputacionService.recalcular(99L)
        );

        verify(
                reputacionRepository,
                never()
        ).findByPerfilTrabajadorId(anyLong());

        verify(
                resenaRepository,
                never()
        ).findByPerfilTrabajadorId(anyLong());

        verify(
                solicitudServicioRepository,
                never()
        ).countByTrabajadorIdAndEstado(
                anyInt(),
                any(EstadoSolicitud.class)
        );
    }

    @Test
    void recalcular_debeCalcularPromedioYTotalDesdeResenas() {

        prepararRecalculo(
                crearResenas(5, 4, 5),
                1L
        );

        ReputacionTrabajadorSalida resultado =
                reputacionService.recalcular(1L);

        assertEquals(
                3,
                resultado.getTotalResenas()
        );

        assertEquals(
                1,
                resultado.getServiciosCompletados()
        );

        assertEquals(
                0,
                new BigDecimal("4.67")
                        .compareTo(
                                resultado.getPromedioCalificacion()
                        )
        );
    }

    @Test
    void recalcular_debeObtenerServiciosCompletadosDesdeSolicitudes() {

        prepararRecalculo(
                crearResenas(5),
                3L
        );

        ReputacionTrabajadorSalida resultado =
                reputacionService.recalcular(1L);

        assertEquals(
                3,
                resultado.getServiciosCompletados()
        );

        verify(
                solicitudServicioRepository,
                times(1)
        ).countByTrabajadorIdAndEstado(
                7,
                EstadoSolicitud.COMPLETADA
        );
    }

    @Test
    void recalcular_debeAsignarNuevoTrabajador() {

        prepararRecalculo(
                crearResenas(3, 4),
                2L
        );

        ReputacionTrabajadorSalida resultado =
                reputacionService.recalcular(1L);

        assertEquals(
                2,
                resultado.getServiciosCompletados()
        );

        assertEquals(
                InsigniaReputacion.NUEVO_TRABAJADOR,
                resultado.getInsignia()
        );
    }

    @Test
    void recalcular_debeAsignarTrabajadorConfiable() {

        prepararRecalculo(
                crearResenas(4, 4, 4, 4, 4),
                10L
        );

        ReputacionTrabajadorSalida resultado =
                reputacionService.recalcular(1L);

        assertEquals(
                5,
                resultado.getTotalResenas()
        );

        assertEquals(
                10,
                resultado.getServiciosCompletados()
        );

        assertEquals(
                InsigniaReputacion.TRABAJADOR_CONFIABLE,
                resultado.getInsignia()
        );
    }

    @Test
    void recalcular_debeAsignarMejorValorado() {

        prepararRecalculo(
                crearResenas(
                        5, 5, 5, 5, 5,
                        4, 4, 4, 4, 4
                ),
                15L
        );

        ReputacionTrabajadorSalida resultado =
                reputacionService.recalcular(1L);

        assertEquals(
                10,
                resultado.getTotalResenas()
        );

        assertEquals(
                15,
                resultado.getServiciosCompletados()
        );

        assertEquals(
                0,
                new BigDecimal("4.50")
                        .compareTo(
                                resultado.getPromedioCalificacion()
                        )
        );

        assertEquals(
                InsigniaReputacion.MEJOR_VALORADO,
                resultado.getInsignia()
        );
    }

    @Test
    void recalcular_debeAsignarTopPlataforma() {

        List<Resena> resenas =
                new ArrayList<>();

        for (int i = 0; i < 16; i++) {
            resenas.add(crearResena(5));
        }

        for (int i = 0; i < 4; i++) {
            resenas.add(crearResena(4));
        }

        prepararRecalculo(
                resenas,
                30L
        );

        ReputacionTrabajadorSalida resultado =
                reputacionService.recalcular(1L);

        assertEquals(
                20,
                resultado.getTotalResenas()
        );

        assertEquals(
                30,
                resultado.getServiciosCompletados()
        );

        assertEquals(
                0,
                new BigDecimal("4.80")
                        .compareTo(
                                resultado.getPromedioCalificacion()
                        )
        );

        assertEquals(
                InsigniaReputacion.TOP_PLATAFORMA,
                resultado.getInsignia()
        );
    }

    @Test
    void recalcular_debeCalcularPuntuacionRanking() {

        prepararRecalculo(
                crearResenas(
                        5, 5, 5, 5, 5,
                        4, 4, 4, 4, 4
                ),
                15L
        );

        ReputacionTrabajadorSalida resultado =
                reputacionService.recalcular(1L);

        assertEquals(
                0,
                new BigDecimal("80.00")
                        .compareTo(
                                resultado.getPuntuacionRanking()
                        )
        );
    }

    @Test
    void recalcular_primeraResenaCincoEstrellasYUnServicioCompletado_debeActualizarReputacion() {

        /*
         * Este caso representa la situación real probada
         * manualmente:
         *
         * 1 reseña de 5 estrellas
         * 1 solicitud COMPLETADA
         */
        prepararRecalculo(
                crearResenas(5),
                1L
        );

        ReputacionTrabajadorSalida resultado =
                reputacionService.recalcular(1L);

        assertEquals(
                1,
                resultado.getTotalResenas()
        );

        assertEquals(
                1,
                resultado.getServiciosCompletados()
        );

        assertEquals(
                0,
                new BigDecimal("5.00")
                        .compareTo(
                                resultado.getPromedioCalificacion()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("53.00")
                        .compareTo(
                                resultado.getPuntuacionRanking()
                        )
        );

        assertEquals(
                InsigniaReputacion.NUEVO_TRABAJADOR,
                resultado.getInsignia()
        );
    }

    @Test
    void modificar_debeActualizarServiciosYRecalcularReputacion() {

        ReputacionTrabajadorModificar dto =
                new ReputacionTrabajadorModificar();

        dto.setServiciosCompletados(10);

        reputacion.setPromedioCalificacion(
                new BigDecimal("4.00")
        );

        reputacion.setTotalResenas(5);

        when(
                reputacionRepository
                        .findByPerfilTrabajadorId(1L)
        ).thenReturn(Optional.of(reputacion));

        when(
                reputacionRepository.save(reputacion)
        ).thenReturn(reputacion);

        ReputacionTrabajadorSalida resultado =
                reputacionService.modificar(
                        1L,
                        dto
                );

        assertEquals(
                10,
                resultado.getServiciosCompletados()
        );

        assertEquals(
                InsigniaReputacion.TRABAJADOR_CONFIABLE,
                resultado.getInsignia()
        );

        assertEquals(
                0,
                new BigDecimal("60.00")
                        .compareTo(
                                resultado.getPuntuacionRanking()
                        )
        );

        verify(
                reputacionRepository,
                times(1)
        ).save(reputacion);
    }

    private void prepararRecalculo(
            List<Resena> resenas,
            long serviciosCompletados
    ) {

        when(
                perfilTrabajadorRepository.findById(1L)
        ).thenReturn(Optional.of(perfilTrabajador));

        when(
                reputacionRepository
                        .findByPerfilTrabajadorId(1L)
        ).thenReturn(Optional.of(reputacion));

        when(
                resenaRepository
                        .findByPerfilTrabajadorId(1L)
        ).thenReturn(resenas);

        when(
                solicitudServicioRepository
                        .countByTrabajadorIdAndEstado(
                                7,
                                EstadoSolicitud.COMPLETADA
                        )
        ).thenReturn(serviciosCompletados);

        when(
                reputacionRepository.save(reputacion)
        ).thenReturn(reputacion);
    }

    private List<Resena> crearResenas(
            int... calificaciones
    ) {

        List<Resena> resenas =
                new ArrayList<>();

        for (int calificacion : calificaciones) {

            resenas.add(
                    crearResena(calificacion)
            );
        }

        return resenas;
    }

    private Resena crearResena(
            int calificacion
    ) {

        Resena resena =
                new Resena();

        resena.setCalificacion(calificacion);

        return resena;
    }
}