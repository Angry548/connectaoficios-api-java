# ConnectaOficios Backend Java

API Spring Boot 4.0.8 / Java 21 para categorías, servicios, perfiles, disponibilidad, zonas, solicitudes, conversaciones, mensajes, reseñas, reputación, promociones, transacciones, notificaciones, reportes y estadísticas.

La API .NET administra principalmente autenticación, login, usuarios y emisión de JWT. Java valida esos tokens para sus endpoints protegidos. El entorno oficial de pruebas es **Docker local: Java + MySQL 8.4**, con perfil Spring `local` e imágenes simuladas sin Cloudinary real.

## 1. Requisitos y apertura

| Flujo | Requisitos |
|---|---|
| Recomendado | Git, Docker con contenedores Linux y Docker Compose v2 |
| Tests sin Docker | Java 21 y Maven Wrapper incluido; no instalar Maven global |
| Herramientas opcionales | Node.js para Newman y wrappers, navegador; k6 instalado solo si se ejecuta directamente sin su wrapper Docker |

Clonar usando la URL que proporciona GitHub en **Code**; sustituir `<URL>`:

```bash
git clone <URL>
cd connectaoficios-api-java
```

En Codespaces, abrir el Codespace sobre la rama asignada y trabajar desde la raíz del repositorio. Su flujo es Bash/Linux; ver [GitHub Codespaces](#github-codespaces).

## 2. Variables de entorno

Crear `.env` **solo si todavía no existe**:

```powershell
# Windows PowerShell
Copy-Item .env.example .env
```

```bash
# Bash / Codespaces
cp .env.example .env
```

`.env` está ignorado por Git. La plantilla contiene valores públicos exclusivamente locales: no introducir secretos reales ni credenciales de producción. No sobrescribir un `.env` existente: las credenciales de un volumen MySQL ya inicializado no cambian al editar variables.

## 3. Docker: construir y levantar

Los mismos comandos funcionan en PowerShell y Bash:

```bash
docker compose config --quiet
docker compose up -d --build
docker compose ps
```

Esperar a que `connectaoficios-java-api` y `mysql` aparezcan **healthy**. La imagen propia es `connectaoficios-java-api:local`.

| Recurso | URL |
|---|---|
| Base de la API | http://localhost:8081 |
| Health local | http://localhost:8081/api/health |
| OpenAPI | http://localhost:8081/v3/api-docs |
| Swagger UI | http://localhost:8081/swagger-ui/index.html |

El health comprueba MySQL con `SELECT 1`. La raíz `/` no es un endpoint de health; usar `/api/health`.

**MySQL:** desde el host, `localhost:3307` por defecto (`MYSQL_HOST_PORT`); dentro de Docker, `mysql:3306`. Base, usuario y contraseña se toman de `.env`. Se conserva el volumen `connectaoficios-mysql-data`; Hibernate crea/actualiza el esquema solo en el perfil local.

Para datos independientes de usuarios, consultar el [seed opcional y los fixtures](docs/testing/README.md#esquema-volumen-y-seed). Las cuentas principales no se crean desde SQL Java.

### Detener y reanudar

```bash
docker compose stop
docker compose start
```

> **ADVERTENCIA: NO ejecutar normalmente `docker compose down -v`. Elimina el volumen MySQL local y sus datos.** Para detener el entorno, usar `stop`.

## 4. Pruebas unitarias por módulo

JUnit 5 / Mockito; clases en `src/test/java/com/connectaoficios/api/servicios/implementaciones/`, por ejemplo:

```text
CategoriaServiceTest
ServicioServiceTest
SolicitudServicioServiceTest
MensajeServiceTest
ResenaServiceTest
ReputacionTrabajadorServiceTest
```

Los tests unitarios y de Spring/H2 no requieren levantar MySQL Docker. Los tests API/integración contra MySQL sí lo requieren.

| Selección | Windows PowerShell | Bash / Codespaces |
|---|---|---|
| Suite completa | `.\mvnw.cmd test` | `./mvnw test` |
| Una clase | `.\mvnw.cmd -Dtest=ServicioServiceTest test` | `./mvnw -Dtest=ServicioServiceTest test` |
| Un método | `.\mvnw.cmd '-Dtest=ServicioServiceTest#nombreMetodo' test` | `./mvnw '-Dtest=ServicioServiceTest#nombreMetodo' test` |

Sustituir `nombreMetodo` por uno existente, por ejemplo `publicar_debeCrearServicioActivo`. Conservar las comillas del selector de método.

### Unit tests mediante Docker

Suite completa y reportes, sin Maven/Java del host:

```bash
docker compose --profile testing run --rm unit-tests
```

Ese comando corresponde al líder porque ejecuta `clean verify` y `site`. Para que un integrante ejecute **solo su clase**, reemplazar el comando del servicio:

```bash
docker compose --profile testing run --rm unit-tests sh ./mvnw -B -Dtest=ServicioServiceTest test
```

`testing` es el perfil de Compose; el runner activa el perfil Spring `test`, usa H2 y no elimina el volumen MySQL. No ejecutar dos procesos Maven simultáneos sobre el mismo `target`.

## 5. Asignaciones del equipo

| Integrante | Clase funcional |
|---|---|
| Eduardo | `ServicioServiceTest` |
| Alfredo | `SolicitudServicioServiceTest` |
| Óscar | `MensajeServiceTest` |
| Nuria | `ResenaServiceTest` y `ReputacionTrabajadorServiceTest` |

Consultar [TEAM_ASSIGNMENTS.md](docs/testing/TEAM_ASSIGNMENTS.md) antes de escribir casos: seis de los ocho propuestos ya existen y dos están parcialmente cubiertos. Acordar reemplazos o ampliaciones; no duplicarlos. **No crear archivos con nombres de integrantes.**

## Flujo para integrante

Este flujo corresponde a la fase futura del equipo, después de que el líder distribuya la base común:

1. Estar en su rama asignada; comprobarla con `git branch --show-current`. No trabajar sus casos sobre `master` o `develop`.
2. Actualizar esa rama con `git pull --ff-only`. Si falla o hay conflictos, consultar al líder.
3. Levantar Docker si la prueba lo requiere.
4. Editar únicamente el archivo funcional de pruebas acordado y añadir/ampliar los casos aprobados.
5. Ejecutar su clase o método usando los comandos anteriores.
6. Confirmar **PASS / BUILD SUCCESS**, sin failures/errors.
7. Revisar `git status`, `git diff` y `git diff --check`.
8. Agregar únicamente las rutas fuente de pruebas necesarias; evitar `git add .`.
9. Hacer commit de sus pruebas y, si corresponde, scripts específicos de su caso.
10. Hacer push únicamente a su rama asignada.

**No generar ni subir reportes finales.** `test` puede crear automáticamente XML Surefire y `jacoco.exec` bajo `target`; son artefactos locales ignorados, no entregables individuales.

### Qué no modificar ni subir

Normalmente no modificar `pom.xml`, `Dockerfile`, `compose.yaml`, `.env.example`, Surefire, JaCoCo ni la configuración global de k6/ZAP. Tampoco reconfigurar Postman/Newman para una asignación unitaria. Si hace falta un cambio general, avisar al líder.

**No versionar:** `.env`, `target/`, `node_modules/`, `jacoco.exec`, tokens, credenciales, reportes temporales ni environments personales de Postman. Mantener las plantillas y el código fuente de pruebas.

## 6. Reportes finales: responsabilidad del líder

Después de integrar los cambios, ejecutar en orden; continuar con `site` solo si `clean verify` pasa:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd site
```

```bash
./mvnw clean verify
./mvnw site
```

El runner Docker `unit-tests` es el equivalente completo. `clean` elimina las evidencias anteriores en `target`.

| Reporte | Archivo | Contenido |
|---|---|---|
| Surefire | `target/site/surefire-report.html` | Tests, failures, errors, skipped y success rate |
| JaCoCo | `target/site/jacoco/index.html` | Cobertura de líneas, ramas, métodos y clases |

La ejecución de referencia del **31/10/2026** reportó **264 tests, 264 passed, 0 failures, 0 errors y 0 skipped**. No es una garantía para cambios posteriores; volver a medir después de integrar. No se fija aquí una cobertura histórica como objetivo garantizado.

Abrir los HTML en navegador. Para evidencia PDF manual: **Ctrl + P → Guardar como PDF**. Lo realiza el líder durante la consolidación; no se automatiza.

El líder consolida: suite completa → Surefire/JaCoCo → Newman → ZAP → k6 Load → k6 Stress → PDF/evidencias. Las campañas de seguridad/rendimiento se ejecutan en su fase acordada, exclusivamente local. Los runners actuales de Newman/k6 producen JSON/JUnit o JSON según la herramienta; no todos generan HTML.

## 7. Postman / Newman, k6 y ZAP

Estas herramientas corresponden al líder o al responsable de la campaña, no al flujo unitario cotidiano.

### Postman / Newman

En [`tests/postman/`](tests/postman/):

- Colección: `connectaoficios-java-local.collection.json`.
- Plantilla: `connectaoficios-java-local.environment.example.json`.

Importarlas en Postman o copiar la plantilla como `tests/postman/local.environment.json` (ignorado). Base: **`http://localhost:8081`**. Completar tokens/IDs obtenidos de .NET local, aplicar el seed y preparar el perfil trabajador según la [guía de integración](docs/testing/README.md#postman--newman). Las plantillas no contienen tokens; no subir el environment personal.

```powershell
.\scripts\testing\newman.ps1
```

```bash
sh scripts/testing/newman.sh
```

Los scripts existentes usan Node.js / `npx newman`, validan el destino local y escriben en `reports/newman/`.

### k6

[`tests/k6/load/`](tests/k6/load/) mide carga prevista; [`tests/k6/stress/`](tests/k6/stress/) incrementa carga para observar degradación. Ambos incluyen Servicios, Solicitudes, Mensajería, Promociones y Reportes/Estadísticas.

Wrapper existente, con Docker y Node.js:

```bash
node scripts/testing/k6.mjs load servicios
# Solo durante la fase de stress acordada:
node scripts/testing/k6.mjs stress servicios
```

Stress requiere habilitar `K6_ALLOW_STRESS=true` en la sesión: Bash `export K6_ALLOW_STRESS=true`; PowerShell `$env:K6_ALLOW_STRESS='true'`. Los tokens van por variables de entorno, sin login .NET bajo carga. Módulos, IDs requeridos, thresholds y resultados: [tests/k6/README.md](tests/k6/README.md).

### OWASP ZAP

Configuración y procedimiento: [`tests/zap/`](tests/zap/). Usar el runner existente:

```bash
node scripts/testing/zap.mjs baseline publico
```

Solo contra Docker local; **no apuntar a Render, Aiven ni producción**. El runner fija el destino y utiliza una red Docker interna. El escaneo activo requiere habilitación explícita y se reserva para la campaña de seguridad. Instrucciones por rol y reportes HTML: [tests/zap/README.md](tests/zap/README.md).

## Usuarios de prueba

**Java no administra login ni usuarios principales.** Estas direcciones son ejemplos de formato, no cuentas ya creadas:

| Rol .NET | Correo de ejemplo          | Contraseña |
|---|----------------------------|-----------|
| Cliente | `cliente123@gmail.com`     | clave1234 |
| Trabajador | `trabajador123@gmail.com`  | clave123  |
| Administrador | `admin123@gmail.com`       |clave123  
| AdministradorPrincipal | `adminprincipal@gmail.com` |clave123  

Los usuarios y contraseñas definitivos se definirán en la API .NET local. Los JWT para Java se obtienen desde .NET; no incluir contraseñas reales ni tokens en el repositorio. Ver [TEST_USERS.example.md](docs/testing/TEST_USERS.example.md), que también contiene ejemplos ficticios y el contrato de claims.

```text
.NET → login → JWT → Java → endpoints protegidos
```

Java valida firma, identidad, rol, issuer, audience y expiración. El emisor .NET local debe coincidir con la configuración JWT de Java para Postman e integración.

## GitHub Codespaces

Abrir el Codespace en su rama asignada. Desde raíz, usando Bash y sin sobrescribir un `.env` existente:

```bash
cp .env.example .env
docker compose config --quiet
docker compose up -d --build
docker compose ps
```

Después, con Java 21 disponible, sustituir `<Clase>` por su clase funcional:

```bash
./mvnw -Dtest=<Clase> test
```

O usar Docker, sin Maven/Java del Codespace:

```bash
# Integrante: solo su clase
docker compose --profile testing run --rm unit-tests sh ./mvnw -B -Dtest=ServicioServiceTest test
# Líder: suite y reportes
docker compose --profile testing run --rm unit-tests
```

Los comandos acceden a `localhost` dentro del Codespace; para abrir Swagger en navegador, usar el puerto **8081** reenviado en la pestaña **Ports**, manteniéndolo privado. Si Docker/Compose no están disponibles en la imagen del Codespace, informar al líder antes de instalar configuraciones adicionales. No se requiere un `.devcontainer` nuevo para seguir esta guía.

## Solución de problemas

| Problema | Acción |
|---|---|
| Maven Central: `Remote host terminated the handshake` durante build | Suele ser conectividad temporal: reintentar `docker compose up -d --build`. Si persiste, revisar red/proxy/certificados con el líder; no cambiar dependencias inmediatamente ni desactivar TLS. |
| Puerto 8081 o 3307 ocupado | Revisar `docker compose ps` y `docker ps --format "table {{.Names}}\t{{.Ports}}"`. Si no es otro contenedor, PowerShell: `Get-NetTCPConnection -LocalPort 8081,3307`; Linux: `ss -ltn`. No cambiar puertos permanentemente sin informar al líder. |
| API en `health: starting` | Esperar y ejecutar `docker compose ps` y `docker compose logs --tail=100 connectaoficios-java-api`. Revisar también `docker compose logs --tail=100 mysql`. No borrar el volumen para resolver el arranque. |
| `./mvnw`: permiso denegado en Linux | Ejecutar `chmod +x mvnw` o invocar `sh ./mvnw`. |

## Documentación de referencia

- [Guía detallada del entorno y testing](docs/testing/README.md).
- [Asignaciones, duplicados y comandos individuales](docs/testing/TEAM_ASSIGNMENTS.md).
- [Usuarios ficticios y contrato JWT](docs/testing/TEST_USERS.example.md).
- [Informe de preparación y evidencias de referencia](docs/testing/PREPARATION_REPORT.md): histórico, no sustituye una validación nueva.
- [Casos recomendados para Xray](docs/testing/XRAY_CASES.md).
- [k6](tests/k6/README.md) y [ZAP](tests/zap/README.md).
