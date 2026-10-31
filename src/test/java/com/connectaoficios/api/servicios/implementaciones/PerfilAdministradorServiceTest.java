package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.perfil.*;
import com.connectaoficios.api.excepciones.*;
import com.connectaoficios.api.modelos.PerfilAdministrador;
import com.connectaoficios.api.repositorios.IPerfilAdministradorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class PerfilAdministradorServiceTest {
    @Mock IPerfilAdministradorRepository repository;
    PerfilAdministradorService service;
    PerfilAdministrador perfil;

    @BeforeEach
    void preparar() {
        service = new PerfilAdministradorService(repository);
        perfil = new PerfilAdministrador();
        perfil.setId(1L);
        perfil.setAdministradorId(10);
        perfil.setFotoUrl("anterior.png");
        perfil.prePersist();
    }

    @Test void crearCorrectamente() {
        var dto = new PerfilAdministradorGuardar();
        dto.setFotoUrl("nueva.png");
        when(repository.saveAndFlush(any())).thenAnswer(i -> {
            PerfilAdministrador guardado = i.getArgument(0);
            guardado.setId(1L);
            guardado.prePersist();
            return guardado;
        });
        var salida = service.guardar(dto, 10);
        assertEquals(10, salida.getAdministradorId());
        assertEquals("nueva.png", salida.getFotoUrl());
        assertNotNull(salida.getFechaCreacion());
    }

    @Test void rechazarDuplicado() {
        when(repository.existsByAdministradorId(10)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> service.guardar(new PerfilAdministradorGuardar(), 10));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test void rechazarDuplicadoConcurrente() {
        var causa = new org.hibernate.exception.ConstraintViolationException(
                "Duplicado", new java.sql.SQLException(), "uk_perfil_administrador");
        when(repository.saveAndFlush(any())).thenThrow(
                new org.springframework.dao.DataIntegrityViolationException("Duplicado", causa));
        assertThrows(ReglaNegocioException.class, () -> service.guardar(new PerfilAdministradorGuardar(), 10));
    }

    @Test void noOcultarOtraViolacionDeIntegridad() {
        var error = new org.springframework.dao.DataIntegrityViolationException("Otra restricción");
        when(repository.saveAndFlush(any())).thenThrow(error);
        assertSame(error, assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> service.guardar(new PerfilAdministradorGuardar(), 10)));
    }

    @Test void obtenerPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(perfil));
        assertEquals(10, service.obtenerPorId(1L).getAdministradorId());
    }

    @Test void obtenerPorAdministradorId() {
        when(repository.findByAdministradorId(10)).thenReturn(Optional.of(perfil));
        assertEquals(1L, service.obtenerPorAdministradorId(10).getId());
    }

    @Test void inexistente() {
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(99L));
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorAdministradorId(99));
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPerfilPublicoPorAdministradorId(99));
        assertThrows(RecursoNoEncontradoException.class, () -> service.modificar(99, new PerfilAdministradorModificar()));
    }

    @Test void modificarFoto() {
        when(repository.findByAdministradorId(10)).thenReturn(Optional.of(perfil));
        when(repository.save(perfil)).thenReturn(perfil);
        var dto = new PerfilAdministradorModificar();
        dto.setFotoUrl("nueva.png");
        assertEquals("nueva.png", service.modificar(10, dto).getFotoUrl());
    }

    @Test void conservarSiNull() {
        when(repository.findByAdministradorId(10)).thenReturn(Optional.of(perfil));
        when(repository.save(perfil)).thenReturn(perfil);
        var fecha = perfil.getFechaCreacion();
        var salida = service.modificar(10, new PerfilAdministradorModificar());
        assertEquals("anterior.png", salida.getFotoUrl());
        assertEquals(fecha, salida.getFechaCreacion());
        assertEquals(10, salida.getAdministradorId());
    }

    @Test void obtenerPublico() {
        when(repository.findByAdministradorId(10)).thenReturn(Optional.of(perfil));
        var salida = service.obtenerPerfilPublicoPorAdministradorId(10);
        assertEquals(1L, salida.getId());
        assertEquals(10, salida.getAdministradorId());
        assertEquals("anterior.png", salida.getFotoUrl());
    }
}
