USE hu04_ensayo;
SELECT 'conteos', (SELECT COUNT(*) FROM conversaciones),(SELECT COUNT(*) FROM mensajes),(SELECT COUNT(*) FROM solicitudes_servicio),(SELECT COUNT(*) FROM mensajes WHERE tipo='TEXT'),(SELECT COUNT(*) FROM adjuntos_mensaje),(SELECT COUNT(*) FROM dispositivos_push);
SELECT 'huerfanos',(SELECT COUNT(*) FROM mensajes m LEFT JOIN conversaciones c ON m.conversacion_id=c.id WHERE c.id IS NULL),(SELECT COUNT(*) FROM solicitudes_servicio s LEFT JOIN conversaciones c ON s.conversacion_id=c.id WHERE s.conversacion_id IS NOT NULL AND c.id IS NULL);
SELECT c.id,COALESCE((SELECT MAX(m.fecha_envio) FROM mensajes m WHERE m.conversacion_id=c.id),c.fecha_creacion) actividad FROM conversaciones c WHERE c.cliente_id=10 ORDER BY actividad DESC,c.id DESC;
EXPLAIN SELECT c.id,COALESCE((SELECT MAX(m.fecha_envio) FROM mensajes m WHERE m.conversacion_id=c.id),c.fecha_creacion) actividad FROM conversaciones c WHERE c.cliente_id=10 ORDER BY actividad DESC,c.id DESC LIMIT 2;
INSERT INTO mensajes (id,conversacion_id,remitente_id,contenido,fecha_envio,leido,tipo) VALUES (52,1,10,'nuevo','2026-01-01 13:00:00',0,'TEXT');
SELECT c.id,COALESCE((SELECT MAX(m.fecha_envio) FROM mensajes m WHERE m.conversacion_id=c.id),c.fecha_creacion) actividad FROM conversaciones c WHERE c.cliente_id=10 ORDER BY actividad DESC,c.id DESC;
SHOW INDEX FROM mensajes;
SELECT CONSTRAINT_NAME, TABLE_NAME, REFERENCED_TABLE_NAME FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA=DATABASE() AND REFERENCED_TABLE_NAME IS NOT NULL;
