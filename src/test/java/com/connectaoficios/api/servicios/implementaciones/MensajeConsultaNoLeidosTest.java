package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.repositorios.IConversacionRepository;
import com.connectaoficios.api.repositorios.IMensajeRepository;
import com.connectaoficios.api.servicios.interfaces.IChatTiempoRealService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Angel Mate: consulta inexistente; conserva MensajeServiceTest de Oscar. */
class MensajeConsultaNoLeidosTest {
    @Test
    void contarNoLeidosRechazaConversacionInexistenteSinConsultarMensajes() {
        var mensajes = mock(IMensajeRepository.class);
        var conversaciones = mock(IConversacionRepository.class);
        var chat = mock(IChatTiempoRealService.class);
        var service = new MensajeService(mensajes, conversaciones, chat);
        assertThrows(RecursoNoEncontradoException.class, () -> service.contarNoLeidos(99L, 10));
        verify(conversaciones).findById(99L);
        verifyNoInteractions(mensajes, chat);
    }
}
