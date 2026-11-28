package com.connectaoficios.api.repositorios;
import com.connectaoficios.api.modelos.DispositivoPush;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface IDispositivoPushRepository extends JpaRepository<DispositivoPush,Long> {
 Optional<DispositivoPush> findByToken(String token);
 List<DispositivoPush> findByUsuarioIdAndActivoTrue(Integer usuarioId);
}
