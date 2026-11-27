-- HU-03: CONSOLIDACION MANUAL, ESPECIFICA PARA defaultdb (MySQL 8).
-- NO ejecutar sin revisar dependencias logicas, triggers, FK y backups;
-- ensayar antes en una copia. Detener TODOS los escritores durante la ventana.
-- DBeaver: usar UNA MISMA conexion, desactivar autocommit y detenerse ante
-- cualquier error (no ejecutar con continuacion automatica ante errores).
-- La creacion del procedimiento es DDL e implica commit: ejecutarla ANTES de
-- comenzar cualquier transaccion. Si ya existe, detenerse y revisar; no reemplazar.
-- Ejecutar secciones 1-2; la llamada abre una transaccion y NO hace COMMIT.
-- No cerrar/reconectar la sesion ni ejecutar DDL hasta decidir COMMIT o ROLLBACK.

-- 1. PREVALIDACIONES: inspeccionar primero los resultados; no modifica datos.
SELECT DATABASE() AS base_actual, @@hostname AS servidor, VERSION() AS version_mysql;
SELECT c.id, c.solicitud_id, c.fecha_creacion, s.cliente_id, s.trabajador_id, s.servicio_id,
       (SELECT COUNT(*) FROM mensajes m WHERE m.conversacion_id = c.id) AS mensajes
FROM conversaciones c JOIN solicitudes_servicio s ON s.id = c.solicitud_id
WHERE c.id IN (1, 2, 5, 6) ORDER BY c.id;
SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_SCHEMA = DATABASE() AND REFERENCED_TABLE_NAME = 'conversaciones';
SELECT TRIGGER_NAME, EVENT_OBJECT_TABLE, EVENT_MANIPULATION
FROM information_schema.TRIGGERS WHERE EVENT_OBJECT_SCHEMA = DATABASE()
AND EVENT_OBJECT_TABLE IN ('conversaciones','mensajes','solicitudes_servicio');

-- 2. Crear una sola vez FUERA de la transaccion. DDL: confirmacion implicita.
DELIMITER $$
CREATE PROCEDURE hu03_consolidar_historico_20261127()
BEGIN
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  IF DATABASE() IS NULL OR DATABASE() <> 'defaultdb' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: seleccionar defaultdb';
  END IF;
  IF (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE()
      AND TABLE_NAME IN ('conversaciones','mensajes','solicitudes_servicio') AND ENGINE='InnoDB') <> 3 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: se requieren las tres tablas InnoDB';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.TRIGGERS WHERE EVENT_OBJECT_SCHEMA=DATABASE()
             AND EVENT_OBJECT_TABLE IN ('conversaciones','mensajes','solicitudes_servicio')) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: revisar triggers antes de continuar';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.KEY_COLUMN_USAGE
             WHERE REFERENCED_TABLE_SCHEMA=DATABASE() AND REFERENCED_TABLE_NAME='conversaciones'
               AND NOT (TABLE_SCHEMA=DATABASE() AND TABLE_NAME='mensajes'
                        AND COLUMN_NAME='conversacion_id' AND REFERENCED_COLUMN_NAME='id')) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: hay otra FK hacia conversaciones';
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND
             ((TABLE_NAME='conversaciones' AND COLUMN_NAME IN ('cliente_id','trabajador_id','servicio_id'))
              OR (TABLE_NAME='solicitudes_servicio' AND COLUMN_NAME='conversacion_id'))) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: el esquema no es PRE-HU-03';
  END IF;

  -- 3. TRANSACCION: el handler revierte automaticamente ante error SQL.
  START TRANSACTION;
  SELECT id FROM conversaciones ORDER BY id FOR UPDATE;
  SELECT id FROM mensajes ORDER BY id FOR UPDATE;
  SELECT id FROM solicitudes_servicio ORDER BY id FOR UPDATE;

  IF (SELECT COUNT(*) FROM conversaciones) <> 6 OR (SELECT COUNT(*) FROM mensajes) <> 51
     OR (SELECT COUNT(*) FROM solicitudes_servicio) <> 15
     OR (SELECT COUNT(*) FROM backup_conversaciones_hu03_20261127) <> 6
     OR (SELECT COUNT(*) FROM backup_mensajes_hu03_20261127) <> 51
     OR (SELECT COUNT(*) FROM backup_solicitudes_hu03_20261127) <> 15 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: conteos actuales o backups no coinciden';
  END IF;
  -- Verificar IDs y campos importantes de las copias, sin mostrar contenido.
  IF EXISTS (SELECT 1 FROM conversaciones c LEFT JOIN backup_conversaciones_hu03_20261127 b ON b.id=c.id
             WHERE b.id IS NULL OR NOT(c.solicitud_id <=> b.solicitud_id) OR NOT(c.fecha_creacion <=> b.fecha_creacion))
     OR EXISTS (SELECT 1 FROM mensajes m LEFT JOIN backup_mensajes_hu03_20261127 b ON b.id=m.id
                WHERE b.id IS NULL OR NOT(m.conversacion_id <=> b.conversacion_id)
                   OR NOT(m.remitente_id <=> b.remitente_id) OR NOT(m.fecha_envio <=> b.fecha_envio)
                   OR NOT(m.leido <=> b.leido) OR NOT(CAST(m.contenido AS BINARY) <=> CAST(b.contenido AS BINARY)))
     OR EXISTS (SELECT 1 FROM solicitudes_servicio s LEFT JOIN backup_solicitudes_hu03_20261127 b ON b.id=s.id
                WHERE b.id IS NULL OR NOT(s.cliente_id <=> b.cliente_id) OR NOT(s.trabajador_id <=> b.trabajador_id)
                   OR NOT(s.servicio_id <=> b.servicio_id) OR NOT(s.estado <=> b.estado)
                   OR NOT(s.fecha_actualizacion <=> b.fecha_actualizacion)) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: los datos difieren de los backups';
  END IF;
  IF (SELECT COUNT(*) FROM conversaciones c JOIN solicitudes_servicio s ON s.id=c.solicitud_id
      WHERE c.id IN (1,2) AND s.cliente_id=6 AND s.trabajador_id=7 AND s.servicio_id=2) <> 2
     OR (SELECT COUNT(*) FROM conversaciones c JOIN solicitudes_servicio s ON s.id=c.solicitud_id
      WHERE c.id IN (5,6) AND s.cliente_id=22 AND s.trabajador_id=24 AND s.servicio_id=12) <> 2
     OR NOT ((SELECT fecha_creacion FROM conversaciones WHERE id=1) <= (SELECT fecha_creacion FROM conversaciones WHERE id=2))
     OR NOT ((SELECT fecha_creacion FROM conversaciones WHERE id=5) <= (SELECT fecha_creacion FROM conversaciones WHERE id=6))
     OR (SELECT COUNT(*) FROM mensajes WHERE conversacion_id=1) <> 2
     OR (SELECT COUNT(*) FROM mensajes WHERE conversacion_id=2) <> 2
     OR (SELECT COUNT(*) FROM mensajes WHERE conversacion_id=5) <> 1
     OR (SELECT COUNT(*) FROM mensajes WHERE conversacion_id=6) <> 3 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: parejas, antiguedad o mensajes no coinciden';
  END IF;
  IF EXISTS (SELECT 1 FROM conversaciones c JOIN solicitudes_servicio s ON s.id=c.solicitud_id
             GROUP BY s.cliente_id,s.trabajador_id,s.servicio_id HAVING COUNT(*) > 1
             AND NOT (COUNT(*)=2 AND ((s.cliente_id=6 AND s.trabajador_id=7 AND s.servicio_id=2)
                  OR (s.cliente_id=22 AND s.trabajador_id=24 AND s.servicio_id=12)))) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: duplicados adicionales';
  END IF;
  IF EXISTS (SELECT 1 FROM mensajes m LEFT JOIN conversaciones c ON c.id=m.conversacion_id WHERE c.id IS NULL)
     OR EXISTS (SELECT 1 FROM conversaciones c LEFT JOIN solicitudes_servicio s ON s.id=c.solicitud_id WHERE s.id IS NULL)
     OR EXISTS (SELECT 1 FROM historial_estados_solicitud h LEFT JOIN solicitudes_servicio s ON s.id=h.solicitud_id WHERE s.id IS NULL)
     OR EXISTS (SELECT 1 FROM resenas r LEFT JOIN solicitudes_servicio s ON s.id=r.solicitud_id WHERE s.id IS NULL) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'HU03: referencias huerfanas';
  END IF;

  -- 4. Grupo 1 y grupo 2: mover solo los cinco mensajes esperados.
  UPDATE mensajes SET conversacion_id=1 WHERE conversacion_id=2;
  IF ROW_COUNT() <> 2 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='HU03: movimiento 2 a 1 incorrecto'; END IF;
  UPDATE mensajes SET conversacion_id=5 WHERE conversacion_id=6;
  IF ROW_COUNT() <> 3 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='HU03: movimiento 6 a 5 incorrecto'; END IF;

  -- 5. Verificacion intermedia antes de eliminar filas redundantes.
  IF (SELECT COUNT(*) FROM mensajes WHERE conversacion_id=1) <> 4
     OR (SELECT COUNT(*) FROM mensajes WHERE conversacion_id=5) <> 4
     OR (SELECT COUNT(*) FROM mensajes WHERE conversacion_id IN (2,6)) <> 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='HU03: verificacion intermedia incorrecta';
  END IF;
  DELETE FROM conversaciones WHERE id IN (2,6);
  IF ROW_COUNT() <> 2 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='HU03: eliminacion incompleta'; END IF;

  -- 6. Validaciones finales dentro de la misma transaccion aun abierta.
  IF (SELECT COUNT(*) FROM conversaciones) <> 4 OR (SELECT COUNT(*) FROM mensajes) <> 51
     OR (SELECT COUNT(*) FROM solicitudes_servicio) <> 15
     OR EXISTS (SELECT 1 FROM conversaciones WHERE id IN (2,6))
     OR EXISTS (SELECT 1 FROM mensajes m LEFT JOIN conversaciones c ON c.id=m.conversacion_id WHERE c.id IS NULL)
     OR EXISTS (SELECT 1 FROM conversaciones c LEFT JOIN solicitudes_servicio s ON s.id=c.solicitud_id WHERE s.id IS NULL)
     OR EXISTS (SELECT 1 FROM historial_estados_solicitud h LEFT JOIN solicitudes_servicio s ON s.id=h.solicitud_id WHERE s.id IS NULL)
     OR EXISTS (SELECT 1 FROM resenas r LEFT JOIN solicitudes_servicio s ON s.id=r.solicitud_id WHERE s.id IS NULL)
     OR EXISTS (SELECT 1 FROM conversaciones c JOIN solicitudes_servicio s ON s.id=c.solicitud_id
                GROUP BY s.cliente_id,s.trabajador_id,s.servicio_id HAVING COUNT(*) > 1)
     OR EXISTS (SELECT 1 FROM mensajes m LEFT JOIN backup_mensajes_hu03_20261127 b ON b.id=m.id
                WHERE b.id IS NULL OR NOT(m.conversacion_id <=> CASE b.conversacion_id WHEN 2 THEN 1 WHEN 6 THEN 5 ELSE b.conversacion_id END)
                   OR NOT(m.remitente_id <=> b.remitente_id) OR NOT(m.fecha_envio <=> b.fecha_envio)
                   OR NOT(m.leido <=> b.leido) OR NOT(CAST(m.contenido AS BINARY) <=> CAST(b.contenido AS BINARY))) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='HU03: validacion final fallida';
  END IF;
  -- Intencionalmente SIN COMMIT: la sesion debe permanecer abierta.
END$$
DELIMITER ;

-- 7. Ejecutar manualmente SOLO despues de crear el procedimiento y revisar.
CALL hu03_consolidar_historico_20261127();

-- 8. Revisar estos SELECT en la MISMA conexion, antes de confirmar.
SELECT (SELECT COUNT(*) FROM conversaciones) AS conversaciones,
       (SELECT COUNT(*) FROM mensajes) AS mensajes,
       (SELECT COUNT(*) FROM solicitudes_servicio) AS solicitudes;
SELECT c.id, COUNT(m.id) AS mensajes FROM conversaciones c LEFT JOIN mensajes m ON m.conversacion_id=c.id
WHERE c.id IN (1,2,5,6) GROUP BY c.id ORDER BY c.id;
SELECT s.cliente_id,s.trabajador_id,s.servicio_id,COUNT(*) AS total
FROM conversaciones c JOIN solicitudes_servicio s ON s.id=c.solicitud_id
GROUP BY s.cliente_id,s.trabajador_id,s.servicio_id HAVING COUNT(*)>1;
SELECT (SELECT COUNT(*) FROM mensajes m LEFT JOIN conversaciones c ON c.id=m.conversacion_id WHERE c.id IS NULL) AS mensajes_huerfanos,
       (SELECT COUNT(*) FROM conversaciones c LEFT JOIN solicitudes_servicio s ON s.id=c.solicitud_id WHERE s.id IS NULL) AS conversaciones_huerfanas,
       (SELECT COUNT(*) FROM historial_estados_solicitud h LEFT JOIN solicitudes_servicio s ON s.id=h.solicitud_id WHERE s.id IS NULL) AS historial_huerfano,
       (SELECT COUNT(*) FROM resenas r LEFT JOIN solicitudes_servicio s ON s.id=r.solicitud_id WHERE s.id IS NULL) AS resenas_huerfanas;
SELECT id, conversacion_id, fecha_envio FROM mensajes WHERE conversacion_id IN (1,5) ORDER BY conversacion_id,fecha_envio,id;

-- 9. DECISION MANUAL: ejecutar exactamente una opcion en la misma conexion.
-- COMMIT;
-- ROLLBACK;
-- Tras resolver la transaccion, retirar el procedimiento con una operacion DDL
-- separada y revisada: DROP PROCEDURE hu03_consolidar_historico_20261127;
