package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.resena.ResenaGuardar;
import com.connectaoficios.api.dtos.resena.ResenaSalida;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.PerfilTrabajador;
import com.connectaoficios.api.modelos.Resena;
import com.connectaoficios.api.modelos.Servicio;
import com.connectaoficios.api.modelos.SolicitudServicio;
import com.connectaoficios.api.repositorios.IResenaRepository;
import com.connectaoficios.api.repositorios.ISolicitudServicioRepository;
import com.connectaoficios.api.servicios.interfaces.IReputacionTrabajadorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    @Mock
    private IResenaRepository resenaRepository;

    @Mock
    private ISolicitudServicioRepository solicitudRepository;

    @Mock
    private IReputacionTrabajadorService reputacionService;

    private ResenaService service;

    private SolicitudServicio solicitud;
    private Servicio servicio;
    private PerfilTrabajador perfil;

    @BeforeEach
    void setUp() {

        service = new ResenaService(
                resenaRepository,
                solicitudRepository,
                reputacionService
        );

        perfil = new PerfilTrabajador();
        perfil.setId(1L);

        servicio = new Servicio();
        servicio.setId(10L);
        servicio.setPerfilTrabajador(perfil);

        solicitud = new SolicitudServicio();
        solicitud.setId(100L);
        solicitud.setClienteId(20);
        solicitud.setEstado(EstadoSolicitud.COMPLETADA);
        solicitud.setServicio(servicio);
    }

    @Test
    void guardar_debeCrearResena() {

        ResenaGuardar dto =
                new ResenaGuardar();

        dto.setSolicitudId(100L);
        dto.setCalificacion(5);
        dto.setComentario("Excelente servicio");

        when(
                solicitudRepository.findById(100L)
        ).thenReturn(Optional.of(solicitud));

        when(
                resenaRepository.existsBySolicitudId(100L)
        ).thenReturn(false);

        when(
                resenaRepository.save(any(Resena.class))
        ).thenAnswer(invocation -> {

            Resena resena =
                    invocation.getArgument(0);

            resena.setId(1L);

            return resena;
        });

        ResenaSalida resultado =
                service.guardar(dto, 20);

        assertNotNull(resultado);

        assertEquals(
                1L,
                resultado.getId()
        );

        assertEquals(
                100L,
                resultado.getSolicitudId()
        );

        assertEquals(
                10L,
                resultado.getServicioId()
        );

        assertEquals(
                1L,
                resultado.getPerfilTrabajadorId()
        );

        assertEquals(
                20,
                resultado.getClienteId()
        );

        assertEquals(
                5,
                resultado.getCalificacion()
        );

        assertEquals(
                "Excelente servicio",
                resultado.getComentario()
        );

        verify(
                resenaRepository,
                times(1)
        ).save(any(Resena.class));

        verify(
                reputacionService,
                times(1)
        ).recalcular(1L);
    }

    @Test
    void guardar_debeEvitarClienteIncorrecto() {

        ResenaGuardar dto =
                new ResenaGuardar();

        dto.setSolicitudId(100L);
        dto.setCalificacion(5);

        when(
                solicitudRepository.findById(100L)
        ).thenReturn(Optional.of(solicitud));

        assertThrows(
                ReglaNegocioException.class,
                () -> service.guardar(dto, 99)
        );

        verify(
                resenaRepository,
                never()
        ).save(any());
    }

    @Test
    void guardar_debeEvitarSolicitudNoCompletada() {

        solicitud.setEstado(
                EstadoSolicitud.PENDIENTE
        );

        ResenaGuardar dto =
                new ResenaGuardar();

        dto.setSolicitudId(100L);
        dto.setCalificacion(5);

        when(
                solicitudRepository.findById(100L)
        ).thenReturn(Optional.of(solicitud));

        assertThrows(
                ReglaNegocioException.class,
                () -> service.guardar(dto, 20)
        );

        verify(
                resenaRepository,
                never()
        ).save(any());
    }

    @Test
    void guardar_debeEvitarResenaDuplicada() {

        ResenaGuardar dto =
                new ResenaGuardar();

        dto.setSolicitudId(100L);
        dto.setCalificacion(5);

        when(
                solicitudRepository.findById(100L)
        ).thenReturn(Optional.of(solicitud));

        when(
                resenaRepository.existsBySolicitudId(100L)
        ).thenReturn(true);

        assertThrows(
                ReglaNegocioException.class,
                () -> service.guardar(dto, 20)
        );

        verify(
                resenaRepository,
                never()
        ).save(any());
    }
}
