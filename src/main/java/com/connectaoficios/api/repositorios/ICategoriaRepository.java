package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
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
}