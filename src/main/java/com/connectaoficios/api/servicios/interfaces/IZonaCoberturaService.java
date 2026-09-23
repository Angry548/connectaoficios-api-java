package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.zona.ZonaCoberturaGuardar;
import com.connectaoficios.api.dtos.zona.ZonaCoberturaModificar;
import com.connectaoficios.api.dtos.zona.ZonaCoberturaSalida;

import java.util.List;

public interface IZonaCoberturaService {

    ZonaCoberturaSalida guardar(ZonaCoberturaGuardar zonaGuardar);

    ZonaCoberturaSalida modificar(
            Long id,
            ZonaCoberturaModificar zonaModificar
    );

    void eliminar(Long id);

    ZonaCoberturaSalida obtenerPorId(Long id);

    List<ZonaCoberturaSalida> listar();

    List<ZonaCoberturaSalida> listarActivas();

    List<ZonaCoberturaSalida> buscarPorDepartamento(String departamento);

    List<ZonaCoberturaSalida> buscarPorMunicipio(String municipio);
}