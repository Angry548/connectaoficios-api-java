# Testing backend Java exclusivamente local

La base de trabajo es master. Esta preparación no crea develop, ramas, merges, commits ni pushes. El trabajo posterior del equipo se hará en sus ramas cuando el responsable prepare develop. Fuera de alcance: E2E/UAT frontend y Component Testing React Native.

Para las asignaciones unitarias individuales, consultar [TEAM_ASSIGNMENTS.md](TEAM_ASSIGNMENTS.md). Los integrantes ejecutan solo su clase/método; los comandos de suite completa y reportes de esta guía corresponden al líder.

## Requisitos

Docker con contenedores Linux y Compose v2; Git; Node.js para los runners auxiliares. No se requiere Maven ni Java del host para el flujo oficial. Internet solo para descargar imágenes/dependencias de herramientas. Java accede exclusivamente a MySQL local, sin Aiven/Render. Cloudinary se simula en local/test. No se usan credenciales reales.

## Arranque

Desde la raíz, solo si NO existe .env:

PowerShell:

```powershell
Copy-Item .env.example .env
docker compose config --quiet
docker compose up -d --build
docker compose ps
Invoke-RestMethod http://localhost:8081/api/health
Invoke-RestMethod http://localhost:8081/v3/api-docs
```

Bash/Linux/Codespaces:

```bash
cp .env.example .env
docker compose config --quiet
docker compose up -d --build
docker compose ps
curl --fail http://localhost:8081/api/health
curl --fail http://localhost:8081/v3/api-docs
```

Swagger: http://localhost:8081/swagger-ui/index.html. Health local ejecuta SELECT 1 y devuelve UP o 503 DOWN sin detalles internos. La API escucha 8080 internamente y 127.0.0.1:8081 en el host. MySQL publica 127.0.0.1:3307 (configurable MYSQL_HOST_PORT).

La plantilla incluye contraseñas PÚBLICAS exclusivamente de testing y una clave JWT LOCAL. No son valores seguros para producción. Un volumen previamente inicializado conserva su usuario/contraseña/base: cambiar .env no modifica esas credenciales. Si el volumen existente usa otros valores locales, conservarlos en .env; no sobrescribirlo ni eliminar el volumen.

## Esquema, volumen y seed

Se conserva connectaoficios-mysql-data. Hibernate update del perfil local sigue creando tablas faltantes; producción conserva ddl-auto=none. No hay 01-schema.sql activo ni scripts montados en el entrypoint.

Con API healthy, seed opcional: node scripts/testing/seed.mjs. Es idempotente en ejecución secuencial; no ejecutar dos cargas simultáneas. Inserta únicamente categorías, zonas y un plan TEST_LOCAL_; no sobrescribe filas existentes ni crea usuarios. SQL omite IDs primarios. Si hay conflictos con datos preexistentes de esos nombres, revisar antes de continuar.

Fixtures dependientes: obtener cuentas/tokens .NET locales; crear perfil trabajador mediante POST /api/perfiles-trabajador con oficioPrincipal y zonaPrincipalId. Postman crea servicios, solicitudes, conversaciones, mensajes, reseñas, promociones y transacciones. No hay contraseñas en SQL. Consultar TEST_USERS.example.md.

Detener y reanudar:

```sh
docker compose stop
docker compose start
# Opcional: retirar contenedores conservando datos
docker compose down
```

NO usar docker compose down -v. No ejecutar limpieza de volúmenes. El seed y los tests API crean datos; no incluyen borrado automático.

## Suite Java y reportes

Flujo oficial sin Maven del host (PowerShell y Bash):

```sh
docker compose --profile testing run --rm unit-tests
```

El servicio ejecuta Maven Wrapper con perfil test, H2 efímero y proveedor de imágenes simulado; no usa la base MySQL persistente. Primero clean verify y solo si pasa, site. No se ignoran fallos. El Wrapper fija Maven 3.9.16. Se invoca con sh para soportar checkouts Windows sin bit ejecutable.

Alternativa diagnóstica con Java 21 instalado:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd site
```

```bash
chmod +x mvnw
./mvnw clean verify
./mvnw site
# También funciona: sh ./mvnw clean verify
```

Reportes nuevos: target/surefire-reports/ (XML), target/site/surefire-report.html, target/site/jacoco/index.html y jacoco.xml. JaCoCo 0.8.14 sin umbral nuevo. clean elimina target: conservar evidencias después de la ejecución final, no antes.

Resumen verificable de contadores XML: node scripts/testing/report-summary.mjs (genera reports/unit/summary.json). Validación de sintaxis y guardas sin tráfico HTTP: node scripts/testing/validate.mjs.

reports/unit/ y reports/coverage/ son destinos opcionales para copiar los XML y el árbol HTML completo (incluye CSS/JS). Reportes ignorados; solo se versiona .gitkeep. No confundir H2 MODE=MySQL con evidencia real MySQL.

## Postman / Newman

Importar tests/postman/connectaoficios-java-local.collection.json y el environment example. Copiar el environment como tests/postman/local.environment.json (ignorado), completar tokens e IDs de .NET local. Tokens vacíos en plantilla. Aplicar seed y crear perfil trabajador previamente.

Bash: sh scripts/testing/newman.sh. PowerShell: .\scripts\testing\newman.ps1. Se utiliza npx newman@6.2.1 sin instalación global. Se valida localhost:8081, no se siguen redirects y --bail conserva los fallos. Reportes JSON/JUnit en reports/newman/. Pueden contener tokens y deben permanecer locales.

La colección cubre OpenAPI, 401, servicio, filtros, solicitud, aceptación, conversación, mensajes/lectura, completar, reseña/reputación, promoción/pago simulado y 403 administrativo. Los IDs creados se encadenan durante la ejecución; no se exporta un environment con tokens.

Integración MySQL/persistencia posterior: ejecutar colección, anotar IDs localmente, reiniciar solo API con docker compose restart connectaoficios-java-api y volver a consultar los mismos IDs. No recrear el volumen. La colección escribe fixtures nuevos en cada ejecución; no es un mecanismo de limpieza.

## k6

Ver tests/k6/README.md. Con tokens en variables de sesión:

```sh
node scripts/testing/k6.mjs load servicios
node scripts/testing/k6.mjs load solicitudes
node scripts/testing/k6.mjs load mensajeria
node scripts/testing/k6.mjs load promociones
node scripts/testing/k6.mjs load reportes-estadisticas
```

El runner usa grafana/k6:1.0.0 y el namespace de red del contenedor API; solo alcanza su loopback 8080 como destino configurado. Tokens pasan por nombres de variables, nunca se escriben en scripts. No ejecuta login. JSON en reports/k6/load/ o stress/.

Stress requiere autorización de fase explícita en la sesión: Bash export K6_ALLOW_STRESS=true; PowerShell $env:K6_ALLOW_STRESS='true'. Después node scripts/testing/k6.mjs stress servicios (o módulo elegido). No se ha ejecutado stress en la preparación. WRITE=true solo para solicitudes/mensajes y genera datos.

## ZAP

Ver tests/zap/README.md. Runner multiplataforma:

```sh
node scripts/testing/zap.mjs baseline publico
node scripts/testing/zap.mjs api-safe cliente
node scripts/testing/zap.mjs api-safe trabajador
node scripts/testing/zap.mjs api-safe administrador
```

Solo red Docker temporal interna; sin destinos arbitrarios. Tokens en CLIENTE_TOKEN/TRABAJADOR_TOKEN/ADMIN_TOKEN. El escaneo activo está bloqueado sin ZAP_ALLOW_ACTIVE=true y se reserva para la fase posterior. No se ha ejecutado ningún ataque. Reportes por rol en reports/zap/.

## WebSocket y compatibilidad mobile

Handshake /ws permite negociación sin JWT HTTP para navegadores; exige origen CORS permitido. Primer frame STOMP CONNECT debe incluir Authorization: Bearer <token local>. El decoder valida firma/expiración/issuer/audience. Suscripciones exclusivamente /topic/conversaciones/<id> del participante. Wildcards y destinos ajenos denegados. SEND denegado: publicar mediante POST /api/mensajes, que comprueba participante y estado y distribuye al topic correspondiente.

Mobile que solo enviaba JWT en handshake debe incluirlo también en CONNECT. No se utiliza JWT en query string. Pruebas unitarias del interceptor y de publicación están incluidas; la negociación real con cliente móvil queda para integración Docker.

Servicios conserva perfilTrabajadorId en body pero verifica dueño JWT. Solicitudes conserva clienteId/trabajadorId opcionales: si faltan se derivan; si difieren del JWT/servicio devuelve 403. Acceso ajeno devuelve 403. Roles admin conservan consultas administrativas. Identidad centralizada NameIdentifier con fallback sub; confirmar con .NET local antes de integrar.

## Codespaces y límites

No requiere .devcontainer. Usar Docker Compose, sh para scripts y puertos reenviados privados. CORS del frontend remoto requiere configurar su origen exacto en .env cuando se integre; no afecta Newman/k6. No depender de rutas Windows. Ejecutar un solo stack por daemon por los nombres fijos y el volumen compartido.

La validación Docker queda pendiente si el host no dispone de CLI/daemon. Imágenes y dependencias se descargan de sus registros oficiales; registrar digests/versiones y hardware en la evidencia final. No afirmar que ZAP/k6/Newman/MySQL han pasado hasta ejecutarlos en sus fases.
