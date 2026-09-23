package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.disponibilidad.DisponibilidadServicioGuardar;
import com.connectaoficios.api.dtos.disponibilidad.DisponibilidadServicioModificar;
import com.connectaoficios.api.dtos.disponibilidad.DisponibilidadServicioSalida;
import com.connectaoficios.api.enums.DiaSemana;
import com.connectaoficios.api.enums.EstadoServicio;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.DisponibilidadServicio;
import com.connectaoficios.api.modelos.Servicio;
import com.connectaoficios.api.repositorios.IDisponibilidadServicioRepository;
import com.connectaoficios.api.repositorios.IServicioRepository;
import com.connectaoficios.api.servicios.interfaces.IDisponibilidadServicioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DisponibilidadServicioService
        implements IDisponibilidadServicioService {

    private final IDisponibilidadServicioRepository disponibilidadRepository;
    private final IServicioRepository servicioRepository;

    public DisponibilidadServicioService(
            IDisponibilidadServicioRepository disponibilidadRepository,
            IServicioRepository servicioRepository
    ) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.servicioRepository = servicioRepository;
    }

    @Override
    @Transactional
    public DisponibilidadServicioSalida guardar(
            DisponibilidadServicioGuardar disponibilidadGuardar
    ) {

        Long servicioId = disponibilidadGuardar.getServicioId();

        Servicio servicio = buscarServicioActivo(servicioId);

        validarHorario(
                disponibilidadGuardar.getHoraInicio(),
                disponibilidadGuardar.getHoraFin()
        );

        validarSinDuplicado(
                servicioId,
                disponibilidadGuardar.getDiaSemana(),
                disponibilidadGuardar.getHoraInicio(),
                disponibilidadGuardar.getHoraFin(),
                null
        );

        validarSinSolapamiento(
                servicioId,
                disponibilidadGuardar.getDiaSemana(),
                disponibilidadGuardar.getHoraInicio(),
                disponibilidadGuardar.getHoraFin(),
                null
        );

        DisponibilidadServicio disponibilidad =
                new DisponibilidadServicio();

        disponibilidad.setServicio(servicio);
        disponibilidad.setDiaSemana(
                disponibilidadGuardar.getDiaSemana()
        );
        disponibilidad.setHoraInicio(
                disponibilidadGuardar.getHoraInicio()
        );
        disponibilidad.setHoraFin(
                disponibilidadGuardar.getHoraFin()
        );
        disponibilidad.setActivo(true);

        DisponibilidadServicio guardada =
                disponibilidadRepository.save(disponibilidad);

        return convertirASalida(guardada);
    }

    @Override
    @Transactional
    public DisponibilidadServicioSalida modificar(
            Long id,
            DisponibilidadServicioModificar disponibilidadModificar
    ) {

        DisponibilidadServicio disponibilidad =
                buscarDisponibilidad(id);

        validarHorario(
                disponibilidadModificar.getHoraInicio(),
                disponibilidadModificar.getHoraFin()
        );

        Long servicioId = disponibilidad.getServicio().getId();

        validarSinDuplicado(
                servicioId,
                disponibilidadModificar.getDiaSemana(),
                disponibilidadModificar.getHoraInicio(),
                disponibilidadModificar.getHoraFin(),
                id
        );

        validarSinSolapamiento(
                servicioId,
                disponibilidadModificar.getDiaSemana(),
                disponibilidadModificar.getHoraInicio(),
                disponibilidadModificar.getHoraFin(),
                id
        );

        disponibilidad.setDiaSemana(
                disponibilidadModificar.getDiaSemana()
        );
        disponibilidad.setHoraInicio(
                disponibilidadModificar.getHoraInicio()
        );
        disponibilidad.setHoraFin(
                disponibilidadModificar.getHoraFin()
        );

        DisponibilidadServicio actualizada =
                disponibilidadRepository.save(disponibilidad);

        return convertirASalida(actualizada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {

        DisponibilidadServicio disponibilidad =
                buscarDisponibilidad(id);

        disponibilidad.setActivo(false);

        disponibilidadRepository.save(disponibilidad);
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadServicioSalida obtenerPorId(Long id) {

        return convertirASalida(buscarDisponibilidad(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadServicioSalida> listarPorServicio(
            Long servicioId
    ) {

        return convertirLista(
                disponibilidadRepository
                        .findAllByServicioIdOrderByDiaSemanaAscHoraInicioAsc(
                                servicioId
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadServicioSalida> listarActivasPorServicio(
            Long servicioId
    ) {

        return convertirLista(
                disponibilidadRepository
                        .findAllByServicioIdAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(
                                servicioId
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadServicioSalida> buscarPorDia(
            DiaSemana diaSemana
    ) {

        return convertirLista(
                disponibilidadRepository
                        .findAllByDiaSemanaAndActivoTrueOrderByHoraInicioAsc(
                                diaSemana
                        )
        );
    }

    private DisponibilidadServicio buscarDisponibilidad(Long id) {

        return disponibilidadRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la disponibilidad del servicio"
                ));
    }

    private Servicio buscarServicioActivo(Long id) {

        Servicio servicio = servicioRepository
                .findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el servicio"
                ));

        if (servicio.getEstado() != EstadoServicio.ACTIVO) {

            throw new ReglaNegocioException(
                    "No se puede asignar disponibilidad "
                            + "a un servicio inactivo"
            );
        }

        return servicio;
    }

    private void validarHorario(
            LocalTime horaInicio,
            LocalTime horaFin
    ) {

        if (!horaFin.isAfter(horaInicio)) {

            throw new ReglaNegocioException(
                    "La hora de fin debe ser posterior "
                            + "a la hora de inicio"
            );
        }
    }

    private void validarSinDuplicado(
            Long servicioId,
            DiaSemana diaSemana,
            LocalTime horaInicio,
            LocalTime horaFin,
            Long idExcluido
    ) {

        boolean existe;

        if (idExcluido == null) {
            existe = disponibilidadRepository
                    .existsByServicioIdAndDiaSemanaAndHoraInicioAndHoraFin(
                            servicioId,
                            diaSemana,
                            horaInicio,
                            horaFin
                    );
        } else {
            existe = disponibilidadRepository
                    .existsByServicioIdAndDiaSemanaAndHoraInicioAndHoraFinAndIdNot(
                            servicioId,
                            diaSemana,
                            horaInicio,
                            horaFin,
                            idExcluido
                    );
        }

        if (existe) {

            throw new ReglaNegocioException(
                    "Ya existe esa disponibilidad para el servicio"
            );
        }
    }

    private void validarSinSolapamiento(
            Long servicioId,
            DiaSemana diaSemana,
            LocalTime horaInicio,
            LocalTime horaFin,
            Long idExcluido
    ) {

        boolean solapa;

        if (idExcluido == null) {
            solapa = disponibilidadRepository
                    .existeSolapamiento(
                            servicioId,
                            diaSemana,
                            horaInicio,
                            horaFin
                    );
        } else {
            solapa = disponibilidadRepository
                    .existeSolapamientoExceptuando(
                            servicioId,
                            diaSemana,
                            horaInicio,
                            horaFin,
                            idExcluido
                    );
        }

        if (solapa) {

            throw new ReglaNegocioException(
                    "El horario seleccionado se solapa "
                            + "con otra disponibilidad"
            );
        }
    }

    private DisponibilidadServicioSalida convertirASalida(
            DisponibilidadServicio disponibilidad
    ) {

        DisponibilidadServicioSalida salida =
                new DisponibilidadServicioSalida();

        salida.setId(disponibilidad.getId());
        salida.setServicioId(
                disponibilidad.getServicio().getId()
        );
        salida.setDiaSemana(disponibilidad.getDiaSemana());
        salida.setHoraInicio(disponibilidad.getHoraInicio());
        salida.setHoraFin(disponibilidad.getHoraFin());
        salida.setActivo(disponibilidad.getActivo());

        return salida;
    }

    private List<DisponibilidadServicioSalida> convertirLista(
            List<DisponibilidadServicio> disponibilidades
    ) {

        List<DisponibilidadServicioSalida> lista =
                new ArrayList<>();

        for (DisponibilidadServicio disponibilidad :
                disponibilidades) {

            lista.add(convertirASalida(disponibilidad));
        }

        return lista;
    }
}