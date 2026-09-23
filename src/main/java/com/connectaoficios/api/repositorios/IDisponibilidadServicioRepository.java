package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.enums.DiaSemana;
import com.connectaoficios.api.modelos.DisponibilidadServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface IDisponibilidadServicioRepository
        extends JpaRepository<DisponibilidadServicio, Long> {

    List<DisponibilidadServicio>
    findAllByServicioIdOrderByDiaSemanaAscHoraInicioAsc(
            Long servicioId
    );

    List<DisponibilidadServicio>
    findAllByServicioIdAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(
            Long servicioId
    );

    List<DisponibilidadServicio>
    findAllByDiaSemanaAndActivoTrueOrderByHoraInicioAsc(
            DiaSemana diaSemana
    );

    boolean existsByServicioIdAndDiaSemanaAndHoraInicioAndHoraFin(
            Long servicioId,
            DiaSemana diaSemana,
            LocalTime horaInicio,
            LocalTime horaFin
    );

    boolean existsByServicioIdAndDiaSemanaAndHoraInicioAndHoraFinAndIdNot(
            Long servicioId,
            DiaSemana diaSemana,
            LocalTime horaInicio,
            LocalTime horaFin,
            Long id
    );

    @Query("""
            SELECT COUNT(d) > 0
            FROM DisponibilidadServicio d
            WHERE d.servicio.id = :servicioId
              AND d.diaSemana = :diaSemana
              AND d.activo = true
              AND d.horaInicio < :horaFin
              AND d.horaFin > :horaInicio
            """)
    boolean existeSolapamiento(
            @Param("servicioId") Long servicioId,
            @Param("diaSemana") DiaSemana diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

    @Query("""
            SELECT COUNT(d) > 0
            FROM DisponibilidadServicio d
            WHERE d.servicio.id = :servicioId
              AND d.diaSemana = :diaSemana
              AND d.activo = true
              AND d.id <> :disponibilidadId
              AND d.horaInicio < :horaFin
              AND d.horaFin > :horaInicio
            """)
    boolean existeSolapamientoExceptuando(
            @Param("servicioId") Long servicioId,
            @Param("diaSemana") DiaSemana diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("disponibilidadId") Long disponibilidadId
    );
}