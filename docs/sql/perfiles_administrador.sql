-- Referencia MySQL; no ejecutado. Fechas administradas por JPA.
-- El UNIQUE ya proporciona el índice para administrador_id; no necesita otro índice.
CREATE TABLE perfiles_administrador (
    id BIGINT NOT NULL AUTO_INCREMENT,
    administrador_id INT NOT NULL,
    foto_url VARCHAR(500) NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_actualizacion DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_perfil_administrador UNIQUE (administrador_id)
) ENGINE=InnoDB;
