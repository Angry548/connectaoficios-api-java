-- Carga opcional MANUAL después de que Hibernate cree las tablas.
-- Sin cuentas, contraseñas ni referencias a usuarios .NET. No sobrescribe filas.
START TRANSACTION;
INSERT INTO categorias (nombre, descripcion, activo)
SELECT 'TEST_LOCAL_Electricidad', 'Catálogo local de pruebas', 1
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre='TEST_LOCAL_Electricidad');
INSERT INTO categorias (nombre, descripcion, activo)
SELECT 'TEST_LOCAL_Plomeria', 'Catálogo local de pruebas', 1
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre='TEST_LOCAL_Plomeria');
INSERT INTO zonas_cobertura (departamento, municipio, localidad, activo)
SELECT 'San Salvador', 'San Salvador', 'TEST_LOCAL_Centro', 1
WHERE NOT EXISTS (SELECT 1 FROM zonas_cobertura WHERE localidad='TEST_LOCAL_Centro');
INSERT INTO zonas_cobertura (departamento, municipio, localidad, activo)
SELECT 'La Libertad', 'Santa Tecla', 'TEST_LOCAL_Tecla', 1
WHERE NOT EXISTS (SELECT 1 FROM zonas_cobertura WHERE localidad='TEST_LOCAL_Tecla');
INSERT INTO planes_promocion (nombre, descripcion, duracion_dias, precio, activo)
SELECT 'TEST_LOCAL_Semanal', 'Plan local de pruebas', 7, 10.00, 1
WHERE NOT EXISTS (SELECT 1 FROM planes_promocion WHERE nombre='TEST_LOCAL_Semanal');
COMMIT;
