package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.PerfilTrabajador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPerfilTrabajadorRepository
        extends JpaRepository<PerfilTrabajador, Long> {

    Optional<PerfilTrabajador> findByTrabajadorId(
            Integer trabajadorId
    );

    boolean existsByTrabajadorId(
            Integer trabajadorId
    );

    @Query(
            value = """
                    SELECT p
                    FROM PerfilTrabajador p
                    LEFT JOIN p.zonaPrincipal z
                    WHERE
                        (
                            :texto IS NULL
                            OR LOWER(p.oficioPrincipal)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(p.descripcionProfesional)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(p.experienciaLaboral)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                        )
                        AND (
                            :zonaPrincipalId IS NULL
                            OR z.id = :zonaPrincipalId
                        )
                        AND (
                            :departamento IS NULL
                            OR LOWER(z.departamento)
                                LIKE LOWER(CONCAT('%', :departamento, '%'))
                        )
                        AND (
                            :municipio IS NULL
                            OR LOWER(z.municipio)
                                LIKE LOWER(CONCAT('%', :municipio, '%'))
                        )
                        AND (
                            :porcentajeMinimo IS NULL
                            OR p.porcentajeCompletitud >= :porcentajeMinimo
                        )
                        AND (
                            :porcentajeMaximo IS NULL
                            OR p.porcentajeCompletitud <= :porcentajeMaximo
                        )
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM PerfilTrabajador p
                    LEFT JOIN p.zonaPrincipal z
                    WHERE
                        (
                            :texto IS NULL
                            OR LOWER(p.oficioPrincipal)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(p.descripcionProfesional)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(p.experienciaLaboral)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                        )
                        AND (
                            :zonaPrincipalId IS NULL
                            OR z.id = :zonaPrincipalId
                        )
                        AND (
                            :departamento IS NULL
                            OR LOWER(z.departamento)
                                LIKE LOWER(CONCAT('%', :departamento, '%'))
                        )
                        AND (
                            :municipio IS NULL
                            OR LOWER(z.municipio)
                                LIKE LOWER(CONCAT('%', :municipio, '%'))
                        )
                        AND (
                            :porcentajeMinimo IS NULL
                            OR p.porcentajeCompletitud >= :porcentajeMinimo
                        )
                        AND (
                            :porcentajeMaximo IS NULL
                            OR p.porcentajeCompletitud <= :porcentajeMaximo
                        )
                    """
    )
    Page<PerfilTrabajador> buscarConFiltros(
            @Param("texto") String texto,
            @Param("zonaPrincipalId") Long zonaPrincipalId,
            @Param("departamento") String departamento,
            @Param("municipio") String municipio,
            @Param("porcentajeMinimo") Integer porcentajeMinimo,
            @Param("porcentajeMaximo") Integer porcentajeMaximo,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM PerfilTrabajador p
            LEFT JOIN p.zonaPrincipal z
            WHERE
                LOWER(p.oficioPrincipal)
                    LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(p.descripcionProfesional)
                    LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(z.departamento)
                    LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(z.municipio)
                    LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(z.localidad)
                    LIKE LOWER(CONCAT('%', :texto, '%'))
            ORDER BY p.oficioPrincipal ASC
            """)
    List<PerfilTrabajador> buscarParaAutocomplete(
            @Param("texto") String texto,
            Pageable pageable
    );
}