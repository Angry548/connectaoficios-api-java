package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.estadistica.EstadisticasSalida;
import com.connectaoficios.api.enums.EstadoReporte;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.servicios.interfaces.IEstadisticasService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EstadisticasService
        implements IEstadisticasService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public EstadisticasSalida obtenerEstadisticas(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    ) {

        if (
                fechaDesde != null &&
                        fechaHasta != null &&
                        fechaDesde.isAfter(fechaHasta)
        ) {
            throw new ReglaNegocioException(
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
        }

        EstadisticasSalida salida =
                new EstadisticasSalida();

        salida.setTotalServicios(
                contarServicios(
                        fechaDesde,
                        fechaHasta
                )
        );

        salida.setTotalSolicitudes(
                contarSolicitudes(
                        null,
                        fechaDesde,
                        fechaHasta
                )
        );

        salida.setTotalSolicitudesCompletadas(
                contarSolicitudes(
                        EstadoSolicitud.COMPLETADA,
                        fechaDesde,
                        fechaHasta
                )
        );

        salida.setTotalReportes(
                contarReportes(
                        null,
                        fechaDesde,
                        fechaHasta
                )
        );

        salida.setSolicitudesPendientes(
                contarSolicitudes(
                        EstadoSolicitud.PENDIENTE,
                        fechaDesde,
                        fechaHasta
                )
        );

        salida.setSolicitudesAceptadas(
                contarSolicitudes(
                        EstadoSolicitud.ACEPTADA,
                        fechaDesde,
                        fechaHasta
                )
        );

        salida.setSolicitudesEnProceso(
                contarSolicitudes(
                        EstadoSolicitud.EN_PROCESO,
                        fechaDesde,
                        fechaHasta
                )
        );

        salida.setSolicitudesRechazadas(
                contarSolicitudes(
                        EstadoSolicitud.RECHAZADA,
                        fechaDesde,
                        fechaHasta
                )
        );

        salida.setSolicitudesCanceladas(
                contarSolicitudes(
                        EstadoSolicitud.CANCELADA,
                        fechaDesde,
                        fechaHasta
                )
        );

        salida.setReportesPendientes(
                contarReportes(
                        EstadoReporte.PENDIENTE,
                        fechaDesde,
                        fechaHasta
                )
        );

        salida.setReportesEnRevision(
                contarReportes(
                        EstadoReporte.EN_REVISION,
                        fechaDesde,
                        fechaHasta
                )
        );

        salida.setReportesResueltos(
                contarReportes(
                        EstadoReporte.RESUELTO,
                        fechaDesde,
                        fechaHasta
                )
        );

        salida.setReportesRechazados(
                contarReportes(
                        EstadoReporte.RECHAZADO,
                        fechaDesde,
                        fechaHasta
                )
        );

        return salida;
    }

    private long contarServicios(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    ) {

        StringBuilder jpql =
                new StringBuilder(
                        """
                        SELECT COUNT(s)
                        FROM Servicio s
                        WHERE s.eliminado = false
                        """
                );

        if (fechaDesde != null) {
            jpql.append(
                    " AND s.fechaCreacion >= :fechaDesde"
            );
        }

        if (fechaHasta != null) {
            jpql.append(
                    " AND s.fechaCreacion <= :fechaHasta"
            );
        }

        TypedQuery<Long> query =
                entityManager.createQuery(
                        jpql.toString(),
                        Long.class
                );

        aplicarFechas(
                query,
                fechaDesde,
                fechaHasta
        );

        return query.getSingleResult();
    }

    private long contarSolicitudes(
            EstadoSolicitud estado,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    ) {

        StringBuilder jpql =
                new StringBuilder(
                        """
                        SELECT COUNT(s)
                        FROM SolicitudServicio s
                        WHERE 1 = 1
                        """
                );

        if (estado != null) {
            jpql.append(
                    " AND s.estado = :estado"
            );
        }

        if (fechaDesde != null) {
            jpql.append(
                    " AND s.fechaCreacion >= :fechaDesde"
            );
        }

        if (fechaHasta != null) {
            jpql.append(
                    " AND s.fechaCreacion <= :fechaHasta"
            );
        }

        TypedQuery<Long> query =
                entityManager.createQuery(
                        jpql.toString(),
                        Long.class
                );

        if (estado != null) {
            query.setParameter(
                    "estado",
                    estado
            );
        }

        aplicarFechas(
                query,
                fechaDesde,
                fechaHasta
        );

        return query.getSingleResult();
    }

    private long contarReportes(
            EstadoReporte estado,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    ) {

        StringBuilder jpql =
                new StringBuilder(
                        """
                        SELECT COUNT(r)
                        FROM Reporte r
                        WHERE 1 = 1
                        """
                );

        if (estado != null) {
            jpql.append(
                    " AND r.estado = :estado"
            );
        }

        if (fechaDesde != null) {
            jpql.append(
                    " AND r.fechaCreacion >= :fechaDesde"
            );
        }

        if (fechaHasta != null) {
            jpql.append(
                    " AND r.fechaCreacion <= :fechaHasta"
            );
        }

        TypedQuery<Long> query =
                entityManager.createQuery(
                        jpql.toString(),
                        Long.class
                );

        if (estado != null) {
            query.setParameter(
                    "estado",
                    estado
            );
        }

        aplicarFechas(
                query,
                fechaDesde,
                fechaHasta
        );

        return query.getSingleResult();
    }

    private void aplicarFechas(
            TypedQuery<?> query,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    ) {

        if (fechaDesde != null) {
            query.setParameter(
                    "fechaDesde",
                    fechaDesde
            );
        }

        if (fechaHasta != null) {
            query.setParameter(
                    "fechaHasta",
                    fechaHasta
            );
        }
    }
}