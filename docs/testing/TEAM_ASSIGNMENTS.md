# Asignaciones del equipo y ejecución individual

Revisión sobre `master`. Este documento no crea ramas, commits, merge ni push. La preparación actual contiene archivos modificados y nuevos sin commit: antes de distribuirla, el líder deberá incorporarlos a la base común en una fase posterior. En particular, Postman y su environment example existen pero todavía no figuran en `git ls-files`.

La suite completa de referencia tiene **264 tests, 264 passed, 0 failures, 0 errors y 0 skipped**. Esta revisión no agrega tests ni aumenta ese total. Las ejecuciones selectivas no sustituyen la evidencia de la suite completa.

## Organización por funcionalidad

Carpeta de las clases de servicio:

`src/test/java/com/connectaoficios/api/servicios/implementaciones/`

| Funcionalidad | Clase donde se amplían las pruebas |
|---|---|
| Servicio | `ServicioServiceTest.java` |
| SolicitudServicio | `SolicitudServicioServiceTest.java` |
| Mensaje | `MensajeServiceTest.java` |
| Conversacion | `ConversacionServiceTest.java` |
| Resena | `ResenaServiceTest.java` |
| ReputacionTrabajador | `ReputacionTrabajadorServiceTest.java` |
| PerfilTrabajador | `PerfilTrabajadorServiceTest.java` |
| PerfilCliente | `PerfilClienteServiceTest.java` |
| PerfilAdministrador | `PerfilAdministradorServiceTest.java` |
| Promocion | `PromocionServiceTest.java` |
| PlanPromocion | `PlanPromocionServiceTest.java` |
| TransaccionPago | `TransaccionPagoServiceTest.java` |
| Notificacion | `NotificacionServiceTest.java` |
| Reporte | `ReporteServiceTest.java` |
| Estadisticas | `EstadisticasServiceTest.java` |
| ServicioAdmin | `ServicioAdminServiceTest.java` |

Los tests de controladores, repositorios y seguridad se mantienen en sus paquetes correspondientes. No crear clases por persona como `EduardoTest`, `OscarTest` o `NuriaTest`. Nombrar por módulo y comportamiento.

## Ocho casos propuestos: detección de duplicados

Los archivos de esta tabla están en la carpeta de servicios indicada arriba. Se revisaron las aserciones y los fixtures, además de los nombres de los métodos.

| Caso | Archivo | Método existente | Estado | Qué cubre y qué falta |
|---|---|---|---|---|
| Eduardo: crear un servicio con trabajador válido | `ServicioServiceTest.java` | `publicar_debeCrearServicioActivo` | **YA EXISTE** | Perfil y categoría existentes, asociación de IDs, título, estado ACTIVO y una llamada a guardar. No crear otro caso equivalente. |
| Eduardo: rechazar datos obligatorios inválidos | `ServicioServiceTest.java` | `publicar_debeLanzarExcepcionCuandoPerfilNoExiste`, `publicar_debeLanzarExcepcionCuandoCategoriaNoExiste`, `publicar_debeLanzarExcepcionCuandoTarifaMinimaEsMayor`, `publicar_debeLanzarExcepcionCuandoFaltaZona` | **EXISTE PARCIALMENTE** | Hay reglas de negocio/referencias inválidas. Faltan casos explícitos de campos requeridos nulos o en blanco del DTO. Las anotaciones de `ServicioGuardar` se validan en el límite HTTP con `@Valid`; llamar directamente al servicio no activa Bean Validation. |
| Alfredo: crear solicitud válida | `SolicitudServicioServiceTest.java` | `guardar_debeCrearSolicitudNueva` | **YA EXISTE** | Servicio existente, participantes, fecha/hora, dirección, descripción, PENDIENTE, persistencia y notificación. |
| Alfredo: impedir transiciones inválidas | `SolicitudServicioServiceTest.java` | `rechazar_debeLanzarExcepcion_cuandoSolicitudNoEstaPendiente`, `iniciar_debeLanzarExcepcion_cuandoSolicitudEstaPendiente`, `completar_debeLanzarExcepcion_cuandoSolicitudEstaPendiente`, `aceptar_debeLanzarExcepcion_cuandoSolicitudYaEstaCompletada`, `cancelar_debeLanzarExcepcion_cuandoSolicitudYaEstaRechazada` | **YA EXISTE** | Rechazo de varias transiciones concretas, sin persistir cambios; el caso de completar también evita recalcular reputación/notificar. No representa todas las combinaciones posibles de estados. |
| Óscar: enviar mensaje válido | `MensajeServiceTest.java` | `guardar_debeGuardarYPublicarMensaje` | **YA EXISTE** | Conversación aceptada, participante válido, remitente/contenido, no leído, persistencia y publicación. |
| Óscar: orden cronológico de mensajes | `MensajeServiceTest.java` | `obtenerPorConversacion_debeRetornarHistorialCronologico` | **EXISTE PARCIALMENTE** | Conserva el orden de dos mensajes devueltos por el mock de `findByConversacionIdOrderByFechaEnvioAsc`. El helper no establece `fechaEnvio`; no compara fechas ni demuestra el orden real de la consulta en base de datos. |
| Nuria: reseña después de finalizar servicio | `ResenaServiceTest.java` | `guardar_debeCrearResena` | **YA EXISTE** | Fixture con solicitud COMPLETADA, cliente correcto, asociación solicitud/servicio/perfil, calificación/comentario y recálculo de reputación. `guardar_debeEvitarSolicitudNoCompletada` cubre el negativo. |
| Nuria: reputación a partir de reseñas | `ReputacionTrabajadorServiceTest.java` | `recalcular_debeCalcularPromedioYTotalDesdeResenas` | **YA EXISTE** | Reseñas 5/4/5, total 3, promedio 4.67 y servicios completados. Hay casos adicionales de insignias, ranking y primera reseña. |

**Resultado: seis YA EXISTE, dos EXISTE PARCIALMENTE, ninguno totalmente NO EXISTE.**

## Decisión de asignación

- **Eduardo:** `ServicioServiceTest.java`. Sustituir el primer caso. Para el segundo, definir los campos inválidos concretos y probar el DTO mediante `jakarta.validation.Validator` o el límite MVC; no esperar artificialmente que el servicio directo ejecute `@Valid`. Si se separa en el futuro, usar un nombre funcional como `ServicioGuardarValidationTest`, nunca el nombre del integrante.
- **Alfredo:** `SolicitudServicioServiceTest.java`. Sustituir ambos casos, ya cubiertos. Elegir después dos escenarios de estados concretos que no estén probados.
- **Óscar:** `MensajeServiceTest.java`. Sustituir el envío válido. Ampliar el método cronológico existente con fechas deterministas y aserciones de orden, sin copiarlo como otro test. La comprobación del `ORDER BY` real pertenece a un test de repositorio/integración separado.
- **Nuria:** `ResenaServiceTest.java` y `ReputacionTrabajadorServiceTest.java`. Sustituir ambos casos. Acordar después escenarios nuevos antes de escribirlos.

Estas son asignaciones documentadas, pendientes de elegir reemplazos. No se crean ahora ocho tests adicionales. No cambiar expectativas ni repetir un caso para cumplir una cuota.

## Ejecutar solo la clase propia

El Wrapper fija Maven y la configuración del repositorio incluye JUnit/Mockito/Surefire. No instalar Maven global ni modificar `pom.xml`. La alternativa del host requiere Java 21; Docker evita también ese requisito.

PowerShell, desde raíz:

```powershell
# Eduardo
.\mvnw.cmd -Dtest=ServicioServiceTest test
# Alfredo
.\mvnw.cmd -Dtest=SolicitudServicioServiceTest test
# Óscar
.\mvnw.cmd -Dtest=MensajeServiceTest test
# Nuria, cada módulo por separado
.\mvnw.cmd -Dtest=ResenaServiceTest test
.\mvnw.cmd -Dtest=ReputacionTrabajadorServiceTest test
```

Bash/Linux, desde raíz:

```bash
./mvnw -Dtest=ServicioServiceTest test
./mvnw -Dtest=SolicitudServicioServiceTest test
./mvnw -Dtest=MensajeServiceTest test
./mvnw -Dtest=ResenaServiceTest test
./mvnw -Dtest=ReputacionTrabajadorServiceTest test
```

### Un solo método

Ejemplo real; sustituir por el nombre exacto del método que corresponda:

```powershell
.\mvnw.cmd '-Dtest=ServicioServiceTest#publicar_debeCrearServicioActivo' test
# Patrón: .\mvnw.cmd '-Dtest=ServicioServiceTest#nombreMetodo' test
```

```bash
./mvnw '-Dtest=ServicioServiceTest#publicar_debeCrearServicioActivo' test
# Patrón: ./mvnw '-Dtest=ServicioServiceTest#nombreMetodo' test
```

Las comillas conservan `#` como parte del selector. El mismo patrón funciona para las otras cuatro clases. Si el nombre no existe, corregirlo: no usar `failIfNoTests=false` para ocultar el error.

### Equivalente Docker para cada integrante

Este comando funciona en PowerShell y Bash, sin Maven/Java del host:

```sh
docker compose --profile testing run --rm unit-tests sh ./mvnw -B -Dtest=ServicioServiceTest test
docker compose --profile testing run --rm unit-tests sh ./mvnw -B -Dtest=SolicitudServicioServiceTest test
docker compose --profile testing run --rm unit-tests sh ./mvnw -B -Dtest=MensajeServiceTest test
docker compose --profile testing run --rm unit-tests sh ./mvnw -B -Dtest=ResenaServiceTest test
docker compose --profile testing run --rm unit-tests sh ./mvnw -B -Dtest=ReputacionTrabajadorServiceTest test
docker compose --profile testing run --rm unit-tests sh ./mvnw -B '-Dtest=ServicioServiceTest#publicar_debeCrearServicioActivo' test
```

Se reemplaza el comando predeterminado del servicio para ejecutar solo `test`; no se ejecutan `clean verify` ni `site`. `testing` es el perfil Compose; `test` es el perfil Spring del runner. Usa H2, no el volumen MySQL persistente. No ejecutar dos procesos Maven simultáneos sobre el mismo `target`.

El comando sin override `docker compose --profile testing run --rm unit-tests` queda reservado al líder: ejecuta suite completa y reportes. El POM actual genera compilados bajo `target`, sin plugins de escritura de fuentes; el montaje del repositorio sigue siendo read/write, no un aislamiento de solo lectura del sistema operativo.

## Responsabilidad de cada integrante

En la fase futura, una vez preparada su rama por el proceso del equipo:

1. Localizar el archivo funcional acordado y comprobar que el nuevo escenario no existe.
2. Añadir o ampliar únicamente los casos aprobados, con datos deterministas y mocks locales.
3. Ejecutar su clase o método y comprobar `BUILD SUCCESS`, sin failures/errors.
4. Revisar `git diff --check`, `git diff` y `git status --short`.
5. Hacer commit únicamente de archivos de prueba nuevos/modificados y scripts específicos de su prueba, si corresponde. Añadir rutas explícitas, evitando `git add .`.
6. Hacer push solo a su rama individual, según el procedimiento futuro del equipo.

**Los integrantes no ejecutan ni suben reportes finales** Surefire HTML, JaCoCo HTML, Newman HTML, ZAP HTML o k6 HTML. Tampoco cambian Maven, Docker, perfiles, Postman, Newman, k6 o ZAP para sus dos casos unitarios.

`test` sí puede producir automáticamente XML/texto Surefire y `jacoco.exec` en `target`; eso es normal y está ignorado. No son reportes finales de entrega. No ejecutar `site`, `verify`, `jacoco:report` ni los runners de carga/seguridad como parte de la asignación individual.

No subir `target/`, resultados de `reports/`, `.env`, environments personales, tokens, credenciales ni `node_modules/`. Las plantillas, código fuente y `.gitkeep` existentes sí pertenecen a la infraestructura común. Nunca borrar el volumen MySQL.

## Flujo posterior del líder: reportes generales

Después de integrar todos los cambios, en una fase posterior autorizada:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd site
```

```bash
./mvnw clean verify
./mvnw site
```

Equivalente oficial Docker:

```sh
docker compose --profile testing run --rm unit-tests
```

El runner usa `clean verify && site`, de modo que no genera el HTML final si la suite falla. En el host, ejecutar `site` únicamente después de que `clean verify` termine correctamente. `clean` elimina evidencias previas bajo `target`.

Paths reales conservados:

- Ejecución: `target/site/surefire-report.html`.
- Cobertura navegable: `target/site/jacoco/index.html`.
- XML originales: `target/surefire-reports/` y `target/site/jacoco/jacoco.xml`.

Abrir los HTML en navegador. Para evidencia PDF manual: **Ctrl + P → Guardar como PDF**. No hay automatización PDF. Los reportes generales los genera y conserva el líder; un test selectivo no acredita cobertura global.

## Ejemplo comentado JUnit 5 / Mockito

Solo ilustración de un patrón ya existente; no copiar como caso nuevo equivalente ni añadir tests ficticios:

```java
// Dentro de una clase funcional con MockitoExtension y sus mocks existentes:
// Arrange: preparar el escenario sin tocar MySQL ni servicios externos.
// when(servicioRepository.findByIdAndEliminadoFalse(999L))
//         .thenReturn(Optional.empty());
//
// Act + Assert: comprobar la excepción que exige el contrato.
// assertThrows(RecursoNoEncontradoException.class,
//         () -> servicioService.obtenerPorId(999L));
//
// Assert con Mockito: verificar interacción y ausencia de escritura.
// verify(servicioRepository).findByIdAndEliminadoFalse(999L);
// verify(servicioRepository, never()).save(any());
```

## Verificaciones de esta revisión

La sintaxis clase/método se comprueba con Wrapper. Surefire escribe automáticamente XML/texto incluso al ejecutar un método: los resultados de esa selección se conservan bajo `target/team-selection-check` y se respalda/restaura la salida de la clase para no dejar un XML de un único método mezclado con la suite general. No se regeneran los HTML. En la comprobación diagnóstica se añade `-Djacoco.skip=true` para conservar el `jacoco.exec` general; los comandos normales del integrante no necesitan esa opción.

Resultado comprobado el 31/10/2026:

- PowerShell, `-Dtest=ServicioServiceTest`: **11 tests, 11 passed, 0 failures/errors/skipped**, exit 0.
- Bash, `-Dtest=ServicioServiceTest#publicar_debeCrearServicioActivo`: **1 test, 1 passed, 0 failures/errors/skipped**, exit 0.
- Continúan existiendo 264 métodos `@Test`; no se ejecutó otra suite completa para cambios exclusivamente documentales y de ignores.
- Los HTML generales y `jacoco.exec` conservan su fecha de la ejecución completa anterior.
- `target/`, `reports/` generados, `jacoco.exec`, `surefire-reports/`, environment personal, `.env` y `node_modules/` están ignorados; la clase de pruebas y las dos plantillas Postman no están ignoradas.
- Postman y su environment example siguen pendientes de incluirse en el futuro commit general del líder. No se hace stage de esos archivos en esta revisión.

Docker no está disponible en esta sesión (ni CLI en PATH ni ejecutable en su ubicación habitual comprobada). No se ejecutan Compose, MySQL, Newman, k6 ni ZAP. La validación Docker sigue pendiente; no se presenta una revisión estática como ejecución exitosa.

Las comprobaciones pendientes del líder están en [README.md](README.md): Compose config, build/up, MySQL healthy, `/api/health`, OpenAPI y Swagger, y runner `unit-tests`. No usar `docker compose down -v`.
