package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.enums.DiaSemana;
import com.connectaoficios.api.enums.EstadoServicio;
import com.connectaoficios.api.modelos.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface IServicioRepository
        extends JpaRepository<Servicio, Long> {

    Optional<Servicio> findByIdAndEliminadoFalse(Long id);

    List<Servicio> findAllByEliminadoFalseOrderByFechaCreacionDesc();

    List<Servicio> findAllByEliminadoFalseAndEstadoOrderByFechaCreacionDesc(
            EstadoServicio estado
    );

    List<Servicio> findAllByPerfilTrabajadorIdAndEliminadoFalseOrderByFechaCreacionDesc(
            Long perfilTrabajadorId
    );

    List<Servicio> findAllByCategoriaIdAndEliminadoFalseOrderByFechaCreacionDesc(
            Long categoriaId
    );

    @Query("""
            SELECT DISTINCT s
            FROM Servicio s
            JOIN s.zonasCobertura z
            WHERE s.eliminado = false
              AND z.id = :zonaId
            ORDER BY s.fechaCreacion DESC
            """)
    List<Servicio> findAllByZonaCoberturaId(@Param("zonaId") Long zonaId);

    @Query("""
            SELECT DISTINCT s
            FROM Servicio s
            LEFT JOIN s.zonasCobertura z
            LEFT JOIN DisponibilidadServicio d
                ON d.servicio.id = s.id
            WHERE s.eliminado = false
              AND (:categoriaId IS NULL OR s.categoria.id = :categoriaId)
              AND (:zonaId IS NULL OR z.id = :zonaId)
              AND (:diaSemana IS NULL
                   OR (d.diaSemana = :diaSemana AND d.activo = true))
              AND (:tarifaMinima IS NULL OR s.tarifaMinima >= :tarifaMinima)
              AND (:tarifaMaxima IS NULL OR s.tarifaMinima <= :tarifaMaxima)
            ORDER BY s.fechaCreacion DESC
            """)
    List<Servicio> buscarConFiltros(
            @Param("categoriaId") Long categoriaId,
            @Param("zonaId") Long zonaId,
            @Param("diaSemana") DiaSemana diaSemana,
            @Param("tarifaMinima") BigDecimal tarifaMinima,
            @Param("tarifaMaxima") BigDecimal tarifaMaxima
    );
}