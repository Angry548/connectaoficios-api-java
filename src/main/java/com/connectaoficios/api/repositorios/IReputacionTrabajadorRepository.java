package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.enums.InsigniaReputacion;
import com.connectaoficios.api.modelos.ReputacionTrabajador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface IReputacionTrabajadorRepository
        extends JpaRepository<ReputacionTrabajador, Long> {

    Optional<ReputacionTrabajador> findByPerfilTrabajadorId(
            Long perfilTrabajadorId
    );

    boolean existsByPerfilTrabajadorId(
            Long perfilTrabajadorId
    );

    List<ReputacionTrabajador> findAllByOrderByPuntuacionRankingDesc();

    @Query(
            value = """
                    SELECT r
                    FROM ReputacionTrabajador r
                    WHERE
                        (
                            :perfilTrabajadorId IS NULL
                            OR r.perfilTrabajador.id = :perfilTrabajadorId
                        )
                        AND (
                            :insignia IS NULL
                            OR r.insignia = :insignia
                        )
                        AND (
                            :promedioMinimo IS NULL
                            OR r.promedioCalificacion >= :promedioMinimo
                        )
                        AND (
                            :promedioMaximo IS NULL
                            OR r.promedioCalificacion <= :promedioMaximo
                        )
                        AND (
                            :totalResenasMinimo IS NULL
                            OR r.totalResenas >= :totalResenasMinimo
                        )
                        AND (
                            :totalResenasMaximo IS NULL
                            OR r.totalResenas <= :totalResenasMaximo
                        )
                        AND (
                            :serviciosCompletadosMinimo IS NULL
                            OR r.serviciosCompletados >= :serviciosCompletadosMinimo
                        )
                        AND (
                            :serviciosCompletadosMaximo IS NULL
                            OR r.serviciosCompletados <= :serviciosCompletadosMaximo
                        )
                        AND (
                            :puntuacionMinima IS NULL
                            OR r.puntuacionRanking >= :puntuacionMinima
                        )
                        AND (
                            :puntuacionMaxima IS NULL
                            OR r.puntuacionRanking <= :puntuacionMaxima
                        )
                    """,
            countQuery = """
                    SELECT COUNT(r)
                    FROM ReputacionTrabajador r
                    WHERE
                        (
                            :perfilTrabajadorId IS NULL
                            OR r.perfilTrabajador.id = :perfilTrabajadorId
                        )
                        AND (
                            :insignia IS NULL
                            OR r.insignia = :insignia
                        )
                        AND (
                            :promedioMinimo IS NULL
                            OR r.promedioCalificacion >= :promedioMinimo
                        )
                        AND (
                            :promedioMaximo IS NULL
                            OR r.promedioCalificacion <= :promedioMaximo
                        )
                        AND (
                            :totalResenasMinimo IS NULL
                            OR r.totalResenas >= :totalResenasMinimo
                        )
                        AND (
                            :totalResenasMaximo IS NULL
                            OR r.totalResenas <= :totalResenasMaximo
                        )
                        AND (
                            :serviciosCompletadosMinimo IS NULL
                            OR r.serviciosCompletados >= :serviciosCompletadosMinimo
                        )
                        AND (
                            :serviciosCompletadosMaximo IS NULL
                            OR r.serviciosCompletados <= :serviciosCompletadosMaximo
                        )
                        AND (
                            :puntuacionMinima IS NULL
                            OR r.puntuacionRanking >= :puntuacionMinima
                        )
                        AND (
                            :puntuacionMaxima IS NULL
                            OR r.puntuacionRanking <= :puntuacionMaxima
                        )
                    """
    )
    Page<ReputacionTrabajador> buscarConFiltros(
            @Param("perfilTrabajadorId") Long perfilTrabajadorId,
            @Param("insignia") InsigniaReputacion insignia,
            @Param("promedioMinimo") BigDecimal promedioMinimo,
            @Param("promedioMaximo") BigDecimal promedioMaximo,
            @Param("totalResenasMinimo") Integer totalResenasMinimo,
            @Param("totalResenasMaximo") Integer totalResenasMaximo,
            @Param("serviciosCompletadosMinimo") Integer serviciosCompletadosMinimo,
            @Param("serviciosCompletadosMaximo") Integer serviciosCompletadosMaximo,
            @Param("puntuacionMinima") BigDecimal puntuacionMinima,
            @Param("puntuacionMaxima") BigDecimal puntuacionMaxima,
            Pageable pageable
    );
}