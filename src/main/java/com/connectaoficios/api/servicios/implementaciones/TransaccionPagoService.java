package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.transaccionpago.TransaccionPagoAprobar;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransaccionPagoService implements ITransaccionPagoService {

    private static final List<EstadoTransaccion> ESTADOS_BLOQUEAN_NUEVA_TRANSACCION =
            List.of(EstadoTransaccion.PENDIENTE, EstadoTransaccion.APROBADA);

    private final ITransaccionPagoRepository transaccionPagoRepository;
    private final IPromocionRepository promocionRepository;
    private final IPromocionService promocionService;

    public TransaccionPagoService(ITransaccionPagoRepository transaccionPagoRepository,
                                  IPromocionRepository promocionRepository,
                                  IPromocionService promocionService) {
        this.transaccionPagoRepository = transaccionPagoRepository;
        this.promocionRepository = promocionRepository;
        this.promocionService = promocionService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionPagoSalida> obtenerTodos() {
        return transaccionPagoRepository.findAll()
                .stream()
                .map(this::convertirASalida)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransaccionPagoSalida> obtenerTodosPaginados(Pageable pageable) {
        return transaccionPagoRepository.findAll(pageable)
                .map(this::convertirASalida);
    }

    @Override
    @Transactional(readOnly = true)
    public TransaccionPagoSalida obtenerPorId(Long id) {
        return convertirASalida(buscarPorIdOLanzar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionPagoSalida> obtenerPorTrabajador(Integer trabajadorId) {
        return transaccionPagoRepository.findByTrabajadorId(trabajadorId)
                .stream()
                .map(this::convertirASalida)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionPagoSalida> obtenerPorPromocion(Long promocionId) {
        return transaccionPagoRepository.findByPromocion_Id(promocionId)
                .stream()
                .map(this::convertirASalida)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransaccionPagoSalida> obtenerPorEstado(EstadoTransaccion estado, Pageable pageable) {
        return transaccionPagoRepository.findByEstado(estado, pageable)
                .map(this::convertirASalida);
    }

    @Override
    @Transactional
    public TransaccionPagoSalida guardar(TransaccionPagoGuardar dto, Integer trabajadorId) {

        Promocion promocion = promocionRepository.findById(dto.getPromocionId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la promoción con id " + dto.getPromocionId()));

        if (!promocion.getTrabajadorId().equals(trabajadorId)) {
            throw new ReglaNegocioException(
                    "La promoción no pertenece al trabajador autenticado");
        }

        if (promocion.getEstado() != EstadoPromocion.PENDIENTE) {
            throw new ReglaNegocioException(
                    "Solo se puede pagar una promoción en estado PENDIENTE");
        }

        if (transaccionPagoRepository.existsByPromocion_IdAndEstadoIn(
                dto.getPromocionId(), ESTADOS_BLOQUEAN_NUEVA_TRANSACCION)) {
            throw new ReglaNegocioException(
                    "Ya existe una transacción pendiente o aprobada para esta promoción");
        }

        TransaccionPago transaccion = new TransaccionPago();
        transaccion.setPromocion(promocion);
        transaccion.setTrabajadorId(trabajadorId);
        transaccion.setMonto(dto.getMonto());
        transaccion.setMoneda(dto.getMoneda());
        transaccion.setEstado(EstadoTransaccion.PENDIENTE);

        TransaccionPago guardada = transaccionPagoRepository.save(transaccion);
        return convertirASalida(guardada);
    }

    @Override
    @Transactional
    public TransaccionPagoSalida aprobar(Long id, TransaccionPagoAprobar dto) {

        TransaccionPago transaccion = buscarPorIdOLanzar(id);

        if (transaccion.getEstado() != EstadoTransaccion.PENDIENTE) {
            throw new ReglaNegocioException(
                    "Solo se puede aprobar una transacción en estado PENDIENTE");
        }

        transaccion.setEstado(EstadoTransaccion.APROBADA);
        transaccion.setReferenciaExterna(dto.getReferenciaExterna());

        TransaccionPago actualizada = transaccionPagoRepository.save(transaccion);

        // CA-05: al aprobar el pago, se activa la promoción asociada.
        promocionService.activar(transaccion.getPromocion().getId());

        return convertirASalida(actualizada);
    }

    @Override
    @Transactional
    public TransaccionPagoSalida rechazar(Long id) {

        TransaccionPago transaccion = buscarPorIdOLanzar(id);

        if (transaccion.getEstado() != EstadoTransaccion.PENDIENTE) {
            throw new ReglaNegocioException(
                    "Solo se puede rechazar una transacción en estado PENDIENTE");
        }

        transaccion.setEstado(EstadoTransaccion.RECHAZADA);
        TransaccionPago actualizada = transaccionPagoRepository.save(transaccion);

        // CA-06: si el pago es rechazado, la promoción no debe activarse.
        promocionService.cancelar(transaccion.getPromocion().getId());

        return convertirASalida(actualizada);
    }

    @Override
    @Transactional
    public TransaccionPagoSalida cancelar(Long id) {

        TransaccionPago transaccion = buscarPorIdOLanzar(id);

        if (transaccion.getEstado() != EstadoTransaccion.PENDIENTE) {
            throw new ReglaNegocioException(
                    "Solo se puede cancelar una transacción en estado PENDIENTE");
        }

        transaccion.setEstado(EstadoTransaccion.CANCELADA);
        return convertirASalida(transaccionPagoRepository.save(transaccion));
    }

    private TransaccionPago buscarPorIdOLanzar(Long id) {
        return transaccionPagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la transacción con id " + id));
    }

    private TransaccionPagoSalida convertirASalida(TransaccionPago transaccion) {
        TransaccionPagoSalida salida = new TransaccionPagoSalida();
        salida.setId(transaccion.getId());
        salida.setPromocionId(transaccion.getPromocion().getId());
        salida.setServicioId(transaccion.getPromocion().getServicio().getId());
        salida.setTrabajadorId(transaccion.getTrabajadorId());
        salida.setMonto(transaccion.getMonto());
        salida.setMoneda(transaccion.getMoneda());
        salida.setReferenciaExterna(transaccion.getReferenciaExterna());
        salida.setEstado(transaccion.getEstado());
        salida.setFecha(transaccion.getFecha());
        return salida;
    }
}