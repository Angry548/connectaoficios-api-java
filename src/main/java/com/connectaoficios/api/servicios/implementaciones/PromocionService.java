package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.promocion.PromocionGuardar;
import com.connectaoficios.api.dtos.promocion.PromocionResumenSalida;
import com.connectaoficios.api.dtos.promocion.PromocionSalida;
import com.connectaoficios.api.enums.EstadoPromocion;
import com.connectaoficios.api.enums.EstadoServicio;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.PlanPromocion;
import com.connectaoficios.api.modelos.Promocion;
import com.connectaoficios.api.modelos.Servicio;
import com.connectaoficios.api.repositorios.IPlanPromocionRepository;
import com.connectaoficios.api.repositorios.IPromocionRepository;
import com.connectaoficios.api.repositorios.IServicioRepository;
import com.connectaoficios.api.servicios.interfaces.IPromocionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromocionService implements IPromocionService {

    private static final List<EstadoPromocion> ESTADOS_BLOQUEAN_NUEVA_PROMOCION =
            List.of(EstadoPromocion.PENDIENTE, EstadoPromocion.ACTIVA);

    private final IPromocionRepository promocionRepository;
    private final IPlanPromocionRepository planPromocionRepository;
    private final IServicioRepository servicioRepository;

    public PromocionService(IPromocionRepository promocionRepository,
                            IPlanPromocionRepository planPromocionRepository,
                            IServicioRepository servicioRepository) {
        this.promocionRepository = promocionRepository;
        this.planPromocionRepository = planPromocionRepository;
        this.servicioRepository = servicioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionSalida> obtenerTodos() {
        return promocionRepository.findAll()
                .stream()
                .map(this::convertirASalida)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromocionSalida> obtenerTodosPaginados(Pageable pageable) {
        return promocionRepository.findAll(pageable)
                .map(this::convertirASalida);
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionSalida obtenerPorId(Long id) {
        return convertirASalida(buscarPorIdOLanzar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionSalida> obtenerPorTrabajador(Integer trabajadorId) {
        return promocionRepository.findByTrabajadorId(trabajadorId)
                .stream()
                .map(this::convertirASalida)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionSalida> obtenerPorServicio(Long servicioId) {
        return promocionRepository.findByServicio_Id(servicioId)
                .stream()
                .map(this::convertirASalida)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromocionSalida> obtenerPorEstado(EstadoPromocion estado, Pageable pageable) {
        return promocionRepository.findByEstado(estado, pageable)
                .map(this::convertirASalida);
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionResumenSalida obtenerResumen(Long servicioId, Long planId) {

        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el servicio con id " + servicioId));

        PlanPromocion plan = planPromocionRepository.findById(planId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el plan de promoción con id " + planId));

        PromocionResumenSalida resumen = new PromocionResumenSalida();
        resumen.setServicioId(servicio.getId());
        resumen.setServicioTitulo(servicio.getTitulo());
        resumen.setPlanId(plan.getId());
        resumen.setPlanNombre(plan.getNombre());
        resumen.setDuracionDias(plan.getDuracionDias());
        resumen.setCostoTotal(plan.getPrecio());
        return resumen;
    }

    @Override
    @Transactional
    public PromocionSalida guardar(PromocionGuardar dto, Integer trabajadorId) {

        Servicio servicio = servicioRepository.findById(dto.getServicioId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el servicio con id " + dto.getServicioId()));

        if (!servicio.getPerfilTrabajador().getTrabajadorId().equals(trabajadorId)) {
            throw new ReglaNegocioException(
                    "El servicio no pertenece al trabajador autenticado");
        }

        if (servicio.getEstado() != EstadoServicio.ACTIVO) {
            throw new ReglaNegocioException(
                    "Solo se puede promocionar un servicio activo");
        }

        PlanPromocion plan = planPromocionRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el plan de promoción con id " + dto.getPlanId()));

        if (!Boolean.TRUE.equals(plan.getActivo())) {
            throw new ReglaNegocioException(
                    "El plan de promoción seleccionado no está disponible");
        }

        if (promocionRepository.existsByServicio_IdAndEstadoIn(
                dto.getServicioId(), ESTADOS_BLOQUEAN_NUEVA_PROMOCION)) {
            throw new ReglaNegocioException(
                    "El servicio ya tiene una promoción pendiente o activa");
        }

        Promocion promocion = new Promocion();
        promocion.setServicio(servicio);
        promocion.setPlan(plan);
        promocion.setTrabajadorId(trabajadorId);
        promocion.setEstado(EstadoPromocion.PENDIENTE);

        Promocion guardada = promocionRepository.save(promocion);
        return convertirASalida(guardada);
    }

    @Override
    @Transactional
    public PromocionSalida activar(Long id) {

        Promocion promocion = buscarPorIdOLanzar(id);

        if (promocion.getEstado() != EstadoPromocion.PENDIENTE) {
            throw new ReglaNegocioException(
                    "Solo se puede activar una promoción en estado PENDIENTE");
        }

        LocalDateTime inicio = LocalDateTime.now();

        promocion.setEstado(EstadoPromocion.ACTIVA);
        promocion.setFechaInicio(inicio);
        promocion.setFechaFin(inicio.plusDays(promocion.getPlan().getDuracionDias()));

        Promocion actualizada = promocionRepository.save(promocion);
        return convertirASalida(actualizada);
    }

    @Override
    @Transactional
    public PromocionSalida cancelar(Long id) {

        Promocion promocion = buscarPorIdOLanzar(id);

        if (promocion.getEstado() == EstadoPromocion.FINALIZADA
                || promocion.getEstado() == EstadoPromocion.CANCELADA) {
            throw new ReglaNegocioException(
                    "No se puede cancelar una promoción ya finalizada o cancelada");
        }

        promocion.setEstado(EstadoPromocion.CANCELADA);
        Promocion actualizada = promocionRepository.save(promocion);
        return convertirASalida(actualizada);
    }

    @Override
    @Transactional
    public PromocionSalida finalizar(Long id) {

        Promocion promocion = buscarPorIdOLanzar(id);

        if (promocion.getEstado() != EstadoPromocion.ACTIVA) {
            throw new ReglaNegocioException(
                    "Solo se puede finalizar una promoción en estado ACTIVA");
        }

        promocion.setEstado(EstadoPromocion.FINALIZADA);
        Promocion actualizada = promocionRepository.save(promocion);
        return convertirASalida(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaVigente(Long id) {
        Promocion promocion = buscarPorIdOLanzar(id);
        return promocion.getEstado() == EstadoPromocion.ACTIVA
                && promocion.getFechaFin() != null
                && promocion.getFechaFin().isAfter(LocalDateTime.now());
    }

    private Promocion buscarPorIdOLanzar(Long id) {
        return promocionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la promoción con id " + id));
    }

    private PromocionSalida convertirASalida(Promocion promocion) {
        PromocionSalida salida = new PromocionSalida();
        salida.setId(promocion.getId());
        salida.setServicioId(promocion.getServicio().getId());
        salida.setPlanId(promocion.getPlan().getId());
        salida.setPlanNombre(promocion.getPlan().getNombre());
        salida.setTrabajadorId(promocion.getTrabajadorId());
        salida.setEstado(promocion.getEstado());
        salida.setFechaInicio(promocion.getFechaInicio());
        salida.setFechaFin(promocion.getFechaFin());
        salida.setFechaCreacion(promocion.getFechaCreacion());
        salida.setVigente(
                promocion.getEstado() == EstadoPromocion.ACTIVA
                        && promocion.getFechaFin() != null
                        && promocion.getFechaFin().isAfter(LocalDateTime.now())
        );
        return salida;
    }
}