package com.connectaoficios.api.excepciones;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorRespuesta> manejarRutaNoEncontrada(Exception ex, HttpServletRequest request) {
        return respuesta(HttpStatus.NOT_FOUND, "No se encontró el recurso solicitado.", request);
    }

    @ExceptionHandler({IllegalArgumentException.class, org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            jakarta.validation.ConstraintViolationException.class})
    public ResponseEntity<ErrorRespuesta> manejarArgumento(Exception ex, HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, "Los datos de la solicitud no son válidos.", request);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarConflicto(Exception ex, HttpServletRequest request) {
        registrarError(ex);
        return respuesta(HttpStatus.CONFLICT, "No se puede completar la operación por un conflicto de datos.", request);
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<ErrorRespuesta> manejarBaseDatos(Exception ex, HttpServletRequest request) {
        registrarError(ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo completar la operación de datos.", request);
    }

    private ResponseEntity<ErrorRespuesta> respuesta(HttpStatus status, String mensaje, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorRespuesta(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), mensaje, request.getRequestURI(), null));
    }

    // Los mensajes y causas de excepciones pueden contener credenciales o SQL.
    private void registrarError(Exception ex) {
        StringBuilder ubicaciones = new StringBuilder();
        agregarTraza(ex, ubicaciones, java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>()));
        LOG.error("Error técnico (mensajes omitidos): {}", ubicaciones);
    }

    private void agregarTraza(Throwable error, StringBuilder salida, java.util.Set<Throwable> vistos) {
        if (error == null || !vistos.add(error)) return;
        salida.append("\nTipo: ").append(error.getClass().getName());
        for (StackTraceElement elemento : error.getStackTrace()) salida.append("\n  at ").append(elemento);
        for (Throwable suprimida : error.getSuppressed()) agregarTraza(suprimida, salida, vistos);
        agregarTraza(error.getCause(), salida, vistos);
    }


    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException ex,
            HttpServletRequest request
    ) {

        ErrorRespuesta error = new ErrorRespuesta(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorRespuesta> manejarReglaNegocio(
            ReglaNegocioException ex,
            HttpServletRequest request
    ) {

        ErrorRespuesta error = new ErrorRespuesta(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errores = new LinkedHashMap<>();

        for (FieldError fieldError :
                ex.getBindingResult().getFieldErrors()) {

            errores.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ErrorRespuesta error = new ErrorRespuesta(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Existen campos inválidos.",
                request.getRequestURI(),
                errores
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRespuesta> manejarAccesoDenegado(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {

        ErrorRespuesta error = new ErrorRespuesta(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "No tiene permisos para acceder a este recurso.",
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarErrorGeneral(
            Exception ex,
            HttpServletRequest request
    ) {

        registrarError(ex);
        ErrorRespuesta error = new ErrorRespuesta(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ocurrió un error interno en el servidor.",
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}