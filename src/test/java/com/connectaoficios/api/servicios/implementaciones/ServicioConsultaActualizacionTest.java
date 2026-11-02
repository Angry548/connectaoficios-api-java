package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.servicio.ServicioModificar;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.*;
import com.connectaoficios.api.repositorios.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Casos de Angel Mate. No altera ServicioServiceTest, asignado a Eduardo. */
class ServicioConsultaActualizacionTest {
    private final IServicioRepository repo = mock(IServicioRepository.class);
    private final ServicioService service = new ServicioService(repo,
            mock(IPerfilTrabajadorRepository.class), mock(ICategoriaRepository.class),
            mock(IZonaCoberturaRepository.class));
    private Servicio servicio;

    @BeforeEach
    void preparar() {
        var perfil = new PerfilTrabajador();
        perfil.setId(2L);
        var categoria = new Categoria();
        categoria.setId(3L);
        servicio = new Servicio();
        servicio.setId(1L);
        servicio.setPerfilTrabajador(perfil);
        servicio.setCategoria(categoria);
        servicio.setTitulo("Instalación");
        servicio.setDescripcion("Instalación eléctrica");
        servicio.setTarifaMinima(BigDecimal.TEN);
        servicio.setTarifaMaxima(new BigDecimal("50"));
    }

    @Test
    void consultarConservaIdentidadYRelaciones() {
        when(repo.findByIdAndEliminadoFalse(1L)).thenReturn(Optional.of(servicio));
        var salida = service.obtenerPorId(1L);
        assertAll(() -> assertEquals(1L, salida.getId()),
                () -> assertEquals(2L, salida.getPerfilTrabajadorId()),
                () -> assertEquals(3L, salida.getCategoriaId()),
                () -> assertEquals("Instalación", salida.getTitulo()));
        verify(repo, never()).save(any());
    }

    @Test
    void actualizarNormalizaTextoYConservaTarifasYRelaciones() {
        when(repo.findByIdAndEliminadoFalse(1L)).thenReturn(Optional.of(servicio));
        when(repo.save(servicio)).thenReturn(servicio);
        var dto = new ServicioModificar();
        dto.setTitulo("  Reparación  ");
        dto.setDescripcion("  Reparación eléctrica  ");
        var salida = service.modificar(1L, dto);
        assertAll(() -> assertEquals("Reparación", salida.getTitulo()),
                () -> assertEquals("Reparación eléctrica", salida.getDescripcion()),
                () -> assertEquals(BigDecimal.TEN, salida.getTarifaMinima()),
                () -> assertEquals(2L, salida.getPerfilTrabajadorId()),
                () -> assertEquals(3L, salida.getCategoriaId()));
        verify(repo).save(servicio);
    }

    @Test
    void actualizarRechazaTituloVacioSinPersistir() {
        when(repo.findByIdAndEliminadoFalse(1L)).thenReturn(Optional.of(servicio));
        var dto = new ServicioModificar();
        dto.setTitulo("   ");
        assertThrows(ReglaNegocioException.class, () -> service.modificar(1L, dto));
        assertEquals("Instalación", servicio.getTitulo());
        verify(repo, never()).save(any());
    }

    @Test
    void listarPorTrabajadorConservaRelacionConPerfil() {
        when(repo.findAllByPerfilTrabajadorIdAndEliminadoFalseOrderByFechaCreacionDesc(2L))
                .thenReturn(List.of(servicio));
        var salida = service.listarPorTrabajador(2L);
        assertEquals(1, salida.size());
        assertEquals(2L, salida.getFirst().getPerfilTrabajadorId());
        verify(repo).findAllByPerfilTrabajadorIdAndEliminadoFalseOrderByFechaCreacionDesc(2L);
        verify(repo, never()).save(any());
    }
}
