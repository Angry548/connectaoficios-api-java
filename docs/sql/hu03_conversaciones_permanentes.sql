-- MySQL 8. Ejecutar manualmente con mantenimiento y copia de seguridad.
-- Antes de ejecutar, consultar este resultado: si hay filas, detenerse y
-- resolver cada historial con una estrategia revisada; nunca borrar ni fusionar aquí.
SELECT s.cliente_id, s.trabajador_id, s.servicio_id, COUNT(*) AS cantidad,
       GROUP_CONCAT(c.id ORDER BY c.id) AS conversaciones
FROM conversaciones c JOIN solicitudes_servicio s ON s.id = c.solicitud_id
GROUP BY s.cliente_id, s.trabajador_id, s.servicio_id HAVING COUNT(*) > 1;

-- Precondición automática antes de cualquier DDL (MySQL hace commit implícito del DDL).
DELIMITER $$
CREATE PROCEDURE hu03_comprobar_duplicados()
BEGIN
  IF DATABASE() IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: seleccione la base de datos';
  END IF;
  IF (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE()
      AND TABLE_NAME IN ('conversaciones','solicitudes_servicio','mensajes','servicios') AND ENGINE='InnoDB') <> 4 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: faltan tablas InnoDB requeridas';
  END IF;
  IF (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
      AND ((TABLE_NAME='conversaciones' AND COLUMN_NAME='id') OR (TABLE_NAME='servicios' AND COLUMN_NAME='id'))
      AND COLUMN_TYPE='bigint') <> 2 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: verificar tipos BIGINT de las FK';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
             AND ((TABLE_NAME='conversaciones' AND COLUMN_NAME IN ('cliente_id','trabajador_id','servicio_id'))
                  OR (TABLE_NAME='solicitudes_servicio' AND COLUMN_NAME='conversacion_id'))) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: columnas nuevas existentes; revisar migracion parcial';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE()
             AND CONSTRAINT_NAME IN ('uk_conversacion_participantes_servicio','fk_conversacion_servicio','fk_solicitud_conversacion'))
     OR EXISTS (SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE()
                AND TABLE_NAME='solicitudes_servicio' AND INDEX_NAME='ix_solicitud_conversacion') THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: existe una restriccion o indice con nombre reservado';
  END IF;
  IF EXISTS (SELECT 1 FROM conversaciones c LEFT JOIN solicitudes_servicio s ON s.id=c.solicitud_id
             LEFT JOIN servicios v ON v.id=s.servicio_id
             WHERE s.id IS NULL OR s.cliente_id IS NULL OR s.trabajador_id IS NULL
                OR s.servicio_id IS NULL OR v.id IS NULL) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: datos requeridos incompletos o huerfanos';
  END IF;
  IF EXISTS (SELECT 1 FROM conversaciones c JOIN solicitudes_servicio s ON s.id=c.solicitud_id
             GROUP BY s.cliente_id,s.trabajador_id,s.servicio_id HAVING COUNT(*) > 1) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: conversaciones duplicadas; revisar historiales antes de migrar';
  END IF;
END$$
DELIMITER ;
CALL hu03_comprobar_duplicados();
DROP PROCEDURE hu03_comprobar_duplicados;

ALTER TABLE conversaciones
  ADD COLUMN cliente_id INT NULL,
  ADD COLUMN trabajador_id INT NULL,
  ADD COLUMN servicio_id BIGINT NULL;
UPDATE conversaciones c JOIN solicitudes_servicio s ON s.id=c.solicitud_id
SET c.cliente_id=s.cliente_id, c.trabajador_id=s.trabajador_id, c.servicio_id=s.servicio_id;
ALTER TABLE conversaciones
  MODIFY cliente_id INT NOT NULL,
  MODIFY trabajador_id INT NOT NULL,
  MODIFY servicio_id BIGINT NOT NULL,
  ADD CONSTRAINT uk_conversacion_participantes_servicio UNIQUE (cliente_id, trabajador_id, servicio_id),
  ADD CONSTRAINT fk_conversacion_servicio FOREIGN KEY (servicio_id) REFERENCES servicios(id);

ALTER TABLE solicitudes_servicio ADD COLUMN conversacion_id BIGINT NULL;
UPDATE solicitudes_servicio s JOIN conversaciones c
  ON c.cliente_id=s.cliente_id AND c.trabajador_id=s.trabajador_id AND c.servicio_id=s.servicio_id
SET s.conversacion_id=c.id;
ALTER TABLE solicitudes_servicio
  ADD INDEX ix_solicitud_conversacion (conversacion_id),
  ADD CONSTRAINT fk_solicitud_conversacion FOREIGN KEY (conversacion_id) REFERENCES conversaciones(id);
-- Se conserva conversaciones.solicitud_id, todas las conversaciones y todos los mensajes.
-- Solicitudes sin conversación anterior quedan con conversacion_id NULL hasta que se abra su chat.
