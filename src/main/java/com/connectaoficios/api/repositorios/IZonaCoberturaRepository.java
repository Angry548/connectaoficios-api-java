package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.ZonaCobertura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IZonaCoberturaRepository
        extends JpaRepository<ZonaCobertura, Long> {

    boolean existsByDepartamentoIgnoreCaseAndMunicipioIgnoreCase(
            String departamento,
            String municipio
    );

    boolean existsByDepartamentoIgnoreCaseAndMunicipioIgnoreCaseAndIdNot(
            String departamento,
            String municipio,
            Long id
    );

    List<ZonaCobertura> findAllByOrderByDepartamentoAscMunicipioAsc();

    List<ZonaCobertura> findAllByActivoTrueOrderByDepartamentoAscMunicipioAsc();

    List<ZonaCobertura> findAllByDepartamentoIgnoreCaseOrderByMunicipioAsc(
            String departamento
    );

    List<ZonaCobertura> findAllByMunicipioIgnoreCaseOrderByDepartamentoAsc(
            String municipio
    );

    @Query(
            value = """
                    SELECT z
                    FROM ZonaCobertura z
                    WHERE
                        (
                            :texto IS NULL
                            OR LOWER(z.departamento)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(z.municipio)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(z.localidad)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
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
                            :localidad IS NULL
                            OR LOWER(z.localidad)
                                LIKE LOWER(CONCAT('%', :localidad, '%'))
                        )
                        AND (
                            :activo IS NULL
                            OR z.activo = :activo
                        )
                    """,
            countQuery = """
                    SELECT COUNT(z)
                    FROM ZonaCobertura z
                    WHERE
                        (
                            :texto IS NULL
                            OR LOWER(z.departamento)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(z.municipio)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(z.localidad)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
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
                            :localidad IS NULL
                            OR LOWER(z.localidad)
                                LIKE LOWER(CONCAT('%', :localidad, '%'))
                        )
                        AND (
                            :activo IS NULL
                            OR z.activo = :activo
                        )
                    """
    )
    Page<ZonaCobertura> buscarConFiltros(
            @Param("texto") String texto,
            @Param("departamento") String departamento,
            @Param("municipio") String municipio,
            @Param("localidad") String localidad,
            @Param("activo") Boolean activo,
            Pageable pageable
    );

    @Query("""
            SELECT z
            FROM ZonaCobertura z
            WHERE z.activo = true
              AND (
                    LOWER(z.departamento)
                        LIKE LOWER(CONCAT('%', :texto, '%'))
                    OR LOWER(z.municipio)
                        LIKE LOWER(CONCAT('%', :texto, '%'))
                    OR LOWER(z.localidad)
                        LIKE LOWER(CONCAT('%', :texto, '%'))
              )
            ORDER BY z.departamento ASC, z.municipio ASC, z.localidad ASC
            """)
    List<ZonaCobertura> buscarParaAutocomplete(
            @Param("texto") String texto,
            Pageable pageable
    );
}