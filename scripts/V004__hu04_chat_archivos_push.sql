-- HU-04. Ejecutar solo tras copia de seguridad y prevalidaciones documentadas.
-- Requiere que no se haya aplicado scripts/hu04_tipo_mensaje.sql.
-- MySQL DDL realiza commits implicitos: no envolver como si fuera atomico.
ALTER TABLE mensajes ADD COLUMN tipo VARCHAR(16) NOT NULL DEFAULT 'TEXT';
UPDATE mensajes SET tipo = 'TEXT' WHERE tipo IS NULL OR tipo = '';
CREATE INDEX idx_mensajes_conversacion_fecha_id ON mensajes (conversacion_id, fecha_envio, id);
CREATE INDEX idx_mensajes_no_leidos ON mensajes (conversacion_id, leido, remitente_id);
CREATE TABLE adjuntos_mensaje (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 mensaje_id BIGINT NOT NULL,
 nombre_original VARCHAR(255) NOT NULL,
 mime_type VARCHAR(100) NOT NULL,
 tamano_bytes BIGINT NOT NULL,
 tipo_adjunto VARCHAR(16) NOT NULL,
 referencia VARCHAR(255) NOT NULL,
 fecha_creacion DATETIME(6) NOT NULL,
 CONSTRAINT fk_adjunto_mensaje FOREIGN KEY (mensaje_id) REFERENCES mensajes(id),
 INDEX idx_adjunto_mensaje (mensaje_id)
) ENGINE=InnoDB;
CREATE TABLE dispositivos_push (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 usuario_id INT NOT NULL,
 token VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 plataforma VARCHAR(16) NOT NULL,
 activo BOOLEAN NOT NULL DEFAULT TRUE,
 fecha_registro DATETIME(6) NOT NULL,
 fecha_actualizacion DATETIME(6) NOT NULL,
 CONSTRAINT uk_push_token UNIQUE (token),
 INDEX idx_push_usuario_activo (usuario_id, activo)
) ENGINE=InnoDB;
