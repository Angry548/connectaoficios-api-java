package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.planpromocion.PlanPromocionGuardar;
import com.connectaoficios.api.dtos.planpromocion.PlanPromocionModificar;
import com.connectaoficios.api.dtos.planpromocion.PlanPromocionSalida;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.PlanPromocion;
import com.connectaoficios.api.repositorios.IPlanPromocionRepository;
import com.connectaoficios.api.servicios.interfaces.IPlanPromocionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlanPromocionService implements IPlanPromocionService {

    private final IPlanPromocionRepository planPromocionRepository;

    public PlanPromocionService(IPlanPromocionRepository planPromocionRepository) {
        this.planPromocionRepository = planPromocionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanPromocionSalida> obtenerTodos() {
        return planPromocionRepository.findAll()
                .stream()
                .map(this::convertirASalida)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlanPromocionSalida> obtenerTodosPaginados(Pageable pageable) {
        return planPromocionRepository.findAll(pageable)
                .map(this::convertirASalida);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlanPromocionSalida> obtenerActivos(Pageable pageable) {
        return planPromocionRepository.findByActivo(true, pageable)
                .map(this::convertirASalida);
    }

    @Override
    @Transactional(readOnly = true)
    public PlanPromocionSalida obtenerPorId(Long id) {
        PlanPromocion plan = buscarPorIdOLanzar(id);
        return convertirASalida(plan);
    }

    @Override
    @Transactional
    public PlanPromocionSalida guardar(PlanPromocionGuardar dto) {

        if (planPromocionRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ReglaNegocioException(
                    "Ya existe un plan de promoción con el nombre '" + dto.getNombre() + "'");
        }

        PlanPromocion plan = new PlanPromocion();
        plan.setNombre(dto.getNombre());
        plan.setDescripcion(dto.getDescripcion());
        plan.setDuracionDias(dto.getDuracionDias());
        plan.setPrecio(dto.getPrecio());
        plan.setActivo(true);

        PlanPromocion guardado = planPromocionRepository.save(plan);
        return convertirASalida(guardado);
    }

    @Override
    @Transactional
    public PlanPromocionSalida modificar(Long id, PlanPromocionModificar dto) {

        PlanPromocion plan = buscarPorIdOLanzar(id);

        if (planPromocionRepository.existsByNombreIgnoreCaseAndIdNot(dto.getNombre(), id)) {
            throw new ReglaNegocioException(
                    "Ya existe otro plan de promoción con el nombre '" + dto.getNombre() + "'");
        }

        plan.setNombre(dto.getNombre());
        plan.setDescripcion(dto.getDescripcion());
        plan.setDuracionDias(dto.getDuracionDias());
        plan.setPrecio(dto.getPrecio());

        PlanPromocion actualizado = planPromocionRepository.save(plan);
        return convertirASalida(actualizado);
    }

    @Override
    @Transactional
    public PlanPromocionSalida activar(Long id) {
        PlanPromocion plan = buscarPorIdOLanzar(id);
        plan.setActivo(true);
        return convertirASalida(planPromocionRepository.save(plan));
    }

    @Override
    @Transactional
    public PlanPromocionSalida desactivar(Long id) {
        PlanPromocion plan = buscarPorIdOLanzar(id);
        plan.setActivo(false);
        return convertirASalida(planPromocionRepository.save(plan));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        PlanPromocion plan = buscarPorIdOLanzar(id);
        planPromocionRepository.delete(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public LocalDateTime calcularFechaFin(Long id, LocalDateTime fechaInicio) {
        PlanPromocion plan = buscarPorIdOLanzar(id);

        if (fechaInicio == null) {
            throw new ReglaNegocioException("La fecha de inicio es obligatoria");
        }

        return fechaInicio.plusDays(plan.getDuracionDias());
    }

    private PlanPromocion buscarPorIdOLanzar(Long id) {
        return planPromocionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el plan de promoción con id " + id));
    }

    private PlanPromocionSalida convertirASalida(PlanPromocion plan) {
        PlanPromocionSalida salida = new PlanPromocionSalida();
        salida.setId(plan.getId());
        salida.setNombre(plan.getNombre());
        salida.setDescripcion(plan.getDescripcion());
        salida.setDuracionDias(plan.getDuracionDias());
        salida.setPrecio(plan.getPrecio());
        salida.setActivo(plan.getActivo());
        return salida;
    }
}