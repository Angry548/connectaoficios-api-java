package com.connectaoficios.api.servicios.implementaciones;

import java.sql.SQLException;
import java.util.Locale;
import java.util.function.Supplier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** Reintenta la operación completa solo después del rollback de la transacción fallida. */
@Component
public class ReintentoConversacion {
    private static final String CONSTRAINT = "uk_conversacion_participantes_servicio";
    private final TransactionTemplate transaccion;

    public ReintentoConversacion(PlatformTransactionManager gestor) {
        transaccion = new TransactionTemplate(gestor);
        transaccion.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public <T> T ejecutar(Supplier<T> operacion) {
        try {
            return transaccion.execute(estado -> operacion.get());
        } catch (DataIntegrityViolationException ex) {
            if (!esColisionDeConversacion(ex)) throw ex;
            // execute ya completó el rollback; esta segunda llamada abre otra transacción.
            return transaccion.execute(estado -> operacion.get());
        }
    }

    private boolean esColisionDeConversacion(Throwable error) {
        boolean duplicado = false;
        boolean constraint = false;
        for (Throwable actual = error; actual != null; actual = actual.getCause()) {
            String mensaje = actual.getMessage();
            if (mensaje != null && mensaje.toLowerCase(Locale.ROOT).contains(CONSTRAINT)) constraint = true;
            if (actual instanceof SQLException sql && (sql.getErrorCode() == 1062 || "23505".equals(sql.getSQLState()))) {
                duplicado = true;
            }
        }
        return duplicado && constraint;
    }
}
