package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.repositorios.IResenaRepository;
import com.connectaoficios.api.repositorios.ISolicitudServicioRepository;
import com.connectaoficios.api.servicios.interfaces.IReputacionTrabajadorService;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Angel Mate: consultas; las pruebas de creacion de Nuria permanecen intactas. */
class ResenaConsultaTest {
    private final IResenaRepository repo = mock(IResenaRepository.class);
    private final ISolicitudServicioRepository solicitudes = mock(ISolicitudServicioRepository.class);
    private final IReputacionTrabajadorService reputacion = mock(IReputacionTrabajadorService.class);
    private final ResenaService service = new ResenaService(repo, solicitudes, reputacion);

    @Test
    void trabajadorSinResenasDevuelveListaVaciaSinRecalcular() {
        when(repo.findByPerfilTrabajadorIdOrderByFechaDesc(7L)).thenReturn(List.of());
        assertTrue(service.obtenerPorPerfilTrabajador(7L).isEmpty());
        verify(repo).findByPerfilTrabajadorIdOrderByFechaDesc(7L);
        verifyNoInteractions(solicitudes, reputacion);
    }

    @Test
    void consultaInexistenteNoGeneraReputacion() {
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(99L));
        verifyNoInteractions(solicitudes, reputacion);
        verify(repo, never()).save(any());
    }
}
