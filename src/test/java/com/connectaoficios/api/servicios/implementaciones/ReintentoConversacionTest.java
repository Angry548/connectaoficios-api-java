package com.connectaoficios.api.servicios.implementaciones;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReintentoConversacionTest {
    @Test
    void colisionRevierteAntesDeAbrirTransaccionDeRecuperacion() {
        var gestor = mock(PlatformTransactionManager.class);
        var primera = mock(TransactionStatus.class);
        var segunda = mock(TransactionStatus.class);
        when(gestor.getTransaction(any())).thenReturn(primera, segunda);
        var reintento = new ReintentoConversacion(gestor);
        var intentos = new AtomicInteger();
        var colision = new DataIntegrityViolationException("duplicado", new SQLException(
                "uk_conversacion_participantes_servicio", "23000", 1062));

        assertEquals(50L, reintento.ejecutar(() -> {
            if (intentos.getAndIncrement() == 0) throw colision;
            return 50L;
        }));
        var orden = inOrder(gestor);
        orden.verify(gestor).getTransaction(any());
        orden.verify(gestor).rollback(primera);
        orden.verify(gestor).getTransaction(any());
        orden.verify(gestor).commit(segunda);
        assertEquals(2, intentos.get());
    }

    @Test
    void otraViolacionDeIntegridadNoSeReintenta() {
        var gestor = mock(PlatformTransactionManager.class);
        var estado = mock(TransactionStatus.class);
        when(gestor.getTransaction(any())).thenReturn(estado);
        var reintento = new ReintentoConversacion(gestor);
        var error = new DataIntegrityViolationException("otra restriccion", new SQLException("otra", "23000", 1062));
        assertSame(error, assertThrows(DataIntegrityViolationException.class,
                () -> reintento.ejecutar(() -> { throw error; })));
        verify(gestor).rollback(estado);
        verify(gestor, times(1)).getTransaction(any());
    }
}
