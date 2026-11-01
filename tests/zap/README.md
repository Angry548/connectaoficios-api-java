# OWASP ZAP local

Runner multiplataforma: node scripts/testing/zap.mjs baseline publico.

Alcance cerrado: contenedor connectaoficios-java-api, alias api-local:8080 en red Docker temporal interna sin acceso a Internet. El runner no acepta una URL. No afecta al volumen MySQL ni elimina la red habitual de Compose. Requiere API local ya healthy; no ejecutar contra otro contenedor con ese nombre.

Modos:
- baseline: spider de Swagger y análisis pasivo.
- api-safe: importa OpenAPI y omite active scan; la importación puede enviar requests y no garantiza ausencia de cambios de dominio.
- api-active: escaneo activo, solo al establecer explícitamente ZAP_ALLOW_ACTIVE=true en la fase posterior. No se ha ejecutado.

Roles/contextos definidos en contexts.json: publico, cliente, trabajador, administrador. Para protegidos pasar el rol al runner y su token por CLIENTE_TOKEN, TRABAJADOR_TOKEN o ADMIN_TOKEN. Ejecutar separadamente para conservar reportes por rol. No hay login .NET en el scanner. Verificar primero el token mediante GET /api/servicios; renovar si expira.

Ejemplo: node scripts/testing/zap.mjs api-safe cliente.

Importar OpenAPI permite analizar CRUD y filtros. Preparar IDs válidos, verificar 401/403 y realizar además pruebas manuales entre DOS clientes y DOS trabajadores: servicio ajeno, solicitud ajena, conversación ajena y lectura ajena. ZAP no demuestra por sí solo ausencia de BOLA. Usar datos de dominio desechables y conservar el volumen existente.

Resultados: reports/zap/<rol>/; pueden contener información sensible y nunca se versionan. Salidas ZAP: 0 éxito, 1 FAIL, 2 WARN, 3 error operativo; no se ocultan warnings.

La imagen stable debe registrarse con su digest en la evidencia final para repetir exactamente una medición. El runner usa el canal oficial y no se ha ejecutado en esta preparación.

Documentación oficial: https://www.zaproxy.org/docs/docker/api-scan/ y https://www.zaproxy.org/docs/getting-further/authentication/handling-auth-yourself/ .
