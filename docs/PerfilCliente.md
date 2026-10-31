# Implementación de PerfilCliente

Se continuaron los cuatro DTOs y la interfaz existentes. No se modificaron SecurityConfig, propiedades de producción, Docker, Aiven ni Render. No se hizo commit ni push.

## Archivos

Agregados en esta continuación:

- `src/main/java/com/connectaoficios/api/modelos/PerfilCliente.java`
- `src/main/java/com/connectaoficios/api/repositorios/IPerfilClienteRepository.java`
- `src/main/java/com/connectaoficios/api/servicios/implementaciones/PerfilClienteService.java`
- `src/main/java/com/connectaoficios/api/controladores/PerfilClienteController.java`
- `src/test/java/com/connectaoficios/api/servicios/implementaciones/PerfilClienteServiceTest.java`
- `src/test/java/com/connectaoficios/api/servicios/implementaciones/ImagenServiceTest.java`
- `src/test/java/com/connectaoficios/api/controladores/PerfilClienteControllerTest.java` (continuación tras revisión)
- `docs/sql/perfiles_cliente.sql`
- Este informe.

Modificados en esta continuación:

- `ImagenController.java`, `IImagenService.java`, `ImagenService.java`.
- `ConversacionSalida.java`, `ConversacionService.java`, `ConversacionServiceTest.java`.
- `IConversacionRepository.java`, `IPerfilTrabajadorRepository.java`.

Conservados de los cambios parciales: los cuatro DTOs Cliente e `IPerfilClienteService`. Los cambios previos en `.dockerignore`, `mvnw.cmd`, `pom.xml`, `.env.example`, `README.md` y `compose.yaml` no se editaron en esta continuación.

## Entidad y tabla

La entidad final es `PerfilCliente`, con getters/setters Lombok, `@Entity`, tabla `perfiles_cliente`, ID autoincremental y restricción `uk_perfil_cliente`.

| Campo Java | Columna | Tipo MySQL | Restricciones |
|---|---|---|---|
| `Long id` | `id` | BIGINT | PK, NOT NULL, AUTO_INCREMENT |
| `Integer clienteId` | `cliente_id` | INT | NOT NULL, UNIQUE |
| `String fotoUrl` | `foto_url` | VARCHAR(500) | nullable |
| `LocalDateTime fechaCreacion` | `fecha_creacion` | DATETIME(6) | NOT NULL, no actualizable por JPA |
| `LocalDateTime fechaActualizacion` | `fecha_actualizacion` | DATETIME(6) | NOT NULL |

`@PrePersist` inicializa ambas fechas y `@PreUpdate` actualiza la segunda. No se agrega relación con un usuario externo ni FK para `cliente_id`. Índices: PRIMARY sobre `id` y UNIQUE `uk_perfil_cliente` sobre `cliente_id`; no se duplica el índice de cliente.

SQL de referencia, no ejecutado: [perfiles_cliente.sql](sql/perfiles_cliente.sql).

## Repository, DTOs y servicio

`IPerfilClienteRepository extends JpaRepository<PerfilCliente, Long>` incluye `existsByClienteId`, `findByClienteId` y `findByClienteIdIn(Collection<Integer>)`.

- `PerfilClienteGuardar`: `fotoUrl` opcional, máximo 500 caracteres.
- `PerfilClienteModificar`: `fotoUrl` opcional, máximo 500 caracteres.
- `PerfilClienteSalida`: id, clienteId, fotoUrl y ambas fechas.
- `PerfilClientePublicoSalida`: id, clienteId y fotoUrl.

`IPerfilClienteService` expone `guardar`, `obtenerPorId`, `obtenerPorClienteId`, `obtenerPerfilPublicoPorClienteId` y `modificar`.

`PerfilClienteService` utiliza mapeo manual y transacciones de Spring. Rechaza duplicados mediante `ReglaNegocioException` antes de guardar; la restricción UNIQUE también impide duplicados concurrentes en la BD. Las búsquedas de perfiles inexistentes lanzan `RecursoNoEncontradoException`. La modificación conserva la foto existente cuando el campo entrante es null.

## Controller y seguridad

Base: `/api/perfiles-cliente`.

| Método | Ruta relativa | Acceso |
|---|---|---|
| POST | (base) | CLIENTE; ID desde jwt.subject; devuelve 201 |
| GET | `/{id}` | Autenticado |
| GET | `/cliente/{clienteId}` | Autenticado |
| GET | `/cliente/{clienteId}/publico` | Autenticado; representación pública |
| PUT | `/mi-perfil` | CLIENTE; ID desde jwt.subject |

POST y PUT aplican `@PreAuthorize("hasRole('CLIENTE')")` y validación del DTO. El móvil no envía clienteId para crear o modificar su propio perfil. La vista pública significa visible a otros usuarios autenticados: SecurityConfig exige autenticación a toda la API salvo Swagger. No se habilitó acceso anónimo.

## Imágenes

Se conserva `POST /api/imagenes/perfil`, multipart con parte `archivo`, respuesta `fotoUrl`. Ahora acepta TRABAJADOR o CLIENTE mediante anotación. Usa las autoridades autenticadas para seleccionar el método del servicio; el ID se obtiene del JWT.

`IImagenService.subirFotoPerfil(archivo, trabajadorId)` conserva su contrato. Se agrega `subirFotoPerfilCliente(archivo, clienteId)`. Ambos reutilizan el mismo código de validación y Cloudinary: carpeta `connectaoficios/perfiles`, public_id `trabajador_{id}` o `cliente_{id}`, overwrite=true, invalidate=true y respuesta desde secure_url. Se mantienen 5 MB y JPEG/PNG/WEBP. No hay eliminación física.

Flujo Cliente: subir imagen, recibir fotoUrl, persistirla mediante POST de creación del perfil o PUT `/api/perfiles-cliente/mi-perfil`. La subida por sí sola no persiste el perfil.

## Conversaciones y N+1

`ConversacionSalida` incorpora `clienteFotoUrl` y `trabajadorFotoUrl`, sin modificar la entidad `Conversacion`. Se resuelven desde los perfiles al consultar. Perfil ausente o foto ausente produce null.

En el listado paginado se extraen conjuntos de IDs, se hace una consulta de perfiles Cliente y una de Trabajador y se construyen mapas independientes por tipo de participante. Las URLs nulas son válidas en los mapas. No se consulta un perfil por conversación.

La consulta principal utiliza `JOIN FETCH c.solicitud s`, evitando consultas adicionales por la asociación LAZY. La consulta COUNT mantiene JOIN normal. Es una asociación uno a uno, por lo que el fetch no multiplica las filas de la página. Las consultas individuales obtienen las dos fotos dentro de la transacción del servicio.

## Pruebas

`PerfilClienteServiceTest`: diez pruebas (incluyendo duplicado concurrente simulado y propagación de otras violaciones de integridad), además de las verificaciones para creación, duplicados, consulta por ID, consulta por clienteId, inexistentes, modificación de foto, conservación ante null y perfil público.

`ConversacionServiceTest`: conserva seis pruebas anteriores y agrega tres para ambas fotos, perfiles ausentes y página con fotos parciales y participantes diferentes. Verifica consultas por lotes y ausencia de consultas individuales en el listado.

`ImagenServiceTest`: verifica public_id separado y opciones compatibles de Cloudinary, además de rechazar formato y tamaño inválidos para Cliente. No contacta Cloudinary.

`PerfilClienteControllerTest`: tres pruebas directas del controller para comprobar que POST y PUT utilizan el subject del JWT y rechazan valores ausentes, no numéricos, no positivos o fuera del rango de Integer antes de invocar el servicio. Estas pruebas no sustituyen la validación HTTP de seguridad con JWT real en Swagger.

Continuación tras la revisión: la creación utiliza saveAndFlush para detectar la restricción UNIQUE dentro del servicio y traducir específicamente uk_perfil_cliente a ReglaNegocioException. Otras violaciones de integridad se propagan sin etiquetarlas como duplicado. La validación compartida de imágenes ahora utiliza un mensaje de usuario, válido para ambos roles. Se conserva la configuración multipart existente: max-request-size=5MB incluye el cuerpo multipart, por lo que un archivo de exactamente 5 MB podría exceder el tamaño total permitido; no se cambiaron propiedades de producción.

Resultado final de `.\mvnw.cmd test`: **151 ejecutadas, 134 aprobadas, 8 fallos, 9 errores, 0 omitidas**. Compilaron el código principal y las pruebas. Pasaron las 18 pruebas nuevas de la funcionalidad: 10 de servicio Cliente, 3 de controller Cliente, 3 de conversaciones y 2 de imágenes; ConversacionServiceTest completo terminó con 9 aprobadas. No se detectaron fallos nuevos en esta funcionalidad.

Persisten los problemas previos: PromocionServiceTest tiene 7 fallos y 7 errores por el resultado null de `findByEstado` en `sincronizarPromocionesVencidas`; SolicitudServicioServiceTest tiene 1 fallo por motivo de rechazo null y 1 error por perfilTrabajador ausente; contextLoads tiene 1 error por la propiedad `cloudinary.url` no definida. Coinciden los totales de 8 fallos y 9 errores reportados en el análisis previo. No se deshabilitaron ni omitieron pruebas. El log completo está en `target/perfil-cliente-continuacion-maven.log` y los reportes en `target/surefire-reports`.

El primer intento dentro del sandbox no pudo acceder a `C:\.m2\repository`; se repitió con ejecución autorizada fuera del sandbox. No se ejecutó `clean`, por lo que el problema anterior de eliminación de target no se considera resuelto.

## React Native y validación pendiente

El móvil debe crear el perfil una sola vez, subir `archivo` con JWT, guardar la fotoUrl retornada en mi-perfil y consumir las dos nuevas URLs de conversaciones. Debe admitir null con un avatar predeterminado y refrescar/inutilizar la caché de imagen al reemplazar una foto que mantiene public_id. No debe enviar un ID propio en POST/PUT de perfil.

Posteriormente podrían necesitar fotografías `MensajeSalida` (avatar de remitente), `ResenaSalida` (autor), `ServicioSalida` y `PromocionSalida` (trabajador), y `TransaccionPagoSalida` (contraparte), según las pantallas que confirme el frontend. No fueron ampliados ahora.

Riesgos pendientes: no se verificó el arranque real en Docker/MySQL; la representación pública requiere JWT; null conserva la foto y no permite borrarla; la traducción del duplicado concurrente a regla de negocio depende de que Hibernate identifique la restricción uk_perfil_cliente; debe confirmarse con MySQL local; la subida seguida de un fallo de guardado puede dejar la imagen sin referencia, como en Trabajador. No se ejecutó clean: el bloqueo previo de target no se considera resuelto.

## Docker local (ejecutar por el usuario)

Si están encendidos:

```powershell
docker compose stop
docker compose up -d --build
```

Si ya están detenidos, ejecutar solamente `docker compose up -d --build`. Después:

```powershell
docker compose ps
docker compose logs --tail=200 connectaoficios-java-api
```

El compose local existente configura `SPRING_JPA_HIBERNATE_DDL_AUTO=update`; `src/main/resources/application.properties` mantiene `none`. La imagen reconstruida incluirá la entidad y Hibernate deberá crear `perfiles_cliente` si no existe. No se deben eliminar volúmenes ni tablas.

Después de reconstruir, comprobar conexión MySQL, reconocimiento de la entidad, creación/actualización de la tabla y arranque sin excepciones. Luego probar Swagger con JWTs de Cliente y Trabajador. Para demostrar creación de tabla, revisar los mensajes de schema/DDL y, si hace falta, consultar el esquema local; un mensaje de inicio por sí solo no prueba que la tabla haya sido creada.

## git status --short

```text
 M .dockerignore
 M mvnw.cmd
 M pom.xml
 M src/main/java/com/connectaoficios/api/controladores/ImagenController.java
 M src/main/java/com/connectaoficios/api/dtos/conversacion/ConversacionSalida.java
 M src/main/java/com/connectaoficios/api/repositorios/IConversacionRepository.java
 M src/main/java/com/connectaoficios/api/repositorios/IPerfilTrabajadorRepository.java
 M src/main/java/com/connectaoficios/api/servicios/implementaciones/ConversacionService.java
 M src/main/java/com/connectaoficios/api/servicios/implementaciones/ImagenService.java
 M src/main/java/com/connectaoficios/api/servicios/interfaces/IImagenService.java
 M src/test/java/com/connectaoficios/api/servicios/implementaciones/ConversacionServiceTest.java
?? .env.example
?? README.md
?? compose.yaml
?? docs/PerfilCliente.md
?? docs/sql/
?? src/main/java/com/connectaoficios/api/controladores/PerfilClienteController.java
?? src/main/java/com/connectaoficios/api/dtos/perfil/PerfilClienteGuardar.java
?? src/main/java/com/connectaoficios/api/dtos/perfil/PerfilClienteModificar.java
?? src/main/java/com/connectaoficios/api/dtos/perfil/PerfilClientePublicoSalida.java
?? src/main/java/com/connectaoficios/api/dtos/perfil/PerfilClienteSalida.java
?? src/main/java/com/connectaoficios/api/modelos/PerfilCliente.java
?? src/main/java/com/connectaoficios/api/repositorios/IPerfilClienteRepository.java
?? src/main/java/com/connectaoficios/api/servicios/implementaciones/PerfilClienteService.java
?? src/main/java/com/connectaoficios/api/servicios/interfaces/IPerfilClienteService.java
?? src/test/java/com/connectaoficios/api/controladores/
?? src/test/java/com/connectaoficios/api/servicios/implementaciones/ImagenServiceTest.java
?? src/test/java/com/connectaoficios/api/servicios/implementaciones/PerfilClienteServiceTest.java
```
