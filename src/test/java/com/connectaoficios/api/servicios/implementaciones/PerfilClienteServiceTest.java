package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.perfil.*;
import com.connectaoficios.api.excepciones.*;
import com.connectaoficios.api.modelos.PerfilCliente;
import com.connectaoficios.api.repositorios.IPerfilClienteRepository;
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
class PerfilClienteServiceTest {
    @Mock IPerfilClienteRepository repository;
    PerfilClienteService service;
    PerfilCliente perfil;

    @BeforeEach
    void preparar() {
        service = new PerfilClienteService(repository);
        perfil = new PerfilCliente();
        perfil.setId(1L);
        perfil.setClienteId(10);
        perfil.setFotoUrl("anterior.png");
        perfil.prePersist();
    }

    @Test void crearCorrectamente() {
        var dto = new PerfilClienteGuardar();
        dto.setFotoUrl("nueva.png");
        when(repository.saveAndFlush(any())).thenAnswer(i -> {
            PerfilCliente guardado = i.getArgument(0);
            guardado.setId(1L);
            guardado.prePersist();
            return guardado;
        });
        var salida = service.guardar(dto, 10);
        assertEquals(10, salida.getClienteId());
        assertEquals("nueva.png", salida.getFotoUrl());
        assertNotNull(salida.getFechaCreacion());
    }

    @Test void rechazarDuplicado() {
        when(repository.existsByClienteId(10)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> service.guardar(new PerfilClienteGuardar(), 10));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test void rechazarDuplicadoConcurrente() {
        var causa = new org.hibernate.exception.ConstraintViolationException(
                "Duplicado", new java.sql.SQLException(), "uk_perfil_cliente");
        when(repository.saveAndFlush(any())).thenThrow(
                new org.springframework.dao.DataIntegrityViolationException("Duplicado", causa));
        assertThrows(ReglaNegocioException.class, () -> service.guardar(new PerfilClienteGuardar(), 10));
    }

    @Test void noOcultarOtraViolacionDeIntegridad() {
        var error = new org.springframework.dao.DataIntegrityViolationException("Otra restricción");
        when(repository.saveAndFlush(any())).thenThrow(error);
        assertSame(error, assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> service.guardar(new PerfilClienteGuardar(), 10)));
    }

    @Test void obtenerPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(perfil));
        assertEquals(10, service.obtenerPorId(1L).getClienteId());
    }

    @Test void obtenerPorClienteId() {
        when(repository.findByClienteId(10)).thenReturn(Optional.of(perfil));
        assertEquals(1L, service.obtenerPorClienteId(10).getId());
    }

    @Test void inexistente() {
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(99L));
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorClienteId(99));
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPerfilPublicoPorClienteId(99));
        assertThrows(RecursoNoEncontradoException.class, () -> service.modificar(99, new PerfilClienteModificar()));
    }

    @Test void modificarFoto() {
        when(repository.findByClienteId(10)).thenReturn(Optional.of(perfil));
        when(repository.save(perfil)).thenReturn(perfil);
        var dto = new PerfilClienteModificar();
        dto.setFotoUrl("nueva.png");
        assertEquals("nueva.png", service.modificar(10, dto).getFotoUrl());
    }

    @Test void conservarSiNull() {
        when(repository.findByClienteId(10)).thenReturn(Optional.of(perfil));
        when(repository.save(perfil)).thenReturn(perfil);
        var fecha = perfil.getFechaCreacion();
        var salida = service.modificar(10, new PerfilClienteModificar());
        assertEquals("anterior.png", salida.getFotoUrl());
        assertEquals(fecha, salida.getFechaCreacion());
        assertEquals(10, salida.getClienteId());
    }

    @Test void obtenerPublico() {
        when(repository.findByClienteId(10)).thenReturn(Optional.of(perfil));
        var salida = service.obtenerPerfilPublicoPorClienteId(10);
        assertEquals(1L, salida.getId());
        assertEquals(10, salida.getClienteId());
        assertEquals("anterior.png", salida.getFotoUrl());
    }
}
