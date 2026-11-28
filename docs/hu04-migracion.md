# Runbook HU-04: pendiente de autorizacion y ensayo MySQL 8.4

No ejecutar en produccion durante la preparacion. Antes de autorizar `defaultdb`, ensayar el procedimiento y V004 en MySQL 8.4 aislado con una copia restaurada y anonimizada que reproduzca todo el esquema posterior a HU-03. El ensayo anterior uso MySQL 8.0 y un esquema sintetico reducido; no demuestra compatibilidad completa con 8.4 ni con el esquema real.

## 1. Ventana, destino y respaldo

1. Detener las escrituras de todas las instancias de la API y de otros clientes; confirmar que no quedan transacciones de escritura. Mantener el servicio detenido hasta terminar las verificaciones.
2. Conectarse solo despues de autorizarlo y comprobar identidad, version y base seleccionada. Si no coincide con el destino autorizado, detenerse. No imprimir credenciales.

```sql
SELECT @@hostname AS servidor, @@port AS puerto, VERSION() AS version_mysql,
       DATABASE() AS base_seleccionada, CURRENT_USER() AS cuenta;
```

3. Obtener un respaldo consistente de esquema y datos con la herramienta aprobada por el proveedor; verificarlo y ensayar su restauracion en otra instancia. Guardar identificador, hora y ubicacion segura. Registrar los conteos reales previos; 4/51/15 son una referencia y deben investigarse si difieren.

## 2. Prevalidaciones de solo lectura

Ejecutar en la base seleccionada y guardar los resultados:

```sql
SELECT (SELECT COUNT(*) FROM conversaciones) AS conversaciones,
       (SELECT COUNT(*) FROM mensajes) AS mensajes,
       (SELECT COUNT(*) FROM solicitudes_servicio) AS solicitudes;
SELECT TABLE_NAME, ENGINE FROM information_schema.TABLES
 WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN
 ('conversaciones','mensajes','solicitudes_servicio','servicios','adjuntos_mensaje','dispositivos_push');
SELECT TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE,COLUMN_DEFAULT
 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
 AND ((TABLE_NAME='mensajes' AND COLUMN_NAME IN ('id','conversacion_id','fecha_envio','leido','remitente_id','tipo'))
 OR (TABLE_NAME='conversaciones' AND COLUMN_NAME IN ('id','solicitud_id','cliente_id','trabajador_id','servicio_id'))
 OR (TABLE_NAME='solicitudes_servicio' AND COLUMN_NAME IN ('id','conversacion_id')))
 ORDER BY TABLE_NAME,ORDINAL_POSITION;
SELECT TABLE_NAME,INDEX_NAME,NON_UNIQUE,SEQ_IN_INDEX,COLUMN_NAME
 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE()
 AND TABLE_NAME IN ('mensajes','conversaciones','solicitudes_servicio')
 ORDER BY TABLE_NAME,INDEX_NAME,SEQ_IN_INDEX;
SELECT TABLE_NAME,CONSTRAINT_NAME,COLUMN_NAME,REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME
 FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA=DATABASE()
 AND TABLE_NAME IN ('mensajes','conversaciones','solicitudes_servicio') AND REFERENCED_TABLE_NAME IS NOT NULL
 ORDER BY TABLE_NAME,CONSTRAINT_NAME,ORDINAL_POSITION;
SELECT COUNT(*) AS mensajes_huerfanos FROM mensajes m LEFT JOIN conversaciones c ON c.id=m.conversacion_id WHERE c.id IS NULL;
SELECT COUNT(*) AS solicitudes_huerfanas FROM solicitudes_servicio s LEFT JOIN conversaciones c ON c.id=s.conversacion_id WHERE s.conversacion_id IS NOT NULL AND c.id IS NULL;
SELECT COUNT(*) AS conversaciones_sin_solicitud FROM conversaciones c LEFT JOIN solicitudes_servicio s ON s.id=c.solicitud_id WHERE s.id IS NULL;
SELECT cliente_id,trabajador_id,servicio_id,COUNT(*) AS cantidad FROM conversaciones GROUP BY cliente_id,trabajador_id,servicio_id HAVING COUNT(*)>1;
```

Detenerse si falta una estructura HU-03, hay huerfanos o duplicados, los tipos de FK son incompatibles, ya existe `mensajes.tipo`, alguna tabla nueva o un indice con nombre reservado, o existe un indice equivalente que requiera ajustar V004. Confirmar `mensajes.id` BIGINT y tablas InnoDB. Inspeccionar si HU-03 ya se ejecuto y si existen sus claves y FKs. No ejecutar el antiguo `scripts/hu04_tipo_mensaje.sql`.

## 3. Ejecutar, solo tras aprobacion especifica

Verificar hash/contenido de `scripts/V004__hu04_chat_archivos_push.sql` y ejecutar una sola vez con un cliente MySQL configurado para detenerse ante el primer error. No usar `--force`. Registrar cada sentencia aplicada y su resultado. No confiar en que `ROLLBACK` deshaga DDL: MySQL realiza commits implicitos para `ALTER TABLE`, `CREATE INDEX` y `CREATE TABLE`.

## 4. Postvalidaciones antes de arrancar Render

```sql
SELECT (SELECT COUNT(*) FROM conversaciones) AS conversaciones,
       (SELECT COUNT(*) FROM mensajes) AS mensajes,
       (SELECT COUNT(*) FROM solicitudes_servicio) AS solicitudes,
       (SELECT COUNT(*) FROM adjuntos_mensaje) AS adjuntos,
       (SELECT COUNT(*) FROM dispositivos_push) AS dispositivos;
SELECT tipo,COUNT(*) AS cantidad FROM mensajes GROUP BY tipo;
SELECT COUNT(*) AS tipos_invalidos FROM mensajes WHERE tipo IS NULL OR tipo<>'TEXT';
SELECT COUNT(*) AS mensajes_huerfanos FROM mensajes m LEFT JOIN conversaciones c ON c.id=m.conversacion_id WHERE c.id IS NULL;
SELECT COUNT(*) AS adjuntos_huerfanos FROM adjuntos_mensaje a LEFT JOIN mensajes m ON m.id=a.mensaje_id WHERE m.id IS NULL;
SELECT COUNT(*) AS solicitudes_huerfanas FROM solicitudes_servicio s LEFT JOIN conversaciones c ON c.id=s.conversacion_id WHERE s.conversacion_id IS NOT NULL AND c.id IS NULL;
SELECT TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE,COLUMN_DEFAULT FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('mensajes','adjuntos_mensaje','dispositivos_push')
 ORDER BY TABLE_NAME,ORDINAL_POSITION;
SELECT TABLE_NAME,INDEX_NAME,NON_UNIQUE,SEQ_IN_INDEX,COLUMN_NAME FROM information_schema.STATISTICS
 WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('mensajes','adjuntos_mensaje','dispositivos_push','conversaciones','solicitudes_servicio')
 ORDER BY TABLE_NAME,INDEX_NAME,SEQ_IN_INDEX;
SELECT TABLE_NAME,CONSTRAINT_NAME,COLUMN_NAME,REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME
 FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA=DATABASE()
 AND TABLE_NAME IN ('mensajes','adjuntos_mensaje','dispositivos_push','conversaciones','solicitudes_servicio')
 ORDER BY TABLE_NAME,CONSTRAINT_NAME,ORDINAL_POSITION;
SELECT cliente_id,trabajador_id,servicio_id,COUNT(*) AS cantidad FROM conversaciones GROUP BY cliente_id,trabajador_id,servicio_id HAVING COUNT(*)>1;
```

Sin escrituras entre ambas mediciones, los tres conteos deben coincidir exactamente con los previos (referencia 4/51/15); los mensajes historicos deben ser TEXT, las tablas nuevas estar vacias y los huerfanos y duplicados ser cero. Comparar claves y relaciones HU-03 antes/despues. Comprobar permisos y configuracion de Render sin mostrar secretos. Solo reactivar escritores cuando todas las comprobaciones, el respaldo y la aprobacion de despliegue esten completos; realizar primero pruebas en ambiente aislado.

## 5. Fallo o reejecucion

Detener inmediatamente el cliente y mantener escritores apagados. No volver a lanzar el script: ya puede existir una columna, indice o tabla tras un commit implicito. Guardar el error y examinar information_schema y conteos para identificar exactamente la ultima sentencia aplicada. Decidir con el administrador entre restaurar el respaldo verificado o preparar un script correctivo especifico y ensayarlo en una copia. No eliminar tablas ni datos nuevos sin respaldo y autorizacion. La recuperacion de un estado parcial no es `ROLLBACK`.

## Configuracion y validaciones posteriores

`CLOUDINARY_URL` es existente y contiene credenciales; nunca mostrarlo. `CHAT_MAX_ARCHIVO_BYTES` es opcional (5242880). El limite multipart global sigue configurado en Spring. Expo usa `https://exp.host/--/api/v2/push/send` en codigo; actualmente no hay variable Expo ni soporte configurado para access token. La conectividad de salida debe permitir Expo. Validar recursos raw autenticados y descarga protegida de Cloudinary, y push a dispositivos de prueba, solo despues de autorizacion separada. La presencia en memoria requiere almacenamiento compartido para varias replicas.
