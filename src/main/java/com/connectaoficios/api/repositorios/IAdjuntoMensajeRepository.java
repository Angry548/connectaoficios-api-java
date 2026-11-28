package com.connectaoficios.api.repositorios;
import com.connectaoficios.api.modelos.AdjuntoMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
public interface IAdjuntoMensajeRepository extends JpaRepository<AdjuntoMensaje,Long> { java.util.List<AdjuntoMensaje> findByMensajeIdIn(java.util.Collection<Long> ids); }
