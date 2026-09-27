package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.PlanPromocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface IPlanPromocionRepository
        extends JpaRepository<PlanPromocion, Long> {

    Page<PlanPromocion> findByActivo(
            Boolean activo,
            Pageable pageable
    );

    Optional<PlanPromocion> findByNombreIgnoreCase(
            String nombre
    );

    boolean existsByNombreIgnoreCase(
            String nombre
    );

    boolean existsByNombreIgnoreCaseAndIdNot(
            String nombre,
            Long id
    );

    @Query(
            value = """
                    SELECT p
                    FROM PlanPromocion p
                    WHERE
                        (
                            :texto IS NULL
                            OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))
                        )
                        AND (
                            :activo IS NULL
                            OR p.activo = :activo
                        )
                        AND (
                            :duracionMinima IS NULL
                            OR p.duracionDias >= :duracionMinima
                        )
                        AND (
                            :duracionMaxima IS NULL
                            OR p.duracionDias <= :duracionMaxima
                        )
                        AND (
                            :precioMinimo IS NULL
                            OR p.precio >= :precioMinimo
                        )
                        AND (
                            :precioMaximo IS NULL
                            OR p.precio <= :precioMaximo
                        )
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM PlanPromocion p
                    WHERE
                        (
                            :texto IS NULL
                            OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))
                        )
                        AND (
                            :activo IS NULL
                            OR p.activo = :activo
                        )
                        AND (
                            :duracionMinima IS NULL
                            OR p.duracionDias >= :duracionMinima
                        )
                        AND (
                            :duracionMaxima IS NULL
                            OR p.duracionDias <= :duracionMaxima
                        )
                        AND (
                            :precioMinimo IS NULL
                            OR p.precio >= :precioMinimo
                        )
                        AND (
                            :precioMaximo IS NULL
                            OR p.precio <= :precioMaximo
                        )
                    """
    )
    Page<PlanPromocion> buscarConFiltros(
            @Param("texto") String texto,
            @Param("activo") Boolean activo,
            @Param("duracionMinima") Integer duracionMinima,
            @Param("duracionMaxima") Integer duracionMaxima,
            @Param("precioMinimo") BigDecimal precioMinimo,
            @Param("precioMaximo") BigDecimal precioMaximo,
            Pageable pageable
    );
}