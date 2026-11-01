# Preparación de master — 31/10/2026

## Resultado ejecutado

Rama master. Sin ramas nuevas, develop, merge, commit ni push. Sin acceso a Render/Aiven. No se leyó ni modificó el contenido de .env. No se tocó el volumen MySQL. No se modificó Jira.

Reproducción inicial NUEVA: 185 tests, 8 failures, 9 errors, 0 skipped. Además de los 8 errors históricos apareció contextLoads sin cloudinary.url. Evidencia local ignorada: reports/unit/audit-baseline.log.

Resultado FINAL: 264 tests, 264 passed, 0 failures, 0 errors, 0 skipped. Son unitarias y pruebas de Spring/HTTP/H2, no una certificación MySQL. Se agregaron 79 casos efectivos sin deshabilitar ni borrar casos antiguos.

Maven Wrapper clean verify: BUILD SUCCESS, exit 0, 31/10/2026 15:04:31 (hora local). Se utilizó únicamente la caché Maven existente del usuario mediante -Dmaven.repo.local; no credenciales reales. Docker no disponible: ésta fue una validación diagnóstica con Java 21 del host. Runner Docker preparado para el equipo.

Maven Wrapper site: BUILD SUCCESS, exit 0, 15:05:40. report-only no repite tests.

Surefire NUEVO: target/site/surefire-report.html, 264 tests/100% éxito, fecha 31/10/2026 15:05:40.
JaCoCo NUEVO 0.8.14: target/site/jacoco/index.html y jacoco.xml, fecha 15:04:31. Sin umbral nuevo ni exclusiones.

| Cobertura | Cubiertos | Total | Porcentaje |
|---|---:|---:|---:|
| LINE | 2579 | 4134 | 62.39% |
| BRANCH | 432 | 882 | 48.98% |
| METHOD | 455 | 724 | 62.85% |
| CLASS | 150 | 164 | 91.46% |

## Causas corregidas

- PromocionServiceTest: contrato paginado real de findByEstado(ACTIVA, Pageable.unpaged()); fixture vacío predeterminado y escenario específico de vencimiento. No lenient ni pruebas deshabilitadas.
- SolicitudServicioServiceTest: fixture sin PerfilTrabajador; expectativa obsoleta registrarServicioCompletado(usuarioId) reemplazada por recalcular(perfilId), conforme al cálculo real basado en solicitudes completadas. IDs de perfil y usuario deliberadamente distintos.
- Producción: motivoRechazo se guardaba pero no se mapeaba al DTO de respuesta. Mapeo corregido sin cambiar la expectativa.
- Producción: comprobaciones nuevas de acceso necesitan transacción read-only para relaciones JPA lazy con open-in-view=false; el flujo HTTP detectó el 500 y ahora pasa.
- Contexto: perfil test evita exigir Cloudinary real.
- Notificaciones: inexistente devuelve 404 y ajena 403, en vez de RuntimeException/500; rol AdministradorPrincipal corregido.
- Imágenes: errores del proveedor no incluyen detalles internos en la respuesta.

## Seguridad implementada

Servicios: rol y dueño del perfil/recurso para crear, modificar, cambiar estado y eliminar.
Solicitudes: cliente del JWT y trabajador derivado del servicio; IDs opcionales compatibles, discrepancias 403. Consulta/listados/transiciones restringidos al participante; consultas administrativas conservadas.
Identidad: NameIdentifier URI .NET primero, fallback sub cuando ausente, entero positivo. Todos los controladores usan la estrategia común. Rol URI/role conservado; alias ADMINISTRADOR_PRINCIPAL normalizado. Confirmar con emisor .NET local al integrar.
STOMP: origen exacto configurable, JWT en CONNECT, topics de conversación con participante, sin comodines, SEND denegado. Publicación por REST autorizado. No hay handshake real con mobile validado todavía.

## Infraestructura entregada

1. Dockerfile: Java 21, build Maven, runtime sin root y curl para health.
2. Compose: API 127.0.0.1:8081, MySQL 8.4 local, bridge existente y volumen connectaoficios-mysql-data conservados. API depende de MySQL healthy. API health SELECT 1 con 503 si falla.
3. .env.example: valores públicos LOCALES completos; .env presente/ignorado, sin mostrar secretos. No sobrescribir .env de un volumen ya inicializado.
4. Perfil local: Hibernate update; producción conserva none. Perfil test: H2. Sin 01-schema.sql activo.
5. Cloudinary real solo fuera de local/test; simulación local valida archivo y devuelve avatar SVG data URI, sin subida ni almacenamiento. No equivale a probar el proveedor externo.
6. Seed MANUAL opcional: dos categorías, dos zonas y un plan. Idempotente secuencial, sin usuarios ni contraseñas; no ejecutado contra MySQL en esta sesión.
7. Postman: 28 requests encadenados, tokens vacíos, IDs capturados, status/JSON/campos/tiempos y bloqueo de destino/redirects.
8. Newman: npx versionado 6.2.1; scripts PowerShell/Bash, --bail y JSON/JUnit ignorados. No ejecutado contra la API.
9. k6: cinco módulos load y cinco stress; tokens por entorno, sin login .NET, loopback validado, thresholds documentados. Lecturas por defecto; escrituras opcionales de solicitudes/mensajes. Contratación masiva de promociones exige preparar servicios únicos adicionales.
10. ZAP: baseline y API safe/active, roles por sesión, red Docker temporal interna sin Internet, destino fijo y active opt-in. No ejecutado. Ver documentación oficial enlazada en tests/zap/README.md.
11. Runner unit-tests del profile testing: clean verify y site dentro de Docker, caché Maven separada y reportes en target del workspace.
12. reports: seis destinos lógicos con .gitkeep; resultados ignorados. No se versionan tokens ni entornos personales.
13. Codespaces: sh/Bash, sin rutas Windows, Wrapper modo 100755, instrucciones Compose y puertos privados. No .devcontainer requerido.

El permiso ejecutable de mvnw es el ÚNICO cambio en el índice (staged), necesario para conservar ese metadato desde Windows. No se hizo commit. El resto permanece sin stage.

## Verificación adicional y limitaciones

- node scripts/testing/validate.mjs: OK, 28 requests, tokens vacíos, guardas locales y sintaxis de 17 scripts, sin HTTP.
- Bash -n: mvnw, newman.sh, verify.sh aprobados.
- Parser PowerShell: newman.ps1 y verify.ps1 aprobados.
- SnakeYAML: compose.yaml válido; no sustituye docker compose config.
- git diff --check y git diff --cached --check: sin errores.
- Windows/OneDrive tenía directorios generados target con ReadOnly que bloqueaban clean. Se retiró ese atributo únicamente dentro de target, con rutas verificadas; clean verify finalmente terminó correctamente.
- Docker/daemon, arranque de imágenes, SQL seed/MySQL, Newman, ZAP y k6 pendientes de ejecución real. No se ejecutaron ataques ni carga/stress.
- La integración HTTP completa probó persistencia H2 y ciclo servicio/solicitud/chat/reseña/reputación/promoción/pago; no demuestra persistencia MySQL tras reinicios.
- Probar CONNECT/origen/suscripción real con mobile; clientes deben enviar Authorization en CONNECT. Se mantienen pruebas de interceptor y aislamiento de publicación.
- Imágenes no verifican firmas binarias del contenido; se valida tamaño y MIME declarado. El proveedor real no se probó.
- Registrar digests de imágenes y hardware en la evidencia final para fijar exactamente versiones/rendimiento.
- No se afirma cobertura de seguridad exhaustiva de todos los CRUD históricos.

Comandos pendientes, desde raíz con .env local adecuado (sin reemplazar credenciales de un volumen existente):

```sh
docker compose config --quiet
docker compose up -d --build
docker compose ps
curl --fail http://localhost:8081/api/health
curl --fail http://localhost:8081/v3/api-docs
curl --fail http://localhost:8081/swagger-ui/index.html
docker compose --profile testing run --rm unit-tests
```

PowerShell: usar Invoke-RestMethod/Invoke-WebRequest para esas URLs. Detener con docker compose stop; nunca down -v.

## Casos recomendados para Jira/Xray

Ver XRAY_CASES.md: identidad JWT, BOLA de servicios/solicitudes, perfiles, avatar, imágenes, notificaciones, leído/no leído, pago simulado, vencimiento, ServicioAdmin, estadísticas, STOMP e integración/persistencia. No se modificó Jira.

## Suite final por clase

| Clase | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| ConnectaoficiosApiJavaApplicationTests | 1 | 0 | 0 | 0 |
| LocalApiSecurityTest | 12 | 0 | 0 | 0 |
| MensajeNoLeidosTotalControllerTest | 5 | 0 | 0 | 0 |
| PerfilAdministradorControllerTest | 3 | 0 | 0 | 0 |
| PerfilAdministradorSeguridadTest | 4 | 0 | 0 | 0 |
| PerfilClienteControllerTest | 3 | 0 | 0 | 0 |
| MensajeNoLeidosTotalTest | 7 | 0 | 0 | 0 |
| AutorizacionRecursosTest | 17 | 0 | 0 | 0 |
| IdentidadJwtTest | 5 | 0 | 0 | 0 |
| StompSeguridadTest | 8 | 0 | 0 | 0 |
| CategoriaServiceTest | 6 | 0 | 0 | 0 |
| ChatTiempoRealServiceTest | 2 | 0 | 0 | 0 |
| ConversacionServiceTest | 9 | 0 | 0 | 0 |
| DisponibilidadServicioServiceTest | 8 | 0 | 0 | 0 |
| EstadisticasServiceTest | 5 | 0 | 0 | 0 |
| ImagenServiceTest | 7 | 0 | 0 | 0 |
| MensajeServiceTest | 9 | 0 | 0 | 0 |
| NotificacionServiceTest | 6 | 0 | 0 | 0 |
| PerfilAdministradorServiceTest | 10 | 0 | 0 | 0 |
| PerfilClienteServiceTest | 10 | 0 | 0 | 0 |
| PerfilTrabajadorServiceTest | 7 | 0 | 0 | 0 |
| PlanPromocionServiceTest | 10 | 0 | 0 | 0 |
| PromocionServiceTest | 15 | 0 | 0 | 0 |
| ReporteServiceTest | 14 | 0 | 0 | 0 |
| ReputacionTrabajadorServiceTest | 17 | 0 | 0 | 0 |
| ResenaServiceTest | 4 | 0 | 0 | 0 |
| ServicioAdminServiceTest | 5 | 0 | 0 | 0 |
| ServicioServiceTest | 11 | 0 | 0 | 0 |
| SolicitudServicioServiceTest | 15 | 0 | 0 | 0 |
| TransaccionPagoServiceTest | 18 | 0 | 0 | 0 |
| UsuarioAvatarServiceTest | 5 | 0 | 0 | 0 |
| ZonaCoberturaServiceTest | 6 | 0 | 0 | 0 |

## Archivos creados (51)

- docker/sql/02-seed-test-data.sql
- docs/testing/PREPARATION_REPORT.md
- docs/testing/README.md
- docs/testing/TEST_USERS.example.md
- docs/testing/XRAY_CASES.md
- reports/coverage/.gitkeep
- reports/k6/load/.gitkeep
- reports/k6/stress/.gitkeep
- reports/newman/.gitkeep
- reports/unit/.gitkeep
- reports/zap/.gitkeep
- scripts/testing/check-local.mjs
- scripts/testing/k6.mjs
- scripts/testing/newman.ps1
- scripts/testing/newman.sh
- scripts/testing/report-summary.mjs
- scripts/testing/seed.mjs
- scripts/testing/validate.mjs
- scripts/testing/verify.ps1
- scripts/testing/verify.sh
- scripts/testing/zap.mjs
- src/main/java/com/connectaoficios/api/controladores/HealthLocalController.java
- src/main/java/com/connectaoficios/api/seguridad/AccesoJava.java
- src/main/java/com/connectaoficios/api/seguridad/IdentidadJwt.java
- src/main/java/com/connectaoficios/api/seguridad/StompSeguridad.java
- src/main/java/com/connectaoficios/api/servicios/implementaciones/ImagenLocalService.java
- src/main/resources/application-local.properties
- src/test/java/com/connectaoficios/api/controladores/LocalApiSecurityTest.java
- src/test/java/com/connectaoficios/api/seguridad/AutorizacionRecursosTest.java
- src/test/java/com/connectaoficios/api/seguridad/IdentidadJwtTest.java
- src/test/java/com/connectaoficios/api/seguridad/StompSeguridadTest.java
- src/test/java/com/connectaoficios/api/servicios/implementaciones/ChatTiempoRealServiceTest.java
- src/test/java/com/connectaoficios/api/servicios/implementaciones/EstadisticasServiceTest.java
- src/test/java/com/connectaoficios/api/servicios/implementaciones/NotificacionServiceTest.java
- src/test/java/com/connectaoficios/api/servicios/implementaciones/ServicioAdminServiceTest.java
- tests/k6/README.md
- tests/k6/common/scenarios.js
- tests/k6/load/mensajeria.js
- tests/k6/load/promociones.js
- tests/k6/load/reportes-estadisticas.js
- tests/k6/load/servicios.js
- tests/k6/load/solicitudes.js
- tests/k6/stress/mensajeria.js
- tests/k6/stress/promociones.js
- tests/k6/stress/reportes-estadisticas.js
- tests/k6/stress/servicios.js
- tests/k6/stress/solicitudes.js
- tests/postman/connectaoficios-java-local.collection.json
- tests/postman/connectaoficios-java-local.environment.example.json
- tests/zap/README.md
- tests/zap/contexts.json

## Archivos modificados (36)

- .dockerignore
- .env.example
- .gitattributes
- .gitignore
- Dockerfile
- README.md
- compose.yaml
- src/main/java/com/connectaoficios/api/config/CloudinaryConfig.java
- src/main/java/com/connectaoficios/api/config/WebSocketConfig.java
- src/main/java/com/connectaoficios/api/controladores/ConversacionController.java
- src/main/java/com/connectaoficios/api/controladores/ImagenController.java
- src/main/java/com/connectaoficios/api/controladores/MensajeController.java
- src/main/java/com/connectaoficios/api/controladores/NotificacionController.java
- src/main/java/com/connectaoficios/api/controladores/PerfilAdministradorController.java
- src/main/java/com/connectaoficios/api/controladores/PerfilClienteController.java
- src/main/java/com/connectaoficios/api/controladores/PerfilTrabajadorController.java
- src/main/java/com/connectaoficios/api/controladores/PromocionController.java
- src/main/java/com/connectaoficios/api/controladores/ReporteController.java
- src/main/java/com/connectaoficios/api/controladores/ResenaController.java
- src/main/java/com/connectaoficios/api/controladores/ServicioController.java
- src/main/java/com/connectaoficios/api/controladores/SolicitudServicioController.java
- src/main/java/com/connectaoficios/api/controladores/TransaccionPagoController.java
- src/main/java/com/connectaoficios/api/dtos/solicitud/SolicitudServicioGuardar.java
- src/main/java/com/connectaoficios/api/seguridad/configuracion/JwtAuthenticationConverter.java
- src/main/java/com/connectaoficios/api/seguridad/configuracion/SecurityConfig.java
- src/main/java/com/connectaoficios/api/servicios/implementaciones/ImagenService.java
- src/main/java/com/connectaoficios/api/servicios/implementaciones/NotificacionService.java
- src/main/java/com/connectaoficios/api/servicios/implementaciones/SolicitudServicioService.java
- src/test/java/com/connectaoficios/api/servicios/implementaciones/ImagenServiceTest.java
- src/test/java/com/connectaoficios/api/servicios/implementaciones/MensajeServiceTest.java
- src/test/java/com/connectaoficios/api/servicios/implementaciones/PerfilTrabajadorServiceTest.java
- src/test/java/com/connectaoficios/api/servicios/implementaciones/PromocionServiceTest.java
- src/test/java/com/connectaoficios/api/servicios/implementaciones/SolicitudServicioServiceTest.java
- src/test/java/com/connectaoficios/api/servicios/implementaciones/TransaccionPagoServiceTest.java
- src/test/resources/application.properties
- mvnw

POM conservado sin cambios. Reportes generados y logs no están en los archivos nuevos versionables.

## git status --short

```text
M .dockerignore
 M .env.example
 M .gitattributes
 M .gitignore
 M Dockerfile
 M README.md
 M compose.yaml
M  mvnw
 M src/main/java/com/connectaoficios/api/config/CloudinaryConfig.java
 M src/main/java/com/connectaoficios/api/config/WebSocketConfig.java
 M src/main/java/com/connectaoficios/api/controladores/ConversacionController.java
 M src/main/java/com/connectaoficios/api/controladores/ImagenController.java
 M src/main/java/com/connectaoficios/api/controladores/MensajeController.java
 M src/main/java/com/connectaoficios/api/controladores/NotificacionController.java
 M src/main/java/com/connectaoficios/api/controladores/PerfilAdministradorController.java
 M src/main/java/com/connectaoficios/api/controladores/PerfilClienteController.java
 M src/main/java/com/connectaoficios/api/controladores/PerfilTrabajadorController.java
 M src/main/java/com/connectaoficios/api/controladores/PromocionController.java
 M src/main/java/com/connectaoficios/api/controladores/ReporteController.java
 M src/main/java/com/connectaoficios/api/controladores/ResenaController.java
 M src/main/java/com/connectaoficios/api/controladores/ServicioController.java
 M src/main/java/com/connectaoficios/api/controladores/SolicitudServicioController.java
 M src/main/java/com/connectaoficios/api/controladores/TransaccionPagoController.java
 M src/main/java/com/connectaoficios/api/dtos/solicitud/SolicitudServicioGuardar.java
 M src/main/java/com/connectaoficios/api/seguridad/configuracion/JwtAuthenticationConverter.java
 M src/main/java/com/connectaoficios/api/seguridad/configuracion/SecurityConfig.java
 M src/main/java/com/connectaoficios/api/servicios/implementaciones/ImagenService.java
 M src/main/java/com/connectaoficios/api/servicios/implementaciones/NotificacionService.java
 M src/main/java/com/connectaoficios/api/servicios/implementaciones/SolicitudServicioService.java
 M src/test/java/com/connectaoficios/api/servicios/implementaciones/ImagenServiceTest.java
 M src/test/java/com/connectaoficios/api/servicios/implementaciones/MensajeServiceTest.java
 M src/test/java/com/connectaoficios/api/servicios/implementaciones/PerfilTrabajadorServiceTest.java
 M src/test/java/com/connectaoficios/api/servicios/implementaciones/PromocionServiceTest.java
 M src/test/java/com/connectaoficios/api/servicios/implementaciones/SolicitudServicioServiceTest.java
 M src/test/java/com/connectaoficios/api/servicios/implementaciones/TransaccionPagoServiceTest.java
 M src/test/resources/application.properties
?? docker/
?? docs/testing/
?? reports/
?? scripts/
?? src/main/java/com/connectaoficios/api/controladores/HealthLocalController.java
?? src/main/java/com/connectaoficios/api/seguridad/AccesoJava.java
?? src/main/java/com/connectaoficios/api/seguridad/IdentidadJwt.java
?? src/main/java/com/connectaoficios/api/seguridad/StompSeguridad.java
?? src/main/java/com/connectaoficios/api/servicios/implementaciones/ImagenLocalService.java
?? src/main/resources/application-local.properties
?? src/test/java/com/connectaoficios/api/controladores/LocalApiSecurityTest.java
?? src/test/java/com/connectaoficios/api/seguridad/
?? src/test/java/com/connectaoficios/api/servicios/implementaciones/ChatTiempoRealServiceTest.java
?? src/test/java/com/connectaoficios/api/servicios/implementaciones/EstadisticasServiceTest.java
?? src/test/java/com/connectaoficios/api/servicios/implementaciones/NotificacionServiceTest.java
?? src/test/java/com/connectaoficios/api/servicios/implementaciones/ServicioAdminServiceTest.java
?? tests/
```
