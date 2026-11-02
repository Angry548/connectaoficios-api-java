# Pruebas Unitarias – Backend Java

Fecha: 02/11/2026. Proyecto: ConnectaOficios. Rama: `master`.
Responsable de esta fase: Angel Mate.

## Herramientas

- JUnit 5.
- Mockito.
- Maven Surefire Report 3.5.6.
- JaCoCo 0.8.14.
- Java 21.0.11 y Maven Wrapper 3.9.16.

## Resultado de ejecución

- Total: **286**.
- Passed: **286**.
- Failed: **0**.
- Errors: **0**.
- Skipped: **0**.
- Success Rate: **100%**.
- Clases/suites ejecutadas: **36**.
- Tiempo mostrado en Surefire HTML: **88.77 s**.
- Última ejecución limpia finalizada: **02/11/2026 02:20:44 UTC−06:00**.
- Generación de reportes finalizada: **02/11/2026 02:21:34 UTC−06:00**.
- Código de salida de Maven: **0** en ambas operaciones; `BUILD SUCCESS`.

Los resultados corresponden a la suite completa, que incluye unitarias Mockito
y pruebas preexistentes Spring/HTTP/H2. No se atribuyen todas a Angel: se conservan
los 264 casos existentes y se agregan **22 casos**, sin duplicar los ya cubiertos.
Asignaciones y trazabilidad: [ANGEL_UNIT_AUDIT.md](ANGEL_UNIT_AUDIT.md).

## Cobertura JaCoCo

Contadores agregados de `target/site/jacoco/jacoco.xml`, exclusivamente de la
última ejecución limpia. Porcentajes calculados como cubiertos / total, redondeados
a dos decimales; el HTML de JaCoCo puede mostrar porcentajes enteros.

| Métrica | Cubiertos | Total | Cobertura |
|---|---:|---:|---:|
| Instructions | 8110 | 12363 | **65.60%** |
| Branches | 452 | 882 | **51.25%** |
| Lines | 2674 | 4134 | **64.68%** |
| Methods | 473 | 724 | **65.33%** |
| Classes | 151 | 164 | **92.07%** |

La cobertura es global del código instrumentado del proyecto, no una métrica
exclusiva de las pruebas nuevas ni un requisito de 100%.

## Rutas de reportes

Desde la raíz del repositorio:

```text
target/site/surefire-report.html
target/site/jacoco/index.html
```

Evidencia original y verificación:

```text
target/surefire-reports/TEST-*.xml
target/site/jacoco/jacoco.xml
reports/unit/final-test.log
reports/unit/final-test.exitcode
reports/unit/final-reports.log
reports/unit/final-reports.exitcode
reports/unit/summary.json
```

Reportes, XML, logs y resumen JSON permanecen ignorados por Git. No contienen
credenciales reales ni requieren servicios productivos para regenerarse.

## Reproducción

PowerShell, desde la raíz, con las dependencias ya instaladas:

```powershell
.\mvnw.cmd -B -o clean test
# Continuar solo si Maven termina con BUILD SUCCESS y código 0.
.\mvnw.cmd -B -o jacoco:report site
node scripts/testing/report-summary.mjs
```

En Bash: `./mvnw -B -o clean test` y después
`./mvnw -B -o jacoco:report site`. El Wrapper evita instalar Maven global.
`-o` impide acceso a Internet; requiere la caché de dependencias y plugins.
El objetivo `jacoco:report` es explícito porque `test` no alcanza la fase `verify`
en la que está configurado el reporte. `site` genera Surefire con `report-only`
y no vuelve a ejecutar pruebas. El flujo `clean verify` seguido de `site` del
README también es correcto, por lo que no se reescribió.

No ejecutar otra selección de pruebas sobre estos artefactos antes de capturar
la evidencia: puede reemplazar XML de la suite. `clean` elimina `target`.

## Evidencia para el PDF

1. Abrir Surefire HTML en el navegador y capturar **Summary**: 286 tests,
   0 errors, 0 failures, 0 skipped, 100% y 88.77 s. El mismo archivo incluye
   paquetes, clases y resultados de los casos.
2. Abrir JaCoCo HTML y capturar la fila **Total**; usar la tabla de este documento
   para los porcentajes de líneas, métodos y clases y los valores a dos decimales.
3. Pegar los resultados y la interpretación siguiente. Opcionalmente usar
   **Ctrl + P → Guardar como PDF** en cada reporte.

## Interpretación

Las 286 pruebas finalizaron satisfactoriamente, sin fallos ni errores, con una
cobertura global del 64.68% de líneas y del 51.25% de ramas del Backend Java.

## Integridad de la entrega

Se conservaron intactas las clases asignadas a Eduardo, Alfredo, Óscar y Nuria.
No se modificaron código productivo, contratos API, Docker, POM ni perfiles.
No se introdujeron secretos. `git diff --check` finalizó sin errores.
