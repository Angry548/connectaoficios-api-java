package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorFiltroDTO;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorGuardar;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorModificar;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorSalida;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.enums.InsigniaReputacion;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.PerfilTrabajador;
import com.connectaoficios.api.modelos.ReputacionTrabajador;
import com.connectaoficios.api.modelos.Resena;
import com.connectaoficios.api.repositorios.IPerfilTrabajadorRepository;
import com.connectaoficios.api.repositorios.IReputacionTrabajadorRepository;
import com.connectaoficios.api.repositorios.IResenaRepository;
import com.connectaoficios.api.repositorios.ISolicitudServicioRepository;
import com.connectaoficios.api.servicios.interfaces.IReputacionTrabajadorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReputacionTrabajadorService
        implements IReputacionTrabajadorService {

    private static final int TAMANIO_MAXIMO_PAGINA = 100;

    private final IReputacionTrabajadorRepository reputacionRepository;
    private final IPerfilTrabajadorRepository perfilTrabajadorRepository;
    private final IResenaRepository resenaRepository;
    private final ISolicitudServicioRepository solicitudServicioRepository;

    public ReputacionTrabajadorService(
            IReputacionTrabajadorRepository reputacionRepository,
            IPerfilTrabajadorRepository perfilTrabajadorRepository,
            IResenaRepository resenaRepository,
            ISolicitudServicioRepository solicitudServicioRepository
    ) {
        this.reputacionRepository = reputacionRepository;
        this.perfilTrabajadorRepository = perfilTrabajadorRepository;
        this.resenaRepository = resenaRepository;
        this.solicitudServicioRepository = solicitudServicioRepository;
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

        reputacion.setPerfilTrabajador(
                perfilTrabajador
        );

        reputacion.setPromedioCalificacion(
                BigDecimal.ZERO
        );

        reputacion.setTotalResenas(
                0
        );

        reputacion.setServiciosCompletados(
                0
        );

        reputacion.setPuntuacionRanking(
                BigDecimal.ZERO
        );

        reputacion.setInsignia(
                InsigniaReputacion.NUEVO_TRABAJADOR
        );

        ReputacionTrabajador reputacionGuardada =
                reputacionRepository.save(
                        reputacion
                );

        return convertirASalida(
                reputacionGuardada
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReputacionTrabajadorSalida obtenerPorPerfilTrabajador(
            Long perfilTrabajadorId
    ) {

        ReputacionTrabajador reputacion =
                buscarPorPerfil(
                        perfilTrabajadorId
                );

        return convertirASalida(
                reputacion
        );
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
                    convertirASalida(
                            reputacion
                    );

            salida.setPosicionRanking(
                    posicion
            );

            ranking.add(
                    salida
            );

            posicion++;
        }

        return ranking;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSalida<ReputacionTrabajadorSalida> buscarConFiltros(
            ReputacionTrabajadorFiltroDTO filtro,
            int pagina,
            int tamanio
    ) {

        validarRangoPromedio(
                filtro.getPromedioMinimo(),
                filtro.getPromedioMaximo()
        );

        validarRangoEnteros(
                filtro.getTotalResenasMinimo(),
                filtro.getTotalResenasMaximo(),
                "reseñas"
        );

        validarRangoEnteros(
                filtro.getServiciosCompletadosMinimo(),
                filtro.getServiciosCompletadosMaximo(),
                "servicios completados"
        );

        validarRangoPuntuacion(
                filtro.getPuntuacionMinima(),
                filtro.getPuntuacionMaxima()
        );

        Pageable pageable =
                crearPageable(
                        pagina,
                        tamanio
                );

        Page<ReputacionTrabajadorSalida> resultado =
                reputacionRepository
                        .buscarConFiltros(
                                filtro.getPerfilTrabajadorId(),
                                filtro.getInsignia(),
                                filtro.getPromedioMinimo(),
                                filtro.getPromedioMaximo(),
                                filtro.getTotalResenasMinimo(),
                                filtro.getTotalResenasMaximo(),
                                filtro.getServiciosCompletadosMinimo(),
                                filtro.getServiciosCompletadosMaximo(),
                                filtro.getPuntuacionMinima(),
                                filtro.getPuntuacionMaxima(),
                                pageable
                        )
                        .map(this::convertirASalida);

        long posicionInicial =
                (long) pageable.getPageNumber()
                        * pageable.getPageSize();

        for (int i = 0; i < resultado.getContent().size(); i++) {

            long posicion =
                    posicionInicial + i + 1;

            if (posicion <= Integer.MAX_VALUE) {
                resultado.getContent()
                        .get(i)
                        .setPosicionRanking(
                                (int) posicion
                        );
            }
        }

        return PaginaSalida.desde(
                resultado
        );
    }

    @Override
    @Transactional
    public ReputacionTrabajadorSalida modificar(
            Long perfilTrabajadorId,
            ReputacionTrabajadorModificar reputacionModificar
    ) {

        ReputacionTrabajador reputacion =
                buscarPorPerfil(
                        perfilTrabajadorId
                );

        reputacion.setServiciosCompletados(
                reputacionModificar.getServiciosCompletados()
        );

        calcularPuntuacion(
                reputacion
        );

        reputacion.setInsignia(
                determinarInsignia(
                        reputacion
                )
        );

        ReputacionTrabajador reputacionActualizada =
                reputacionRepository.save(
                        reputacion
                );

        return convertirASalida(
                reputacionActualizada
        );
    }

    @Override
    @Transactional
    public ReputacionTrabajadorSalida recalcular(
            Long perfilTrabajadorId
    ) {

        PerfilTrabajador perfilTrabajador =
                perfilTrabajadorRepository
                        .findById(perfilTrabajadorId)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró el perfil del trabajador"
                                )
                        );

        ReputacionTrabajador reputacion =
                reputacionRepository
                        .findByPerfilTrabajadorId(
                                perfilTrabajadorId
                        )
                        .orElseGet(() -> {

                            ReputacionTrabajador nuevaReputacion =
                                    new ReputacionTrabajador();

                            nuevaReputacion.setPerfilTrabajador(
                                    perfilTrabajador
                            );

                            nuevaReputacion.setPromedioCalificacion(
                                    BigDecimal.ZERO
                            );

                            nuevaReputacion.setTotalResenas(
                                    0
                            );

                            nuevaReputacion.setServiciosCompletados(
                                    0
                            );

                            nuevaReputacion.setPuntuacionRanking(
                                    BigDecimal.ZERO
                            );

                            nuevaReputacion.setInsignia(
                                    InsigniaReputacion.NUEVO_TRABAJADOR
                            );

                            return reputacionRepository.save(
                                    nuevaReputacion
                            );
                        });

        List<Resena> resenas =
                resenaRepository
                        .findByPerfilTrabajadorId(
                                perfilTrabajadorId
                        );

        int totalResenas =
                resenas.size();

        BigDecimal promedioCalificacion =
                BigDecimal.ZERO;

        if (!resenas.isEmpty()) {

            int sumaCalificaciones =
                    resenas.stream()
                            .mapToInt(
                                    Resena::getCalificacion
                            )
                            .sum();

            promedioCalificacion =
                    BigDecimal.valueOf(
                                    sumaCalificaciones
                            )
                            .divide(
                                    BigDecimal.valueOf(
                                            totalResenas
                                    ),
                                    2,
                                    RoundingMode.HALF_UP
                            );
        }

        long totalServiciosCompletados =
                solicitudServicioRepository
                        .countByTrabajadorIdAndEstado(
                                perfilTrabajador
                                        .getTrabajadorId(),
                                EstadoSolicitud.COMPLETADA
                        );

        int serviciosCompletados =
                Math.toIntExact(
                        totalServiciosCompletados
                );

        reputacion.setTotalResenas(
                totalResenas
        );

        reputacion.setPromedioCalificacion(
                promedioCalificacion
        );

        reputacion.setServiciosCompletados(
                serviciosCompletados
        );

        calcularPuntuacion(
                reputacion
        );

        reputacion.setInsignia(
                determinarInsignia(
                        reputacion
                )
        );

        ReputacionTrabajador reputacionActualizada =
                reputacionRepository.save(
                        reputacion
                );

        return convertirASalida(
                reputacionActualizada
        );
    }

    private Pageable crearPageable(
            int pagina,
            int tamanio
    ) {

        int paginaSegura =
                Math.max(
                        pagina,
                        0
                );

        int tamanioSeguro =
                Math.max(
                        1,
                        Math.min(
                                tamanio,
                                TAMANIO_MAXIMO_PAGINA
                        )
                );

        return PageRequest.of(
                paginaSegura,
                tamanioSeguro,
                Sort.by(
                        Sort.Direction.DESC,
                        "puntuacionRanking"
                ).and(
                        Sort.by(
                                Sort.Direction.ASC,
                                "id"
                        )
                )
        );
    }

    private void validarRangoPromedio(
            BigDecimal minimo,
            BigDecimal maximo
    ) {

        BigDecimal cero =
                BigDecimal.ZERO;

        BigDecimal cinco =
                BigDecimal.valueOf(5);

        if (minimo != null
                && (minimo.compareTo(cero) < 0
                || minimo.compareTo(cinco) > 0)) {

            throw new ReglaNegocioException(
                    "El promedio mínimo debe estar entre 0 y 5"
            );
        }

        if (maximo != null
                && (maximo.compareTo(cero) < 0
                || maximo.compareTo(cinco) > 0)) {

            throw new ReglaNegocioException(
                    "El promedio máximo debe estar entre 0 y 5"
            );
        }

        if (minimo != null
                && maximo != null
                && minimo.compareTo(maximo) > 0) {

            throw new ReglaNegocioException(
                    "El promedio mínimo no puede ser mayor que el promedio máximo"
            );
        }
    }

    private void validarRangoEnteros(
            Integer minimo,
            Integer maximo,
            String nombre
    ) {

        if (minimo != null
                && minimo < 0) {

            throw new ReglaNegocioException(
                    "El valor mínimo de "
                            + nombre
                            + " no puede ser negativo"
            );
        }

        if (maximo != null
                && maximo < 0) {

            throw new ReglaNegocioException(
                    "El valor máximo de "
                            + nombre
                            + " no puede ser negativo"
            );
        }

        if (minimo != null
                && maximo != null
                && minimo > maximo) {

            throw new ReglaNegocioException(
                    "El valor mínimo de "
                            + nombre
                            + " no puede ser mayor que el máximo"
            );
        }
    }

    private void validarRangoPuntuacion(
            BigDecimal minimo,
            BigDecimal maximo
    ) {

        if (minimo != null
                && minimo.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new ReglaNegocioException(
                    "La puntuación mínima no puede ser negativa"
            );
        }

        if (maximo != null
                && maximo.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new ReglaNegocioException(
                    "La puntuación máxima no puede ser negativa"
            );
        }

        if (minimo != null
                && maximo != null
                && minimo.compareTo(
                maximo
        ) > 0) {

            throw new ReglaNegocioException(
                    "La puntuación mínima no puede ser mayor que la puntuación máxima"
            );
        }
    }

    private ReputacionTrabajador buscarPorPerfil(
            Long perfilTrabajadorId
    ) {

        return reputacionRepository
                .findByPerfilTrabajadorId(
                        perfilTrabajadorId
                )
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
                BigDecimal.valueOf(
                                totalResenas
                        )
                        .multiply(
                                BigDecimal.valueOf(2)
                        );

        BigDecimal puntosServicios =
                BigDecimal.valueOf(
                        serviciosCompletados
                );

        BigDecimal puntuacion =
                puntosCalificacion
                        .add(
                                puntosResenas
                        )
                        .add(
                                puntosServicios
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        reputacion.setPuntuacionRanking(
                puntuacion
        );
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

        salida.setId(
                reputacion.getId()
        );

        salida.setPerfilTrabajadorId(
                reputacion
                        .getPerfilTrabajador()
                        .getId()
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