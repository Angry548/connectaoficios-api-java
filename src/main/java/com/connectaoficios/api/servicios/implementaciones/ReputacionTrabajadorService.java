package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorGuardar;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorModificar;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorSalida;
import com.connectaoficios.api.enums.InsigniaReputacion;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.PerfilTrabajador;
import com.connectaoficios.api.modelos.ReputacionTrabajador;
import com.connectaoficios.api.repositorios.IPerfilTrabajadorRepository;
import com.connectaoficios.api.repositorios.IReputacionTrabajadorRepository;
import com.connectaoficios.api.servicios.interfaces.IReputacionTrabajadorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReputacionTrabajadorService
        implements IReputacionTrabajadorService {

    private final IReputacionTrabajadorRepository reputacionRepository;
    private final IPerfilTrabajadorRepository perfilTrabajadorRepository;

    public ReputacionTrabajadorService(
            IReputacionTrabajadorRepository reputacionRepository,
            IPerfilTrabajadorRepository perfilTrabajadorRepository
    ) {
        this.reputacionRepository = reputacionRepository;
        this.perfilTrabajadorRepository = perfilTrabajadorRepository;
    }

    @Override
    @Transactional
    public ReputacionTrabajadorSalida guardar(
            ReputacionTrabajadorGuardar reputacionGuardar
    ) {

        Long perfilTrabajadorId =
                reputacionGuardar.getPerfilTrabajadorId();

        if (reputacionRepository
                .existsByPerfilTrabajadorId(perfilTrabajadorId)) {

            throw new ReglaNegocioException(
                    "El perfil del trabajador ya posee una reputación"
            );
        }

        PerfilTrabajador perfilTrabajador =
                perfilTrabajadorRepository
                        .findById(perfilTrabajadorId)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró el perfil del trabajador"
                                )
                        );

        ReputacionTrabajador reputacion =
                new ReputacionTrabajador();

        reputacion.setPerfilTrabajador(perfilTrabajador);
        reputacion.setPromedioCalificacion(BigDecimal.ZERO);
        reputacion.setTotalResenas(0);
        reputacion.setServiciosCompletados(0);
        reputacion.setPuntuacionRanking(BigDecimal.ZERO);
        reputacion.setInsignia(
                InsigniaReputacion.NUEVO_TRABAJADOR
        );

        ReputacionTrabajador reputacionGuardada =
                reputacionRepository.save(reputacion);

        return convertirASalida(reputacionGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public ReputacionTrabajadorSalida obtenerPorPerfilTrabajador(
            Long perfilTrabajadorId
    ) {

        ReputacionTrabajador reputacion =
                buscarPorPerfil(perfilTrabajadorId);

        return convertirASalida(reputacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReputacionTrabajadorSalida> obtenerRanking() {

        List<ReputacionTrabajador> reputaciones =
                reputacionRepository
                        .findAllByOrderByPuntuacionRankingDesc();

        List<ReputacionTrabajadorSalida> ranking =
                new ArrayList<>();

        int posicion = 1;

        for (ReputacionTrabajador reputacion : reputaciones) {

            ReputacionTrabajadorSalida salida =
                    convertirASalida(reputacion);

            salida.setPosicionRanking(posicion);

            ranking.add(salida);

            posicion++;
        }

        return ranking;
    }

    @Override
    @Transactional
    public ReputacionTrabajadorSalida modificar(
            Long perfilTrabajadorId,
            ReputacionTrabajadorModificar reputacionModificar
    ) {

        ReputacionTrabajador reputacion =
                buscarPorPerfil(perfilTrabajadorId);

        reputacion.setServiciosCompletados(
                reputacionModificar.getServiciosCompletados()
        );

        calcularPuntuacion(reputacion);

        reputacion.setInsignia(
                determinarInsignia(reputacion)
        );

        ReputacionTrabajador reputacionActualizada =
                reputacionRepository.save(reputacion);

        return convertirASalida(reputacionActualizada);
    }

    @Override
    @Transactional
    public ReputacionTrabajadorSalida recalcular(
            Long perfilTrabajadorId
    ) {

        ReputacionTrabajador reputacion =
                buscarPorPerfil(perfilTrabajadorId);

        calcularPuntuacion(reputacion);

        reputacion.setInsignia(
                determinarInsignia(reputacion)
        );

        ReputacionTrabajador reputacionActualizada =
                reputacionRepository.save(reputacion);

        return convertirASalida(reputacionActualizada);
    }

    private ReputacionTrabajador buscarPorPerfil(
            Long perfilTrabajadorId
    ) {

        return reputacionRepository
                .findByPerfilTrabajadorId(perfilTrabajadorId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró la reputación del trabajador"
                        )
                );
    }

    private void calcularPuntuacion(
            ReputacionTrabajador reputacion
    ) {

        BigDecimal promedio =
                reputacion.getPromedioCalificacion() != null
                        ? reputacion.getPromedioCalificacion()
                        : BigDecimal.ZERO;

        int totalResenas =
                reputacion.getTotalResenas() != null
                        ? reputacion.getTotalResenas()
                        : 0;

        int serviciosCompletados =
                reputacion.getServiciosCompletados() != null
                        ? reputacion.getServiciosCompletados()
                        : 0;

        BigDecimal puntosCalificacion =
                promedio.multiply(
                        BigDecimal.valueOf(10)
                );

        BigDecimal puntosResenas =
                BigDecimal.valueOf(totalResenas)
                        .multiply(BigDecimal.valueOf(2));

        BigDecimal puntosServicios =
                BigDecimal.valueOf(serviciosCompletados);

        BigDecimal puntuacion =
                puntosCalificacion
                        .add(puntosResenas)
                        .add(puntosServicios)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        reputacion.setPuntuacionRanking(puntuacion);
    }

    private InsigniaReputacion determinarInsignia(
            ReputacionTrabajador reputacion
    ) {

        BigDecimal promedio =
                reputacion.getPromedioCalificacion() != null
                        ? reputacion.getPromedioCalificacion()
                        : BigDecimal.ZERO;

        int totalResenas =
                reputacion.getTotalResenas() != null
                        ? reputacion.getTotalResenas()
                        : 0;

        int serviciosCompletados =
                reputacion.getServiciosCompletados() != null
                        ? reputacion.getServiciosCompletados()
                        : 0;

        if (promedio.compareTo(
                BigDecimal.valueOf(4.8)
        ) >= 0
                && totalResenas >= 20
                && serviciosCompletados >= 30) {

            return InsigniaReputacion.TOP_PLATAFORMA;
        }

        if (promedio.compareTo(
                BigDecimal.valueOf(4.5)
        ) >= 0
                && totalResenas >= 10) {

            return InsigniaReputacion.MEJOR_VALORADO;
        }

        if (serviciosCompletados >= 10
                && totalResenas >= 5) {

            return InsigniaReputacion.TRABAJADOR_CONFIABLE;
        }

        return InsigniaReputacion.NUEVO_TRABAJADOR;
    }

    private ReputacionTrabajadorSalida convertirASalida(
            ReputacionTrabajador reputacion
    ) {

        ReputacionTrabajadorSalida salida =
                new ReputacionTrabajadorSalida();

        salida.setId(reputacion.getId());

        salida.setPerfilTrabajadorId(
                reputacion.getPerfilTrabajador().getId()
        );

        salida.setPromedioCalificacion(
                reputacion.getPromedioCalificacion()
        );

        salida.setTotalResenas(
                reputacion.getTotalResenas()
        );

        salida.setServiciosCompletados(
                reputacion.getServiciosCompletados()
        );

        salida.setPuntuacionRanking(
                reputacion.getPuntuacionRanking()
        );

        salida.setPosicionRanking(
                reputacion.getPosicionRanking()
        );

        salida.setInsignia(
                reputacion.getInsignia()
        );

        salida.setFechaActualizacion(
                reputacion.getFechaActualizacion()
        );

        return salida;
    }
}