-- Referencia MySQL; no ejecutado. Fechas administradas por JPA.
-- El UNIQUE ya proporciona el índice para cliente_id; no necesita otro índice.
CREATE TABLE perfiles_cliente (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id INT NOT NULL,
    foto_url VARCHAR(500) NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_actualizacion DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_perfil_cliente UNIQUE (cliente_id)
) ENGINE=InnoDB;
