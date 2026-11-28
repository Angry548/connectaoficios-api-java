SELECT VERSION(), DATABASE(), @@hostname, @@port;
SELECT (SELECT COUNT(*) FROM conversaciones) conversaciones,(SELECT COUNT(*) FROM mensajes) mensajes,(SELECT COUNT(*) FROM solicitudes_servicio) solicitudes;
SELECT TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='mensajes' AND COLUMN_NAME IN ('id','tipo');
SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('adjuntos_mensaje','dispositivos_push');
SELECT INDEX_NAME,SEQ_IN_INDEX,COLUMN_NAME FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='mensajes' ORDER BY INDEX_NAME,SEQ_IN_INDEX;
SELECT COUNT(*) AS huerfanos FROM mensajes m LEFT JOIN conversaciones c ON c.id=m.conversacion_id WHERE c.id IS NULL;
