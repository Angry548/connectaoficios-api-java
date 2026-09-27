package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICategoriaRepository
        extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(
            String nombre,
            Long id
    );

    List<Categoria> findAllByOrderByNombreAsc();

    List<Categoria> findAllByActivoTrueOrderByNombreAsc();

    @Query(
            value = """
                    SELECT c
                    FROM Categoria c
                    WHERE
                        (
                            :texto IS NULL
                            OR LOWER(c.nombre)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(c.descripcion)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                        )
                        AND (
                            :activo IS NULL
                            OR c.activo = :activo
                        )
                    """,
            countQuery = """
                    SELECT COUNT(c)
                    FROM Categoria c
                    WHERE
                        (
                            :texto IS NULL
                            OR LOWER(c.nombre)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(c.descripcion)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                        )
                        AND (
                            :activo IS NULL
                            OR c.activo = :activo
                        )
                    """
    )
    Page<Categoria> buscarConFiltros(
            @Param("texto") String texto,
            @Param("activo") Boolean activo,
            Pageable pageable
    );

    @Query("""
            SELECT c
            FROM Categoria c
            WHERE c.activo = true
              AND (
                    LOWER(c.nombre)
                        LIKE LOWER(CONCAT('%', :texto, '%'))
                    OR LOWER(c.descripcion)
                        LIKE LOWER(CONCAT('%', :texto, '%'))
              )
            ORDER BY c.nombre ASC
            """)
    List<Categoria> buscarParaAutocomplete(
            @Param("texto") String texto,
            Pageable pageable
    );
}