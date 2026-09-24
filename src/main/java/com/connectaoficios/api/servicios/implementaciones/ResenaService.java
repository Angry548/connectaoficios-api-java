package com.connectaoficios.api.servicios.implementaciones;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResenaService implements IResenaService {

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

        SolicitudServicio solicitud =
                solicitudRepository
                        .findById(dto.getSolicitudId())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró la solicitud de servicio"
                                )
                        );

        if (!clienteId.equals(solicitud.getClienteId())) {
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
                .existsBySolicitudId(solicitud.getId())) {

            throw new ReglaNegocioException(
                    "La solicitud ya posee una reseña"
            );
        }

        Servicio servicio =
                solicitud.getServicio();

        PerfilTrabajador perfil =
                servicio.getPerfilTrabajador();

        Resena resena = new Resena();

        resena.setSolicitud(solicitud);
        resena.setServicio(servicio);
        resena.setPerfilTrabajador(perfil);
        resena.setClienteId(clienteId);
        resena.setCalificacion(dto.getCalificacion());
        resena.setComentario(dto.getComentario());

        Resena guardada =
                resenaRepository.save(resena);

        reputacionService.recalcular(
                perfil.getId()
        );

        return convertirASalida(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public ResenaSalida obtenerPorId(Long id) {

        Resena resena = resenaRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró la reseña"
                        )
                );

        return convertirASalida(resena);
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
                    convertirASalida(resena)
            );
        }

        return salida;
    }

    private ResenaSalida convertirASalida(
            Resena resena
    ) {

        ResenaSalida salida = new ResenaSalida();

        salida.setId(resena.getId());
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