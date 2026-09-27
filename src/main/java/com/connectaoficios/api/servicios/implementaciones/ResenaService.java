package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.resena.ResenaFiltroDTO;
import com.connectaoficios.api.dtos.resena.ResenaGuardar;
import com.connectaoficios.api.dtos.resena.ResenaSalida;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.PerfilTrabajador;
import com.connectaoficios.api.modelos.Resena;
import com.connectaoficios.api.modelos.Servicio;
import com.connectaoficios.api.modelos.SolicitudServicio;
import com.connectaoficios.api.repositorios.IResenaRepository;
import com.connectaoficios.api.repositorios.ISolicitudServicioRepository;
import com.connectaoficios.api.servicios.interfaces.IReputacionTrabajadorService;
import com.connectaoficios.api.servicios.interfaces.IResenaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResenaService implements IResenaService {

    private static final int TAMANIO_MAXIMO_PAGINA = 100;

    private final IResenaRepository resenaRepository;
    private final ISolicitudServicioRepository solicitudRepository;
    private final IReputacionTrabajadorService reputacionService;

    public ResenaService(
            IResenaRepository resenaRepository,
            ISolicitudServicioRepository solicitudRepository,
            IReputacionTrabajadorService reputacionService
    ) {
        this.resenaRepository = resenaRepository;
        this.solicitudRepository = solicitudRepository;
        this.reputacionService = reputacionService;
    }

    @Override
    @Transactional
    public ResenaSalida guardar(
            ResenaGuardar dto,
            Integer clienteId
    ) {

        if (clienteId == null) {
            throw new ReglaNegocioException(
                    "No se pudo identificar al cliente autenticado"
            );
        }

        SolicitudServicio solicitud =
                solicitudRepository
                        .findById(dto.getSolicitudId())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró la solicitud de servicio"
                                )
                        );

        if (!clienteId.equals(
                solicitud.getClienteId()
        )) {

            throw new ReglaNegocioException(
                    "Solo el cliente de la solicitud puede registrar la reseña"
            );
        }

        if (solicitud.getEstado()
                != EstadoSolicitud.COMPLETADA) {

            throw new ReglaNegocioException(
                    "Solo se pueden calificar servicios completados"
            );
        }

        if (resenaRepository
                .existsBySolicitudId(
                        solicitud.getId()
                )) {

            throw new ReglaNegocioException(
                    "La solicitud ya posee una reseña"
            );
        }

        Servicio servicio =
                solicitud.getServicio();

        PerfilTrabajador perfil =
                servicio.getPerfilTrabajador();

        Resena resena =
                new Resena();

        resena.setSolicitud(
                solicitud
        );

        resena.setServicio(
                servicio
        );

        resena.setPerfilTrabajador(
                perfil
        );

        resena.setClienteId(
                clienteId
        );

        resena.setCalificacion(
                dto.getCalificacion()
        );

        resena.setComentario(
                normalizarComentario(
                        dto.getComentario()
                )
        );

        Resena guardada =
                resenaRepository.save(
                        resena
                );

        reputacionService.recalcular(
                perfil.getId()
        );

        return convertirASalida(
                guardada
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ResenaSalida obtenerPorId(
            Long id
    ) {

        Resena resena =
                resenaRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró la reseña"
                                )
                        );

        return convertirASalida(
                resena
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResenaSalida> obtenerPorPerfilTrabajador(
            Long perfilTrabajadorId
    ) {

        List<Resena> resenas =
                resenaRepository
                        .findByPerfilTrabajadorIdOrderByFechaDesc(
                                perfilTrabajadorId
                        );

        List<ResenaSalida> salida =
                new ArrayList<>();

        for (Resena resena : resenas) {

            salida.add(
                    convertirASalida(
                            resena
                    )
            );
        }

        return salida;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSalida<ResenaSalida> buscarConFiltros(
            ResenaFiltroDTO filtro,
            int pagina,
            int tamanio
    ) {

        validarCalificacion(
                filtro.getCalificacion()
        );

        validarRangoFechas(
                filtro.getFechaDesde(),
                filtro.getFechaHasta()
        );

        String texto =
                normalizarTexto(
                        filtro.getTexto()
                );

        Pageable pageable =
                crearPageable(
                        pagina,
                        tamanio
                );

        Page<ResenaSalida> resultado =
                resenaRepository
                        .buscarConFiltros(
                                filtro.getPerfilTrabajadorId(),
                                filtro.getServicioId(),
                                filtro.getClienteId(),
                                filtro.getCalificacion(),
                                texto,
                                filtro.getFechaDesde(),
                                filtro.getFechaHasta(),
                                pageable
                        )
                        .map(this::convertirASalida);

        return PaginaSalida.desde(
                resultado
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
                        "fecha"
                )
        );
    }

    private void validarCalificacion(
            Integer calificacion
    ) {

        if (calificacion != null
                && (calificacion < 1
                || calificacion > 5)) {

            throw new ReglaNegocioException(
                    "La calificación debe estar entre 1 y 5"
            );
        }
    }

    private void validarRangoFechas(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    ) {

        if (fechaDesde != null
                && fechaHasta != null
                && fechaDesde.isAfter(fechaHasta)) {

            throw new ReglaNegocioException(
                    "La fecha inicial no puede ser posterior a la fecha final"
            );
        }
    }

    private String normalizarTexto(
            String texto
    ) {

        if (texto == null) {
            return null;
        }

        String textoLimpio =
                texto.trim();

        return textoLimpio.isBlank()
                ? null
                : textoLimpio;
    }

    private String normalizarComentario(
            String comentario
    ) {

        if (comentario == null) {
            return null;
        }

        String comentarioLimpio =
                comentario.trim();

        return comentarioLimpio.isBlank()
                ? null
                : comentarioLimpio;
    }

    private ResenaSalida convertirASalida(
            Resena resena
    ) {

        ResenaSalida salida =
                new ResenaSalida();

        salida.setId(
                resena.getId()
        );

        salida.setSolicitudId(
                resena.getSolicitud().getId()
        );

        salida.setServicioId(
                resena.getServicio().getId()
        );

        salida.setPerfilTrabajadorId(
                resena.getPerfilTrabajador().getId()
        );

        salida.setClienteId(
                resena.getClienteId()
        );

        salida.setCalificacion(
                resena.getCalificacion()
        );

        salida.setComentario(
                resena.getComentario()
        );

        salida.setFechaCreacion(
                resena.getFecha()
        );

        return salida;
    }
}