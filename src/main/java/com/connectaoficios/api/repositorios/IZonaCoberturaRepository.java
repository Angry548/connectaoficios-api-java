package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.ZonaCobertura;
import org.springframework.data.jpa.repository.JpaRepository;
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
}