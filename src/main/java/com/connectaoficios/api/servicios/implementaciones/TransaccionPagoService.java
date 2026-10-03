package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.transaccionpago.TransaccionPagoAprobar;
import com.connectaoficios.api.dtos.transaccionpago.TransaccionPagoFiltroDTO;
import com.connectaoficios.api.dtos.transaccionpago.TransaccionPagoGuardar;
import com.connectaoficios.api.dtos.transaccionpago.TransaccionPagoSalida;
import com.connectaoficios.api.enums.EstadoPromocion;
import com.connectaoficios.api.enums.EstadoTransaccion;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.Promocion;
import com.connectaoficios.api.modelos.TransaccionPago;
import com.connectaoficios.api.repositorios.IPromocionRepository;
import com.connectaoficios.api.repositorios.ITransaccionPagoRepository;
import com.connectaoficios.api.servicios.interfaces.IPromocionService;
import com.connectaoficios.api.servicios.interfaces.ITransaccionPagoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TransaccionPagoService implements ITransaccionPagoService {

    private static final int TAMANIO_MAXIMO_PAGINA = 100;

    private static final List<EstadoTransaccion> ESTADOS_BLOQUEAN_NUEVA_TRANSACCION =
            List.of(
                    EstadoTransaccion.PENDIENTE,
                    EstadoTransaccion.APROBADA
            );

    private final ITransaccionPagoRepository transaccionPagoRepository;
    private final IPromocionRepository promocionRepository;
    private final IPromocionService promocionService;

    public TransaccionPagoService(
            ITransaccionPagoRepository transaccionPagoRepository,
            IPromocionRepository promocionRepository,
            IPromocionService promocionService
    ) {
        this.transaccionPagoRepository = transaccionPagoRepository;
        this.promocionRepository = promocionRepository;
        this.promocionService = promocionService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionPagoSalida> obtenerTodos() {
        return transaccionPagoRepository
                .findAll()
                .stream()
                .map(this::convertirASalida)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransaccionPagoSalida> obtenerTodosPaginados(
            Pageable pageable
    ) {
        return transaccionPagoRepository
                .findAll(pageable)
                .map(this::convertirASalida);
    }

    @Override
    @Transactional(readOnly = true)
    public TransaccionPagoSalida obtenerPorId(
            Long id
    ) {
        return convertirASalida(
                buscarPorIdOLanzar(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionPagoSalida> obtenerPorTrabajador(
            Integer trabajadorId
    ) {
        return transaccionPagoRepository
                .findByTrabajadorId(trabajadorId)
                .stream()
                .map(this::convertirASalida)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionPagoSalida> obtenerPorPromocion(
            Long promocionId
    ) {
        return transaccionPagoRepository
                .findByPromocion_Id(promocionId)
                .stream()
                .map(this::convertirASalida)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransaccionPagoSalida> obtenerPorEstado(
            EstadoTransaccion estado,
            Pageable pageable
    ) {
        return transaccionPagoRepository
                .findByEstado(
                        estado,
                        pageable
                )
                .map(this::convertirASalida);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSalida<TransaccionPagoSalida> buscarConFiltros(
            TransaccionPagoFiltroDTO filtro,
            int pagina,
            int tamanio
    ) {
        validarRangoMontos(
                filtro.getMontoMinimo(),
                filtro.getMontoMaximo()
        );

        validarRangoFechas(
                filtro.getFechaDesde(),
                filtro.getFechaHasta()
        );

        String moneda =
                normalizarTexto(
                        filtro.getMoneda()
                );

        if (moneda != null) {
            moneda =
                    moneda.toUpperCase(
                            Locale.ROOT
                    );
        }

        String referenciaExterna =
                normalizarTexto(
                        filtro.getReferenciaExterna()
                );

        Pageable pageable =
                crearPageable(
                        pagina,
                        tamanio
                );

        Page<TransaccionPagoSalida> resultado =
                transaccionPagoRepository
                        .buscarConFiltros(
                                filtro.getPromocionId(),
                                filtro.getServicioId(),
                                filtro.getTrabajadorId(),
                                filtro.getEstado(),
                                moneda,
                                referenciaExterna,
                                filtro.getMontoMinimo(),
                                filtro.getMontoMaximo(),
                                filtro.getFechaDesde(),
                                filtro.getFechaHasta(),
                                pageable
                        )
                        .map(this::convertirASalida);

        return PaginaSalida.desde(
                resultado
        );
    }

    @Override
    @Transactional
    public TransaccionPagoSalida guardar(
            TransaccionPagoGuardar dto,
            Integer trabajadorId
    ) {
        Promocion promocion =
                promocionRepository
                        .findById(dto.getPromocionId())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró la promoción con id "
                                                + dto.getPromocionId()
                                )
                        );

        if (!promocion
                .getTrabajadorId()
                .equals(trabajadorId)) {

            throw new ReglaNegocioException(
                    "La promoción no pertenece al trabajador autenticado"
            );
        }

        if (promocion.getEstado()
                != EstadoPromocion.PENDIENTE) {

            throw new ReglaNegocioException(
                    "Solo se puede pagar una promoción en estado PENDIENTE"
            );
        }

        if (transaccionPagoRepository
                .existsByPromocion_IdAndEstadoIn(
                        dto.getPromocionId(),
                        ESTADOS_BLOQUEAN_NUEVA_TRANSACCION
                )) {

            throw new ReglaNegocioException(
                    "Ya existe una transacción pendiente o aprobada para esta promoción"
            );
        }

        BigDecimal precioEsperado =
                promocion
                        .getPlan()
                        .getPrecio();

        if (dto.getMonto()
                .compareTo(precioEsperado) != 0) {

            throw new ReglaNegocioException(
                    "El monto de la transacción no coincide con el precio del plan"
            );
        }

        String moneda =
                dto.getMoneda()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (moneda.isBlank()) {
            throw new ReglaNegocioException(
                    "La moneda no puede estar vacía"
            );
        }

        TransaccionPago transaccion =
                new TransaccionPago();

        transaccion.setPromocion(
                promocion
        );

        transaccion.setTrabajadorId(
                trabajadorId
        );

        transaccion.setMonto(
                precioEsperado
        );

        transaccion.setMoneda(
                moneda
        );

        transaccion.setEstado(
                EstadoTransaccion.PENDIENTE
        );

        TransaccionPago guardada =
                transaccionPagoRepository.save(
                        transaccion
                );

        return convertirASalida(
                guardada
        );
    }

    @Override
    @Transactional
    public TransaccionPagoSalida aprobar(
            Long id,
            TransaccionPagoAprobar dto
    ) {
        TransaccionPago transaccion =
                buscarPorIdOLanzar(id);

        if (transaccion.getEstado()
                != EstadoTransaccion.PENDIENTE) {

            throw new ReglaNegocioException(
                    "Solo se puede aprobar una transacción en estado PENDIENTE"
            );
        }

        transaccion.setEstado(
                EstadoTransaccion.APROBADA
        );

        transaccion.setReferenciaExterna(
                dto.getReferenciaExterna().trim()
        );

        TransaccionPago actualizada =
                transaccionPagoRepository.save(
                        transaccion
                );

        promocionService.activar(
                transaccion
                        .getPromocion()
                        .getId()
        );

        return convertirASalida(
                actualizada
        );
    }

    @Override
    @Transactional
    public TransaccionPagoSalida procesarPagoSimulado(
            Long id,
            Integer trabajadorId
    ) {
        TransaccionPago transaccion =
                buscarPorIdOLanzar(id);

        if (!transaccion
                .getTrabajadorId()
                .equals(trabajadorId)) {

            throw new ReglaNegocioException(
                    "La transacción no pertenece al trabajador autenticado"
            );
        }

        if (transaccion.getEstado()
                != EstadoTransaccion.PENDIENTE) {

            throw new ReglaNegocioException(
                    "Solo se puede procesar una transacción en estado PENDIENTE"
            );
        }

        Promocion promocion =
                transaccion.getPromocion();

        if (promocion.getEstado()
                != EstadoPromocion.PENDIENTE) {

            throw new ReglaNegocioException(
                    "La promoción ya no se encuentra pendiente de pago"
            );
        }

        String referencia =
                "SIM-"
                        + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 16)
                        .toUpperCase(Locale.ROOT);

        transaccion.setEstado(
                EstadoTransaccion.APROBADA
        );

        transaccion.setReferenciaExterna(
                referencia
        );

        TransaccionPago actualizada =
                transaccionPagoRepository.save(
                        transaccion
                );

        promocionService.activar(
                promocion.getId()
        );

        return convertirASalida(
                actualizada
        );
    }

    @Override
    @Transactional
    public TransaccionPagoSalida rechazar(
            Long id
    ) {
        TransaccionPago transaccion =
                buscarPorIdOLanzar(id);

        if (transaccion.getEstado()
                != EstadoTransaccion.PENDIENTE) {

            throw new ReglaNegocioException(
                    "Solo se puede rechazar una transacción en estado PENDIENTE"
            );
        }

        transaccion.setEstado(
                EstadoTransaccion.RECHAZADA
        );

        TransaccionPago actualizada =
                transaccionPagoRepository.save(
                        transaccion
                );

        promocionService.cancelar(
                transaccion
                        .getPromocion()
                        .getId()
        );

        return convertirASalida(
                actualizada
        );
    }

    @Override
    @Transactional
    public TransaccionPagoSalida cancelar(
            Long id,
            Integer trabajadorId
    ) {
        TransaccionPago transaccion =
                buscarPorIdOLanzar(id);

        if (!transaccion
                .getTrabajadorId()
                .equals(trabajadorId)) {

            throw new ReglaNegocioException(
                    "La transacción no pertenece al trabajador autenticado"
            );
        }

        if (transaccion.getEstado()
                != EstadoTransaccion.PENDIENTE) {

            throw new ReglaNegocioException(
                    "Solo se puede cancelar una transacción en estado PENDIENTE"
            );
        }

        transaccion.setEstado(
                EstadoTransaccion.CANCELADA
        );

        TransaccionPago actualizada =
                transaccionPagoRepository.save(
                        transaccion
                );

        promocionService.cancelar(
                transaccion
                        .getPromocion()
                        .getId()
        );

        return convertirASalida(
                actualizada
        );
    }

    private Pageable crearPageable(
            int pagina,
            int tamanio
    ) {
        int paginaSegura =
                Math.max(
                        pagina,
                        0
                );

        int tamanioSeguro =
                Math.max(
                        1,
                        Math.min(
                                tamanio,
                                TAMANIO_MAXIMO_PAGINA
                        )
                );

        return PageRequest.of(
                paginaSegura,
                tamanioSeguro,
                Sort.by(
                        Sort.Direction.DESC,
                        "fecha"
                )
        );
    }

    private void validarRangoMontos(
            BigDecimal montoMinimo,
            BigDecimal montoMaximo
    ) {
        if (montoMinimo != null
                && montoMinimo.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new ReglaNegocioException(
                    "El monto mínimo no puede ser negativo"
            );
        }

        if (montoMaximo != null
                && montoMaximo.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new ReglaNegocioException(
                    "El monto máximo no puede ser negativo"
            );
        }

        if (montoMinimo != null
                && montoMaximo != null
                && montoMinimo.compareTo(
                montoMaximo
        ) > 0) {

            throw new ReglaNegocioException(
                    "El monto mínimo no puede ser mayor que el monto máximo"
            );
        }
    }

    private void validarRangoFechas(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    ) {
        if (fechaDesde != null
                && fechaHasta != null
                && fechaDesde.isAfter(fechaHasta)) {

            throw new ReglaNegocioException(
                    "La fecha inicial no puede ser posterior a la fecha final"
            );
        }
    }

    private String normalizarTexto(
            String texto
    ) {
        if (texto == null) {
            return null;
        }

        String valor =
                texto.trim();

        return valor.isEmpty()
                ? null
                : valor;
    }

    private TransaccionPago buscarPorIdOLanzar(
            Long id
    ) {
        return transaccionPagoRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró la transacción con id "
                                        + id
                        )
                );
    }

    private TransaccionPagoSalida convertirASalida(
            TransaccionPago transaccion
    ) {
        TransaccionPagoSalida salida =
                new TransaccionPagoSalida();

        salida.setId(
                transaccion.getId()
        );

        salida.setPromocionId(
                transaccion
                        .getPromocion()
                        .getId()
        );

        salida.setServicioId(
                transaccion
                        .getPromocion()
                        .getServicio()
                        .getId()
        );

        salida.setTrabajadorId(
                transaccion.getTrabajadorId()
        );

        salida.setMonto(
                transaccion.getMonto()
        );

        salida.setMoneda(
                transaccion.getMoneda()
        );

        salida.setReferenciaExterna(
                transaccion.getReferenciaExterna()
        );

        salida.setEstado(
                transaccion.getEstado()
        );

        salida.setFecha(
                transaccion.getFecha()
        );

        return salida;
    }
}