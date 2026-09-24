package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.PerfilTrabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio del perfil del trabajador.
 *
 * <p><b>Nota de integración:</b> esta interfaz pertenece al módulo de
 * PerfilTrabajador. Aquí se incluye únicamente la versión mínima necesaria
 * para que el módulo de Servicio (CON-4) compile y funcione mientras el
 * integrante responsable termina su implementación completa. Cuando se haga
 * la integración en develop se debe conservar la versión definitiva.</p>
 */
@Repository
public interface IPerfilTrabajadorRepository
        extends JpaRepository<PerfilTrabajador, Long> {
}